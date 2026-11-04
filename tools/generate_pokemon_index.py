"""Generate an identity-only national index from PokeAPI. Python 3.10+, stdlib only.

Normal Android builds use the checked, pinned asset; they never run this downloader.
Candidate generation does not commit, merge, publish or update an installed app.
"""
import argparse
import concurrent.futures
import csv
import io
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
CSV_BASE = "https://raw.githubusercontent.com/PokeAPI/pokeapi/master/data/v2/csv/"


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


def enrich_payload(payload, downloader=None):
    """Bulk reference files from the same upstream, never one HTTP call per search.

    A single Git commit pins all CSVs to a consistent source revision.
    """
    if downloader is None:
        def downloader(url):
            request = urllib.request.Request(url, headers={"User-Agent": "RotomPedia-index-generator/2"})
            with urllib.request.urlopen(request, timeout=60) as response:
                return response.read().decode("utf-8")
    revision = json.loads(downloader("https://api.github.com/repos/PokeAPI/pokeapi/commits?path=data/v2/csv&per_page=1"))[0]["sha"]
    if not re.fullmatch(r"[a-f0-9]{40}", revision):
        raise ValueError("Invalid upstream revision")
    base = CSV_BASE.replace("/master/", f"/{revision}/")
    def rows(name):
        return list(csv.DictReader(io.StringIO(downloader(base + name))))
    ids = {row["pokemonId"] for row in payload["varieties"]}
    types = {int(row["id"]): row["identifier"] for row in rows("types.csv")}
    payload["varietyTypes"] = [{"pokemonId": int(row["pokemon_id"]), "typeName": types[int(row["type_id"])], "slot": int(row["slot"])}
                               for row in rows("pokemon_types.csv") if int(row["pokemon_id"]) in ids]
    payload["forms"] = [{"pokemonFormId": int(row["id"]), "pokemonId": int(row["pokemon_id"]), "slug": row["identifier"],
                         "isMega": row["is_mega"] == "1", "isBattleOnly": row["is_battle_only"] == "1",
                         "isDefault": row["is_default"] == "1", "formOrder": int(row["form_order"] or "0")}
                        for row in rows("pokemon_forms.csv") if int(row["pokemon_id"]) in ids]
    form_ids = {row["pokemonFormId"] for row in payload["forms"]}
    payload["formNames"] = [{"resourceId": int(row["pokemon_form_id"]), "language": "es",
                             "name": row["pokemon_name"] or row["form_name"]}
                            for row in rows("pokemon_form_names.csv") if int(row["pokemon_form_id"]) in form_ids
                            and row["local_language_id"] == "7" and (row["pokemon_name"] or row["form_name"])]
    payload["referenceRevision"] = revision
    enrich_egg_groups(payload, downloader)
    payload["explorerComplete"] = True
    validate_payload(payload)
    return payload


def enrich_egg_groups(payload, downloader):
    """Extend the existing snapshot, keeping its identities and pinned revision."""
    revision = payload["referenceRevision"]
    if not re.fullmatch(r"[a-f0-9]{40}", revision):
        raise ValueError("Invalid upstream revision")
    base = CSV_BASE.replace("/master/", f"/{revision}/")
    def rows(name):
        return list(csv.DictReader(io.StringIO(downloader(base + name))))
    names = {int(r["egg_group_id"]): r["name"] for r in rows("egg_group_prose.csv") if r["local_language_id"] == "7"}
    payload["eggGroups"] = [{"id": int(r["id"]), "slug": r["identifier"], "nameEs": names.get(int(r["id"]))}
                            for r in rows("egg_groups.csv")]
    species_ids = {r["speciesId"] for r in payload["species"]}
    payload["speciesEggGroups"] = [{"speciesId": int(r["species_id"]), "eggGroupId": int(r["egg_group_id"])}
                                   for r in rows("pokemon_egg_groups.csv") if int(r["species_id"]) in species_ids]
    payload["eggGroupsComplete"] = True
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
    if payload.get("eggGroupsComplete", False) or payload.get("eggGroups") or payload.get("speciesEggGroups"):
        groups, links = payload.get("eggGroups", []), payload.get("speciesEggGroups", [])
        if not re.fullmatch(r"[a-f0-9]{40}", payload.get("referenceRevision", "")):
            raise ValueError("Invalid egg group reference revision")
        group_ids = {r["id"] for r in groups}
        if not groups or len(group_ids) != len(groups) or len({r["slug"] for r in groups}) != len(groups) or any(r["id"] <= 0 or not r["slug"] for r in groups):
            raise ValueError("Invalid egg groups")
        if any(r["speciesId"] not in ids or r["eggGroupId"] not in group_ids for r in links):
            raise ValueError("Orphan egg group relationship")
        if len({(r["speciesId"], r["eggGroupId"]) for r in links}) != len(links):
            raise ValueError("Duplicate egg group relationship")
        if {r["speciesId"] for r in links} != ids or any(sum(r["speciesId"] == sid for r in links) not in (1, 2) for sid in ids):
            raise ValueError("Incomplete egg group coverage")
    if payload.get("explorerComplete", False):
        if not re.fullmatch(r"[a-f0-9]{40}", payload.get("referenceRevision", "")):
            raise ValueError("Invalid upstream revision")
        pokemon_ids = {row["pokemonId"] for row in varieties}
        typed = payload["varietyTypes"]
        if {row["pokemonId"] for row in typed} != pokemon_ids or any(row["slot"] not in (1, 2) or not row["typeName"] for row in typed):
            raise ValueError("Incomplete type coverage")
        if len({(row["pokemonId"], row["slot"]) for row in typed}) != len(typed):
            raise ValueError("Duplicate type slot")
        forms = payload["forms"]
        if len({row["pokemonFormId"] for row in forms}) != len(forms) or any(row["pokemonId"] not in pokemon_ids for row in forms):
            raise ValueError("Invalid form identity")
        if {row["pokemonId"] for row in forms} != pokemon_ids:
            raise ValueError("Incomplete form coverage")
        defaults = [row["pokemonId"] for row in forms if row["isDefault"]]
        if len(defaults) != len(set(defaults)):
            raise ValueError("Duplicate default form")
        if any(row["pokemonFormId"] <= 0 or not row["slug"] for row in forms):
            raise ValueError("Invalid form")
        form_ids = {row["pokemonFormId"] for row in forms}
        if any(row["resourceId"] not in form_ids or not row["name"] for row in payload["formNames"]):
            raise ValueError("Invalid form name")


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
    parser.add_argument("--enrich-eggs-only", action="store_true", help="Extend the existing verified snapshot at its pinned revision; preserve all identities")
    parser.add_argument("--previous", help="Reject disappearing/reassigned identities against this snapshot")
    args = parser.parse_args()
    if args.verify_only:
        payload = verify(json.loads(Path(args.output).read_text(encoding="utf-8")))
    elif args.enrich_eggs_only:
        previous = verify(json.loads(Path(args.output).read_text(encoding="utf-8")))
        def download(url):
            with urllib.request.urlopen(urllib.request.Request(url, headers={"User-Agent": "RotomPedia-index-generator/2"}), timeout=60) as response:
                return response.read().decode("utf-8")
        payload = enrich_egg_groups(json.loads(canonical(previous)), download)
        check_changes(previous, payload)
        atomic_write(args.output, json.dumps(envelope_for(payload), ensure_ascii=False, indent=2) + "\n")
    else:
        client = Client(args.cache, args.refresh)
        national = client.get(SOURCE, max_age=0)
        urls = [BASE + f"pokemon-species/{resource_id(row['pokemon_species']['url'], 'pokemon-species')}/"
                for row in national["pokemon_entries"]]
        with concurrent.futures.ThreadPoolExecutor(max_workers=args.workers) as executor:
            documents = list(executor.map(client.get, urls))
        payload = build_payload(national, documents)
        payload = enrich_payload(payload)
        old_path = Path(args.previous or args.output)
        if old_path.exists():
            check_changes(verify(json.loads(old_path.read_text(encoding="utf-8"))), payload)
        envelope = envelope_for(payload)
        atomic_write(args.output, json.dumps(envelope, ensure_ascii=False, indent=2) + "\n")
    print(f"Verified: {payload['speciesCount']} species; {payload['varietyCount']} varieties; "
          f"{len(payload.get('forms', []))} form identities. Explorer metadata: {payload.get('explorerComplete', False)}. "
          "Selectable entries are Pokemon varieties, not every cosmetic form.")


if __name__ == "__main__":
    main()
