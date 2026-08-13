#!/usr/bin/env python3
# SPDX-License-Identifier: MIT
"""Generate the exact CobbleFurnies 1.2/Athena 4.0.6 metadata profile."""

from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path
from typing import Any
import zipfile


PROFILE_ROOT = Path("src/main/resources/bluemap-cobblefurnies/profiles")
PROFILE_DIRECTORY = PROFILE_ROOT / "cobblefurnies/1.2-athena-4.0.6"
CATALOG_PATH = PROFILE_ROOT / "exact-artifacts.json"
PROFILE_PATH = PROFILE_DIRECTORY / "profile.json"
DEFINITIONS_PATH = PROFILE_DIRECTORY / "definitions.tsv"
STATUES_PATH = PROFILE_DIRECTORY / "statues.tsv"
RESOURCES_PATH = PROFILE_DIRECTORY / "required-resources.tsv"

COBBLEFURNIES_FILENAME = "CobbleFurnies-neoforge-1.2.jar"
COBBLEFURNIES_SIZE = 2_343_464
COBBLEFURNIES_SHA1 = "451c445ff636c1e5821f13e3a3f40ee16ecb3342"
COBBLEFURNIES_SHA256 = (
    "82894965d01bfb00fb6109ac275622a157d415ef0957d41fd6478b6d64ce34f8"
)
COBBLEFURNIES_SHA512 = (
    "ada48117ce384c226a20bb261331fdca32d59f2da6466caa4f7fed8c989ea26c7"
    "a548e8227be8c130d3462d3bc31c901962889fd61c8676e016ae104ecdaf934"
)
ATHENA_FILENAME = "athena-neoforge-1.21.1-4.0.6.jar"
ATHENA_SIZE = 99_944
ATHENA_SHA1 = "4bcbdf388bd5e387beca7c627224aac33584b55b"
ATHENA_SHA256 = (
    "43699885bbce3343916d4c5c4940cf0e3f9f6f02fdeb46e8655e121b42282ec5"
)
ATHENA_SHA512 = (
    "ab40a306a26ce834daae921a1e87768cd2538a4bfe27a4480f97af854084cc334"
    "e7416b1bd0b7583834a32a86951283f29fd4b1df7b98a967a6b26a3ec05e2cf"
)

COLORS = (
    "red", "orange", "yellow", "brown", "lime", "green", "cyan",
    "light_blue", "blue", "magenta", "purple", "pink", "white",
    "light_gray", "gray", "black",
)
ROLES = ("particle", "empty", "center", "vertical", "horizontal")
STATUES: dict[str, tuple[int, int, int, str]] = {
    "ancient": (28, 43, 17_977,
        "1fe6618351515cf00420483b920eb3a5b9a907db62668a523064c02d61b39b23"),
    "bulbasaur": (27, 46, 19_442,
        "d024b050bdad80ace9f8f69715aefb223f6abb5e89fc7dd6a1d2691229aaf5c7"),
    "charmander": (37, 59, 25_252,
        "556dbff4f0efbc17225279f33b3ef7461e9894a56050489ccd46c870666b1515"),
    "pikachu": (24, 32, 14_014,
        "f4afbaf181efa7331dbd9b7fc360e5afe62fc1c4f2de9bfb976f4f7d6c65cb40"),
    "squirtle": (29, 41, 17_722,
        "30c5a836bdeed841f6c7e1af858e6d06d59bb29b696dfc25ba2f6f368910afab"),
}


def digest_bytes(raw: bytes, algorithm: str = "sha256") -> str:
    return hashlib.new(algorithm, raw).hexdigest()


def digest_path(path: Path, algorithm: str) -> str:
    value = hashlib.new(algorithm)
    with path.open("rb") as source:
        for chunk in iter(lambda: source.read(64 * 1024), b""):
            value.update(chunk)
    return value.hexdigest()


def roster_digest(values: list[str]) -> str:
    return digest_bytes("".join(f"{value}\n" for value in sorted(values)).encode())


def canonical_json(value: Any) -> bytes:
    return (json.dumps(value, sort_keys=True, separators=(",", ":")) + "\n").encode()


def verify_file_identity(
    path: Path, *, filename: str, size: int, sha1: str, sha256: str, sha512: str
) -> None:
    if not path.is_file() or path.name != filename or path.stat().st_size != size:
        raise ValueError(f"unexpected exact artifact: {path}")
    for algorithm, expected in (
        ("sha1", sha1), ("sha256", sha256), ("sha512", sha512)
    ):
        actual = digest_path(path, algorithm)
        if actual != expected:
            raise ValueError(f"{path.name} {algorithm} changed: {actual}")


def _require_json(archive: zipfile.ZipFile, path: str) -> dict[str, Any]:
    try:
        value = json.loads(archive.read(path))
    except KeyError as error:
        raise ValueError(f"missing resource {path}") from error
    if not isinstance(value, dict):
        raise ValueError(f"resource is not an object: {path}")
    return value


def _definition(
    archive: zipfile.ZipFile, block: str, family: str
) -> tuple[str, ...]:
    path = f"assets/cobblefurnies/blockstates/{block}.json"
    value = _require_json(archive, path)
    if set(value) != {"variants", "athena:loader", "ctm_textures"}:
        raise ValueError(f"Athena blockstate keys changed: {path}")
    if value.get("athena:loader") != f"athena:{family}":
        raise ValueError(f"Athena loader changed: {path}")
    variants = value.get("variants")
    if variants != {"": {"model": "minecraft:block/air"}}:
        raise ValueError(f"Athena stock placeholder changed: {path}")
    textures = value.get("ctm_textures")
    if not isinstance(textures, dict) or set(textures) != set(ROLES):
        raise ValueError(f"Athena texture roles changed: {path}")
    row = (f"cobblefurnies:{block}", family, *(textures[role] for role in ROLES))
    if any(not isinstance(field, str) for field in row):
        raise ValueError(f"Athena definition is malformed: {path}")
    return row


def _validate_statue_schema(raw: bytes, name: str) -> tuple[int, int]:
    value = json.loads(raw)
    if not isinstance(value, dict) or set(value) != {"version", "animations", "model"}:
        raise ValueError(f"statue {name} root schema changed")
    if value.get("version") != "0.7.2" or value.get("animations") != {}:
        raise ValueError(f"statue {name} version/animation contract changed")
    model = value.get("model")
    if not isinstance(model, dict) or set(model) != {"groups", "texture"}:
        raise ValueError(f"statue {name} model schema changed")
    if model.get("texture") != [128, 128] or not isinstance(model.get("groups"), dict):
        raise ValueError(f"statue {name} texture/group contract changed")
    groups = model["groups"]
    cube_count = 0
    for group_name, group in groups.items():
        if not isinstance(group_name, str) or not isinstance(group, dict):
            raise ValueError(f"statue {name} group is malformed")
        if not set(group).issubset({"origin", "parent", "rotate", "cubes"}):
            raise ValueError(f"statue {name} group fields changed")
        cubes = group.get("cubes", [])
        if not isinstance(cubes, list):
            raise ValueError(f"statue {name} cube list changed")
        cube_count += len(cubes)
        for cube in cubes:
            if not isinstance(cube, dict) or not set(cube).issubset(
                {"origin", "from", "size", "offset", "rotate", "uvs"}
            ):
                raise ValueError(f"statue {name} cube fields changed")
            uvs = cube.get("uvs")
            if not isinstance(uvs, dict) or set(uvs) != {
                "front", "back", "right", "left", "bottom", "top"
            }:
                raise ValueError(f"statue {name} UV faces changed")
    return len(groups), cube_count


def build_outputs(cobblefurnies: Path, athena: Path) -> dict[Path, bytes]:
    verify_file_identity(
        cobblefurnies,
        filename=COBBLEFURNIES_FILENAME,
        size=COBBLEFURNIES_SIZE,
        sha1=COBBLEFURNIES_SHA1,
        sha256=COBBLEFURNIES_SHA256,
        sha512=COBBLEFURNIES_SHA512,
    )
    verify_file_identity(
        athena,
        filename=ATHENA_FILENAME,
        size=ATHENA_SIZE,
        sha1=ATHENA_SHA1,
        sha256=ATHENA_SHA256,
        sha512=ATHENA_SHA512,
    )

    definitions: list[tuple[str, ...]] = []
    resources: set[str] = set()
    statue_rows: list[tuple[str, ...]] = []
    with zipfile.ZipFile(cobblefurnies) as archive:
        names = archive.namelist()
        if len(names) != len(set(names)):
            raise ValueError("CobbleFurnies JAR has duplicate entries")
        for color in COLORS:
            for suffix, family in (("_poke_wool", "ctm"),
                                   ("_poke_wool_carpet", "carpet_ctm")):
                block = color + suffix
                row = _definition(archive, block, family)
                definitions.append(row)
                resources.add(f"assets/cobblefurnies/blockstates/{block}.json")
                for texture in row[2:]:
                    namespace, value = texture.split(":", 1)
                    resources.add(f"assets/{namespace}/textures/{value}.png")

        for name, (groups, cubes, size, sha256) in STATUES.items():
            block_id = f"cobblefurnies:statue_{name}"
            blockstate = f"assets/cobblefurnies/blockstates/statue_{name}.json"
            block_model = f"assets/cobblefurnies/models/block/statue/statue_{name}.json"
            model = f"assets/cobblefurnies/models/bb/statue_{name}.bbs.json"
            texture_path = f"assets/cobblefurnies/textures/block/statue/statue_{name}.png"
            raw = archive.read(model)
            observed_groups, observed_cubes = _validate_statue_schema(raw, name)
            if (len(raw), digest_bytes(raw), observed_groups, observed_cubes) != (
                size, sha256, groups, cubes
            ):
                raise ValueError(f"statue {name} exact model identity changed")
            blockstate_value = _require_json(archive, blockstate)
            expected_model = f"cobblefurnies:block/statue/statue_{name}"
            if blockstate_value != {"variants": {"": {"model": expected_model}}}:
                raise ValueError(f"statue {name} blockstate changed")
            _require_json(archive, block_model)
            texture_key = f"cobblefurnies:block/statue/statue_{name}"
            statue_rows.append((
                name, block_id, model, texture_key, str(groups), str(cubes),
                str(cubes * 12), str(size), sha256,
            ))
            resources.update((blockstate, block_model, model, texture_path))

        required_rows: list[str] = []
        for path in sorted(resources):
            try:
                raw = archive.read(path)
            except KeyError as error:
                raise ValueError(f"required resource is missing: {path}") from error
            required_rows.append(f"{path}\t{len(raw)}\t{digest_bytes(raw)}")

    definitions.sort()
    statue_rows.sort()
    if len(definitions) != 32 or len(statue_rows) != 5:
        raise ValueError("closed route count changed")
    definition_bytes = ("\n".join("\t".join(row) for row in definitions) + "\n").encode()
    statue_bytes = ("\n".join("\t".join(row) for row in statue_rows) + "\n").encode()
    resource_bytes = ("\n".join(required_rows) + "\n").encode()
    routed = [row[0] for row in definitions] + [row[1] for row in statue_rows]
    textures = sorted({value for row in definitions for value in row[2:]} | {
        row[3] for row in statue_rows
    })

    profile = {
        "profile_id": "cobblefurnies-1.2-athena-4.0.6",
        "pack": "All the Mons 1.2.0",
        "minecraft": "1.21.1",
        "neoforge": "21.1.248",
        "cobblefurnies": "1.2",
        "athena": "4.0.6",
        "owned_ids": 37,
        "athena_ctm_ids": 16,
        "athena_carpet_ctm_ids": 16,
        "statue_ids": 5,
        "statue_cubes": sum(value[1] for value in STATUES.values()),
        "statue_triangles": sum(value[1] * 12 for value in STATUES.values()),
        "required_resources": len(required_rows),
        "required_textures": len(textures),
        "routed_roster_sha256": roster_digest(routed),
        "definitions_sha256": digest_bytes(definition_bytes),
        "statues_sha256": digest_bytes(statue_bytes),
        "required_resources_sha256": digest_bytes(resource_bytes),
    }
    artifacts = {
        "schema": 1,
        "profile": profile["profile_id"],
        "artifacts": [
            {"mod_id": "cobblefurnies", "filename": COBBLEFURNIES_FILENAME,
             "size": COBBLEFURNIES_SIZE, "sha1": COBBLEFURNIES_SHA1,
             "sha256": COBBLEFURNIES_SHA256, "sha512": COBBLEFURNIES_SHA512},
            {"mod_id": "athena", "filename": ATHENA_FILENAME,
             "size": ATHENA_SIZE, "sha1": ATHENA_SHA1,
             "sha256": ATHENA_SHA256, "sha512": ATHENA_SHA512},
        ],
    }
    return {
        CATALOG_PATH: canonical_json(artifacts),
        PROFILE_PATH: canonical_json(profile),
        DEFINITIONS_PATH: definition_bytes,
        STATUES_PATH: statue_bytes,
        RESOURCES_PATH: resource_bytes,
    }


def apply_outputs(outputs: dict[Path, bytes], *, check: bool) -> None:
    for path, expected in outputs.items():
        if check:
            if not path.is_file() or path.read_bytes() != expected:
                raise ValueError(f"generated profile drift: {path}")
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_bytes(expected)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--cobblefurnies", required=True, type=Path)
    parser.add_argument("--athena", required=True, type=Path)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    outputs = build_outputs(args.cobblefurnies, args.athena)
    apply_outputs(outputs, check=args.check)
    print("Verified 32 Athena routes, five static statues, and 37 owned IDs.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
