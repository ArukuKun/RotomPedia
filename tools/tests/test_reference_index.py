import importlib.util
import json
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[2]
spec = importlib.util.spec_from_file_location('reference_generator', ROOT / 'tools/generate_reference_index.py')
generator = importlib.util.module_from_spec(spec)
spec.loader.exec_module(generator)


class ReferenceTests(unittest.TestCase):
    def envelope(self):
        return json.loads((ROOT / generator.TARGET).read_text(encoding='utf-8'))

    def test_reference_integrity_spanish_and_reference_relations(self):
        p = generator.verify(self.envelope())
        self.assertGreaterEqual(len(p['moves']), 937)
        self.assertGreaterEqual(len(p['abilities']), 374)
        move = next(m for m in p['moves'] if m['slug'] == 'flamethrower')
        self.assertEqual(('Lanzallamas', 'fire', 'special', 90, 0), (move['nameEs'], move['type'], move['category'], move['power'], move['priority']))
        self.assertTrue(any(t['language'] == 'es' for t in move['texts']))
        self.assertEqual('Mar Llamas', next(a for a in p['abilities'] if a['slug'] == 'blaze')['nameEs'])
        self.assertTrue(any(r['moveId'] == 53 for r in p['machines']))

    def test_corrupt_hash_and_orphan_relation_rejected(self):
        e = self.envelope()
        e['payloadJson'] += ' '
        with self.assertRaises(ValueError):
            generator.verify(e)
        p = generator.verify(self.envelope())
        p['pokemonAbilities'].append({'pokemonId': 38, 'abilityId': 999999, 'slot': 1})
        with self.assertRaises(ValueError):
            generator.validate(p)

    def test_variety_links_do_not_replace_regional_with_base(self):
        p = generator.verify(self.envelope())
        normal = {x['abilityId'] for x in p['pokemonAbilities'] if x['pokemonId'] == 38}
        alola = {x['abilityId'] for x in p['pokemonAbilities'] if x['pokemonId'] == 10104}
        self.assertEqual({18, 70}, normal)
        self.assertEqual({81, 117}, alola)

    def test_links_only_use_bundled_pokemon_and_item_identities(self):
        p = generator.verify(self.envelope())
        pokemon = json.loads(json.loads((ROOT / 'RotomPedia/app/src/main/assets/pokemon-index.json').read_text(encoding='utf-8'))['payloadJson'])
        items = json.loads(json.loads((ROOT / 'RotomPedia/app/src/main/assets/item-index.json').read_text(encoding='utf-8'))['payloadJson'])
        self.assertTrue({x['pokemonId'] for x in p['pokemonAbilities']} <= {x['pokemonId'] for x in pokemon['varieties']})
        self.assertTrue({x['itemId'] for x in p['machines']} <= {x['itemId'] for x in items['items']})
