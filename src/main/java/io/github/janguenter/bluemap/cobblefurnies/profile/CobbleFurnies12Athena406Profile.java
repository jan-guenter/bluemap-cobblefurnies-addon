/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.cobblefurnies.profile;

import de.bluecolored.bluemap.core.util.Key;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Exact All the Mons 1.2.0 CobbleFurnies 1.2/Athena 4.0.6 profile. */
public final class CobbleFurnies12Athena406Profile {

    public static final String PROFILE_ID = "cobblefurnies-1.2-athena-4.0.6";
    public static final String COBBLEFURNIES_SHA256 =
            "82894965d01bfb00fb6109ac275622a157d415ef0957d41fd6478b6d64ce34f8";
    public static final long COBBLEFURNIES_SIZE = 2_343_464L;
    public static final String ATHENA_SHA256 =
            "43699885bbce3343916d4c5c4940cf0e3f9f6f02fdeb46e8655e121b42282ec5";
    public static final long ATHENA_SIZE = 99_944L;
    public static final int ATHENA_BLOCK_COUNT = 32;
    public static final int STATUE_BLOCK_COUNT = 5;
    public static final int ROUTED_BLOCK_COUNT = 37;
    public static final int REQUIRED_RESOURCE_COUNT = 132;
    public static final int REQUIRED_TEXTURE_COUNT = 85;

    private static final String ROOT =
            "/bluemap-cobblefurnies/profiles/cobblefurnies/1.2-athena-4.0.6/";
    public static final DefinitionCatalog ATHENA = DefinitionCatalog.load(
            ROOT + "definitions.tsv", ATHENA_BLOCK_COUNT,
            "2c00307f4bc09596ce28700eae8f3ae8a3830ca326d2b818ad65c68f2ff342b5"
    );
    public static final Map<String, CobbleFurniesDefinition> DEFINITIONS =
            ATHENA.definitions();
    public static final Map<String, StatueDefinition> STATUES = StatueCatalog.load(
            ROOT + "statues.tsv", STATUE_BLOCK_COUNT,
            "5d3035d9c7f9bba494404c9c98f3500d3c3a06b3306e62776ad6c5d6c55bf07c"
    );
    public static final Map<String, ExactResourceManifest.Identity> RESOURCES =
            ExactResourceManifest.load(
                    ROOT + "required-resources.tsv", REQUIRED_RESOURCE_COUNT,
                    "9966bc564b628b31e54b4b79ccacf8e470a37910ebda2a23f7c6b743f557637a"
            );
    public static final Set<String> ROUTED_BLOCKS;
    public static final Set<Key> REQUIRED_TEXTURES;

    static {
        LinkedHashSet<String> blocks = new LinkedHashSet<>(DEFINITIONS.keySet());
        blocks.addAll(STATUES.keySet());
        ROUTED_BLOCKS = Set.copyOf(blocks);
        LinkedHashSet<Key> textures = ATHENA.textureIds().stream()
                .map(Key::parse)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        STATUES.values().stream().map(StatueDefinition::texture).forEach(textures::add);
        REQUIRED_TEXTURES = Set.copyOf(textures);
        if (ROUTED_BLOCKS.size() != ROUTED_BLOCK_COUNT
                || REQUIRED_TEXTURES.size() != REQUIRED_TEXTURE_COUNT) {
            throw new IllegalStateException("exact profile roster changed");
        }
    }

    private CobbleFurnies12Athena406Profile() {
    }
}
