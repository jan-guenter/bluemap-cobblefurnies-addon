/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.cobblefurnies.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.MaxCapacityReachedException;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.map.hires.block.BlockRenderer;
import de.bluecolored.bluemap.core.map.hires.block.ResourceModelRenderer;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import io.github.janguenter.bluemap.cobblefurnies.activation.CompiledProfile;
import io.github.janguenter.bluemap.cobblefurnies.activation.CobbleFurniesRuntime;
import io.github.janguenter.bluemap.cobblefurnies.model.CtmConnections;
import io.github.janguenter.bluemap.cobblefurnies.model.CtmTextureRole;
import io.github.janguenter.bluemap.cobblefurnies.model.CubeFace;
import io.github.janguenter.bluemap.cobblefurnies.model.StatueFacing;
import io.github.janguenter.bluemap.cobblefurnies.model.StatueModel;
import io.github.janguenter.bluemap.cobblefurnies.profile.CobbleFurnies12Athena406Profile;
import io.github.janguenter.bluemap.cobblefurnies.profile.CobbleFurniesDefinition;
import io.github.janguenter.bluemap.cobblefurnies.profile.LoaderFamily;

import java.util.function.Consumer;

/** Exact 32-route Athena renderer plus five lower-half static statues. */
final class CobbleFurniesRenderer implements BlockRenderer {

    private static final float CARPET_HEIGHT = 1F / 16F;
    private final ResourcePack resourcePack;
    private final CobbleFurniesRuntime runtime;
    private final ResourceModelRenderer stock;
    private final AthenaQuadEmitter athena;
    private final StatueMeshEmitter statues;
    private final BoundedDiagnostics diagnostics = new BoundedDiagnostics();

    CobbleFurniesRenderer(
            ResourcePack resourcePack,
            TextureGallery textureGallery,
            RenderSettings renderSettings,
            CobbleFurniesRuntime runtime
    ) {
        this.resourcePack = resourcePack;
        this.runtime = runtime;
        this.stock = new ResourceModelRenderer(resourcePack, textureGallery, renderSettings);
        this.athena = new AthenaQuadEmitter(resourcePack, textureGallery, renderSettings);
        this.statues = new StatueMeshEmitter(resourcePack, textureGallery, renderSettings);
    }

    @Override
    public void render(
            BlockNeighborhood block,
            Variant original,
            TileModelView target,
            Color mapColor
    ) {
        int start = target.getStart();
        Color initialMapColor = new Color().set(mapColor);
        CompiledProfile compiled = runtime.profile();
        if (!runtime.route().isActive() || compiled == null) {
            renderStock(block, target, mapColor);
            return;
        }
        String blockId = block.getBlockState().getId().getFormatted();
        CobbleFurniesDefinition definition =
                CobbleFurnies12Athena406Profile.DEFINITIONS.get(blockId);
        try {
            if (definition != null) {
                renderAthena(definition, block, target, mapColor, start, initialMapColor);
                return;
            }
            StatueModel statue = compiled.statues().get(blockId);
            if (!usesStaticStatue(statue != null, block.getBlockState())) {
                renderStock(block, target, mapColor);
                return;
            }
            StatueFacing facing = StatueFacing.parse(
                    block.getBlockState().getProperties().get("facing")
            );
            statues.beginVariantColor();
            if (!statues.emit(statue, facing, block, target, mapColor)) {
                diagnostics.report("statue-resource-render-failed");
                resetAndRenderStock(block, target, start, mapColor, initialMapColor);
                return;
            }
            statues.finishVariantColor(mapColor);
        } catch (MaxCapacityReachedException exception) {
            throw capacityFailure(exception);
        } catch (IllegalArgumentException exception) {
            diagnostics.report("malformed-persisted-state");
            resetAndRenderStock(block, target, start, mapColor, initialMapColor);
        } catch (RuntimeException exception) {
            diagnostics.report("contained-render-failure");
            resetAndRenderStock(block, target, start, mapColor, initialMapColor);
        }
    }

    private void renderAthena(
            CobbleFurniesDefinition definition,
            BlockNeighborhood block,
            TileModelView target,
            Color mapColor,
            int start,
            Color initialMapColor
    ) {
        athena.beginVariantColor();
        boolean rendered = definition.family() == LoaderFamily.CTM
                ? renderCtmCube(definition, block, target, mapColor)
                : renderCarpet(definition, block, target, mapColor);
        if (!rendered) {
            diagnostics.report("athena-resource-render-failed");
            resetAndRenderStock(block, target, start, mapColor, initialMapColor);
        } else {
            athena.finishVariantColor(mapColor);
        }
    }

    private boolean renderCtmCube(
            CobbleFurniesDefinition definition,
            BlockNeighborhood block,
            TileModelView target,
            Color mapColor
    ) {
        for (CubeFace face : CubeFace.values()) {
            if (sameBlock(block, face.normal())) {
                continue;
            }
            if (!emitCtmFace(
                    definition, block, target, mapColor, face, 0F,
                    connections(block, face), true
            )) {
                return false;
            }
        }
        return true;
    }

    private boolean renderCarpet(
            CobbleFurniesDefinition definition,
            BlockNeighborhood block,
            TileModelView target,
            Color mapColor
    ) {
        for (CubeFace face : new CubeFace[]{
                CubeFace.NORTH, CubeFace.SOUTH, CubeFace.WEST, CubeFace.EAST
        }) {
            if (!athena.emit(
                    block, target, mapColor, face, 0F,
                    0F, 0F, 1F, CARPET_HEIGHT,
                    Key.parse(definition.texture("particle")), 0, true
            )) {
                return false;
            }
        }
        for (CubeFace face : new CubeFace[]{CubeFace.UP, CubeFace.DOWN}) {
            float depth = face == CubeFace.UP ? 15F / 16F : 1F / 16F;
            if (!emitCtmFace(
                    definition, block, target, mapColor, face, depth,
                    connections(block, face), false
            )) {
                return false;
            }
        }
        return true;
    }

    private boolean emitCtmFace(
            CobbleFurniesDefinition definition,
            BlockNeighborhood block,
            TileModelView target,
            Color mapColor,
            CubeFace face,
            float depth,
            CtmConnections ctm,
            boolean cullable
    ) {
        if (ctm.completelyConnected()) {
            return emitFull(
                    definition, "empty", block, target, mapColor, face, depth, cullable
            );
        }
        return emitQuarter(definition, block, target, mapColor, face, depth,
                0F, 0.5F, 0.5F, 1F, ctm.quadrants().get(0), cullable)
                && emitQuarter(definition, block, target, mapColor, face, depth,
                0.5F, 0.5F, 1F, 1F, ctm.quadrants().get(1), cullable)
                && emitQuarter(definition, block, target, mapColor, face, depth,
                0F, 0F, 0.5F, 0.5F, ctm.quadrants().get(2), cullable)
                && emitQuarter(definition, block, target, mapColor, face, depth,
                0.5F, 0F, 1F, 0.5F, ctm.quadrants().get(3), cullable);
    }

    private boolean emitQuarter(
            CobbleFurniesDefinition definition,
            BlockNeighborhood block,
            TileModelView target,
            Color mapColor,
            CubeFace face,
            float depth,
            float left,
            float bottom,
            float right,
            float top,
            CtmTextureRole role,
            boolean cullable
    ) {
        return athena.emit(
                block, target, mapColor, face, depth, left, bottom, right, top,
                Key.parse(definition.texture(role.wireName())), 0, cullable
        );
    }

    private boolean emitFull(
            CobbleFurniesDefinition definition,
            String role,
            BlockNeighborhood block,
            TileModelView target,
            Color mapColor,
            CubeFace face,
            float depth,
            boolean cullable
    ) {
        return athena.emit(
                block, target, mapColor, face, depth, 0F, 0F, 1F, 1F,
                Key.parse(definition.texture(role)), 0, cullable
        );
    }

    static CtmConnections connections(BlockNeighborhood block, CubeFace face) {
        CubeFace.Vec up = face.localUp();
        CubeFace.Vec down = face.localDown();
        CubeFace.Vec left = face.localLeft();
        CubeFace.Vec right = face.localRight();
        return new CtmConnections(
                sameState(block, up), sameState(block, down),
                sameState(block, left), sameState(block, right),
                sameState(block, up.add(left)), sameState(block, up.add(right)),
                sameState(block, down.add(left)), sameState(block, down.add(right))
        );
    }

    static boolean isLowerHalf(BlockState state) {
        return "lower".equals(state.getProperties().get("half"));
    }

    static boolean usesStaticStatue(boolean compiledStatuePresent, BlockState state) {
        return compiledStatuePresent && isLowerHalf(state);
    }

    private static boolean sameState(BlockNeighborhood block, CubeFace.Vec offset) {
        return block.getNeighborBlock(offset.x(), offset.y(), offset.z())
                .getBlockState().equals(block.getBlockState());
    }

    private static boolean sameBlock(BlockNeighborhood block, CubeFace.Vec offset) {
        return block.getNeighborBlock(offset.x(), offset.y(), offset.z())
                .getBlockState().getId().equals(block.getBlockState().getId());
    }

    private void resetAndRenderStock(
            BlockNeighborhood block,
            TileModelView target,
            int start,
            Color mapColor,
            Color initialMapColor
    ) {
        resetPartialGeometry(target, start, mapColor, initialMapColor);
        renderStock(block, target, mapColor);
    }

    static void resetPartialGeometry(
            TileModelView target, int start, Color mapColor, Color initialMapColor
    ) {
        target.getTileModel().reset(start);
        target.initialize(start);
        mapColor.set(initialMapColor);
    }

    static MaxCapacityReachedException capacityFailure(
            MaxCapacityReachedException exception
    ) {
        return exception;
    }

    private void renderStock(
            BlockNeighborhood block, TileModelView target, Color mapColor
    ) {
        de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState state =
                resourcePack.getBlockStates().get(block.getBlockState().getId());
        if (state == null) {
            return;
        }
        forEachIsolatedVariant(
                state, block.getBlockState(), block.getX(), block.getY(), block.getZ(), target,
                variant -> stock.render(block, variant, target, mapColor)
        );
    }

    static void forEachIsolatedVariant(
            de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState state,
            BlockState worldState,
            int x,
            int y,
            int z,
            TileModelView target,
            Consumer<Variant> renderer
    ) {
        state.forEach(worldState, x, y, z, variant -> {
            target.initialize();
            renderer.accept(variant);
        });
    }
}
