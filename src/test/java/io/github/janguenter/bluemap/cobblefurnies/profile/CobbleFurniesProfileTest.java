/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.cobblefurnies.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CobbleFurniesProfileTest {

    @Test
    void exactMetadataOnlyCatalogHasClosedThirtySevenIdRoster() {
        assertEquals(32, CobbleFurnies12Athena406Profile.DEFINITIONS.size());
        assertEquals(5, CobbleFurnies12Athena406Profile.STATUES.size());
        assertEquals(37, CobbleFurnies12Athena406Profile.ROUTED_BLOCKS.size());
        assertEquals(85, CobbleFurnies12Athena406Profile.REQUIRED_TEXTURES.size());
        assertEquals(132, CobbleFurnies12Athena406Profile.RESOURCES.size());
        Map<LoaderFamily, Integer> counts = new EnumMap<>(LoaderFamily.class);
        CobbleFurnies12Athena406Profile.DEFINITIONS.values().forEach(
                definition -> counts.merge(definition.family(), 1, Integer::sum)
        );
        assertEquals(Map.of(
                LoaderFamily.CTM, 16,
                LoaderFamily.CARPET_CTM, 16
        ), counts);
        assertTrue(CobbleFurnies12Athena406Profile.ROUTED_BLOCKS.stream()
                .allMatch(block -> block.startsWith("cobblefurnies:")));
        assertEquals(2_652, CobbleFurnies12Athena406Profile.STATUES.values().stream()
                .mapToInt(StatueDefinition::triangleCount).sum());
    }

    @Test
    void onlyPokeWoolPairsAndFiveLowerAnchorStatuesAreOwned() {
        Set<String> colors = Set.of(
                "red", "orange", "yellow", "brown", "lime", "green", "cyan",
                "light_blue", "blue", "magenta", "purple", "pink", "white",
                "light_gray", "gray", "black"
        );
        for (String color : colors) {
            assertTrue(CobbleFurnies12Athena406Profile.DEFINITIONS.containsKey(
                    "cobblefurnies:" + color + "_poke_wool"
            ));
            assertTrue(CobbleFurnies12Athena406Profile.DEFINITIONS.containsKey(
                    "cobblefurnies:" + color + "_poke_wool_carpet"
            ));
        }
        assertEquals(
                List.of("ancient", "bulbasaur", "charmander", "pikachu", "squirtle"),
                CobbleFurnies12Athena406Profile.STATUES.values().stream()
                        .map(StatueDefinition::name).sorted().toList()
        );
        assertFalse(CobbleFurnies12Athena406Profile.ROUTED_BLOCKS.contains(
                "cobblefurnies:oak_chair"
        ));
    }
}
