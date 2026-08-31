/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.cobblefurnies.adapter.bluemap523;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import io.github.janguenter.bluemap.cobblefurnies.profile.ExactResourceManifest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

/** Captures and verifies the exact active resource closure during one reload. */
final class ActiveResourceLoader {

    private static final int MAX_ROOTS = 4_096;

    private ActiveResourceLoader() {
    }

    static Result load(
            ResourcePack resourcePack,
            Iterable<Path> roots,
            Map<String, ExactResourceManifest.Identity> manifest
    ) throws IOException, InterruptedException {
        Map<String, byte[]> captured = new HashMap<>();
        int rootCount = 0;
        for (Path root : roots) {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            if (++rootCount > MAX_ROOTS) {
                return Result.invalid("active-resource-root-budget");
            }
            resourcePack.loadResourcePath(root, activeRoot ->
                    collect(activeRoot, manifest, captured));
        }
        if (captured.size() != manifest.size()) {
            return Result.invalid("active-resource-roster-mismatch");
        }
        for (Map.Entry<String, ExactResourceManifest.Identity> entry : manifest.entrySet()) {
            byte[] raw = captured.get(entry.getKey());
            ExactResourceManifest.Identity identity = entry.getValue();
            if (raw == null || raw.length != identity.size()
                    || !identity.sha256().equals(digest(raw))) {
                return Result.invalid("active-resource-hash-drift");
            }
        }
        Map<String, byte[]> models = new LinkedHashMap<>();
        captured.entrySet().stream()
                .filter(entry -> entry.getKey().endsWith(".bbs.json"))
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> models.put(entry.getKey(), entry.getValue().clone()));
        return Result.success(Map.copyOf(models));
    }

    private static void collect(
            Path root,
            Map<String, ExactResourceManifest.Identity> requested,
            Map<String, byte[]> output
    ) throws IOException {
        for (Map.Entry<String, ExactResourceManifest.Identity> entry : requested.entrySet()) {
            if (output.containsKey(entry.getKey())) {
                continue;
            }
            Path resource = root.resolve(entry.getKey());
            if (!Files.isRegularFile(resource)) {
                continue;
            }
            int limit = entry.getValue().size();
            long size = Files.size(resource);
            if (size != limit) {
                output.put(entry.getKey(), new byte[0]);
                continue;
            }
            byte[] raw = Files.readAllBytes(resource);
            output.put(entry.getKey(), raw.length == limit ? raw : new byte[0]);
        }
    }

    private static String digest(byte[] raw) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(raw));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }

    record Result(boolean valid, String reason, Map<String, byte[]> models) {
        Result {
            models = Map.copyOf(models);
        }

        private static Result success(Map<String, byte[]> models) {
            return new Result(true, "exact-active-resources", models);
        }

        private static Result invalid(String reason) {
            return new Result(false, reason, Map.of());
        }
    }
}
