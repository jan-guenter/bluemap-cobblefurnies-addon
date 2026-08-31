/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.cobblefurnies.adapter.bluemap523;

import io.github.janguenter.bluemap.addon.adapter.api.bluemap523.ResourceExtensionType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AdapterBoundaryTest {

    @Test
    void usesSharedAdapterHelpersWithoutLocalCopies() {
        assertInstanceOf(ResourceExtensionType.class, BlueMap523Adapter.extensionType());
        assertEquals(BlueMap523Adapter.EXTENSION_KEY,
                BlueMap523Adapter.extensionType().getKey());
        assertInstanceOf(CobbleFurniesResourceExtension.class,
                BlueMap523Adapter.extensionType().create(null));
        assertThrows(ClassNotFoundException.class, () -> Class.forName(
                "io.github.janguenter.bluemap.cobblefurnies.adapter.bluemap523."
                        + "AdapterCompatibility"
        ));
        assertThrows(ClassNotFoundException.class, () -> Class.forName(
                "io.github.janguenter.bluemap.cobblefurnies.adapter.bluemap523."
                        + "CobbleFurniesResourceExtensionType"
        ));
    }
}
