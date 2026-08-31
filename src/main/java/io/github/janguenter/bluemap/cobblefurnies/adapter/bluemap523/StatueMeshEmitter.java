/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.cobblefurnies.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModel;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import io.github.janguenter.bluemap.cobblefurnies.model.StatueFacing;
import io.github.janguenter.bluemap.cobblefurnies.model.StatueModel;
import io.github.janguenter.bluemap.cobblefurnies.model.StatueModel.Quad;
import io.github.janguenter.bluemap.cobblefurnies.model.StatueModel.Vec3;
import io.github.janguenter.bluemap.cobblefurnies.model.StatueModel.Vertex;

/** Emits compiled no-cull BBS quads using only the installed texture key. */
final class StatueMeshEmitter {

    private static final int[][] NO_CULL_TRIANGLE_ORDER = {
            {0, 1, 2}, {0, 2, 3}, {0, 2, 1}, {0, 3, 2}
    };
    private static final Variant IDENTITY = new Variant(
            new ResourcePath<Model>("bluemap", "block/missing"), 0F, 0F, 0F
    );

    private final ResourcePack resourcePack;
    private final TextureGallery textureGallery;
    private final RenderSettings renderSettings;
    private float mapColorOpacity;

    StatueMeshEmitter(
            ResourcePack resourcePack,
            TextureGallery textureGallery,
            RenderSettings renderSettings
    ) {
        this.resourcePack = resourcePack;
        this.textureGallery = textureGallery;
        this.renderSettings = renderSettings;
    }

    void beginVariantColor() {
        mapColorOpacity = 0F;
    }

    void finishVariantColor(Color mapColor) {
        AthenaQuadEmitter.finishVariantColor(mapColor, mapColorOpacity);
    }

    boolean emit(
            StatueModel model,
            StatueFacing facing,
            BlockNeighborhood block,
            TileModelView target,
            Color mapColor
    ) {
        Texture texture = resourcePack.getTextures().get(model.texture());
        if (texture == null) {
            return false;
        }
        int material = textureGallery.get(model.texture());
        for (Quad quad : model.quads()) {
            Vec3 normal = quad.normal().rotateY(facing.degrees());
            if (renderSettings.isRenderTopOnly() && normal.y() <= 0D) {
                continue;
            }
            Direction direction = nearestDirection(normal);
            FaceLighting.Sample light = FaceLighting.sample(block, direction, IDENTITY, 0);
            if (AthenaQuadEmitter.hiddenByCave(
                    block.isRemoveIfCave(),
                    renderSettings.isCaveDetectionUsesBlockLight(),
                    light
            )) {
                continue;
            }
            emitQuad(quad, facing, target, material, light);
            if (normal.y() > 0D) {
                Color average = new Color().set(texture.getColorPremultiplied());
                float lightFactor = Math.max(light.sunlight(), light.blocklight()) / 15F;
                lightFactor = (1F - renderSettings.getAmbientLight()) * lightFactor
                        + renderSettings.getAmbientLight();
                average.r *= lightFactor;
                average.g *= lightFactor;
                average.b *= lightFactor;
                mapColorOpacity = Math.max(mapColorOpacity, average.a);
                mapColor.add(average);
            }
        }
        return true;
    }

    private static void emitQuad(
            Quad quad,
            StatueFacing facing,
            TileModelView target,
            int material,
            FaceLighting.Sample light
    ) {
        Vertex first = rotate(quad.first(), facing);
        Vertex second = rotate(quad.second(), facing);
        Vertex third = rotate(quad.third(), facing);
        Vertex fourth = rotate(quad.fourth(), facing);
        Vertex[] vertices = {first, second, third, fourth};
        int start = target.add(emittedTriangleCount(1));
        TileModel mesh = target.getTileModel();
        for (int triangle = 0; triangle < NO_CULL_TRIANGLE_ORDER.length; triangle++) {
            int[] order = NO_CULL_TRIANGLE_ORDER[triangle];
            Vertex a = vertices[order[0]];
            Vertex b = vertices[order[1]];
            Vertex c = vertices[order[2]];
            positions(mesh, start + triangle, a, b, c);
            uvs(mesh, start + triangle, a, b, c);
        }
        for (int index = start; index < start + NO_CULL_TRIANGLE_ORDER.length; index++) {
            mesh.setMaterialIndex(index, material);
            mesh.setColor(index, 1F, 1F, 1F);
            mesh.setAOs(index, 1F, 1F, 1F);
            mesh.setSunlight(index, light.sunlight());
            mesh.setBlocklight(index, light.blocklight());
        }
    }

    static int emittedTriangleCount(int sourceQuads) {
        if (sourceQuads < 0) {
            throw new IllegalArgumentException("negative source quad count");
        }
        return Math.multiplyExact(sourceQuads, NO_CULL_TRIANGLE_ORDER.length);
    }

    static int[] triangleOrder(int triangle) {
        if (triangle < 0 || triangle >= NO_CULL_TRIANGLE_ORDER.length) {
            throw new IllegalArgumentException("invalid no-cull triangle index");
        }
        return NO_CULL_TRIANGLE_ORDER[triangle].clone();
    }

    private static Vertex rotate(Vertex vertex, StatueFacing facing) {
        return new Vertex(
                vertex.position().rotateFacing(facing.degrees()), vertex.u(), vertex.v()
        );
    }

    private static void positions(
            TileModel mesh, int index, Vertex first, Vertex second, Vertex third
    ) {
        mesh.setPositions(
                index,
                (float) first.position().x(),
                (float) first.position().y(),
                (float) first.position().z(),
                (float) second.position().x(),
                (float) second.position().y(),
                (float) second.position().z(),
                (float) third.position().x(),
                (float) third.position().y(),
                (float) third.position().z()
        );
    }

    private static void uvs(
            TileModel mesh, int index, Vertex first, Vertex second, Vertex third
    ) {
        mesh.setUvs(
                index, first.u(), first.v(), second.u(), second.v(), third.u(), third.v()
        );
    }

    static Direction nearestDirection(Vec3 normal) {
        double x = Math.abs(normal.x());
        double y = Math.abs(normal.y());
        double z = Math.abs(normal.z());
        if (y >= x && y >= z) {
            return normal.y() >= 0D ? Direction.UP : Direction.DOWN;
        }
        if (x >= z) {
            return normal.x() >= 0D ? Direction.EAST : Direction.WEST;
        }
        return normal.z() >= 0D ? Direction.SOUTH : Direction.NORTH;
    }
}
