# SPDX-License-Identifier: MIT
"""Static release-boundary regression coverage."""

from __future__ import annotations

import json
from pathlib import Path
import unittest


ROOT = Path(__file__).resolve().parents[2]


class ReleaseContractTest(unittest.TestCase):
    def test_alpha2_candidate_provenance_matches_reproducible_payloads(self) -> None:
        release = json.loads((ROOT / "provenance/release.json").read_text())
        self.assertEqual("owner-accepted-release-candidate", release["status"])
        self.assertEqual("0.1.0-alpha.2", release["version"])
        self.assertEqual("v0.1.0-alpha.2", release["tag"])
        self.assertEqual(
            {
                "production_jar": {
                    "file_name": "bluemap-cobblefurnies-addon-0.1.0-alpha.2.jar",
                    "size": 109_187,
                    "sha256": "71bf381f34e5fcb93aed8737198afba4909d8e07a6d8da4183ec5d6a618db52e",
                },
                "sources_jar": {
                    "file_name": "bluemap-cobblefurnies-addon-0.1.0-alpha.2-sources.jar",
                    "size": 55_106,
                    "sha256": "7726d20c435a7352143747683375871efeadd53abc291748dbf7c70086bb2278",
                },
                "pom": {
                    "file_name": "bluemap-cobblefurnies-addon-0.1.0-alpha.2.pom",
                    "size": 1_407,
                    "sha256": "89ec5f41f98ca7425fa4cc116f9c7d92207c9fc74aafd298a42d9dfdcf04b183",
                },
                "gradle_module": {
                    "file_name": "bluemap-cobblefurnies-addon-0.1.0-alpha.2.module.json",
                    "size": 2_861,
                    "sha256": "3adebf6abb8bb26ce9674cfd68133fb1b95a5540bfd70063eaee88ac821ce8d1",
                },
            },
            release["final_release_artifacts"],
        )
        migration = release["athena_model_migration"]
        self.assertEqual(
            "4a503a63f7f10b7c414c6c1228207a5ba00bfd54",
            migration["module_commit"],
        )
        self.assertFalse(migration["renderer_or_gallery_behavior_change"])

    def test_provenance_locks_exact_unbundled_artifacts_without_source_claim(self) -> None:
        provenance = json.loads((ROOT / "provenance/upstreams.json").read_text())
        cobblefurnies = provenance["artifacts"]["cobblefurnies"]
        self.assertEqual("CobbleFurnies-neoforge-1.2.jar", cobblefurnies["file_name"])
        self.assertEqual(2_343_464, cobblefurnies["size"])
        self.assertEqual(
            "451c445ff636c1e5821f13e3a3f40ee16ecb3342",
            cobblefurnies["sha1"],
        )
        self.assertEqual(
            "82894965d01bfb00fb6109ac275622a157d415ef0957d41fd6478b6d64ce34f8",
            cobblefurnies["sha256"],
        )
        self.assertEqual((1_188_698, 8_340_192), (
            cobblefurnies["curseforge_project_id"],
            cobblefurnies["curseforge_file_id"],
        ))
        self.assertEqual(("AXY1OO9m", "Ynk3uYUi"), (
            cobblefurnies["modrinth_project_id"],
            cobblefurnies["modrinth_version_id"],
        ))
        self.assertEqual("MIT", cobblefurnies["declared_license"])
        self.assertEqual("not_asserted", cobblefurnies["source_correlation"])
        self.assertFalse(cobblefurnies["bundled"])
        self.assertFalse(provenance["upstream_source_correlation_asserted"])
        self.assertFalse(provenance["third_party_content_bundled"])
        self.assertEqual(
            "c474a82b6bfd1b4173d119cb1e053a5458167e4b",
            provenance["first_party_reuse"]["commit"],
        )
        self.assertEqual(
            "BlueMap add-on activation/profile/adapter framework and Athena "
            "connected-texture emission outside the extracted pure model primitives",
            provenance["first_party_reuse"]["scope"],
        )
        module = provenance["shared_source_module"]
        self.assertEqual("0.1.0-alpha.1", module["version"])
        self.assertEqual(
            "4a503a63f7f10b7c414c6c1228207a5ba00bfd54",
            module["commit"],
        )
        self.assertEqual(
            "882689c2f9a0875547f4e30aefd68659103d5046",
            module["source_tree"],
        )
        self.assertEqual("exact gitlink-pinned sources compiled into the consumer",
                         module["integration"])
        self.assertFalse(module["artifact_bundled"])

    def test_ci_requires_exact_inputs_and_never_mentions_inherited_chipped_input(self) -> None:
        workflow = (ROOT / ".github/workflows/ci.yml").read_text()
        for exact in (
            "files/8340/192/CobbleFurnies-neoforge-1.2.jar",
            "data/b1ZV3DIJ/versions/dJgL278E/athena-neoforge-1.21.1-4.0.6.jar",
            "-PreleaseGate=true",
            "-PcobblefurniesJar=",
            "-PathenaJar=",
            "verifyPinnedArtifacts",
            "verifyPublicationMetadata",
        ):
            self.assertIn(exact, workflow)
        self.assertNotIn("chippedJar", workflow)
        self.assertNotIn("BAscRYKm", workflow)

    def test_adapted_athena_emitter_discloses_first_party_mit_origin(self) -> None:
        source = (ROOT / (
            "src/main/java/io/github/janguenter/bluemap/cobblefurnies/"
            "adapter/bluemap522/AthenaQuadEmitter.java"
        )).read_text()
        self.assertIn("Project-authored MIT adaptation", source)
        self.assertIn("first-party BlueMap Chipped", source)
        self.assertIn("c474a82b6bfd1b4173d119cb1e053a5458167e4b", source)
        self.assertNotIn("independently implemented", source)

        notice = (ROOT / "NOTICE.md").read_text()
        self.assertIn("activation/profile/adapter framework", notice)
        self.assertIn("CobbleFurnies-specific", notice)
        self.assertNotIn("profile integration were authored", notice)

    def test_release_is_pr_reviewed_immutable_and_exactly_four_payloads(self) -> None:
        workflow = (ROOT / ".github/workflows/release.yml").read_text()
        for exact in (
            "pull-requests: read",
            "git cat-file -t",
            "git merge-base --is-ancestor",
            "/commits/${commit}/pulls",
            '.merge_commit_sha == \\"${commit}\\"',
            "/pulls/${number}/files",
            "verify-version-increase.py",
            'f"bluemap-cobblefurnies-addon-{version}.jar"',
            'f"bluemap-cobblefurnies-addon-{version}-sources.jar"',
            'f"bluemap-cobblefurnies-addon-{version}.pom"',
            'f"bluemap-cobblefurnies-addon-{version}.module"',
            'for algorithm in ("sha1", "md5")',
            "Immutable Maven version is partial or inconsistent",
            "actions/attest-build-provenance@0f67c3f4856b2e3261c31976d6725780e5e4c373",
            "--draft=false --prerelease",
        ):
            self.assertIn(exact, workflow)

    def test_stale_profile_directory_is_absent(self) -> None:
        self.assertFalse((
            ROOT / "src/main/resources/bluemap-cobblefurnies/profiles/cobblefurnies"
            / "4.0.2-athena-4.0.6"
        ).exists())


if __name__ == "__main__":
    unittest.main()
