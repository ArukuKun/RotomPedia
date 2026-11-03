"""C evidence: GET and validate exact-variety normal/shiny candidates from B's gaps."""
import concurrent.futures
import datetime as dt
import hashlib
import json
from pathlib import Path
import urllib.error
import urllib.request

ROOT = Path(__file__).resolve().parents[1]
BASE = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/"


def inspect(row):
    result = {key: row[key] for key in ("pokemonId", "speciesId", "nationalNumber", "slug")}
    result["colours"] = {}
    for shiny in (False, True):
        suffix = f"{'shiny/' if shiny else ''}{row['pokemonId']}.png"
        sources = []
        for source, path in (("STANDARD", ""), ("ARTWORK", "other/official-artwork/"), ("HOME", "other/home/")):
            url = BASE + path + suffix
            item = {"source": source, "url": url}
            try:
                with urllib.request.urlopen(url, timeout=30) as response:
                    content = response.read()
                    item.update(httpStatus=response.status, validPng=content.startswith(b"\x89PNG\r\n\x1a\n"),
                                sha256=hashlib.sha256(content).hexdigest())
            except urllib.error.HTTPError as error:
                item["httpStatus"] = error.code
            except OSError as error:
                item["error"] = str(error)
            sources.append(item)
        selected = next((item["source"] for item in sources if item.get("validPng") and item.get("httpStatus") == 200), None)
        result["colours"]["shiny" if shiny else "normal"] = {"candidates": sources, "spriteStyleResolvedSource": selected,
                                                                   "placeholderExpected": selected is None}
    return result


def main():
    baseline = json.loads((ROOT / "tools/data/explorer-sprite-audit.json").read_text(encoding="utf-8"))
    with concurrent.futures.ThreadPoolExecutor(max_workers=4) as pool:
        rows = list(pool.map(inspect, baseline["missing"]))
    report = {"checkedAt": dt.datetime.now(dt.timezone.utc).isoformat(), "snapshotSha256": baseline["snapshotSha256"],
              "scope": "B missing standard sprites; exact Pokemon variety IDs; PNG signatures verified via GET",
              "cases": rows}
    target = ROOT / "tools/data/image-resolution-audit.json"
    target.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"Verified {len(rows)} varieties in normal and shiny; {target}")
    for row in rows:
        print(row["slug"], {colour: value["spriteStyleResolvedSource"] or "placeholder" for colour, value in row["colours"].items()})


if __name__ == "__main__":
    main()
