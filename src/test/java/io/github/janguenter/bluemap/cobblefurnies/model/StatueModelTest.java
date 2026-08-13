/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.cobblefurnies.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.bluecolored.bluemap.core.util.Direction;
import io.github.janguenter.bluemap.cobblefurnies.adapter.bluemap522.StatueMeshEmitterAccess;
import io.github.janguenter.bluemap.cobblefurnies.model.StatueModel.Vec3;
import org.junit.jupiter.api.Test;

class StatueModelTest {

    @Test
    void rotationsUseXThenYThenZAndFacingUsesLowerBlockCenter() {
        Vec3 rotated = new Vec3(1D, 0D, 0D).rotate(new Vec3(0D, 90D, 90D));
        assertVector(new Vec3(0D, 0D, -1D), rotated);
        assertVector(new Vec3(0.5D, 0D, 1D),
                new Vec3(1D, 0D, 0.5D).rotateFacing(-90D));
    }

    @Test
    void persistedFacingTableMatchesClientRendererTransform() {
        assertEquals(0D, StatueFacing.parse("north").degrees());
        assertEquals(-90D, StatueFacing.parse("EAST").degrees());
        assertEquals(180D, StatueFacing.parse("south").degrees());
        assertEquals(90D, StatueFacing.parse("west").degrees());
        assertThrows(IllegalArgumentException.class, () -> StatueFacing.parse("up"));
    }

    @Test
    void nearestLightingDirectionUsesDominantNormalAxis() {
        assertEquals(Direction.UP,
                StatueMeshEmitterAccess.nearestDirection(new Vec3(0.2D, 1D, 0.3D)));
        assertEquals(Direction.WEST,
                StatueMeshEmitterAccess.nearestDirection(new Vec3(-2D, 0.5D, 1D)));
        assertEquals(Direction.NORTH,
                StatueMeshEmitterAccess.nearestDirection(new Vec3(0D, 0D, -3D)));
    }

    private static void assertVector(Vec3 expected, Vec3 actual) {
        assertEquals(expected.x(), actual.x(), 1.0E-12D);
        assertEquals(expected.y(), actual.y(), 1.0E-12D);
        assertEquals(expected.z(), actual.z(), 1.0E-12D);
    }
}
