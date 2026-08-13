/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.cobblefurnies.profile;

import de.bluecolored.bluemap.core.util.Key;

import java.util.Objects;

/** Exact installed-resource identity and closed complexity budget for one statue. */
public record StatueDefinition(
        String name,
        String blockId,
        String modelResource,
        Key texture,
        int groupCount,
        int cubeCount,
        int triangleCount,
        int modelBytes,
        String modelSha256
) {

    public StatueDefinition {
        Objects.requireNonNull(texture, "texture");
        if (!name.matches("[a-z]+")
                || !blockId.equals("cobblefurnies:statue_" + name)
                || !modelResource.equals(
                        "assets/cobblefurnies/models/bb/statue_" + name + ".bbs.json")
                || groupCount < 1 || groupCount > 64
                || cubeCount < 1 || cubeCount > 128
                || triangleCount != cubeCount * 12
                || modelBytes < 1 || modelBytes > 64 * 1024
                || !modelSha256.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("malformed exact statue definition");
        }
    }
}
