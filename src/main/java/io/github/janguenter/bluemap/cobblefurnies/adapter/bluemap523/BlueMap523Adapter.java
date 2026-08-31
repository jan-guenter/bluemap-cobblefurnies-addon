/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.cobblefurnies.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.addon.adapter.api.bluemap523.RegistryGuard;
import io.github.janguenter.bluemap.addon.adapter.api.bluemap523.ResourceExtensionType;
import io.github.janguenter.bluemap.cobblefurnies.activation.CobbleFurniesRuntime;

/** Exact BlueMap 5.23 feature-backport internal ABI boundary. */
public final class BlueMap523Adapter {

    private static final CobbleFurniesRuntime RUNTIME = CobbleFurniesRuntime.INSTANCE;
    private static final Key RENDERER_KEY =
            Key.parse("bluemap_cobblefurnies:exact_shape");
    static final Key EXTENSION_KEY = Key.parse("bluemap_cobblefurnies:exact_profile");
    private static final BlockRendererType RENDERER = new BlockRendererType.Impl(
            RENDERER_KEY,
            (pack, gallery, settings) -> new CobbleFurniesRenderer(pack, gallery, settings, RUNTIME)
    );
    private static final ResourcePack.Extension<CobbleFurniesResourceExtension> EXTENSION =
            new ResourceExtensionType<>(
                    EXTENSION_KEY,
                    pack -> new CobbleFurniesResourceExtension(pack, RUNTIME)
            );

    private BlueMap523Adapter() {
    }

    public static synchronized boolean install() {
        if (!RegistryGuard.canRegister(BlockRendererType.REGISTRY, RENDERER)
                || !RegistryGuard.canRegister(ResourcePack.Extension.REGISTRY, EXTENSION)) {
            RUNTIME.disable("registry-collision");
            return false;
        }
        if (!RegistryGuard.register(BlockRendererType.REGISTRY, RENDERER)
                || !RegistryGuard.register(ResourcePack.Extension.REGISTRY, EXTENSION)) {
            RUNTIME.disable("registry-collision");
            return false;
        }
        return true;
    }

    static boolean isExpectedDispatch(Variant variant) {
        return variant != null
                && variant.getRenderer() == RENDERER
                && ResourcePack.MISSING_BLOCK_MODEL.equals(variant.getModel())
                && !variant.isTransformed()
                && !variant.isUvlock()
                && Double.compare(variant.getWeight(), 1D) == 0;
    }

    static ResourcePack.Extension<CobbleFurniesResourceExtension> extensionType() {
        return EXTENSION;
    }
}
