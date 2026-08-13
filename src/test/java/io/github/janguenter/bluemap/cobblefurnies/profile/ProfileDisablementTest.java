/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.cobblefurnies.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class ProfileDisablementTest {

    @Test
    void propertyAndEnvironmentValuesMergeCanonically() {
        ProfileDisablement disabled = ProfileDisablement.from(
                " CobbleFurnies-1.2-Athena-4.0.6,INVALID VALUE ",
                "future,cobblefurnies-1.2-athena-4.0.6"
        );
        assertEquals(
                Set.of("cobblefurnies-1.2-athena-4.0.6", "future"),
                disabled.disabledProfiles()
        );
        assertTrue(disabled.isDisabled("COBBLEFURNIES-1.2-ATHENA-4.0.6"));
        assertFalse(disabled.isDisabled("missing"));
    }
}
