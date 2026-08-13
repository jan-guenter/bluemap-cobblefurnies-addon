/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.cobblefurnies.adapter.bluemap522;

import de.bluecolored.bluemap.core.util.Direction;
import io.github.janguenter.bluemap.cobblefurnies.model.StatueModel.Vec3;

/** Package bridge that keeps production visibility narrow. */
public final class StatueMeshEmitterAccess {

    private StatueMeshEmitterAccess() {
    }

    public static Direction nearestDirection(Vec3 normal) {
        return StatueMeshEmitter.nearestDirection(normal);
    }
}
