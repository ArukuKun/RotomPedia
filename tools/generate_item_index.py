"""Pinned lightweight item identities/names/categories/attributes. No per-search HTTP."""
import argparse
import csv
import hashlib
import io
import json
from pathlib import Path
import re
import urllib.request

TARGET = Path("RotomPedia/app/src/main/assets/item-index.json")


def download(url):
    with urllib.request.urlopen(urllib.request.Request(url, headers={"User-Agent": "RotomPedia-index"}), timeout=60) as response:
        return response.read().decode("utf-8")


def validate(payload):
    if not re.fullmatch(r"[a-f0-9]{40}", payload["revision"]):
        raise ValueError("Invalid revision")
    rows = payload["items"]
    if not rows or len({r["itemId"] for r in rows}) != len(rows):
        raise ValueError("Duplicate/empty identities")
    if any(r["itemId"] <= 0 or not r["slug"] or not r["category"] or not r["name"] for r in rows):
        raise ValueError("Invalid item")


def verify(envelope):
    if envelope["formatVersion"] != 1 or hashlib.sha256(envelope["payloadJson"].encode()).hexdigest() != envelope["sha256"]:
        raise ValueError("Invalid format/hash")
    payload = json.loads(envelope["payloadJson"])
    validate(payload)
    return payload


def generate():
    revision = json.loads(download("https://api.github.com/repos/PokeAPI/pokeapi/commits?path=data/v2/csv&per_page=1"))[0]["sha"]
    if not re.fullmatch(r"[a-f0-9]{40}", revision):
        raise ValueError("Invalid revision")
    base = f"https://raw.githubusercontent.com/PokeAPI/pokeapi/{revision}/data/v2/csv/"
    def rows(name):
        return list(csv.DictReader(io.StringIO(download(base + name + ".csv"))))
    names = {(int(r["item_id"]), int(r["local_language_id"])): r["name"] for r in rows("item_names")}
    categories = {int(r["id"]): r["identifier"] for r in rows("item_categories")}
    flags = {int(r["id"]): r["identifier"] for r in rows("item_flags")}
    attributes = {}
    for r in rows("item_flag_map"):
        attributes.setdefault(int(r["item_id"]), []).append(flags[int(r["item_flag_id"])])
    items = [{"itemId": int(r["id"]), "slug": r["identifier"],
              "name": names.get((int(r["id"]), 7), names.get((int(r["id"]), 9), r["identifier"])),
              "nameLanguage": "es" if (int(r["id"]), 7) in names else "en" if (int(r["id"]), 9) in names else "slug",
              "category": categories[int(r["category_id"])],
              "attributes": "|".join(sorted(attributes.get(int(r["id"]), [])))} for r in rows("items")]
    payload = {"revision": revision, "items": sorted(items, key=lambda r: r["itemId"])}
    validate(payload)
    if TARGET.exists():
        old = verify(json.loads(TARGET.read_text(encoding="utf-8")))
        current = {r["itemId"]: r["slug"] for r in items}
        if any(current.get(r["itemId"]) != r["slug"] for r in old["items"]):
            raise ValueError("Disappearing/reassigned item identity")
    text = json.dumps(payload, ensure_ascii=False, sort_keys=True, separators=(",", ":"))
    TARGET.write_text(json.dumps({"formatVersion": 1, "sha256": hashlib.sha256(text.encode()).hexdigest(),
                                 "payloadJson": text}, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--verify-only", action="store_true")
    args = parser.parse_args()
    if not args.verify_only:
        generate()
    print(f"Verified {len(verify(json.loads(TARGET.read_text(encoding='utf-8')))['items'])} items")
