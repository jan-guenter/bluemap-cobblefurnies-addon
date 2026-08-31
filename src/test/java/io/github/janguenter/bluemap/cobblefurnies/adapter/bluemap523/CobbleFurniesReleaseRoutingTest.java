/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.cobblefurnies.adapter.bluemap523;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.BlockState;
import io.github.janguenter.bluemap.cobblefurnies.activation.CobbleFurniesRuntime;
import io.github.janguenter.bluemap.cobblefurnies.profile.CobbleFurnies12Athena406Profile;
import java.lang.reflect.Constructor;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CobbleFurniesReleaseRoutingTest {

    private static final Key SYNTHETIC =
            Key.parse("bluemap_cobblefurnies:exact_shape");

    @Test
    void inactiveExtensionPublishesNothingAndExactActivationRoutesOnlyOwnedRoster()
            throws ReflectiveOperationException {
        CobbleFurniesRuntime runtime = freshRuntime();
        CobbleFurniesResourceExtension extension =
                new CobbleFurniesResourceExtension(null, runtime);
        Key stock = Key.parse("cobblefurnies:oak_chair");

        assertEquals(Set.of(), extension.collectUsedTextureKeys());
        for (String block : CobbleFurnies12Athena406Profile.ROUTED_BLOCKS) {
            Key physical = Key.parse(block);
            assertEquals(physical, extension.getBlockStateKey(physical));
        }

        runtime.route().activate();

        assertEquals(
                CobbleFurnies12Athena406Profile.REQUIRED_TEXTURES,
                extension.collectUsedTextureKeys()
        );
        assertEquals(85, extension.collectUsedTextureKeys().size());
        for (String block : CobbleFurnies12Athena406Profile.ROUTED_BLOCKS) {
            assertEquals(SYNTHETIC, extension.getBlockStateKey(Key.parse(block)));
        }
        assertEquals(stock, extension.getBlockStateKey(stock));
        assertEquals(Key.parse("minecraft:stone"),
                extension.getBlockStateKey(Key.parse("minecraft:stone")));
    }

    @Test
    void onlySerializedLowerHalfDispatchesToTheStaticStatueMesh() {
        BlockState lower = BlockState.fromString(
                "cobblefurnies:statue_pikachu[facing=south,half=lower]"
        );
        BlockState upper = BlockState.fromString(
                "cobblefurnies:statue_pikachu[facing=south,half=upper]"
        );
        BlockState missing = BlockState.fromString(
                "cobblefurnies:statue_pikachu[facing=south]"
        );

        assertTrue(CobbleFurniesRenderer.usesStaticStatue(true, lower));
        assertFalse(CobbleFurniesRenderer.usesStaticStatue(true, upper));
        assertFalse(CobbleFurniesRenderer.usesStaticStatue(true, missing));
        assertFalse(CobbleFurniesRenderer.usesStaticStatue(false, lower));
    }

    private static CobbleFurniesRuntime freshRuntime()
            throws ReflectiveOperationException {
        Constructor<CobbleFurniesRuntime> constructor =
                CobbleFurniesRuntime.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }
}
