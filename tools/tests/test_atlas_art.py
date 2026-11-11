"""Byte-level provenance guard; does not certify spatial alignment or legal ownership."""
import hashlib
from pathlib import Path
import struct
import unittest

ROOT = Path(__file__).resolve().parents[2]


class AtlasArtTests(unittest.TestCase):
    def test_corrected_cc0_recreation_is_pinned_and_has_packaged_notice(self):
        data = (ROOT / 'RotomPedia/app/src/main/res/drawable/hoenn_atlas_cc0.png').read_bytes()
        expected = 'bb7ff006a8625d44bc8ef7b4e10a668e777a1efa1eeb9e3b538bce8fe80cd255'
        self.assertEqual(expected, hashlib.sha256(data).hexdigest())
        self.assertEqual((1507, 990), struct.unpack('>II', data[16:24]))
        for path in ('tools/licenses/Hoenn-atlas-CC0.md', 'RotomPedia/app/src/main/res/raw/hoenn_atlas_license.txt'):
            notice = (ROOT / path).read_text(encoding='utf-8')
            self.assertIn(expected, notice)
            self.assertIn('Ztash', notice)
            self.assertIn('CC0', notice)
