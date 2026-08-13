/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.cobblefurnies.model;

/** One exact five-texture Athena CTM role. */
public enum CtmTextureRole {
    PARTICLE("particle"),
    EMPTY("empty"),
    CENTER("center"),
    VERTICAL("vertical"),
    HORIZONTAL("horizontal");

    private final String wireName;

    CtmTextureRole(String wireName) {
        this.wireName = wireName;
    }

    public String wireName() {
        return wireName;
    }
}
