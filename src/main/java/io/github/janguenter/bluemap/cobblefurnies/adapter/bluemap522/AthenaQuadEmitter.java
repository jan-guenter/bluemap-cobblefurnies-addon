/*
 * SPDX-License-Identifier: MIT
 *
 * Project-authored MIT adaptation of the owner's first-party BlueMap Chipped
 * Add-on Athena quad emitter at commit
 * c474a82b6bfd1b4173d119cb1e053a5458167e4b. Third-party models and textures
 * remain operator-installed and are referenced only by resource key.
 */
package io.github.janguenter.bluemap.cobblefurnies.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModel;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.BlockProperties;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import de.bluecolored.bluemap.core.world.block.ExtendedBlock;
import io.github.janguenter.bluemap.cobblefurnies.model.CubeFace;

/** Emits deterministic face-local quads with cropped geometry-locked UVs. */
final class AthenaQuadEmitter {

    private static final de.bluecolored.bluemap.core.resources.ResourcePath<
            de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model
            > TRANSFORM_SENTINEL = new de.bluecolored.bluemap.core.resources.ResourcePath<>(
                    "bluemap", "block/missing"
            );
    private static final Variant IDENTITY_TRANSFORM = new Variant(
            TRANSFORM_SENTINEL, 0F, 0F, 0F
    );

    private final ResourcePack resourcePack;
    private final TextureGallery textureGallery;
    private final RenderSettings renderSettings;
    private float mapColorOpacity;

    AthenaQuadEmitter(
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
        finishVariantColor(mapColor, mapColorOpacity);
    }

    boolean emit(
            BlockNeighborhood block,
            TileModelView target,
            Color mapColor,
            CubeFace face,
            float depth,
            float left,
            float bottom,
            float right,
            float top,
            Key textureKey,
            int clockwiseQuarterTurns,
            boolean cullable
    ) {
        if (!validBounds(depth, left, bottom, right, top)) {
            return false;
        }
        Texture texture = resourcePack.getTextures().get(textureKey);
        if (texture == null) {
            return false;
        }
        if (renderSettings.isRenderTopOnly() && face.normal().y() < 1) {
            return true;
        }
        if (cullable && culled(block, face)) {
            return true;
        }
        Direction direction = Direction.valueOf(face.name());
        FaceLighting.Sample light = FaceLighting.sample(
                block,
                direction,
                IDENTITY_TRANSFORM,
                0
        );
        if (hiddenByCave(
                block.isRemoveIfCave(),
                renderSettings.isCaveDetectionUsesBlockLight(),
                light
        )) {
            return true;
        }

        Point bottomLeft = point(face, depth, left, bottom);
        Point bottomRight = point(face, depth, right, bottom);
        Point topRight = point(face, depth, right, top);
        Point topLeft = point(face, depth, left, top);

        float[][] rawUvs = {
                {left, 1F - bottom},
                {right, 1F - bottom},
                {right, 1F - top},
                {left, 1F - top}
        };
        int rotation = Math.floorMod(clockwiseQuarterTurns, 4);
        float[] uv0 = rotateUv(rawUvs[0], rotation);
        float[] uv1 = rotateUv(rawUvs[1], rotation);
        float[] uv2 = rotateUv(rawUvs[2], rotation);
        float[] uv3 = rotateUv(rawUvs[3], rotation);

        int start = target.add(2);
        TileModel mesh = target.getTileModel();
        mesh.setPositions(
                start,
                bottomLeft.x(), bottomLeft.y(), bottomLeft.z(),
                bottomRight.x(), bottomRight.y(), bottomRight.z(),
                topRight.x(), topRight.y(), topRight.z()
        );
        mesh.setPositions(
                start + 1,
                bottomLeft.x(), bottomLeft.y(), bottomLeft.z(),
                topRight.x(), topRight.y(), topRight.z(),
                topLeft.x(), topLeft.y(), topLeft.z()
        );
        mesh.setUvs(start, uv0[0], uv0[1], uv1[0], uv1[1], uv2[0], uv2[1]);
        mesh.setUvs(start + 1, uv0[0], uv0[1], uv2[0], uv2[1], uv3[0], uv3[1]);
        int material = textureGallery.get(textureKey);
        mesh.setMaterialIndex(start, material);
        mesh.setMaterialIndex(start + 1, material);
        mesh.setColor(start, 1F, 1F, 1F);
        mesh.setColor(start + 1, 1F, 1F, 1F);
        mesh.setAOs(start, 1F, 1F, 1F);
        mesh.setAOs(start + 1, 1F, 1F, 1F);

        mesh.setSunlight(start, light.sunlight());
        mesh.setSunlight(start + 1, light.sunlight());
        mesh.setBlocklight(start, light.blocklight());
        mesh.setBlocklight(start + 1, light.blocklight());
        if (face == CubeFace.UP) {
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
        return true;
    }

    static void finishVariantColor(Color mapColor, float maximumOpacity) {
        if (mapColor.a > 0F) {
            mapColor.flatten().straight();
            mapColor.a = maximumOpacity;
        }
    }

    static boolean hiddenByCave(
            boolean removeIfCave,
            boolean caveDetectionUsesBlockLight,
            FaceLighting.Sample light
    ) {
        int visibleLight = caveDetectionUsesBlockLight
                ? Math.max(light.sunlight(), light.blocklight())
                : light.sunlight();
        return removeIfCave && visibleLight == 0;
    }

    private static boolean culled(BlockNeighborhood block, CubeFace face) {
        CubeFace.Vec normal = face.normal();
        ExtendedBlock neighbor = block.getNeighborBlock(normal.x(), normal.y(), normal.z());
        BlockProperties properties = neighbor.getProperties();
        return properties.isCulling()
                || properties.getCullingIdentical()
                && neighbor.getBlockState().equals(block.getBlockState());
    }

    static Point point(
            CubeFace face,
            float depth,
            float horizontal,
            float vertical
    ) {
        CubeFace.Vec normal = face.normal();
        CubeFace.Vec right = face.uvRight();
        CubeFace.Vec up = face.uvUp();
        float normalScale = 0.5F - depth;
        float horizontalScale = horizontal - 0.5F;
        float verticalScale = vertical - 0.5F;
        return new Point(
                0.5F + normal.x() * normalScale
                        + right.x() * horizontalScale + up.x() * verticalScale,
                0.5F + normal.y() * normalScale
                        + right.y() * horizontalScale + up.y() * verticalScale,
                0.5F + normal.z() * normalScale
                        + right.z() * horizontalScale + up.z() * verticalScale
        );
    }

    static boolean validBounds(
            float depth,
            float left,
            float bottom,
            float right,
            float top
    ) {
        return Float.isFinite(depth) && Float.isFinite(left) && Float.isFinite(bottom)
                && Float.isFinite(right) && Float.isFinite(top)
                && depth >= 0F && depth <= 1F
                && left >= 0F && left < right && right <= 1F
                && bottom >= 0F && bottom < top && top <= 1F;
    }

    static float[] rotateUv(float[] uv, int clockwiseQuarterTurns) {
        float u = uv[0];
        float v = uv[1];
        return switch (clockwiseQuarterTurns) {
            case 0 -> new float[]{u, v};
            case 1 -> new float[]{1F - v, u};
            case 2 -> new float[]{1F - u, 1F - v};
            case 3 -> new float[]{v, 1F - u};
            default -> throw new IllegalArgumentException("invalid UV quarter turn");
        };
    }

    record Point(float x, float y, float z) {
    }
}
