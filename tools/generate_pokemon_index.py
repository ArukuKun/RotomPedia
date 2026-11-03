"""Generate an identity-only national index from PokeAPI. Python 3.10+, stdlib only.

Normal Android builds use the checked, pinned asset; they never run this downloader.
Candidate generation does not commit, merge, publish or update an installed app.
"""
import argparse
import concurrent.futures
import datetime as dt
import hashlib
import json
import os
from pathlib import Path
import re
import tempfile
import time
import urllib.error
import urllib.request

BASE = "https://pokeapi.co/api/v2/"
SOURCE = BASE + "pokedex/national/"


def resource_id(url, endpoint):
    match = re.fullmatch(re.escape(BASE + endpoint + "/") + r"([1-9][0-9]*)/?", url)
    if not match:
        raise ValueError(f"Invalid {endpoint} resource URL: {url}")
    return int(match[1])


def canonical(value):
    return json.dumps(value, ensure_ascii=False, sort_keys=True, separators=(",", ":"))


def atomic_write(path, text):
    path = Path(path)
    path.parent.mkdir(parents=True, exist_ok=True)
    fd, temporary = tempfile.mkstemp(dir=path.parent, suffix=".tmp")
    try:
        with os.fdopen(fd, "w", encoding="utf-8", newline="\n") as handle:
            handle.write(text)
        os.replace(temporary, path)
    finally:
        if os.path.exists(temporary):
            os.unlink(temporary)


class Client:
    def __init__(self, cache, refresh=False):
        self.cache = Path(cache)
        self.refresh = refresh

    def get(self, url, max_age=7 * 86400):
        if not url.startswith(BASE):
            raise ValueError("Only PokeAPI resources are allowed")
        path = self.cache / (hashlib.sha256(url.encode()).hexdigest() + ".json")
        cached = None
        if path.exists():
            try:
                cached = json.loads(path.read_text(encoding="utf-8"))
            except (ValueError, OSError):
                pass
        if cached and not self.refresh and time.time() - cached["fetchedAt"] < max_age:
            return cached["data"]
        headers = {"User-Agent": "RotomPedia-index-generator/1", "Accept": "application/json"}
        if cached and cached.get("etag"):
            headers["If-None-Match"] = cached["etag"]
        for attempt in range(4):
            try:
                with urllib.request.urlopen(urllib.request.Request(url, headers=headers), timeout=30) as response:
                    data = json.load(response)
                    atomic_write(path, canonical({"fetchedAt": time.time(), "etag": response.headers.get("ETag"), "data": data}))
                    return data
            except urllib.error.HTTPError as error:
                if error.code == 304 and cached:
                    cached["fetchedAt"] = time.time()
                    atomic_write(path, canonical(cached))
                    return cached["data"]
                if error.code not in (429, 500, 502, 503, 504) or attempt == 3:
                    raise
                retry = error.headers.get("Retry-After", "")
                time.sleep(min(60, int(retry)) if retry.isdigit() else 2 ** attempt)
            except (OSError, TimeoutError):
                if attempt == 3:
                    raise
                time.sleep(2 ** attempt)
        raise RuntimeError("Download failed")


def build_payload(national, species_documents):
    entries = national["pokemon_entries"]
    if len(entries) != len(species_documents):
        raise ValueError("Incomplete species download")
    documents = {doc["id"]: doc for doc in species_documents}
    if len(documents) != len(entries):
        raise ValueError("Duplicate species document")
    species, varieties = [], []
    for entry in entries:
        sid = resource_id(entry["pokemon_species"]["url"], "pokemon-species")
        doc = documents[sid]
        if doc["name"] != entry["pokemon_species"]["name"]:
            raise ValueError("Conflicting species slug")
        names = [{"language": row["language"]["name"], "name": row["name"]}
                 for row in doc["names"] if row["language"]["name"] in ("es", "en")]
        if not names:
            names = [{"language": "slug", "name": doc["name"]}]
        species.append({"speciesId": sid, "nationalNumber": entry["entry_number"],
                        "slug": doc["name"], "generationId": resource_id(doc["generation"]["url"], "generation"),
                        "evolutionChainUrl": (doc.get("evolution_chain") or {}).get("url"), "names": names})
        for row in doc["varieties"]:
            varieties.append({"pokemonId": resource_id(row["pokemon"]["url"], "pokemon"),
                              "speciesId": sid, "slug": row["pokemon"]["name"], "isDefault": row["is_default"]})
    payload = {"source": SOURCE, "speciesCount": len(species), "varietyCount": len(varieties),
               "species": sorted(species, key=lambda x: x["nationalNumber"]),
               "varieties": sorted(varieties, key=lambda x: (x["speciesId"], not x["isDefault"], x["slug"]))}
    validate_payload(payload)
    return payload


def validate_payload(payload):
    species, varieties = payload["species"], payload["varieties"]
    if payload["source"] != SOURCE or not species:
        raise ValueError("Invalid source or empty index")
    if payload["speciesCount"] != len(species) or payload["varietyCount"] != len(varieties):
        raise ValueError("Invalid counts")
    for rows, key in ((species, "speciesId"), (species, "slug"), (varieties, "pokemonId"), (varieties, "slug")):
        if len({row[key] for row in rows}) != len(rows):
            raise ValueError(f"Duplicate {key}")
    if {row["nationalNumber"] for row in species} != set(range(1, len(species) + 1)):
        raise ValueError("Missing or duplicated national number")
    ids = {row["speciesId"] for row in species}
    if any(row["speciesId"] <= 0 or row["generationId"] <= 0 or not row["slug"] or not row["names"] for row in species):
        raise ValueError("Invalid species")
    if any(row["speciesId"] not in ids or row["pokemonId"] <= 0 or not row["slug"] or type(row["isDefault"]) is not bool for row in varieties):
        raise ValueError("Orphan or invalid variety")
    if any(sum(row["isDefault"] for row in varieties if row["speciesId"] == sid) != 1 for sid in ids):
        raise ValueError("Each species needs exactly one default variety")


def envelope_for(payload):
    validate_payload(payload)
    content = canonical(payload)
    return {"formatVersion": 1, "generatorVersion": "1",
            "generatedAt": dt.datetime.now(dt.timezone.utc).isoformat().replace("+00:00", "Z"),
            "contentSha256": hashlib.sha256(content.encode("utf-8")).hexdigest(), "payloadJson": content}


def verify(envelope):
    if envelope["formatVersion"] != 1 or envelope["generatorVersion"] != "1":
        raise ValueError("Unsupported format")
    dt.datetime.fromisoformat(envelope["generatedAt"].replace("Z", "+00:00"))
    if hashlib.sha256(envelope["payloadJson"].encode("utf-8")).hexdigest() != envelope["contentSha256"]:
        raise ValueError("Hash mismatch")
    payload = json.loads(envelope["payloadJson"])
    validate_payload(payload)
    return payload


def check_changes(previous, current):
    for key, identity, fields in (("species", "speciesId", ("nationalNumber", "slug")),
                                  ("varieties", "pokemonId", ("speciesId", "slug"))):
        new = {row[identity]: row for row in current[key]}
        for row in previous[key]:
            other = new.get(row[identity])
            if other is None or any(other[field] != row[field] for field in fields):
                raise ValueError(f"Disappearing/reassigned identity: {key}/{row[identity]}")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", default="RotomPedia/app/src/main/assets/pokemon-index.json")
    parser.add_argument("--cache", default=".cache/pokeapi-index")
    parser.add_argument("--workers", type=int, default=4, choices=range(1, 5))
    parser.add_argument("--refresh", action="store_true")
    parser.add_argument("--verify-only", action="store_true")
    parser.add_argument("--previous", help="Reject disappearing/reassigned identities against this snapshot")
    args = parser.parse_args()
    if args.verify_only:
        payload = verify(json.loads(Path(args.output).read_text(encoding="utf-8")))
    else:
        client = Client(args.cache, args.refresh)
        national = client.get(SOURCE, max_age=0)
        urls = [BASE + f"pokemon-species/{resource_id(row['pokemon_species']['url'], 'pokemon-species')}/"
                for row in national["pokemon_entries"]]
        with concurrent.futures.ThreadPoolExecutor(max_workers=args.workers) as executor:
            documents = list(executor.map(client.get, urls))
        payload = build_payload(national, documents)
        old_path = Path(args.previous or args.output)
        if old_path.exists():
            check_changes(verify(json.loads(old_path.read_text(encoding="utf-8"))), payload)
        envelope = envelope_for(payload)
        atomic_write(args.output, json.dumps(envelope, ensure_ascii=False, indent=2) + "\n")
    print(f"Verified: {payload['speciesCount']} species; {payload['varietyCount']} varieties. Forms are on-demand, not complete.")


if __name__ == "__main__":
    main()
