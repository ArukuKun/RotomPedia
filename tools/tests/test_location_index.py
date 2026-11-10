"""Synthetic source relationships only; no invented coordinates or factual fixtures."""
import copy
import importlib.util
import json
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch

ROOT = Path(__file__).resolve().parents[2]
spec = importlib.util.spec_from_file_location('location_generator', ROOT / 'tools/generate_location_index.py')
generator = importlib.util.module_from_spec(spec)
spec.loader.exec_module(generator)


def tables():
    t = {name: [] for name in generator.FILES}
    t.update({
        'regions': [{'id': '3', 'identifier': 'hoenn'}],
        'versions': [{'id': '9', 'version_group_id': '6', 'identifier': 'emerald'},
                     {'id': '6', 'version_group_id': '5', 'identifier': 'ruby'}],
        'version_groups': [{'id': '6', 'identifier': 'emerald'}, {'id': '5', 'identifier': 'ruby-sapphire'}],
        'version_group_regions': [{'region_id': '3', 'version_group_id': '6'}],
        'locations': [{'id': '20', 'region_id': '3', 'identifier': 'fixture-route'}],
        'location_names': [{'location_id': '20', 'local_language_id': '7', 'name': 'Ruta de prueba'}],
        'location_areas': [{'id': '30', 'location_id': '20', 'identifier': ''},
                           {'id': '31', 'location_id': '20', 'identifier': 'unknown'}],
        'encounter_methods': [{'id': '1', 'identifier': 'walk'}, {'id': '18', 'identifier': 'gift'}],
        'encounter_slots': [{'id': '40', 'version_group_id': '6', 'encounter_method_id': '1', 'slot': '1', 'rarity': '20'},
                            {'id': '41', 'version_group_id': '6', 'encounter_method_id': '18', 'slot': '', 'rarity': '100'}],
        'encounters': [{'id': '50', 'version_id': '9', 'location_area_id': '30', 'encounter_slot_id': '40', 'pokemon_id': '10104', 'min_level': '2', 'max_level': '5'},
                       {'id': '51', 'version_id': '9', 'location_area_id': '30', 'encounter_slot_id': '41', 'pokemon_id': '1', 'min_level': '5', 'max_level': '5'},
                       {'id': '52', 'version_id': '6', 'location_area_id': '30', 'encounter_slot_id': '40', 'pokemon_id': '1', 'min_level': '7', 'max_level': '9'}],
        'encounter_conditions': [{'id': '2', 'identifier': 'time'}],
        'encounter_condition_values': [{'id': '60', 'encounter_condition_id': '2', 'identifier': 'time-night', 'is_default': '0'}],
        'encounter_condition_value_map': [{'encounter_id': '50', 'encounter_condition_value_id': '60'}],
        'location_area_encounter_rates': [{'location_area_id': '30', 'encounter_method_id': '1', 'version_id': '9', 'rate': '10'}]
    })
    return t


class LocationIndexTests(unittest.TestCase):
    def payload(self):
        return generator.build(tables(), 'hoenn', 'emerald', {1, 10104})

    def test_checked_real_candidate_matches_the_existing_national_index_without_installing_a_map(self):
        pokemon = json.loads((ROOT / 'RotomPedia/app/src/main/assets/pokemon-index.json').read_text(encoding='utf-8'))
        ids = {r['pokemonId'] for r in json.loads(pokemon['payloadJson'])['varieties']}
        candidate = ROOT / 'RotomPedia/app/src/test/resources/hoenn-emerald-location-candidate.json'
        p = generator.verify(json.loads(candidate.read_text(encoding='utf-8')), ids, pokemon['contentSha256'])
        self.assertEqual(('hoenn', 'emerald', 9), (p['region'], p['version'], p['versionId']))
        self.assertEqual((171, 115, 1644, 155), tuple(p['coverage'][k] for k in
                         ('declaredAreas', 'areasWithEncounters', 'encounterRows', 'pokemonVarieties')))
        self.assertFalse((ROOT / 'RotomPedia/app/src/main/assets/hoenn-emerald-location-candidate.json').exists())

    def test_shipped_emerald_asset_is_verified_and_filters_excluded_distribution_rows(self):
        national = json.loads((ROOT / 'RotomPedia/app/src/main/assets/pokemon-index.json').read_text(encoding='utf-8'))
        ids = {r['pokemonId'] for r in json.loads(national['payloadJson'])['varieties']}
        shipped = ROOT / 'RotomPedia/app/src/main/assets/hoenn-emerald-location-pilot.json'
        source = ROOT / 'RotomPedia/app/src/test/resources/hoenn-emerald-location-candidate.json'
        self.assertEqual(source.read_bytes(), shipped.read_bytes())
        payload = generator.verify(json.loads(shipped.read_text(encoding='utf-8')), ids, national['contentSha256'])
        self.assertEqual((171, 115, 1644, 155), tuple(payload['coverage'][k] for k in
                         ('declaredAreas', 'areasWithEncounters', 'encounterRows', 'pokemonVarieties')))
        excluded = [r for r in payload['encounters'] if r['methodId'] in {
            m['id'] for m in payload['methods'] if 'colosseum' in m['slug'].split('-') or 'xd' in m['slug'].split('-')
        }]
        self.assertEqual({70694, 70697}, {r['id'] for r in excluded})
        accepted = [r for r in payload['encounters'] if r not in excluded]
        self.assertEqual(1642, len(accepted))
        self.assertEqual(114, len({r['areaId'] for r in accepted}))

    def test_exact_version_slots_conditions_varieties_and_method_rates_are_not_conflated(self):
        p = self.payload()
        self.assertEqual([50, 51], [r['id'] for r in p['encounters']])
        self.assertEqual(10104, p['encounters'][0]['pokemonId'])
        self.assertEqual([60], p['encounters'][0]['conditionIds'])
        self.assertEqual(20, p['encounters'][0]['chance'])
        self.assertEqual(10, p['methodRates'][0]['rate'])
        self.assertEqual(18, p['encounters'][1]['methodId'])  # gift, not wild grass
        self.assertIsNone(p['encounters'][1]['slot'])
        self.assertEqual({'es': 'Ruta de prueba'}, p['locations'][0]['names'])

    def test_empty_declared_area_is_preserved_as_coverage_gap_not_proof_of_no_encounters(self):
        p = self.payload()
        self.assertEqual(2, len(p['areas']))
        self.assertEqual(1, p['coverage']['areasWithEncounters'])
        self.assertEqual('not-sourced', p['coverage']['objects'])
        self.assertEqual('not-sourced', p['coverage']['cartography'])
        self.assertFalse(any('x' in r or 'y' in r for r in p['locations']))

    def test_orphan_reassigned_duplicate_and_invalid_encounter_data_are_rejected(self):
        base = self.payload()
        for field, value in [('areaId', 999), ('pokemonId', 999), ('methodId', 999), ('versionId', 6),
                             ('minLevel', 0), ('maxLevel', 1), ('chance', 101), ('slot', -1), ('conditionIds', [999])]:
            p = copy.deepcopy(base)
            p['encounters'][0][field] = value
            with self.subTest(field=field), self.assertRaises(ValueError):
                generator.validate(p, {1, 10104})
        p = copy.deepcopy(base)
        p['encounters'].append(p['encounters'][0])
        with self.assertRaises(ValueError):
            generator.validate(p, {1, 10104})

    def test_wrong_slot_game_or_missing_pokemon_identity_cannot_silently_change_coverage(self):
        t = tables()
        t['encounter_slots'][0]['version_group_id'] = '5'
        with self.assertRaises(ValueError):
            generator.build(t, 'hoenn', 'emerald', {1, 10104})
        with self.assertRaises(ValueError):
            generator.build(tables(), 'hoenn', 'emerald', {1})

    def test_candidate_hash_identity_pin_and_provenance_are_checked(self):
        p = self.payload()
        p['source'] = {'revision': 'a' * 40, 'pokemonIndexSha256': 'b' * 64,
                       'files': [{'file': f + '.csv', 'sha256': 'c' * 64} for f in generator.FILES],
                       'licenseUrl': 'https://github.com/PokeAPI/pokeapi/blob/' + 'a' * 40 + '/LICENSE.md',
                       'licenseText': 'Fixture attribution, not a real data licence'}
        e = generator.envelope(p)
        self.assertEqual(p, generator.verify(e, {1, 10104}, 'b' * 64))
        with self.assertRaises(ValueError):
            generator.verify(e, {1, 10104}, 'd' * 64)
        e['payloadJson'] += ' '
        with self.assertRaises(ValueError):
            generator.verify(e, {1, 10104}, 'b' * 64)

    def test_claiming_cartography_or_objects_or_combining_duplicate_method_rates_is_rejected(self):
        for field in ('objects', 'cartography'):
            p = self.payload()
            p['coverage'][field] = 'complete'
            with self.assertRaises(ValueError):
                generator.validate(p, {1, 10104})
        p = self.payload()
        p['methodRates'].append(p['methodRates'][0])
        with self.assertRaises(ValueError):
            generator.validate(p, {1, 10104})

    def test_source_cache_cannot_be_mislabeled_as_another_revision_or_silently_accepted_without_provenance(self):
        with tempfile.TemporaryDirectory() as directory:
            cache = Path(directory)
            with self.assertRaises(ValueError):
                generator.read_tables(cache, 'a' * 40)
            (cache / 'source-manifest.json').write_text(json.dumps({'revision': 'b' * 40, 'files': []}))
            with self.assertRaises(ValueError):
                generator.read_tables(cache, 'a' * 40)

    def test_cli_binds_existing_pokemon_envelope_content_hash_and_verifies_without_network(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            assets = root / 'RotomPedia/app/src/main/assets'
            assets.mkdir(parents=True)
            text = generator.canonical({'referenceRevision': 'a' * 40, 'varieties': [{'pokemonId': 1}, {'pokemonId': 10104}]})
            (assets / 'pokemon-index.json').write_text(json.dumps({'payloadJson': text, 'contentSha256': generator.digest(text.encode())}))
            licence = root / 'tools/licenses/PokeAPI-BSD-3-Clause.txt'
            licence.parent.mkdir(parents=True)
            licence.write_text('Synthetic attribution for test only')
            output = root / 'candidate.json'
            manifest = [{'file': f + '.csv', 'sha256': 'c' * 64} for f in generator.FILES]
            with patch.object(generator, 'ROOT', root), patch.object(generator, 'read_tables', return_value=(tables(), manifest)), \
                    patch('sys.argv', ['generator', '--cache', str(root), '--output', str(output)]):
                generator.main()
            with patch.object(generator, 'ROOT', root), patch('sys.argv', ['generator', '--verify-only', str(output)]), \
                    patch.object(generator.urllib.request, 'urlopen', side_effect=AssertionError('Unexpected network')):
                generator.main()

    def test_changed_cached_csv_is_rejected_without_refreshing_or_relabeling_it(self):
        with tempfile.TemporaryDirectory() as directory:
            cache = Path(directory)
            raw = b'id\n1\n'
            manifest = []
            for name in generator.FILES:
                path = cache / (name + '.csv')
                path.write_bytes(raw)
                manifest.append({'file': path.name, 'sha256': generator.digest(raw)})
            (cache / 'source-manifest.json').write_text(json.dumps({'revision': 'a' * 40, 'files': manifest}))
            with patch.object(generator.urllib.request, 'urlopen', side_effect=AssertionError('Unexpected network')):
                generator.read_tables(cache, 'a' * 40)
                (cache / 'encounters.csv').write_bytes(b'id\n2\n')
                with self.assertRaises(ValueError):
                    generator.read_tables(cache, 'a' * 40)
