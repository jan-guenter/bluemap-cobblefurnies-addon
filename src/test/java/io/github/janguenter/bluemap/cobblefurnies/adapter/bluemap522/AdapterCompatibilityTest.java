/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.cobblefurnies.adapter.bluemap522;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AdapterCompatibilityTest {

    @Test
    void onlyAuditedBlueMapIdentitiesAreAccepted() {
        assertTrue(AdapterCompatibility.supported(
                AdapterCompatibility.UPSTREAM_VERSION,
                AdapterCompatibility.UPSTREAM_COMMIT
        ));
        assertTrue(AdapterCompatibility.supported(
                AdapterCompatibility.BACKPORT_VERSION,
                AdapterCompatibility.BACKPORT_COMMIT
        ));
        assertFalse(AdapterCompatibility.supported(
                AdapterCompatibility.BACKPORT_VERSION,
                "0".repeat(40)
        ));
    }
}
