/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.cobblefurnies.adapter.bluemap522;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.cobblefurnies.activation.CobbleFurniesRuntime;

/** Resource-pack extension factory registered before resource loading begins. */
final class CobbleFurniesResourceExtensionType
        implements ResourcePack.Extension<CobbleFurniesResourceExtension> {

    static final Key KEY = Key.parse("bluemap_cobblefurnies:exact_profile");

    private final CobbleFurniesRuntime runtime;

    CobbleFurniesResourceExtensionType(CobbleFurniesRuntime runtime) {
        this.runtime = runtime;
    }

    @Override
    public Key getKey() {
        return KEY;
    }

    @Override
    public CobbleFurniesResourceExtension create(ResourcePack pack) {
        return new CobbleFurniesResourceExtension(pack, runtime);
    }
}
