/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.cobblefurnies.adapter.bluemap523;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.bluecolored.bluemap.core.util.math.Color;
import io.github.janguenter.bluemap.addon.render.core.adapter.bluemap523.FaceLighting;
import io.github.janguenter.bluemap.resource.athena.model.CubeFace;
import org.junit.jupiter.api.Test;

class AthenaQuadEmitterTest {

    @Test
    void carpetTopAndBottomShareTheExactOneSixteenthPlane() {
        AthenaQuadEmitter.Point up = AthenaQuadEmitter.point(
                CubeFace.UP, 15F / 16F, 0F, 0F
        );
        AthenaQuadEmitter.Point down = AthenaQuadEmitter.point(
                CubeFace.DOWN, 1F / 16F, 0F, 0F
        );
        assertEquals(1F / 16F, up.y(), 0.00001F);
        assertEquals(1F / 16F, down.y(), 0.00001F);
    }

    @Test
    void horizontalPanePlanesAreCenteredAtSevenAndNineSixteenths() {
        AthenaQuadEmitter.Point north = AthenaQuadEmitter.point(
                CubeFace.NORTH, 7F / 16F, 0F, 0F
        );
        AthenaQuadEmitter.Point south = AthenaQuadEmitter.point(
                CubeFace.SOUTH, 7F / 16F, 0F, 0F
        );
        assertEquals(7F / 16F, north.z(), 0.00001F);
        assertEquals(9F / 16F, south.z(), 0.00001F);
    }

    @Test
    void uvQuarterTurnsPreserveGeometryLockedCropCoordinates() {
        float[] uv = {0.25F, 0.75F};
        assertArrayEquals(new float[]{0.25F, 0.75F},
                AthenaQuadEmitter.rotateUv(uv, 0));
        assertArrayEquals(new float[]{0.25F, 0.25F},
                AthenaQuadEmitter.rotateUv(uv, 1));
        assertArrayEquals(new float[]{0.75F, 0.25F},
                AthenaQuadEmitter.rotateUv(uv, 2));
        assertArrayEquals(new float[]{0.75F, 0.75F},
                AthenaQuadEmitter.rotateUv(uv, 3));
    }

    @Test
    void malformedOrDegenerateBoundsFailBeforeEmission() {
        assertTrue(AthenaQuadEmitter.validBounds(0F, 0F, 0F, 0.5F, 0.5F));
        assertFalse(AthenaQuadEmitter.validBounds(0F, 0.5F, 0F, 0.5F, 1F));
        assertFalse(AthenaQuadEmitter.validBounds(Float.NaN, 0F, 0F, 1F, 1F));
    }

    @Test
    void fourOpaqueQuadrantsNormalizeToOneRepresentativeOpacity() {
        Color accumulated = new Color().set(0F, 0F, 0F, 0F, true);
        Color quadrant = new Color().set(0.25F, 0.5F, 0.75F, 1F, true);
        for (int index = 0; index < 4; index++) {
            accumulated.add(quadrant);
        }
        assertEquals(4F, accumulated.a, 0.00001F);

        AthenaQuadEmitter.finishVariantColor(accumulated, 1F);

        assertEquals(1F, accumulated.a, 0.00001F);
        assertEquals(0.25F, accumulated.r, 0.00001F);
        assertEquals(0.5F, accumulated.g, 0.00001F);
        assertEquals(0.75F, accumulated.b, 0.00001F);
    }

    @Test
    void caveRemovalMatchesBlueMapSunAndOptionalBlockLightPolicy() {
        FaceLighting.Sample dark = new FaceLighting.Sample(0, 0);
        FaceLighting.Sample torch = new FaceLighting.Sample(0, 7);
        FaceLighting.Sample sky = new FaceLighting.Sample(9, 0);

        assertFalse(AthenaQuadEmitter.hiddenByCave(false, false, dark));
        assertTrue(AthenaQuadEmitter.hiddenByCave(true, false, dark));
        assertTrue(AthenaQuadEmitter.hiddenByCave(true, false, torch));
        assertFalse(AthenaQuadEmitter.hiddenByCave(true, true, torch));
        assertFalse(AthenaQuadEmitter.hiddenByCave(true, false, sky));
    }
}
