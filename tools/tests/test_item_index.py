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
