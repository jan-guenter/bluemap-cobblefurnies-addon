/*
 * SPDX-License-Identifier: MIT
 *
 * This is an independently authored interpreter for the exact installed BBS
 * 0.7.2 resource schema. It retains no third-party JSON, texture, class, or
 * derived mesh in the add-on artifact.
 */
package io.github.janguenter.bluemap.cobblefurnies.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.cobblefurnies.model.StatueModel.Bounds;
import io.github.janguenter.bluemap.cobblefurnies.model.StatueModel.Quad;
import io.github.janguenter.bluemap.cobblefurnies.model.StatueModel.Vec3;
import io.github.janguenter.bluemap.cobblefurnies.model.StatueModel.Vertex;
import io.github.janguenter.bluemap.cobblefurnies.profile.StatueDefinition;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Strict compile-on-reload BBS 0.7.2 interpreter for the five exact statues. */
public final class BbsStatueCompiler {

    private static final Set<String> ROOT_KEYS = Set.of("version", "animations", "model");
    private static final Set<String> MODEL_KEYS = Set.of("groups", "texture");
    private static final Set<String> GROUP_KEYS =
            Set.of("origin", "parent", "rotate", "cubes");
    private static final Set<String> CUBE_KEYS =
            Set.of("origin", "from", "size", "offset", "rotate", "uvs");
    private static final Set<String> FACE_KEYS =
            Set.of("front", "back", "right", "left", "bottom", "top");
    private static final int MAX_DEPTH = 32;
    private static final Vec3 ZERO = new Vec3(0D, 0D, 0D);

    private BbsStatueCompiler() {
    }

    public static StatueModel compile(StatueDefinition definition, byte[] raw) {
        if (raw == null || raw.length != definition.modelBytes()
                || !definition.modelSha256().equals(digest(raw))) {
            throw new IllegalArgumentException("statue model hash drift");
        }
        JsonObject root = parseObject(raw);
        requireKeys(root, ROOT_KEYS, true, "root");
        if (!"0.7.2".equals(string(root.get("version")))) {
            throw new IllegalArgumentException("unsupported BBS version");
        }
        JsonObject animations = object(root.get("animations"), "animations");
        if (!animations.keySet().isEmpty()) {
            throw new IllegalArgumentException("animated BBS model is outside scope");
        }
        JsonObject model = object(root.get("model"), "model");
        requireKeys(model, MODEL_KEYS, true, "model");
        JsonArray texture = array(model.get("texture"), "texture");
        if (texture.size() != 2 || integer(texture.get(0)) != 128
                || integer(texture.get(1)) != 128) {
            throw new IllegalArgumentException("BBS texture dimensions changed");
        }
        JsonObject rawGroups = object(model.get("groups"), "groups");
        if (rawGroups.size() != definition.groupCount()) {
            throw new IllegalArgumentException("BBS group count changed");
        }

        Map<String, RawGroup> groups = parseGroups(rawGroups);
        validateHierarchy(groups);
        int cubeCount = groups.values().stream().mapToInt(group -> group.cubes.size()).sum();
        if (cubeCount != definition.cubeCount()) {
            throw new IllegalArgumentException("BBS cube count changed");
        }

        List<Quad> quads = new ArrayList<>(definition.cubeCount() * 6);
        for (RawGroup group : groups.values()) {
            List<RawGroup> chain = parentChain(group, groups);
            for (RawCube cube : group.cubes) {
                emitCube(cube, chain, quads);
            }
        }
        if (quads.size() * 2 != definition.triangleCount()) {
            throw new IllegalArgumentException("BBS triangle budget changed");
        }
        return new StatueModel(
                definition.blockId(), definition.texture(), quads, bounds(quads)
        );
    }

    private static Map<String, RawGroup> parseGroups(JsonObject source) {
        Map<String, RawGroup> groups = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : source.entrySet()) {
            String name = entry.getKey();
            if (!name.matches("[a-z0-9_]+")) {
                throw new IllegalArgumentException("malformed BBS group name");
            }
            JsonObject object = object(entry.getValue(), "group");
            requireKeys(object, GROUP_KEYS, false, "group");
            if (!object.has("origin")) {
                throw new IllegalArgumentException("BBS group has no origin");
            }
            Vec3 origin = vector(object.get("origin"), "group origin", false);
            Vec3 rotation = object.has("rotate")
                    ? vector(object.get("rotate"), "group rotation", true) : ZERO;
            String parent = object.has("parent") ? string(object.get("parent")) : null;
            if (parent != null && !parent.matches("[a-z0-9_]+")) {
                throw new IllegalArgumentException("malformed BBS parent");
            }
            List<RawCube> cubes = new ArrayList<>();
            if (object.has("cubes")) {
                JsonArray array = array(object.get("cubes"), "cubes");
                for (JsonElement cube : array) {
                    cubes.add(parseCube(object(cube, "cube")));
                }
            }
            if (groups.put(name, new RawGroup(name, parent, origin, rotation, cubes)) != null) {
                throw new IllegalArgumentException("duplicate BBS group");
            }
        }
        return groups;
    }

    private static RawCube parseCube(JsonObject object) {
        requireKeys(object, CUBE_KEYS, false, "cube");
        if (!object.has("origin") || !object.has("from")
                || !object.has("size") || !object.has("uvs")) {
            throw new IllegalArgumentException("BBS cube is incomplete");
        }
        Vec3 origin = vector(object.get("origin"), "cube origin", false);
        Vec3 from = vector(object.get("from"), "cube from", false);
        Vec3 size = vector(object.get("size"), "cube size", false);
        if (size.x() < 0D || size.y() < 0D || size.z() < 0D) {
            throw new IllegalArgumentException("negative BBS cube size");
        }
        double offset = object.has("offset") ? number(object.get("offset")) : 0D;
        if (Math.abs(offset) > 16D) {
            throw new IllegalArgumentException("invalid BBS cube offset");
        }
        Vec3 rotation = object.has("rotate")
                ? vector(object.get("rotate"), "cube rotation", true) : ZERO;
        JsonObject uvs = object(object.get("uvs"), "uvs");
        requireKeys(uvs, FACE_KEYS, true, "uvs");
        Map<String, UvRect> faces = new HashMap<>();
        for (String face : FACE_KEYS) {
            JsonArray values = array(uvs.get(face), "face UV");
            if (values.size() != 4) {
                throw new IllegalArgumentException("BBS face UV length changed");
            }
            double u1 = boundedUv(values.get(0));
            double v1 = boundedUv(values.get(1));
            double u2 = boundedUv(values.get(2));
            double v2 = boundedUv(values.get(3));
            faces.put(face, new UvRect(u1, v1, u2, v2));
        }
        return new RawCube(origin, from, size, offset, rotation, Map.copyOf(faces));
    }

    private static void validateHierarchy(Map<String, RawGroup> groups) {
        for (RawGroup group : groups.values()) {
            Set<String> seen = new HashSet<>();
            RawGroup current = group;
            int depth = 0;
            while (current.parent != null) {
                if (!seen.add(current.name) || ++depth > MAX_DEPTH) {
                    throw new IllegalArgumentException("cyclic or deep BBS hierarchy");
                }
                current = groups.get(current.parent);
                if (current == null) {
                    throw new IllegalArgumentException("missing BBS parent");
                }
            }
            if (!seen.add(current.name)) {
                throw new IllegalArgumentException("cyclic BBS hierarchy");
            }
        }
    }

    private static List<RawGroup> parentChain(
            RawGroup group, Map<String, RawGroup> groups
    ) {
        List<RawGroup> result = new ArrayList<>();
        RawGroup current = group;
        while (current != null) {
            result.add(current);
            current = current.parent == null ? null : groups.get(current.parent);
        }
        return result;
    }

    private static void emitCube(
            RawCube cube, List<RawGroup> groupChain, List<Quad> output
    ) {
        double minX = cube.from.x() - cube.offset;
        double minY = cube.from.y() - cube.offset;
        double minZ = cube.from.z() - cube.offset;
        double maxX = cube.from.x() + cube.size.x() + cube.offset;
        double maxY = cube.from.y() + cube.size.y() + cube.offset;
        double maxZ = cube.from.z() + cube.size.z() + cube.offset;
        emit(output, cube, groupChain, "front", false,
                point(maxX, minY, minZ), point(minX, minY, minZ),
                point(minX, maxY, minZ), point(maxX, maxY, minZ));
        emit(output, cube, groupChain, "back", false,
                point(minX, minY, maxZ), point(maxX, minY, maxZ),
                point(maxX, maxY, maxZ), point(minX, maxY, maxZ));
        emit(output, cube, groupChain, "right", true,
                point(maxX, minY, minZ), point(maxX, minY, maxZ),
                point(maxX, maxY, maxZ), point(maxX, maxY, minZ));
        emit(output, cube, groupChain, "left", true,
                point(minX, minY, maxZ), point(minX, minY, minZ),
                point(minX, maxY, minZ), point(minX, maxY, maxZ));
        emit(output, cube, groupChain, "top", false,
                point(minX, maxY, minZ), point(maxX, maxY, minZ),
                point(maxX, maxY, maxZ), point(minX, maxY, maxZ));
        emit(output, cube, groupChain, "bottom", false,
                point(minX, minY, maxZ), point(maxX, minY, maxZ),
                point(maxX, minY, minZ), point(minX, minY, minZ));
    }

    private static void emit(
            List<Quad> output,
            RawCube cube,
            List<RawGroup> chain,
            String face,
            boolean flipU,
            Vec3 first,
            Vec3 second,
            Vec3 third,
            Vec3 fourth
    ) {
        UvRect uv = cube.faces.get(face);
        double u1 = flipU ? uv.u2 : uv.u1;
        double u2 = flipU ? uv.u1 : uv.u2;
        output.add(new Quad(
                vertex(transform(first, cube, chain), u1, uv.v2),
                vertex(transform(second, cube, chain), u2, uv.v2),
                vertex(transform(third, cube, chain), u2, uv.v1),
                vertex(transform(fourth, cube, chain), u1, uv.v1)
        ));
    }

    private static Vec3 transform(Vec3 point, RawCube cube, List<RawGroup> chain) {
        Vec3 transformed = point.rotateAbout(cube.origin, cube.rotation);
        for (RawGroup group : chain) {
            transformed = transformed.rotateAbout(group.origin, group.rotation);
        }
        return new Vec3(
                transformed.x() / 16D + 0.5D,
                transformed.y() / 16D,
                transformed.z() / 16D + 0.5D
        );
    }

    private static Vertex vertex(Vec3 point, double u, double v) {
        return new Vertex(point, (float) (u / 128D), (float) (v / 128D));
    }

    private static Vec3 point(double x, double y, double z) {
        return new Vec3(x, y, z);
    }

    private static Bounds bounds(List<Quad> quads) {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;
        for (Quad quad : quads) {
            for (Vertex vertex : List.of(
                    quad.first(), quad.second(), quad.third(), quad.fourth()
            )) {
                Vec3 point = vertex.position();
                minX = Math.min(minX, point.x());
                minY = Math.min(minY, point.y());
                minZ = Math.min(minZ, point.z());
                maxX = Math.max(maxX, point.x());
                maxY = Math.max(maxY, point.y());
                maxZ = Math.max(maxZ, point.z());
            }
        }
        return new Bounds(new Vec3(minX, minY, minZ), new Vec3(maxX, maxY, maxZ));
    }

    private static JsonObject parseObject(byte[] raw) {
        try {
            JsonElement element = JsonParser.parseReader(
                    new StringReader(new String(raw, StandardCharsets.UTF_8))
            );
            return object(element, "root");
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("malformed BBS JSON", exception);
        }
    }

    private static void requireKeys(
            JsonObject object, Set<String> allowed, boolean exact, String label
    ) {
        if (exact ? !object.keySet().equals(allowed) : !allowed.containsAll(object.keySet())) {
            throw new IllegalArgumentException("unsupported BBS " + label + " fields");
        }
    }

    private static JsonObject object(JsonElement value, String label) {
        if (value == null || !value.isJsonObject()) {
            throw new IllegalArgumentException(label + " must be an object");
        }
        return value.getAsJsonObject();
    }

    private static JsonArray array(JsonElement value, String label) {
        if (value == null || !value.isJsonArray()) {
            throw new IllegalArgumentException(label + " must be an array");
        }
        return value.getAsJsonArray();
    }

    private static String string(JsonElement value) {
        if (value == null || !value.isJsonPrimitive()
                || !value.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException("BBS string is malformed");
        }
        return value.getAsString();
    }

    private static int integer(JsonElement value) {
        double number = number(value);
        if (number != Math.rint(number) || number < Integer.MIN_VALUE
                || number > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("BBS integer is malformed");
        }
        return (int) number;
    }

    private static double number(JsonElement value) {
        try {
            if (value == null || !value.isJsonPrimitive()
                    || !value.getAsJsonPrimitive().isNumber()) {
                throw new IllegalArgumentException("BBS number is malformed");
            }
            double result = value.getAsDouble();
            if (!Double.isFinite(result) || Math.abs(result) > 4096D) {
                throw new IllegalArgumentException("BBS number is out of bounds");
            }
            return result;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("BBS number is malformed", exception);
        }
    }

    private static double boundedUv(JsonElement value) {
        double result = number(value);
        if (result < 0D || result > 128D) {
            throw new IllegalArgumentException("BBS UV is outside the texture");
        }
        return result;
    }

    private static Vec3 vector(JsonElement value, String label, boolean rotation) {
        JsonArray array = array(value, label);
        if (array.size() != 3) {
            throw new IllegalArgumentException(label + " must have three values");
        }
        Vec3 result = new Vec3(number(array.get(0)), number(array.get(1)), number(array.get(2)));
        if (rotation && (Math.abs(result.x()) > 360D || Math.abs(result.y()) > 360D
                || Math.abs(result.z()) > 360D)) {
            throw new IllegalArgumentException("BBS rotation is out of bounds");
        }
        return result;
    }

    private static String digest(byte[] raw) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(raw));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }

    private record RawGroup(
            String name, String parent, Vec3 origin, Vec3 rotation, List<RawCube> cubes
    ) {
        private RawGroup {
            cubes = List.copyOf(cubes);
        }
    }

    private record RawCube(
            Vec3 origin,
            Vec3 from,
            Vec3 size,
            double offset,
            Vec3 rotation,
            Map<String, UvRect> faces
    ) {
    }

    private record UvRect(double u1, double v1, double u2, double v2) {
    }
}
