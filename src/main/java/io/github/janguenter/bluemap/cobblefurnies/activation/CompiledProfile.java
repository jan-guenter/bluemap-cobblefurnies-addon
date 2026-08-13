/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.cobblefurnies.activation;

import io.github.janguenter.bluemap.cobblefurnies.model.StatueModel;

import java.util.Map;

/** Immutable resource-reload result published atomically to render workers. */
public record CompiledProfile(Map<String, StatueModel> statues) {
    public CompiledProfile {
        statues = Map.copyOf(statues);
        if (statues.size() != 5) {
            throw new IllegalArgumentException("compiled statue roster changed");
        }
    }
}
