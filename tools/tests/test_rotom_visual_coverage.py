"""Structural guards, not a claim of visual/device validation."""
from pathlib import Path
import re
import unittest

ROOT = Path(__file__).resolve().parents[2]
KOTLIN = ROOT / 'RotomPedia/app/src/main/java/com/duoc/rotompedia'
SCREENS = KOTLIN / 'ui/screens'


class RotomVisualCoverageTests(unittest.TestCase):
    def test_live_home_route_uses_new_home_and_legacy_is_scoped_only_to_deferred_cartography(self):
        main = (KOTLIN / 'MainActivity.kt').read_text(encoding='utf-8')
        self.assertIn('RotomHomeScreen(', main)
        self.assertNotIn('fun MenuButton(', main)
        self.assertEqual(1, len(re.findall(r'RotompediaTheme\(legacy\s*=\s*true\)', main)))
        self.assertGreater(main.index('RotompediaTheme(legacy = true)'), main.index('composable("map_viewer/'))
        self.assertIn('pokemon_moves/{pokemonId}', main)
        self.assertIn('onMovesClick =', main)

    def test_all_non_map_full_screen_destinations_use_the_shared_device_frame(self):
        for name in ('RotomHomeScreen', 'LoginScreen', 'RegisterScreen', 'PokedexScreen', 'PokemonMovesScreen'):
            self.assertIn('RotomScaffold(', (SCREENS / f'{name}.kt').read_text(encoding='utf-8'), name)
        for name in ('PokemonDetailScreen', 'ReferenceScreens', 'CompetitiveItemsScreen', 'TeamBuilderScreen',
                     'PokemonVersusScreen', 'StatCalculatorScreen', 'CatchRateCalculatorScreen', 'GymLeadersScreen', 'TypeChartScreen'):
            text = (SCREENS / f'{name}.kt').read_text(encoding='utf-8')
            self.assertIn('RotomPageScaffold as Scaffold', text, name)
            self.assertIn('RotomPageTopBar as TopAppBar', text, name)

    def test_migrated_screens_and_dialogs_do_not_pin_dark_neutral_surfaces(self):
        # Type/efficacy/stat identity colors are intentionally not generic reading surfaces.
        neutral = r'0F0F0F|0F1115|121212|141414|161616|181818|1A1A1A|1C1C1C|1E1E1E|242424|262626|2A2A2A'
        pattern = re.compile(r'(?:containerColor\s*=|\.background\()\s*Color\(0xFF(?:' + neutral + r')\)')
        found = []
        for path in SCREENS.glob('*.kt'):
            if path.name in {'MapScreen.kt', 'LocationDetailBottomSheet.kt'}:
                continue  # Explicitly deferred, not silently claimed as migrated.
            for n, line in enumerate(path.read_text(encoding='utf-8').splitlines(), 1):
                if pattern.search(line):
                    found.append(f'{path.name}:{n}: {line.strip()}')
        self.assertEqual([], found, '\n'.join(found))
