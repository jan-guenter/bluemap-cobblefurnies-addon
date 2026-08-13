/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.cobblefurnies.adapter.bluemap522;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import de.bluecolored.bluemap.core.map.hires.ArrayTileModel;
import de.bluecolored.bluemap.core.map.hires.MaxCapacityReachedException;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.util.math.Color;
import org.junit.jupiter.api.Test;

class CobbleFurniesRendererFailurePolicyTest {

    @Test
    void fallbackAtomicallyDropsPartialGeometryAndRestoresMapColor() {
        ArrayTileModel model = new ArrayTileModel(8);
        model.add(2);
        TileModelView target = new TileModelView(model);
        int start = target.getStart();
        target.add(4);
        Color initial = new Color().set(0.2F, 0.3F, 0.4F, 0.5F, true);
        Color changed = new Color().set(0.9F, 0.8F, 0.7F, 1F, true);

        CobbleFurniesRenderer.resetPartialGeometry(target, start, changed, initial);

        assertEquals(2, model.size());
        assertEquals(0, target.getSize());
        assertEquals(initial.r, changed.r, 0F);
        assertEquals(initial.g, changed.g, 0F);
        assertEquals(initial.b, changed.b, 0F);
        assertEquals(initial.a, changed.a, 0F);
    }

    @Test
    void capacityFailureIsReturnedUnwrappedForImmediateRethrow() {
        MaxCapacityReachedException failure = new MaxCapacityReachedException("full");
        assertSame(failure, CobbleFurniesRenderer.capacityFailure(failure));
    }
}
