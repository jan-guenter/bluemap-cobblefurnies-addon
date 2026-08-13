/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.cobblefurnies.profile;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ExactModArtifactDetectorTest {

    @Test
    void onlyTheExactInstalledPairActivates() {
        String cobblefurniesValue = System.getProperty("cobblefurniesJar");
        String athenaValue = System.getProperty("athenaJar");
        if (cobblefurniesValue == null || athenaValue == null) {
            requireReleaseInputs("missing -PcobblefurniesJar or -PathenaJar");
            return;
        }
        Path cobblefurnies = Path.of(cobblefurniesValue);
        Path athena = Path.of(athenaValue);
        if (!Files.isRegularFile(cobblefurnies) || !Files.isRegularFile(athena)) {
            requireReleaseInputs("exact artifact property does not name a regular file");
            return;
        }
        assertTrue(ExactModArtifactDetector.matchesRequiredPair(List.of(cobblefurnies, athena)));
        assertFalse(ExactModArtifactDetector.matchesRequiredPair(List.of(cobblefurnies)));
        assertFalse(ExactModArtifactDetector.matches(
                List.of(cobblefurnies, athena),
                Map.of(
                        "cobblefurnies", new ExactModArtifactDetector.Identity(
                                CobbleFurnies12Athena406Profile.COBBLEFURNIES_SHA256,
                                CobbleFurnies12Athena406Profile.COBBLEFURNIES_SIZE + 1
                        ),
                        "athena", new ExactModArtifactDetector.Identity(
                                CobbleFurnies12Athena406Profile.ATHENA_SHA256,
                                CobbleFurnies12Athena406Profile.ATHENA_SIZE
                        )
                )
        ));
    }

    private static void requireReleaseInputs(String detail) {
        if (Boolean.getBoolean("cobblefurnies.releaseGate")) {
            fail("release gate requires exact CobbleFurnies and Athena inputs: " + detail);
        }
    }
}
