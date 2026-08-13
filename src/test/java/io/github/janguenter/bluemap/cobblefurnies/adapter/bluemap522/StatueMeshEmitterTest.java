/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.cobblefurnies.adapter.bluemap522;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.janguenter.bluemap.cobblefurnies.profile.CobbleFurnies12Athena406Profile;
import io.github.janguenter.bluemap.cobblefurnies.profile.StatueDefinition;
import org.junit.jupiter.api.Test;

class StatueMeshEmitterTest {

    @Test
    void everySourceQuadEmitsFrontAndExactReverseTriangles() {
        assertEquals(4, StatueMeshEmitter.emittedTriangleCount(1));
        assertArrayEquals(new int[]{0, 1, 2}, StatueMeshEmitter.triangleOrder(0));
        assertArrayEquals(new int[]{0, 2, 3}, StatueMeshEmitter.triangleOrder(1));
        assertArrayEquals(new int[]{0, 2, 1}, StatueMeshEmitter.triangleOrder(2));
        assertArrayEquals(new int[]{0, 3, 2}, StatueMeshEmitter.triangleOrder(3));
    }

    @Test
    void exactFiveStatuesEmitTheLockedNoCullTriangleCensus() {
        int sourceQuads = CobbleFurnies12Athena406Profile.STATUES.values().stream()
                .mapToInt(StatueDefinition::cubeCount)
                .sum() * 6;
        assertEquals(1_326, sourceQuads);
        assertEquals(2_652, sourceQuads * 2);
        assertEquals(5_304, StatueMeshEmitter.emittedTriangleCount(sourceQuads));
    }
}
