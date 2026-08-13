/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.cobblefurnies.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.janguenter.bluemap.cobblefurnies.profile.CobbleFurnies12Athena406Profile;
import io.github.janguenter.bluemap.cobblefurnies.profile.StatueDefinition;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;
import org.junit.jupiter.api.Test;

class BbsStatueCompilerTest {

    @Test
    void exactFiveBindPoseModelsCompileWithinLockedBudgets() throws IOException {
        String artifact = System.getProperty("cobblefurniesJar");
        if (artifact == null || !Files.isRegularFile(Path.of(artifact))) {
            requireReleaseInput();
            return;
        }
        int quads = 0;
        try (ZipFile jar = new ZipFile(artifact)) {
            for (StatueDefinition definition
                    : CobbleFurnies12Athena406Profile.STATUES.values()) {
                byte[] raw = jar.getInputStream(
                        jar.getEntry(definition.modelResource())
                ).readAllBytes();
                StatueModel model = BbsStatueCompiler.compile(definition, raw);
                assertEquals(definition.cubeCount() * 6, model.quads().size());
                assertTrue(model.bounds().minimum().y() < model.bounds().maximum().y());
                assertTrue(model.bounds().maximum().y() > 2.9D);
                assertTrue(model.bounds().maximum().y() < 3.7D);
                quads += model.quads().size();
            }
        }
        assertEquals(1_326, quads);
    }

    @Test
    void byteDriftFailsBeforeParsingOrMeshDerivation() throws IOException {
        String artifact = System.getProperty("cobblefurniesJar");
        if (artifact == null || !Files.isRegularFile(Path.of(artifact))) {
            requireReleaseInput();
            return;
        }
        StatueDefinition definition = CobbleFurnies12Athena406Profile.STATUES.values()
                .iterator().next();
        byte[] raw;
        try (ZipFile jar = new ZipFile(artifact)) {
            raw = jar.getInputStream(jar.getEntry(definition.modelResource())).readAllBytes();
        }
        raw[raw.length - 2] ^= 1;
        assertThrows(IllegalArgumentException.class,
                () -> BbsStatueCompiler.compile(definition, raw));
    }

    private static void requireReleaseInput() {
        if (Boolean.getBoolean("cobblefurnies.releaseGate")) {
            fail("release gate requires -PcobblefurniesJar with the exact 1.2 artifact");
        }
    }
}
