"""Pinned move/ability reference, localized texts and verified machine/ability relations.

Normal Android builds never download this data. Pokemon learnsets remain on-demand.
"""
import csv
import hashlib
import io
import json
from pathlib import Path
import re
import urllib.request
import argparse

TARGET = Path('RotomPedia/app/src/main/assets/reference-index.json')


def download(url):
    with urllib.request.urlopen(urllib.request.Request(url, headers={'User-Agent': 'RotomPedia-index'}), timeout=90) as r:
        return r.read().decode('utf-8')


def validate(p):
    if not re.fullmatch(r'[a-f0-9]{40}', p['revision']):
        raise ValueError('Invalid revision')
    for kind in ('moves', 'abilities', 'versions'):
        rows = p[kind]
        if not rows or len({r['id'] for r in rows}) != len(rows) or any(r['id'] <= 0 or not r['slug'] for r in rows):
            raise ValueError('Invalid identities: ' + kind)
    moves = {r['id'] for r in p['moves']}
    abilities = {r['id'] for r in p['abilities']}
    versions = {r['slug'] for r in p['versions']}
    if any(r['moveId'] not in moves or r['versionGroup'] not in versions for r in p['machines']):
        raise ValueError('Orphan machine')
    if any(r['abilityId'] not in abilities or r['pokemonId'] <= 0 or r['slot'] <= 0 for r in p['pokemonAbilities']):
        raise ValueError('Orphan ability')
    if any(t['versionGroup'] not in versions or t['language'] not in ('es', 'en') or not t['text']
           for kind in ('moves', 'abilities') for r in p[kind] for t in r['texts']):
        raise ValueError('Invalid localized text')


def verify(e):
    if e['formatVersion'] != 1 or hashlib.sha256(e['payloadJson'].encode()).hexdigest() != e['sha256']:
        raise ValueError('Invalid format/hash')
    p = json.loads(e['payloadJson'])
    validate(p)
    return p


def generate():
    revision = json.loads(download('https://api.github.com/repos/PokeAPI/pokeapi/commits?path=data/v2/csv&per_page=1'))[0]['sha']
    if not re.fullmatch(r'[a-f0-9]{40}', revision):
        raise ValueError('Invalid revision')
    base = f'https://raw.githubusercontent.com/PokeAPI/pokeapi/{revision}/data/v2/csv/'
    def rows(file):
        return list(csv.DictReader(io.StringIO(download(base + file + '.csv'))))
    versions = [{'id': int(r['id']), 'slug': r['identifier'], 'order': int(r['order'])} for r in rows('version_groups')]
    version = {r['id']: r['slug'] for r in versions}
    types = {int(r['id']): r['identifier'] for r in rows('types')}
    classes = {int(r['id']): r['identifier'] for r in rows('move_damage_classes')}
    def names(file, key):
        return {(int(r[key]), int(r['local_language_id'])): r['name'] for r in rows(file)}
    move_names = names('move_names', 'move_id')
    ability_names = names('ability_names', 'ability_id')
    def effects(file, key):
        return {(int(r[key]), int(r['local_language_id'])): r['effect'] or r['short_effect'] for r in rows(file)
                if r['local_language_id'] in ('7', '9')}
    move_effects = effects('move_effect_prose', 'move_effect_id')
    ability_effects = effects('ability_prose', 'ability_id')
    def texts(file, key):
        # One best available es/en text per exact version group, not across games.
        grouped = {}
        for r in rows(file):
            if r['language_id'] not in ('7', '9') or not r['flavor_text'].strip():
                continue
            k = (int(r[key]), int(r['version_group_id']))
            if k not in grouped or r['language_id'] == '7':
                grouped[k] = {'versionGroup': version[k[1]], 'language': 'es' if r['language_id'] == '7' else 'en',
                              'text': ' '.join(r['flavor_text'].split())}
        result = {}
        for (identity, _), text in sorted(grouped.items()):
            result.setdefault(identity, []).append(text)
        return result
    move_texts = texts('move_flavor_text', 'move_id')
    ability_texts = texts('ability_flavor_text', 'ability_id')
    def integer(value):
        return int(value) if value else None
    moves = []
    for r in rows('moves'):
        id = int(r['id'])
        effect_id = integer(r['effect_id'])
        def effect(language):
            text = move_effects.get((effect_id, language))
            if text:
                text = text.replace('$effect_chance', r['effect_chance'] or '?')
            return text
        moves.append({'id': id, 'slug': r['identifier'], 'nameEs': move_names.get((id, 7)),
                      'nameEn': move_names.get((id, 9), r['identifier']), 'type': types[int(r['type_id'])],
                      'category': classes[int(r['damage_class_id'])], 'power': integer(r['power']), 'pp': integer(r['pp']),
                      'accuracy': integer(r['accuracy']), 'priority': int(r['priority']),
                      'effectEs': effect(7), 'effectEn': effect(9), 'texts': move_texts.get(id, [])})
    abilities = []
    for r in rows('abilities'):
        id = int(r['id'])
        abilities.append({'id': id, 'slug': r['identifier'], 'nameEs': ability_names.get((id, 7)),
                          'nameEn': ability_names.get((id, 9), r['identifier']), 'mainSeries': r['is_main_series'] == '1',
                          'effectEs': ability_effects.get((id, 7)), 'effectEn': ability_effects.get((id, 9)), 'texts': ability_texts.get(id, [])})
    pokemon = json.loads(json.loads(Path('RotomPedia/app/src/main/assets/pokemon-index.json').read_text(encoding='utf-8'))['payloadJson'])
    pokemon_ids = {r['pokemonId'] for r in pokemon['varieties']}
    items = json.loads(json.loads(Path('RotomPedia/app/src/main/assets/item-index.json').read_text(encoding='utf-8'))['payloadJson'])
    item_ids = {r['itemId'] for r in items['items']}
    links = [{'pokemonId': int(r['pokemon_id']), 'abilityId': int(r['ability_id']), 'hidden': r['is_hidden'] == '1', 'slot': int(r['slot'])}
             for r in rows('pokemon_abilities') if int(r['pokemon_id']) in pokemon_ids]
    machines = [{'itemId': int(r['item_id']), 'moveId': int(r['move_id']), 'versionGroup': version[int(r['version_group_id'])]}
                for r in rows('machines') if int(r['item_id']) in item_ids]
    p = {'revision': revision, 'versions': versions, 'moves': moves, 'abilities': abilities, 'pokemonAbilities': links, 'machines': machines}
    validate(p)
    if TARGET.exists():
        old = verify(json.loads(TARGET.read_text(encoding='utf-8')))
        for kind in ('moves', 'abilities'):
            current = {r['id']: r['slug'] for r in p[kind]}
            if any(current.get(r['id']) != r['slug'] for r in old[kind]):
                raise ValueError('Disappearing/reassigned reference identity')
    text = json.dumps(p, ensure_ascii=False, sort_keys=True, separators=(',', ':'))
    TARGET.write_text(json.dumps({'formatVersion': 1, 'sha256': hashlib.sha256(text.encode()).hexdigest(), 'payloadJson': text},
                                ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--verify-only', action='store_true')
    args = parser.parse_args()
    if not args.verify_only:
        generate()
    p = verify(json.loads(TARGET.read_text(encoding='utf-8')))
    print(f"Verified {len(p['moves'])} moves, {len(p['abilities'])} abilities, {len(p['pokemonAbilities'])} variety ability links")
