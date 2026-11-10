"""Pinned location/encounter candidate. Does not author map coordinates or modify app assets.

Downloads only with --download; normal builds/tests never contact the network.
An explicit output outside the installed assets keeps an unapproved pilot out of the app.
"""
import argparse
import csv
import hashlib
import io
import json
from pathlib import Path
import re
import urllib.request

ROOT = Path(__file__).resolve().parents[1]
FILES = ('regions', 'versions', 'version_groups', 'version_group_regions', 'locations', 'location_names',
         'location_areas', 'location_area_prose', 'encounters', 'encounter_slots', 'encounter_methods',
         'encounter_method_prose', 'encounter_conditions', 'encounter_condition_values',
         'encounter_condition_value_prose', 'encounter_condition_value_map', 'location_area_encounter_rates')


def canonical(value):
    return json.dumps(value, ensure_ascii=False, sort_keys=True, separators=(',', ':'))


def digest(value):
    return hashlib.sha256(value).hexdigest()


def indexed(rows, field='id'):
    result = {int(r[field]): r for r in rows}
    if len(result) != len(rows) or any(identity <= 0 for identity in result):
        raise ValueError('Duplicate/invalid source identity: ' + field)
    return result


def names(rows, field, identity):
    return {('es' if r['local_language_id'] == '7' else 'en'): ' '.join(r['name'].split())
            for r in rows if int(r[field]) == identity and r['local_language_id'] in ('7', '9') and r['name'].strip()}


def build(t, region_slug, version_slug, pokemon_ids):
    region = next(r for r in t['regions'] if r['identifier'] == region_slug)
    version = next(r for r in t['versions'] if r['identifier'] == version_slug)
    rid, vid, gid = int(region['id']), int(version['id']), int(version['version_group_id'])
    group = indexed(t['version_groups'])[gid]
    if not any(int(r['region_id']) == rid and int(r['version_group_id']) == gid for r in t['version_group_regions']):
        raise ValueError('Version group does not visit the selected region')
    locations = [{'id': int(r['id']), 'slug': r['identifier'], 'names': names(t['location_names'], 'location_id', int(r['id']))}
                 for r in t['locations'] if r['region_id'] == str(rid)]
    lids = {r['id'] for r in locations}
    areas = [{'id': int(r['id']), 'locationId': int(r['location_id']), 'identifier': r['identifier'],
              'names': names(t['location_area_prose'], 'location_area_id', int(r['id']))}
             for r in t['location_areas'] if int(r['location_id']) in lids]
    aids = {r['id'] for r in areas}
    slots, methods = indexed(t['encounter_slots']), indexed(t['encounter_methods'])
    selected = [r for r in t['encounters'] if r['version_id'] == str(vid) and int(r['location_area_id']) in aids]
    encounter_ids = set(indexed(selected))
    conditions = {}
    for row in t['encounter_condition_value_map']:
        if int(row['encounter_id']) in encounter_ids:
            conditions.setdefault(int(row['encounter_id']), []).append(int(row['encounter_condition_value_id']))
    encounters = []
    for row in selected:
        slot = slots[int(row['encounter_slot_id'])]
        if int(slot['version_group_id']) != gid:
            raise ValueError('Encounter slot belongs to a different game group')
        encounters.append({'id': int(row['id']), 'areaId': int(row['location_area_id']), 'versionId': vid,
                           'pokemonId': int(row['pokemon_id']), 'methodId': int(slot['encounter_method_id']),
                           'slotId': int(slot['id']), 'slot': int(slot['slot']) if slot['slot'] else None,
                           'chance': int(slot['rarity']) if slot['rarity'] else None,
                           'minLevel': int(row['min_level']), 'maxLevel': int(row['max_level']),
                           'conditionIds': sorted(conditions.get(int(row['id']), []))})
    rates = [{'areaId': int(r['location_area_id']), 'methodId': int(r['encounter_method_id']),
              'versionId': vid, 'rate': int(r['rate'])}
             for r in t['location_area_encounter_rates'] if r['version_id'] == str(vid) and int(r['location_area_id']) in aids]
    mids = {r['methodId'] for r in encounters + rates}
    cids = {c for r in encounters for c in r['conditionIds']}
    condition_values = indexed(t['encounter_condition_values'])
    condition_kinds = indexed(t['encounter_conditions'])
    values = []
    for cid in sorted(cids):
        row = condition_values[cid]
        kind = condition_kinds[int(row['encounter_condition_id'])]
        values.append({'id': cid, 'slug': row['identifier'], 'condition': kind['identifier'],
                       'isDefault': row['is_default'] == '1',
                       'names': names(t['encounter_condition_value_prose'], 'encounter_condition_value_id', cid)})
    result = {'regionId': rid, 'region': region_slug, 'versionId': vid, 'version': version_slug,
              'versionGroup': group['identifier'], 'locations': sorted(locations, key=lambda r: r['id']),
              'areas': sorted(areas, key=lambda r: r['id']), 'encounters': sorted(encounters, key=lambda r: r['id']),
              'methodRates': sorted(rates, key=lambda r: (r['areaId'], r['methodId'])),
              'methods': [{'id': mid, 'slug': methods[mid]['identifier'],
                           'names': names(t['encounter_method_prose'], 'encounter_method_id', mid)} for mid in sorted(mids)],
              'conditions': values,
              'coverage': {'declaredAreas': len(areas), 'areasWithEncounters': len({r['areaId'] for r in encounters}),
                           'encounterRows': len(encounters), 'pokemonVarieties': len({r['pokemonId'] for r in encounters}),
                           'objects': 'not-sourced', 'cartography': 'not-sourced'}}
    validate(result, pokemon_ids)
    return result


def validate(p, pokemon_ids):
    if p['regionId'] <= 0 or p['versionId'] <= 0 or not all(p[k] for k in ('region', 'version', 'versionGroup')):
        raise ValueError('Invalid region/game identity')
    loc, areas, methods, conditions = (indexed(p[k]) for k in ('locations', 'areas', 'methods', 'conditions'))
    if not loc or not areas or not p['encounters']:
        raise ValueError('Empty pilot')
    for rows in (loc.values(), methods.values(), conditions.values()):
        if any(not r['slug'] or any(lang not in ('es', 'en') or not text.strip() for lang, text in r['names'].items()) for r in rows):
            raise ValueError('Invalid localized identity')
    if any(r['locationId'] not in loc for r in areas.values()):
        raise ValueError('Orphan area')
    indexed(p['encounters'])
    for r in p['encounters']:
        if (r['areaId'] not in areas or r['versionId'] != p['versionId'] or r['pokemonId'] not in pokemon_ids or
                r['methodId'] not in methods or r['slotId'] <= 0 or (r['slot'] is not None and r['slot'] < 0) or
                not 1 <= r['minLevel'] <= r['maxLevel'] <= 100 or
                (r['chance'] is not None and not 0 <= r['chance'] <= 100) or
                len(set(r['conditionIds'])) != len(r['conditionIds']) or not set(r['conditionIds']) <= conditions.keys()):
            raise ValueError('Invalid encounter identity, slot, levels, chance or conditions')
    seen = set()
    for r in p['methodRates']:
        key = (r['areaId'], r['methodId'], r['versionId'])
        if key in seen or r['areaId'] not in areas or r['methodId'] not in methods or r['versionId'] != p['versionId'] or not 0 <= r['rate'] <= 100:
            raise ValueError('Invalid/duplicate method rate')
        seen.add(key)
    expected = {'declaredAreas': len(areas), 'areasWithEncounters': len({r['areaId'] for r in p['encounters']}),
                'encounterRows': len(p['encounters']), 'pokemonVarieties': len({r['pokemonId'] for r in p['encounters']}),
                'objects': 'not-sourced', 'cartography': 'not-sourced'}
    if p['coverage'] != expected:
        raise ValueError('Coverage cannot claim unverified objects/cartography or hide missing areas')


def envelope(p):
    text = canonical(p)
    return {'formatVersion': 1, 'sha256': digest(text.encode()), 'payloadJson': text}


def verify(e, pokemon_ids, pokemon_hash):
    if e['formatVersion'] != 1 or digest(e['payloadJson'].encode()) != e['sha256']:
        raise ValueError('Invalid format/hash')
    p = json.loads(e['payloadJson'])
    source = p['source']
    if (not re.fullmatch(r'[a-f0-9]{40}', source['revision']) or source['pokemonIndexSha256'] != pokemon_hash or
            len(source['files']) != len(FILES) or {r['file'] for r in source['files']} != {f + '.csv' for f in FILES} or
            any(not re.fullmatch(r'[a-f0-9]{64}', r['sha256']) for r in source['files']) or
            source['licenseUrl'] != f"https://github.com/PokeAPI/pokeapi/blob/{source['revision']}/LICENSE.md" or
            not source['licenseText'].strip()):
        raise ValueError('Missing source pin/hash/attribution or mismatched Pokemon index')
    validate(p, pokemon_ids)
    return p


def read_tables(cache, revision, download=False):
    if not re.fullmatch(r'[a-f0-9]{40}', revision):
        raise ValueError('A full pinned revision is required')
    tables, manifest = {}, []
    stamp = cache / 'source-manifest.json'
    pinned = json.loads(stamp.read_text(encoding='utf-8')) if stamp.exists() else None
    if pinned is not None and pinned['revision'] != revision:
        raise ValueError('Cache belongs to a different source revision')
    if pinned is None and not download:
        raise ValueError('Cache provenance missing; validate it once with --download')
    expected = {r['file']: r['sha256'] for r in pinned['files']} if pinned else {}
    for name in FILES:
        path = cache / (name + '.csv')
        if not path.exists() or pinned is None:
            if not download:
                raise ValueError('Missing cached source; use --download explicitly: ' + name)
            cache.mkdir(parents=True, exist_ok=True)
            url = f'https://raw.githubusercontent.com/PokeAPI/pokeapi/{revision}/data/v2/csv/{name}.csv'
            with urllib.request.urlopen(urllib.request.Request(url, headers={'User-Agent': 'RotomPedia-map-candidate'}), timeout=90) as response:
                raw = response.read()
            if path.exists() and digest(path.read_bytes()) != digest(raw):
                raise ValueError('Cached source differs from pinned upstream; refusing replacement: ' + name)
            if not path.exists():
                path.write_bytes(raw)
        raw = path.read_bytes()
        if pinned is not None and digest(raw) != expected.get(path.name):
            raise ValueError('Cached source checksum mismatch: ' + name)
        tables[name] = list(csv.DictReader(io.StringIO(raw.decode('utf-8'))))
        manifest.append({'file': path.name, 'sha256': digest(raw)})
    if pinned is None:
        stamp.write_text(canonical({'revision': revision, 'files': manifest}) + '\n', encoding='utf-8')
    return tables, manifest


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--cache', type=Path)
    parser.add_argument('--download', action='store_true')
    parser.add_argument('--compare', action='store_true')
    parser.add_argument('--region', default='hoenn')
    parser.add_argument('--version', default='emerald')
    parser.add_argument('--output', type=Path)
    parser.add_argument('--verify-only', type=Path)
    args = parser.parse_args()
    e = json.loads((ROOT / 'RotomPedia/app/src/main/assets/pokemon-index.json').read_text(encoding='utf-8'))
    if digest(e['payloadJson'].encode()) != e['contentSha256']:
        raise ValueError('Pokemon index hash mismatch')
    pokemon = json.loads(e['payloadJson'])
    ids = {r['pokemonId'] for r in pokemon['varieties']}
    if args.verify_only:
        p = verify(json.loads(args.verify_only.read_text(encoding='utf-8')), ids, e['contentSha256'])
        print(p['region'], p['version'], p['coverage'])
        return
    if args.cache is None or (not args.compare and args.output is None):
        parser.error('--cache and --output (or --compare) are required')
    revision = pokemon['referenceRevision']
    t, manifest = read_tables(args.cache, revision, args.download)
    if args.compare:
        for region, version in (('hoenn', 'emerald'), ('kanto', 'firered'), ('kanto', 'leafgreen')):
            p = build(t, region, version, ids)
            print(region, version, p['coverage'])
        return
    # No accidental publication of an unapproved candidate into the installed application.
    if (ROOT / 'RotomPedia/app/src/main/assets').resolve() in args.output.resolve().parents:
        raise ValueError('Pilot is not approved for app assets yet')
    p = build(t, args.region, args.version, ids)
    p['source'] = {'revision': revision, 'pokemonIndexSha256': e['contentSha256'], 'files': manifest,
                   'licenseUrl': f'https://github.com/PokeAPI/pokeapi/blob/{revision}/LICENSE.md',
                   'licenseText': (ROOT / 'tools/licenses/PokeAPI-BSD-3-Clause.txt').read_text(encoding='utf-8')}
    result = envelope(p)
    verify(result, ids, e['contentSha256'])
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(args.output, p['coverage'], 'sha256=' + result['sha256'])


if __name__ == '__main__':
    main()
