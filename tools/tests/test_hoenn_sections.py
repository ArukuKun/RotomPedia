"""Static source linkage tests; the source grid is not latitude/longitude."""
import json
import re
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SECTIONS = ROOT / "RotomPedia/app/src/main/java/com/duoc/rotompedia/data/maps/HoennSections.kt"
LOCATIONS = ROOT / "RotomPedia/app/src/main/assets/hoenn-emerald-location-pilot.json"


class HoennSectionsTest(unittest.TestCase):
    def test_section_references_are_sane_and_bound_to_pilot_locations(self):
        text = SECTIONS.read_text(encoding="utf-8")
        sections = [
            (slug, *map(int, coords))
            for slug, *coords in re.findall(
                r'HoennSection\("([\w-]+)", (\d+), (\d+), (\d+), (\d+)\)', text
            )
        ]
        self.assertEqual(68, len(sections))
        self.assertEqual(len(sections), len({x[0] for x in sections}))
        for slug, x, y, width, height in sections:
            with self.subTest(slug=slug):
                self.assertGreaterEqual(x, 0)
                self.assertGreaterEqual(y, 0)
                self.assertGreater(width, 0)
                self.assertGreater(height, 0)
                self.assertLessEqual(x + width, 28)
                self.assertLessEqual(y + height, 15)
        envelope = json.loads(LOCATIONS.read_text(encoding="utf-8"))
        payload = json.loads(envelope["payloadJson"])
        pilot_slugs = {row["slug"] for row in payload["locations"]}
        linked = set(s[0] for s in sections).intersection(pilot_slugs)
        self.assertGreaterEqual(len(linked), 60)
        # Audited coordinates from the pinned Emerald decompilation, not guessed image pixels.
        by_slug = {r[0]: r[1:] for r in sections}
        self.assertEqual((4, 10, 1, 1), by_slug["hoenn-route-101"])
        self.assertEqual((9, 10, 3, 1), by_slug["hoenn-route-134"])
        self.assertEqual((21, 7, 1, 1), by_slug["sootopolis-city"])
        self.assertEqual((24, 5, 2, 1), by_slug["mossdeep-city"])
        self.assertEqual((6, 3, 1, 1), by_slug["fiery-path"])
        self.assertNotIn("colosseum", pilot_slugs)


if __name__ == "__main__":
    unittest.main()
