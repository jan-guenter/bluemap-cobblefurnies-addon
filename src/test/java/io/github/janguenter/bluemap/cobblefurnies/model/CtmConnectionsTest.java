/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.cobblefurnies.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CtmConnectionsTest {

    @Test
    void allMasksRoundTripInExactAthenaBitOrder() {
        for (int mask = 0; mask <= 0xFF; mask++) {
            CtmConnections connections = CtmConnections.fromMask(mask);
            assertEquals(mask, connections.mask());
            assertEquals(
                    java.util.List.of(
                            select(mask, 0, 2, 4),
                            select(mask, 0, 3, 5),
                            select(mask, 1, 2, 6),
                            select(mask, 1, 3, 7)
                    ),
                    connections.quadrants(),
                    "mask " + mask
            );
        }
    }

    @Test
    void quadrantTruthTableMatchesFiveTextureContract() {
        assertEquals(CtmTextureRole.PARTICLE, CtmSelector.select(false, false, false));
        assertEquals(CtmTextureRole.PARTICLE, CtmSelector.select(false, false, true));
        assertEquals(CtmTextureRole.VERTICAL, CtmSelector.select(true, false, false));
        assertEquals(CtmTextureRole.HORIZONTAL, CtmSelector.select(false, true, false));
        assertEquals(CtmTextureRole.CENTER, CtmSelector.select(true, true, false));
        assertEquals(CtmTextureRole.EMPTY, CtmSelector.select(true, true, true));
    }

    @Test
    void fullConnectionCollapsesOnlyExactAllTrueMask() {
        assertTrue(CtmConnections.fromMask(0xFF).completelyConnected());
        assertFalse(CtmConnections.fromMask(0x7F).completelyConnected());
    }

    private static CtmTextureRole select(
            int mask,
            int verticalBit,
            int horizontalBit,
            int diagonalBit
    ) {
        boolean vertical = (mask & (1 << verticalBit)) != 0;
        boolean horizontal = (mask & (1 << horizontalBit)) != 0;
        boolean diagonal = (mask & (1 << diagonalBit)) != 0;
        if (!vertical && !horizontal) {
            return CtmTextureRole.PARTICLE;
        }
        if (vertical && !horizontal) {
            return CtmTextureRole.VERTICAL;
        }
        if (!vertical) {
            return CtmTextureRole.HORIZONTAL;
        }
        return diagonal ? CtmTextureRole.EMPTY : CtmTextureRole.CENTER;
    }
}
