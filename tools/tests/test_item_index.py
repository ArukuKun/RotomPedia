import importlib.util
import json
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[2]
spec = importlib.util.spec_from_file_location("item_generator", ROOT / "tools/generate_item_index.py")
generator = importlib.util.module_from_spec(spec)
spec.loader.exec_module(generator)


class ItemIndexTests(unittest.TestCase):
    def envelope(self):
        return json.loads((ROOT / generator.TARGET).read_text(encoding="utf-8"))

    def test_snapshot_coverage_names_and_duplicate_slugs_keep_distinct_ids(self):
        payload = generator.verify(self.envelope())
        self.assertGreaterEqual(len(payload["items"]), 2200)
        shuca = next(r for r in payload["items"] if r["itemId"] == 168)
        self.assertEqual(("shuca-berry", "Baya Acardo", "type-protection"), (shuca["slug"], shuca["name"], shuca["category"]))
        self.assertEqual(2, sum(r["slug"] == "roseli-berry" for r in payload["items"]))

    def test_corrupt_hash_rejected(self):
        envelope = self.envelope()
        envelope["payloadJson"] += " "
        with self.assertRaises(ValueError):
            generator.verify(envelope)

    def test_duplicate_ids_rejected(self):
        payload = generator.verify(self.envelope())
        payload["items"].append(dict(payload["items"][0]))
        with self.assertRaises(ValueError):
            generator.validate(payload)

    def test_pockets_cover_every_item_by_official_category_not_keyword_or_equippability(self):
        p = generator.verify(self.envelope())
        self.assertTrue(p['pocketsComplete'])
        self.assertEqual(8, len(p['pockets']))
        self.assertEqual(54, len(p['categoryPockets']))
        mapping = {c['category']: c['pocketId'] for c in p['categoryPockets']}
        self.assertTrue({i['category'] for i in p['items']} <= mapping.keys())
        self.assertEqual(5, mapping['medicine'])  # Berry category, not the medicine pocket.
        self.assertEqual(2, mapping['healing'])
        self.assertEqual(4, mapping['all-machines'])
        self.assertEqual(8, mapping['z-crystals'])
        self.assertEqual(1, mapping['held-items'])
        self.assertEqual(2, sum(i['slug'] == 'roseli-berry' for i in p['items']))

    def test_invalid_pocket_relations_duplicates_and_missing_coverage_are_rejected(self):
        for change in (
            lambda p: p['pockets'].append(dict(p['pockets'][0])),
            lambda p: p['categoryPockets'].append(dict(p['categoryPockets'][0])),
            lambda p: p['categoryPockets'][0].update(pocketId=999),
            lambda p: p['categoryPockets'].clear(),
            lambda p: p['items'][0].update(category='unknown-future-category'),
        ):
            p = generator.verify(self.envelope())
            change(p)
            with self.assertRaises(ValueError):
                generator.validate(p)

    def test_legacy_snapshot_without_pockets_remains_valid_and_partial_coverage_is_explicit(self):
        p = generator.verify(self.envelope())
        for key in ('pockets', 'categoryPockets', 'pocketsComplete'):
            p.pop(key)
        generator.validate(p)
        p = generator.verify(self.envelope())
        p['pocketsComplete'] = False
        p['items'][0]['category'] = 'unknown-future-category'
        generator.validate(p)
