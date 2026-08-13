# SPDX-License-Identifier: MIT
"""Regression coverage for the accepted 46-cell stock fixture."""

from __future__ import annotations

import importlib.util
from pathlib import Path
import unittest


ROOT = Path(__file__).resolve().parents[2]
SPEC = importlib.util.spec_from_file_location(
    "gallery_generate", ROOT / "gallery/generate.py"
)
assert SPEC is not None and SPEC.loader is not None
gallery = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(gallery)


class GalleryTest(unittest.TestCase):
    def test_locked_fixture_has_exact_cells_blocks_and_aabb(self) -> None:
        cases = gallery.cases()
        placements = [item for case in cases for item in case["placements"]]
        self.assertEqual(46, len(cases))
        self.assertEqual(71, len(placements))
        self.assertEqual(71, len({(item["x"], item["y"], item["z"])
                                  for item in placements}))
        self.assertEqual((208, 100, 208), (
            min(item["x"] for item in placements),
            min(item["y"] for item in placements),
            min(item["z"] for item in placements),
        ))
        self.assertEqual((240, 101, 236), (
            max(item["x"] for item in placements),
            max(item["y"] for item in placements),
            max(item["z"] for item in placements),
        ))

    def test_locked_topologies_and_south_statues_cannot_drift(self) -> None:
        cases = {case["case_id"]: case for case in gallery.cases()}
        self.assertEqual(
            {(232, 216), (233, 216), (232, 215)},
            xz(cases["red-wool-open-l"]),
        )
        self.assertEqual(
            {(x, z) for x in range(238, 241) for z in range(215, 218)},
            xz(cases["red-wool-full-three-by-three"]),
        )
        self.assertEqual(
            {(232, 226), (233, 226), (232, 225)},
            xz(cases["blue-carpet-open-l"]),
        )
        self.assertEqual(
            {(x, z) for x in range(238, 241) for z in range(225, 228)},
            xz(cases["blue-carpet-full-three-by-three"]),
        )
        statues = [case for case in cases.values()
                   if case["family"] == "statue-bind-pose"]
        self.assertEqual(5, len(statues))
        self.assertTrue(all(
            "facing=south" in item["block"]
            for case in statues for item in case["placements"]
        ))

    def test_generated_commands_lock_prep_camera_and_block_entity_checks(self) -> None:
        generated = gallery.outputs()
        build = generated[gallery.FUNCTIONS / "build.mcfunction"].decode()
        verify = generated[gallery.FUNCTIONS / "verify.mcfunction"].decode()
        pose = generated[gallery.FUNCTIONS / "pose.mcfunction"].decode()
        self.assertIn("forceload add 204 204 244 240", build)
        self.assertIn("fill 204 100 204 244 104 240 minecraft:air replace", build)
        self.assertIn("fill 204 99 204 244 99 240 minecraft:smooth_stone replace", build)
        self.assertEqual(71, verify.count("execute unless block "))
        self.assertEqual(5, verify.count("execute unless data block "))
        upper_absence_checks = [
            line for line in verify.splitlines()
            if line.startswith("execute if data block ")
        ]
        self.assertEqual(5, len(upper_absence_checks))
        self.assertEqual(
            {
                f"execute if data block {x} 101 208 id run scoreboard players add "
                "#failures cobblefurnies_gallery 1"
                for x in (208, 212, 216, 220, 224)
            },
            set(upper_absence_checks),
        )
        self.assertIn("tp @s 221.5 108 254.5 180 14", pose)


def xz(case: dict[str, object]) -> set[tuple[int, int]]:
    return {(item["x"], item["z"]) for item in case["placements"]}


if __name__ == "__main__":
    unittest.main()
