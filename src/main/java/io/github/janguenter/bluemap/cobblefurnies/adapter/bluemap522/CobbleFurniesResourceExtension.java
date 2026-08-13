/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.cobblefurnies.adapter.bluemap522;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePackExtension;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.VariantSet;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variants;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.BlockProperties;
import de.bluecolored.bluemap.core.world.BlockState;
import io.github.janguenter.bluemap.cobblefurnies.activation.CompiledProfile;
import io.github.janguenter.bluemap.cobblefurnies.activation.CobbleFurniesRuntime;
import io.github.janguenter.bluemap.cobblefurnies.model.BbsStatueCompiler;
import io.github.janguenter.bluemap.cobblefurnies.model.StatueModel;
import io.github.janguenter.bluemap.cobblefurnies.profile.CobbleFurnies12Athena406Profile;
import io.github.janguenter.bluemap.cobblefurnies.profile.CobbleFurniesDefinition;
import io.github.janguenter.bluemap.cobblefurnies.profile.ExactModArtifactDetector;
import io.github.janguenter.bluemap.cobblefurnies.profile.LoaderFamily;
import io.github.janguenter.bluemap.cobblefurnies.profile.ProfileDisablement;
import io.github.janguenter.bluemap.cobblefurnies.profile.StatueDefinition;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Exact artifact/resource activation and 37-ID routing. */
final class CobbleFurniesResourceExtension implements ResourcePackExtension {

    private static final Key SYNTHETIC = Key.parse("bluemap_cobblefurnies:exact_shape");

    private final ResourcePack resourcePack;
    private final CobbleFurniesRuntime runtime;

    CobbleFurniesResourceExtension(ResourcePack resourcePack, CobbleFurniesRuntime runtime) {
        this.resourcePack = resourcePack;
        this.runtime = runtime;
    }

    @Override
    public void loadResources(Iterable<Path> roots) throws IOException, InterruptedException {
        if (ProfileDisablement.current().isDisabled(
                CobbleFurnies12Athena406Profile.PROFILE_ID
        )) {
            runtime.inactive("operator-disabled");
            return;
        }
        if (!ExactModArtifactDetector.matchesRequiredPair(roots)) {
            runtime.inactive("exact-artifact-pair-missing");
            return;
        }
        ActiveResourceLoader.Result resources = ActiveResourceLoader.load(
                resourcePack, roots, CobbleFurnies12Athena406Profile.RESOURCES
        );
        if (!resources.valid()) {
            runtime.inactive(resources.reason());
            return;
        }
        Map<String, StatueModel> statues = new LinkedHashMap<>();
        try {
            for (StatueDefinition definition
                    : CobbleFurnies12Athena406Profile.STATUES.values()) {
                byte[] raw = resources.models().get(definition.modelResource());
                StatueModel model = BbsStatueCompiler.compile(definition, raw);
                statues.put(definition.blockId(), model);
            }
        } catch (IllegalArgumentException exception) {
            runtime.inactive("statue-compile-failed");
            return;
        }
        de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState dispatch =
                resourcePack.getBlockStates().get(SYNTHETIC);
        if (!validDispatch(dispatch)) {
            runtime.inactive("synthetic-dispatch-invalid");
            return;
        }
        runtime.activate(new CompiledProfile(statues));
    }

    @Override
    public Set<Key> collectUsedTextureKeys() {
        return runtime.route().isActive()
                ? CobbleFurnies12Athena406Profile.REQUIRED_TEXTURES : Set.of();
    }

    @Override
    public void bake() {
        if (!runtime.route().isActive()) {
            return;
        }
        for (Key texture : CobbleFurnies12Athena406Profile.REQUIRED_TEXTURES) {
            if (resourcePack.getTextures().get(texture) == null) {
                runtime.inactive("required-texture-missing");
                return;
            }
        }
    }

    @Override
    public Key getBlockStateKey(Key key) {
        return runtime.route().isActive()
                && CobbleFurnies12Athena406Profile.ROUTED_BLOCKS.contains(key.getFormatted())
                ? SYNTHETIC : key;
    }

    @Override
    public void getBlockProperties(BlockState state, BlockProperties.Builder builder) {
        if (!runtime.route().isActive()) {
            return;
        }
        String blockId = state.getId().getFormatted();
        if (CobbleFurnies12Athena406Profile.STATUES.containsKey(blockId)) {
            builder.culling(false).occluding(false).cullingIdentical(false);
            return;
        }
        CobbleFurniesDefinition definition =
                CobbleFurnies12Athena406Profile.DEFINITIONS.get(blockId);
        if (definition == null) {
            return;
        }
        if (definition.family() == LoaderFamily.CARPET_CTM) {
            builder.culling(false).occluding(false).cullingIdentical(false);
            return;
        }
        List<Texture> textures = definition.textures().stream()
                .map(Key::parse)
                .map(resourcePack.getTextures()::get)
                .toList();
        boolean opaque = opaqueTextures(textures);
        builder.culling(opaque).occluding(opaque).cullingIdentical(false);
    }

    static boolean opaqueTextures(List<Texture> textures) {
        return !textures.isEmpty() && textures.stream().allMatch(texture ->
                texture != null && texture.getColorStraight().a >= 1F
        );
    }

    private static boolean validDispatch(
            de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState state
    ) {
        if (state == null || state.getMultipart() != null) {
            return false;
        }
        Variants variants = state.getVariants();
        if (variants == null || variants.getDefaultVariant() == null) {
            return false;
        }
        VariantSet set = variants.getDefaultVariant();
        if (set.getVariants().length != 1) {
            return false;
        }
        Variant variant = set.getVariants()[0];
        return BlueMap522Adapter.isExpectedDispatch(variant);
    }
}
