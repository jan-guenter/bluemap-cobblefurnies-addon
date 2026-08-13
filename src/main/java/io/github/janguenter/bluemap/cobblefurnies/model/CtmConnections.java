/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.cobblefurnies.model;

import java.util.List;

/** Exact Athena connection bit order and its four face quadrants. */
public record CtmConnections(
        boolean up,
        boolean down,
        boolean left,
        boolean right,
        boolean upLeft,
        boolean upRight,
        boolean downLeft,
        boolean downRight
) {

    public static CtmConnections fromMask(int mask) {
        if (mask < 0 || mask > 0xFF) {
            throw new IllegalArgumentException("CTM mask must be an unsigned byte");
        }
        return new CtmConnections(
                bit(mask, 0), bit(mask, 1), bit(mask, 2), bit(mask, 3),
                bit(mask, 4), bit(mask, 5), bit(mask, 6), bit(mask, 7)
        );
    }

    public int mask() {
        return (up ? 1 : 0)
                | (down ? 2 : 0)
                | (left ? 4 : 0)
                | (right ? 8 : 0)
                | (upLeft ? 16 : 0)
                | (upRight ? 32 : 0)
                | (downLeft ? 64 : 0)
                | (downRight ? 128 : 0);
    }

    public List<CtmTextureRole> quadrants() {
        return List.of(
                CtmSelector.select(up, left, upLeft),
                CtmSelector.select(up, right, upRight),
                CtmSelector.select(down, left, downLeft),
                CtmSelector.select(down, right, downRight)
        );
    }

    public boolean completelyConnected() {
        return mask() == 0xFF;
    }

    private static boolean bit(int mask, int index) {
        return (mask & (1 << index)) != 0;
    }
}
