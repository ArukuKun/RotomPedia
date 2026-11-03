"""Read-only B audit; records exact-variety failures for C, never selects fallbacks."""
import concurrent.futures
import datetime as dt
import json
from pathlib import Path
import urllib.error
import urllib.request

ROOT = Path(__file__).resolve().parents[1]
SPRITES = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/"


def status(url):
    try:
        with urllib.request.urlopen(urllib.request.Request(url, method="HEAD"), timeout=30) as response:
            return response.status
    except urllib.error.HTTPError as error:
        return error.code
    except (OSError, urllib.error.URLError) as error:
        return type(error).__name__


def audit(variety, species):
    pokemon_id = variety["pokemonId"]
    url = f"{SPRITES}{pokemon_id}.png"
    result = status(url)
    if result == 200:
        return None
    row = {**variety, "nationalNumber": species[variety["speciesId"]]["nationalNumber"],
           "requestedUrl": url, "httpStatus": result}
    try:
        with urllib.request.urlopen(f"https://pokeapi.co/api/v2/pokemon/{pokemon_id}/", timeout=30) as response:
            data = json.load(response)
        actual_species = int(data["species"]["url"].strip("/").split("/")[-1])
        row["apiIdentityVerified"] = data["id"] == pokemon_id and actual_species == variety["speciesId"] and data["name"] == variety["slug"]
        row["apiFrontDefault"] = data["sprites"]["front_default"]
        row["cause"] = ("identity-mismatch" if not row["apiIdentityVerified"] else
                        "standard-resource-unavailable" if result == 404 and row["apiFrontDefault"] is None else
                        "conventional-url-missing" if result == 404 else "transport-or-server-error")
        # Evidence only. Do NOT wire this into the app in B.
        candidates = [data["sprites"]["other"].get("official-artwork", {}).get("front_default"),
                      data["sprites"]["other"].get("home", {}).get("front_default")]
        row["exactVarietyAlternativeEvidence"] = [{"url": candidate, "httpStatus": status(candidate)}
                                                   for candidate in dict.fromkeys(candidates) if candidate]
    except (OSError, ValueError, KeyError) as error:
        row["cause"] = "api-verification-inconclusive"
        row["verificationError"] = str(error)
    return row


def main():
    envelope = json.loads((ROOT / "RotomPedia/app/src/main/assets/pokemon-index.json").read_text(encoding="utf-8"))
    payload = json.loads(envelope["payloadJson"])
    species = {s["speciesId"]: s for s in payload["species"]}
    with concurrent.futures.ThreadPoolExecutor(max_workers=12) as pool:
        missing = [row for row in pool.map(lambda v: audit(v, species), payload["varieties"]) if row]
    report = {"checkedAt": dt.datetime.now(dt.timezone.utc).isoformat(), "snapshotSha256": envelope["contentSha256"],
              "checkedVarieties": len(payload["varieties"]), "scope": "conventional front_default exact-variety URL",
              "phaseC": ["Resolve exact-variety artwork/HOME where standard sprites are unavailable",
                         "Preserve identity and shiny/source semantics; never replace with default species artwork",
                         "Define provenance for variety-specific descriptions; species flavor text is not form-specific",
                         "Cosmetic pokemon-form selection is not exhaustive in B (varieties only)"],
              "missing": sorted(missing, key=lambda row: (row["nationalNumber"], row["pokemonId"]))}
    target = ROOT / "tools/data/explorer-sprite-audit.json"
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"Audited {report['checkedVarieties']} varieties; {len(missing)} missing/error cases: {target}")


if __name__ == "__main__":
    main()
