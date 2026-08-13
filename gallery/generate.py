#!/usr/bin/env python3
# SPDX-License-Identifier: MIT
"""Generate the bounded 46-cell CobbleFurnies visual-review gallery."""

from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path


ROOT = Path(__file__).resolve().parent
FUNCTIONS = ROOT / "datapack/data/cobblefurnies_gallery/function"
COLORS = (
    "red", "orange", "yellow", "brown", "lime", "green", "cyan",
    "light_blue", "blue", "magenta", "purple", "pink", "white",
    "light_gray", "gray", "black",
)
STATUES = ("ancient", "bulbasaur", "charmander", "pikachu", "squirtle")
CONTROLS = ("oak_chair", "oak_table", "red_sofa", "red_cabinetry", "poke_ball_desk")


def placement(block: str, x: int, y: int, z: int) -> dict[str, object]:
    return {"block": block, "x": x, "y": y, "z": z}


def cases() -> list[dict[str, object]]:
    specs: list[tuple[str, str, str, int, int, list[dict[str, object]]]] = []
    for index, name in enumerate(STATUES):
        block = f"cobblefurnies:statue_{name}"
        x = 208 + index * 4
        specs.append((
            f"statue-{name}", "statue-bind-pose", "two-high south-facing bind pose",
            x, 208, [
                placement(f"{block}[facing=south,half=lower]", x, 100, 208),
                placement(f"{block}[facing=south,half=upper]", x, 101, 208),
            ],
        ))

    for index, color in enumerate(COLORS):
        row = index // 8
        x = 208 + (index % 8) * 2
        z = 216 + row * 2
        specs.append((
            f"{color}-poke-wool", "ctm-isolated", "one isolated full cube",
            x, z, [placement(f"cobblefurnies:{color}_poke_wool", x, 100, z)],
        ))
    for index, color in enumerate(COLORS):
        row = index // 8
        x = 208 + (index % 8) * 2
        z = 224 + row * 2
        specs.append((
            f"{color}-poke-wool-carpet", "carpet-ctm-isolated",
            "one isolated one-sixteenth carpet", x, z,
            [placement(f"cobblefurnies:{color}_poke_wool_carpet", x, 100, z)],
        ))

    specs.extend((
        ("red-wool-open-l", "ctm-topology", "open L; southeast diagonal missing",
         232, 216, [placement("cobblefurnies:red_poke_wool", x, 100, z)
                    for x, z in ((232, 216), (233, 216), (232, 215))]),
        ("red-wool-full-three-by-three", "ctm-topology", "full 3-by-3",
         239, 216, [placement("cobblefurnies:red_poke_wool", x, 100, z)
                    for z in range(215, 218) for x in range(238, 241)]),
        ("blue-carpet-open-l", "carpet-ctm-topology",
         "open L; southeast diagonal missing", 232, 226,
         [placement("cobblefurnies:blue_poke_wool_carpet", x, 100, z)
          for x, z in ((232, 226), (233, 226), (232, 225))]),
        ("blue-carpet-full-three-by-three", "carpet-ctm-topology", "full 3-by-3",
         239, 226, [placement("cobblefurnies:blue_poke_wool_carpet", x, 100, z)
                    for z in range(225, 228) for x in range(238, 241)]),
    ))

    control_states = (
        "oak_chair[facing=south,waterlogged=false]",
        "oak_table[east=false,north=false,south=false,waterlogged=false,west=false]",
        "red_sofa[facing=south,type=single,waterlogged=false]",
        "red_cabinetry[facing=south,shape=default,waterlogged=false]",
        "poke_ball_desk[facing=south]",
    )
    for index, (block, state) in enumerate(zip(CONTROLS, control_states, strict=True)):
        x = 208 + index * 4
        specs.append((
            f"stock-{block.replace('_', '-')}", "stock-control",
            "outside the 37-ID route and rendered by stock BlueMap", x, 236,
            [placement(f"cobblefurnies:{state}", x, 100, 236)],
        ))
    if len(specs) != 46:
        raise AssertionError(f"gallery roster changed: {len(specs)}")

    result: list[dict[str, object]] = []
    for index, (case_id, family, notes, x, z, blocks) in enumerate(specs, start=1):
        result.append({
            "index": index,
            "case_id": case_id,
            "family": family,
            "notes": notes,
            "anchor": {"x": x, "y": 100, "z": z},
            "placements": blocks,
        })
    physical = sum(len(case["placements"]) for case in result)
    if physical != 71:
        raise AssertionError(f"physical fixture count changed: {physical}")
    return result


def command(case: dict[str, object], item: dict[str, object]) -> str:
    del case
    return f"setblock {item['x']} {item['y']} {item['z']} {item['block']} replace"


def outputs() -> dict[Path, bytes]:
    roster = cases()
    document = {
        "schema": 1,
        "profile": "cobblefurnies-1.2-athena-4.0.6",
        "logical_cells": 46,
        "physical_mod_blocks": 71,
        "occupied_aabb": {"min": [208, 100, 208], "max": [240, 101, 236]},
        "cleanup_support_aabb": {"min": [204, 99, 204], "max": [244, 104, 240]},
        "camera": {"position": [221.5, 108, 254.5], "yaw": 180, "pitch": 14},
        "bluemap_view": "221.5:100:222:33.47:0:1.32946:0:0:perspective",
        "cases": roster,
    }
    case_json = (json.dumps(document, indent=2, sort_keys=True) + "\n").encode()
    rows = ["index\tcase_id\tfamily\tx\ty\tz\tplacements\tnotes"]
    for case in roster:
        anchor = case["anchor"]
        rows.append("\t".join((
            str(case["index"]), str(case["case_id"]), str(case["family"]),
            str(anchor["x"]), str(anchor["y"]), str(anchor["z"]),
            str(len(case["placements"])), str(case["notes"]),
        )))
    case_tsv = ("\n".join(rows) + "\n").encode()

    build = [
        "# Generated by gallery/generate.py; do not hand-edit.",
        "forceload add 204 204 244 240",
        "fill 204 100 204 244 104 240 minecraft:air replace",
        "fill 204 99 204 244 99 240 minecraft:smooth_stone replace",
    ]
    verify = [
        "# Generated by gallery/generate.py; do not hand-edit.",
        "scoreboard objectives add cobblefurnies_gallery dummy",
        "scoreboard players set #failures cobblefurnies_gallery 0",
    ]
    for case in roster:
        build.append(f"# {case['index']:02d} {case['case_id']}")
        for item in case["placements"]:
            build.append(command(case, item))
            verify.append(
                f"execute unless block {item['x']} {item['y']} {item['z']} {item['block']} "
                "run scoreboard players add #failures cobblefurnies_gallery 1"
            )
        if case["family"] == "statue-bind-pose":
            lower, upper = case["placements"]
            verify.append(
                f"execute unless data block {lower['x']} {lower['y']} {lower['z']} "
                '{id:"cobblefurnies:statue"} '
                "run scoreboard players add #failures cobblefurnies_gallery 1"
            )
            verify.append(
                f"execute if data block {upper['x']} {upper['y']} {upper['z']} id "
                "run scoreboard players add #failures cobblefurnies_gallery 1"
            )
    build.append(
        'tellraw @a [{"text":"Built CobbleFurnies 46-cell / 71-block gallery.",'
        '"color":"green"}]'
    )
    verify.extend((
        'execute if score #failures cobblefurnies_gallery matches 0 run tellraw @a '
        '[{"text":"CobbleFurnies gallery verification passed: 46 cells / 71 blocks.","color":"green"}]',
        'execute unless score #failures cobblefurnies_gallery matches 0 run tellraw @a '
        '[{"text":"CobbleFurnies gallery verification failed; inspect #failures.","color":"red"}]',
    ))
    clear = (
        "# Generated by gallery/generate.py; do not hand-edit.\n"
        "fill 204 99 204 244 104 240 minecraft:air replace\n"
        'tellraw @a [{"text":"Cleared CobbleFurnies gallery.","color":"yellow"}]\n'
    ).encode()
    pose = (
        "# Generated by gallery/generate.py; do not hand-edit.\n"
        "tp @s 221.5 108 254.5 180 14\n"
    ).encode()
    release = (
        "# Generated by gallery/generate.py; do not hand-edit.\n"
        "forceload remove 204 204 244 240\n"
        'tellraw @a [{"text":"Released CobbleFurnies gallery chunks.","color":"yellow"}]\n'
    ).encode()
    load = (
        "# Generated by gallery/generate.py; do not hand-edit.\n"
        'tellraw @a [{"text":"CobbleFurnies gallery ready: run /function '
        'cobblefurnies_gallery:build","color":"aqua"}]\n'
    ).encode()
    generated = {
        ROOT / "cases.json": case_json,
        ROOT / "cases.tsv": case_tsv,
        FUNCTIONS / "build.mcfunction": ("\n".join(build) + "\n").encode(),
        FUNCTIONS / "verify.mcfunction": ("\n".join(verify) + "\n").encode(),
        FUNCTIONS / "clear.mcfunction": clear,
        FUNCTIONS / "pose.mcfunction": pose,
        FUNCTIONS / "release.mcfunction": release,
        FUNCTIONS / "load.mcfunction": load,
    }
    sums = []
    for path, raw in sorted(generated.items(), key=lambda item: item[0].as_posix()):
        sums.append(f"{hashlib.sha256(raw).hexdigest()}  {path.relative_to(ROOT)}")
    generated[ROOT / "SHA256SUMS"] = ("\n".join(sums) + "\n").encode()
    return generated


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    for path, expected in outputs().items():
        if args.check:
            if not path.is_file() or path.read_bytes() != expected:
                raise SystemExit(f"generated gallery drift: {path.relative_to(ROOT)}")
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_bytes(expected)
    print("Verified deterministic 46-cell CobbleFurnies gallery.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
