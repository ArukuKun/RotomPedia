import importlib.util
import json
from pathlib import Path
import unittest

spec = importlib.util.spec_from_file_location("generator", Path(__file__).resolve().parents[1] / "generate_pokemon_index.py")
generator = importlib.util.module_from_spec(spec)
spec.loader.exec_module(generator)


class IndexTests(unittest.TestCase):
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
