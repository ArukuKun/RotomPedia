"""Keep backend/source diagnostics out of client-facing Kotlin UI copy, not out of data or docs."""
from pathlib import Path
import re
import unittest

ROOT = Path(__file__).resolve().parents[2]
UI = ROOT / 'RotomPedia/app/src/main/java/com/duoc/rotompedia/ui'
DIAGNOSTIC = re.compile(r'PokeAPI|caché|cache local|Fuente:|CSV oficiales|WikiDex|snapshot|índice local|copia guardada', re.I)
STRING = re.compile(r'"(?:\\.|[^"\\])*"')


def diagnostics(line):
    if line.lstrip().startswith(('//', '*', '/*')):
        return []
    return [s for s in STRING.findall(line) if not s[1:].startswith(('https:', 'http:')) and DIAGNOSTIC.search(s)]


class ClientCopyTests(unittest.TestCase):
    def test_ui_string_literals_do_not_expose_backend_source_or_cache_diagnostics(self):
        found = []
        for path in UI.rglob('*.kt'):
            for number, line in enumerate(path.read_text(encoding='utf-8').splitlines(), 1):
                for text in diagnostics(line):
                    found.append(f'{path.relative_to(ROOT)}:{number}: {text}')
        self.assertEqual([], found, '\n'.join(found))

    def test_guard_allows_network_resources_and_comments_but_detects_client_notices(self):
        self.assertEqual([], diagnostics('AsyncImage("https://raw.githubusercontent.com/PokeAPI/sprites/master/items/a.png")'))
        self.assertEqual([], diagnostics('// PokeAPI cache is an internal implementation detail.'))
        self.assertTrue(diagnostics('Text("Ficha disponible en caché local.")'))
        self.assertTrue(diagnostics('val message = "PokeAPI no registra datos"'))
