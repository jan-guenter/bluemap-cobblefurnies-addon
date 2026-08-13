/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.cobblefurnies.profile;

import java.util.List;
import java.util.Objects;

/** Metadata-only Athena route reconstructed from exact installed JSON. */
public record CobbleFurniesDefinition(
        String blockId,
        LoaderFamily family,
        List<String> textures
) {

    public CobbleFurniesDefinition {
        Objects.requireNonNull(blockId, "blockId");
        Objects.requireNonNull(family, "family");
        textures = List.copyOf(textures);
        if (!blockId.matches("cobblefurnies:[a-z_]+_poke_wool(?:_carpet)?")
                || textures.size() != family.textureRoles().size()) {
            throw new IllegalArgumentException("malformed CobbleFurnies Athena definition");
        }
        for (String texture : textures) {
            if (!texture.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) {
                throw new IllegalArgumentException("malformed texture key");
            }
        }
    }

    public String texture(String role) {
        return textures.get(family.textureIndex(role));
    }
}
