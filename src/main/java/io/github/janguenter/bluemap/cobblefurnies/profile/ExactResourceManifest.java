/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.cobblefurnies.profile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

/** Hash-locked active resource roster; contains no third-party bytes. */
public final class ExactResourceManifest {

    private static final int MAX_BYTES = 64 * 1024;

    private ExactResourceManifest() {
    }

    public static Map<String, Identity> load(
            String resource, int expectedRows, String expectedSha256
    ) {
        byte[] raw;
        try (InputStream input = ExactResourceManifest.class.getResourceAsStream(resource)) {
            if (input == null) {
                throw new IllegalStateException("resource manifest is missing");
            }
            raw = input.readNBytes(MAX_BYTES + 1);
        } catch (IOException exception) {
            throw new IllegalStateException("resource manifest is unreadable", exception);
        }
        if (raw.length > MAX_BYTES || !expectedSha256.equals(digest(raw))) {
            throw new IllegalStateException("resource manifest integrity mismatch");
        }
        Map<String, Identity> result = new LinkedHashMap<>();
        String previous = null;
        String text = new String(raw, StandardCharsets.US_ASCII);
        for (String line : text.split("\n", -1)) {
            if (line.isEmpty()) {
                continue;
            }
            String[] fields = line.split("\t", -1);
            if (fields.length != 3 || !fields[0].startsWith("assets/cobblefurnies/")) {
                throw new IllegalStateException("resource manifest row is malformed");
            }
            int size;
            try {
                size = Integer.parseInt(fields[1]);
            } catch (NumberFormatException exception) {
                throw new IllegalStateException("resource size is malformed", exception);
            }
            Identity identity = new Identity(size, fields[2]);
            if (previous != null && previous.compareTo(fields[0]) >= 0) {
                throw new IllegalStateException("resource manifest is not sorted");
            }
            if (result.put(fields[0], identity) != null) {
                throw new IllegalStateException("resource manifest repeats a path");
            }
            previous = fields[0];
        }
        if (result.size() != expectedRows) {
            throw new IllegalStateException("resource manifest row count changed");
        }
        return Collections.unmodifiableMap(result);
    }

    private static String digest(byte[] raw) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(raw));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }

    public record Identity(int size, String sha256) {
        public Identity {
            if (size < 1 || size > 1024 * 1024 || !sha256.matches("[0-9a-f]{64}")) {
                throw new IllegalArgumentException("invalid resource identity");
            }
        }
    }
}
