/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.cobblefurnies.model;

import de.bluecolored.bluemap.core.util.Key;

import java.util.List;
import java.util.Objects;

/** Immutable time-zero BBS mesh compiled from operator-installed resources. */
public record StatueModel(
        String blockId,
        Key texture,
        List<Quad> quads,
        Bounds bounds
) {

    public StatueModel {
        Objects.requireNonNull(blockId, "blockId");
        Objects.requireNonNull(texture, "texture");
        quads = List.copyOf(quads);
        Objects.requireNonNull(bounds, "bounds");
        if (!blockId.startsWith("cobblefurnies:statue_") || quads.isEmpty()) {
            throw new IllegalArgumentException("invalid compiled statue");
        }
    }

    /** One no-cull textured quad in deterministic source order. */
    public record Quad(Vertex first, Vertex second, Vertex third, Vertex fourth) {
        public Quad {
            Objects.requireNonNull(first, "first");
            Objects.requireNonNull(second, "second");
            Objects.requireNonNull(third, "third");
            Objects.requireNonNull(fourth, "fourth");
        }

        public Vec3 normal() {
            Vec3 a = second.position().subtract(first.position());
            Vec3 b = fourth.position().subtract(first.position());
            return a.cross(b).normalizedOr(new Vec3(0D, 1D, 0D));
        }
    }

    public record Vertex(Vec3 position, float u, float v) {
        public Vertex {
            Objects.requireNonNull(position, "position");
            if (!Float.isFinite(u) || !Float.isFinite(v)) {
                throw new IllegalArgumentException("non-finite statue UV");
            }
        }
    }

    public record Vec3(double x, double y, double z) {
        public Vec3 {
            if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
                throw new IllegalArgumentException("non-finite statue coordinate");
            }
        }

        public Vec3 add(Vec3 other) {
            return new Vec3(x + other.x, y + other.y, z + other.z);
        }

        public Vec3 subtract(Vec3 other) {
            return new Vec3(x - other.x, y - other.y, z - other.z);
        }

        public Vec3 scale(double factor) {
            return new Vec3(x * factor, y * factor, z * factor);
        }

        public Vec3 cross(Vec3 other) {
            return new Vec3(
                    y * other.z - z * other.y,
                    z * other.x - x * other.z,
                    x * other.y - y * other.x
            );
        }

        public Vec3 normalizedOr(Vec3 fallback) {
            double length = Math.sqrt(x * x + y * y + z * z);
            return length <= 1.0E-12D ? fallback : scale(1D / length);
        }

        public Vec3 rotateX(double degrees) {
            double angle = Math.toRadians(degrees);
            double cosine = Math.cos(angle);
            double sine = Math.sin(angle);
            return new Vec3(x, y * cosine - z * sine, y * sine + z * cosine);
        }

        public Vec3 rotateY(double degrees) {
            double angle = Math.toRadians(degrees);
            double cosine = Math.cos(angle);
            double sine = Math.sin(angle);
            return new Vec3(x * cosine + z * sine, y, -x * sine + z * cosine);
        }

        public Vec3 rotateZ(double degrees) {
            double angle = Math.toRadians(degrees);
            double cosine = Math.cos(angle);
            double sine = Math.sin(angle);
            return new Vec3(x * cosine - y * sine, x * sine + y * cosine, z);
        }

        /** Point experiences X, then Y, then Z, matching the BBS pose stack. */
        public Vec3 rotate(Vec3 degrees) {
            return rotateX(degrees.x).rotateY(degrees.y).rotateZ(degrees.z);
        }

        public Vec3 rotateAbout(Vec3 pivot, Vec3 degrees) {
            return subtract(pivot).rotate(degrees).add(pivot);
        }

        public Vec3 rotateFacing(double degrees) {
            return subtract(new Vec3(0.5D, 0D, 0.5D))
                    .rotateY(degrees)
                    .add(new Vec3(0.5D, 0D, 0.5D));
        }
    }

    public record Bounds(Vec3 minimum, Vec3 maximum) {
        public Bounds {
            Objects.requireNonNull(minimum, "minimum");
            Objects.requireNonNull(maximum, "maximum");
            if (minimum.x > maximum.x || minimum.y > maximum.y
                    || minimum.z > maximum.z) {
                throw new IllegalArgumentException("invalid statue bounds");
            }
        }
    }
}
