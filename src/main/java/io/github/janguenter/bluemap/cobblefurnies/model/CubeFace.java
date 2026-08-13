/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.cobblefurnies.model;

/** World-space face plus the installed-model UV basis used by CobbleFurnies. */
public enum CubeFace {
    DOWN(0, -1, 0, 1, 0, 0, 0, 0, 1),
    UP(0, 1, 0, 1, 0, 0, 0, 0, -1),
    NORTH(0, 0, -1, -1, 0, 0, 0, 1, 0),
    SOUTH(0, 0, 1, 1, 0, 0, 0, 1, 0),
    WEST(-1, 0, 0, 0, 0, 1, 0, 1, 0),
    EAST(1, 0, 0, 0, 0, -1, 0, 1, 0);

    private final Vec normal;
    private final Vec uvRight;
    private final Vec uvUp;

    CubeFace(
            int normalX, int normalY, int normalZ,
            int rightX, int rightY, int rightZ,
            int upX, int upY, int upZ
    ) {
        normal = new Vec(normalX, normalY, normalZ);
        uvRight = new Vec(rightX, rightY, rightZ);
        uvUp = new Vec(upX, upY, upZ);
    }

    public Vec normal() {
        return normal;
    }

    public Vec uvRight() {
        return uvRight;
    }

    public Vec uvUp() {
        return uvUp;
    }

    public Vec localUp() {
        return uvUp;
    }

    public Vec localDown() {
        return uvUp.scale(-1);
    }

    public Vec localRight() {
        return uvRight;
    }

    public Vec localLeft() {
        return uvRight.scale(-1);
    }

    public record Vec(int x, int y, int z) {

        public Vec scale(int factor) {
            return new Vec(x * factor, y * factor, z * factor);
        }

        public Vec add(Vec other) {
            return new Vec(x + other.x, y + other.y, z + other.z);
        }
    }
}
