/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.cobblefurnies.model;

/** Pure quadrant selector for the exact Athena 4.0.6 five-texture contract. */
public final class CtmSelector {

    private CtmSelector() {
    }

    public static CtmTextureRole select(
            boolean vertical,
            boolean horizontal,
            boolean diagonal
    ) {
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
