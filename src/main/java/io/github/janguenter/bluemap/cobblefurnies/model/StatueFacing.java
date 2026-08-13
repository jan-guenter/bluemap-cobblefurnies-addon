/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.cobblefurnies.model;

import java.util.Locale;

/** Persisted lower-half facing mapped to the exact static client transform. */
public enum StatueFacing {
    NORTH("north", 0D),
    EAST("east", -90D),
    SOUTH("south", 180D),
    WEST("west", 90D);

    private final String wireName;
    private final double degrees;

    StatueFacing(String wireName, double degrees) {
        this.wireName = wireName;
        this.degrees = degrees;
    }

    public double degrees() {
        return degrees;
    }

    public static StatueFacing parse(String value) {
        if (value == null) {
            throw new IllegalArgumentException("missing statue facing");
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        for (StatueFacing facing : values()) {
            if (facing.wireName.equals(normalized)) {
                return facing;
            }
        }
        throw new IllegalArgumentException("unsupported statue facing");
    }
}
