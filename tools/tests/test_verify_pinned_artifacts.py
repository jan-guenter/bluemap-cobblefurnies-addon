# SPDX-License-Identifier: MIT
"""Unit coverage for deterministic fail-closed profile generation."""

from __future__ import annotations

import importlib.util
from pathlib import Path
import tempfile
import unittest


ROOT = Path(__file__).resolve().parents[2]
SPEC = importlib.util.spec_from_file_location(
    "generate_profile", ROOT / "tools/generate_profile.py"
)
assert SPEC is not None and SPEC.loader is not None
generate_profile = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(generate_profile)


class ProfileHelpersTest(unittest.TestCase):
    def test_roster_digest_is_sorted_and_newline_terminated(self) -> None:
        self.assertEqual(
            generate_profile.roster_digest(["z", "a"]),
            generate_profile.digest_bytes(b"a\nz\n"),
        )

    def test_identity_gate_rejects_wrong_filename_before_hashing(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "wrong.jar"
            path.write_bytes(b"")
            with self.assertRaisesRegex(ValueError, "unexpected exact artifact"):
                generate_profile.verify_file_identity(
                    path,
                    filename="expected.jar",
                    size=0,
                    sha1="",
                    sha256="",
                    sha512="",
                )

    def test_canonical_json_is_order_independent(self) -> None:
        self.assertEqual(
            generate_profile.canonical_json({"b": 2, "a": 1}),
            b'{"a":1,"b":2}\n',
        )

    def test_closed_rosters_match_the_bounded_runtime_scope(self) -> None:
        self.assertEqual(16, len(generate_profile.COLORS))
        self.assertEqual(5, len(generate_profile.ROLES))
        self.assertEqual(5, len(generate_profile.STATUES))
        self.assertEqual(221, sum(value[1] for value in generate_profile.STATUES.values()))


if __name__ == "__main__":
    unittest.main()
