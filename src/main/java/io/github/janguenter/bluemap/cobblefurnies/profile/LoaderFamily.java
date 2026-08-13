/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.cobblefurnies.profile;

import java.util.List;
import java.util.Locale;

/** The two Athena 4.0.6 loader families owned by this exact profile. */
public enum LoaderFamily {
    CTM("ctm"),
    CARPET_CTM("carpet_ctm");

    private static final List<String> ROLES =
            List.of("particle", "empty", "center", "vertical", "horizontal");
    private final String wireName;

    LoaderFamily(String wireName) {
        this.wireName = wireName;
    }

    public String wireName() {
        return wireName;
    }

    public List<String> textureRoles() {
        return ROLES;
    }

    public int textureIndex(String role) {
        int index = ROLES.indexOf(role);
        if (index < 0) {
            throw new IllegalArgumentException("unsupported texture role: " + role);
        }
        return index;
    }

    public static LoaderFamily parse(String value) {
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        for (LoaderFamily family : values()) {
            if (family.wireName.equals(normalized)) {
                return family;
            }
        }
        throw new IllegalArgumentException("unsupported loader family: " + value);
    }
}
