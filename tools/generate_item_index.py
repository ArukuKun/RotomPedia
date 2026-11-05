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
    pockets = payload.get('pockets', [])
    categories = payload.get('categoryPockets', [])
    if pockets or categories or payload.get('pocketsComplete', False):
        if not pockets or not categories:
            raise ValueError('Incomplete pocket metadata')
        for values, id_key, slug_key in ((pockets, 'id', 'slug'), (categories, 'categoryId', 'category')):
            if len({v[id_key] for v in values}) != len(values) or len({v[slug_key] for v in values}) != len(values):
                raise ValueError('Duplicate pocket/category identity')
            if any(v[id_key] <= 0 or not v[slug_key] for v in values):
                raise ValueError('Invalid pocket/category identity')
        ids = {p['id'] for p in pockets}
        if any(c['pocketId'] not in ids for c in categories) or any(not p['name'] or not p['nameLanguage'] for p in pockets):
            raise ValueError('Orphan pocket or missing name')
        if payload.get('pocketsComplete', False) and not {r['category'] for r in rows} <= {c['category'] for c in categories}:
            raise ValueError('Missing category pocket coverage')


def verify(envelope):
    if envelope["formatVersion"] != 1 or hashlib.sha256(envelope["payloadJson"].encode()).hexdigest() != envelope["sha256"]:
        raise ValueError("Invalid format/hash")
    payload = json.loads(envelope["payloadJson"])
    validate(payload)
    return payload


def add_pockets(payload):
    base = f"https://raw.githubusercontent.com/PokeAPI/pokeapi/{payload['revision']}/data/v2/csv/"
    def rows(name):
        return list(csv.DictReader(io.StringIO(download(base + name + '.csv'))))
    names = {(int(r['item_pocket_id']), int(r['local_language_id'])): r['name'] for r in rows('item_pocket_names')}
    payload['pockets'] = [{'id': int(r['id']), 'slug': r['identifier'],
                           'name': names.get((int(r['id']), 7), names.get((int(r['id']), 9), r['identifier'])),
                           'nameLanguage': 'es' if (int(r['id']), 7) in names else 'en' if (int(r['id']), 9) in names else 'slug'}
                          for r in rows('item_pockets')]
    payload['categoryPockets'] = [{'categoryId': int(r['id']), 'category': r['identifier'], 'pocketId': int(r['pocket_id'])}
                                  for r in rows('item_categories')]
    payload['pocketsComplete'] = True
    return payload


def write(payload):
    validate(payload)
    text = json.dumps(payload, ensure_ascii=False, sort_keys=True, separators=(",", ":"))
    TARGET.write_text(json.dumps({"formatVersion": 1, "sha256": hashlib.sha256(text.encode()).hexdigest(),
                                  "payloadJson": text}, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


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
    add_pockets(payload)
    validate(payload)
    if TARGET.exists():
        old = verify(json.loads(TARGET.read_text(encoding="utf-8")))
        current = {r["itemId"]: r["slug"] for r in items}
        if any(current.get(r["itemId"]) != r["slug"] for r in old["items"]):
            raise ValueError("Disappearing/reassigned item identity")
    write(payload)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--verify-only", action="store_true")
    parser.add_argument("--add-pockets", action="store_true", help="Enrich existing index at its pinned revision without changing item data")
    args = parser.parse_args()
    if args.verify_only and args.add_pockets:
        parser.error('--verify-only and --add-pockets are mutually exclusive')
    if args.add_pockets:
        write(add_pockets(verify(json.loads(TARGET.read_text(encoding='utf-8')))))
    elif not args.verify_only:
        generate()
    print(f"Verified {len(verify(json.loads(TARGET.read_text(encoding='utf-8')))['items'])} items")
