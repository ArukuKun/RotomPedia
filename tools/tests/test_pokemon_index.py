import importlib.util
import json
from pathlib import Path
import unittest

spec = importlib.util.spec_from_file_location("generator", Path(__file__).resolve().parents[1] / "generate_pokemon_index.py")
generator = importlib.util.module_from_spec(spec)
spec.loader.exec_module(generator)


class IndexTests(unittest.TestCase):
    def bundled(self):
        path = Path(__file__).resolve().parents[2] / "RotomPedia/app/src/main/assets/pokemon-index.json"
        return generator.verify(json.loads(path.read_text(encoding="utf-8")))

    def test_bundled_explorer_covers_all_varieties_without_inventing_defaults(self):
        payload = self.bundled()
        self.assertTrue(payload["explorerComplete"])
        self.assertEqual(1351, len(payload["varieties"]))
        ids = {v["pokemonId"] for v in payload["varieties"]}
        self.assertEqual(ids, {r["pokemonId"] for r in payload["varietyTypes"]})
        self.assertEqual(ids, {r["pokemonId"] for r in payload["forms"]})
        self.assertFalse(next(r for r in payload["forms"] if r["pokemonId"] == 10264)["isDefault"])

    def test_missing_type_coverage_rejected(self):
        payload = self.bundled()
        payload["varietyTypes"] = [r for r in payload["varietyTypes"] if r["pokemonId"] != 10104]
        with self.assertRaises(ValueError):
            generator.validate_payload(payload)

    def test_regional_dexes_pin_Hisui_starter_order_and_source_editions(self):
        payload = self.bundled()
        self.assertTrue(payload["regionalDexesComplete"])
        self.assertEqual(1445, len(payload["regionalDexNumbers"]))
        hisui = next(r for r in payload["regionalDexes"] if r["region"] == "hisui")
        self.assertEqual(["legends-arceus"], hisui["versionGroups"])
        numbers = {r["speciesId"]: r["number"] for r in payload["regionalDexNumbers"] if r["dexId"] == hisui["id"]}
        self.assertEqual([3, 6, 9], [numbers[s] for s in (724, 157, 503)])
        self.assertEqual(242, len(numbers))

    def test_regional_orphan_duplicate_and_incomplete_records_rejected(self):
        for kind in ("orphan", "duplicate", "missing"):
            payload = self.bundled()
            if kind == "orphan":
                payload["regionalDexNumbers"][0]["speciesId"] = 999999
            elif kind == "duplicate":
                payload["regionalDexNumbers"].append(dict(payload["regionalDexNumbers"][0]))
            else:
                payload["regionalDexNumbers"] = [r for r in payload["regionalDexNumbers"] if r["dexId"] != 30]
            with self.assertRaises(ValueError):
                generator.validate_payload(payload)

    def test_egg_groups_cover_all_species_with_pinned_relationships(self):
        payload = self.bundled()
        self.assertTrue(payload["eggGroupsComplete"])
        self.assertEqual(15, len(payload["eggGroups"]))
        self.assertEqual(1304, len(payload["speciesEggGroups"]))
        self.assertEqual({1, 7}, {r["eggGroupId"] for r in payload["speciesEggGroups"] if r["speciesId"] == 1})
        self.assertEqual("Campo", next(r["nameEs"] for r in payload["eggGroups"] if r["slug"] == "ground"))

    def test_orphan_duplicate_or_missing_egg_group_is_rejected(self):
        for kind in ("orphan", "duplicate", "missing", "unflagged"):
            payload = self.bundled()
            if kind in ("orphan", "unflagged"):
                payload["speciesEggGroups"][0]["eggGroupId"] = 999
                if kind == "unflagged":
                    payload["eggGroupsComplete"] = False
            elif kind == "duplicate":
                payload["speciesEggGroups"].append(dict(payload["speciesEggGroups"][0]))
            else:
                payload["speciesEggGroups"] = [r for r in payload["speciesEggGroups"] if r["speciesId"] != 1]
            with self.assertRaises(ValueError):
                generator.validate_payload(payload)

    def test_orphan_form_and_duplicate_type_slot_rejected(self):
        for field, bad in (("forms", {"pokemonFormId": 99999, "pokemonId": 99999}),
                           ("varietyTypes", None)):
            payload = self.bundled()
            payload[field].append(bad or dict(payload[field][0]))
            with self.assertRaises(ValueError):
                generator.validate_payload(payload)

    def payload(self):
        return {"source": generator.SOURCE, "speciesCount": 1, "varietyCount": 2,
                "species": [{"speciesId": 3, "nationalNumber": 1, "slug": "venusaur", "generationId": 1,
                             "evolutionChainUrl": None, "names": [{"language": "es", "name": "Venusaur"}]}],
                "varieties": [{"pokemonId": 3, "speciesId": 3, "slug": "venusaur", "isDefault": True},
                              {"pokemonId": 10033, "speciesId": 3, "slug": "venusaur-mega", "isDefault": False}]}

    def test_round_trip(self):
        payload = self.payload()
        self.assertEqual(payload, generator.verify(generator.envelope_for(payload)))

    def test_orphan_rejected(self):
        payload = self.payload()
        payload["varieties"][0]["speciesId"] = 0
        with self.assertRaises(ValueError):
            generator.validate_payload(payload)

    def test_duplicate_id_rejected(self):
        payload = self.payload()
        payload["varieties"][1]["pokemonId"] = 3
        with self.assertRaises(ValueError):
            generator.validate_payload(payload)

    def test_missing_default_rejected(self):
        payload = self.payload()
        payload["varieties"][0]["isDefault"] = False
        with self.assertRaises(ValueError):
            generator.validate_payload(payload)

    def test_national_number_is_not_species_id(self):
        payload = self.payload()
        generator.validate_payload(payload)
        self.assertNotEqual(payload["species"][0]["nationalNumber"], payload["species"][0]["speciesId"])

    def test_hash_tampering_rejected(self):
        envelope = generator.envelope_for(self.payload())
        envelope["payloadJson"] += " "
        with self.assertRaises(ValueError):
            generator.verify(envelope)

    def test_foreign_url_rejected(self):
        with self.assertRaises(ValueError):
            generator.resource_id("https://example.com/pokemon/3/", "pokemon")

    def test_disappearing_identity_rejected(self):
        payload = self.payload()
        changed = json.loads(json.dumps(payload))
        changed["varieties"].pop()
        changed["varietyCount"] = 1
        with self.assertRaises(ValueError):
            generator.check_changes(payload, changed)

    def test_build_uses_species_relationship_not_slug(self):
        national = {"pokemon_entries": [{"entry_number": 1, "pokemon_species": {"name": "venusaur", "url": generator.BASE + "pokemon-species/3/"}}]}
        document = {"id": 3, "name": "venusaur", "generation": {"url": generator.BASE + "generation/1/"},
                    "names": [], "varieties": [{"is_default": True, "pokemon": {"name": "unrelated-slug", "url": generator.BASE + "pokemon/55/"}}]}
        payload = generator.build_payload(national, [document])
        self.assertEqual(3, payload["varieties"][0]["speciesId"])


if __name__ == "__main__":
    unittest.main()
