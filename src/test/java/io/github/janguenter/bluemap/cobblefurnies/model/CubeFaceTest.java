/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.cobblefurnies.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.janguenter.bluemap.resource.athena.model.CubeFace;
import org.junit.jupiter.api.Test;

class CubeFaceTest {

    @Test
    void exactFaceLocalNeighborBasesAreStable() {
        assertBasis(CubeFace.UP, vec(0, 0, -1), vec(0, 0, 1), vec(-1, 0, 0), vec(1, 0, 0));
        assertBasis(CubeFace.DOWN, vec(0, 0, 1), vec(0, 0, -1), vec(-1, 0, 0), vec(1, 0, 0));
        assertBasis(CubeFace.NORTH, vec(0, 1, 0), vec(0, -1, 0), vec(1, 0, 0), vec(-1, 0, 0));
        assertBasis(CubeFace.SOUTH, vec(0, 1, 0), vec(0, -1, 0), vec(-1, 0, 0), vec(1, 0, 0));
        assertBasis(CubeFace.WEST, vec(0, 1, 0), vec(0, -1, 0), vec(0, 0, -1), vec(0, 0, 1));
        assertBasis(CubeFace.EAST, vec(0, 1, 0), vec(0, -1, 0), vec(0, 0, 1), vec(0, 0, -1));
    }

    private static void assertBasis(
            CubeFace face,
            CubeFace.Vec up,
            CubeFace.Vec down,
            CubeFace.Vec left,
            CubeFace.Vec right
    ) {
        assertEquals(up, face.localUp());
        assertEquals(down, face.localDown());
        assertEquals(left, face.localLeft());
        assertEquals(right, face.localRight());
    }

    private static CubeFace.Vec vec(int x, int y, int z) {
        return new CubeFace.Vec(x, y, z);
    }
}
