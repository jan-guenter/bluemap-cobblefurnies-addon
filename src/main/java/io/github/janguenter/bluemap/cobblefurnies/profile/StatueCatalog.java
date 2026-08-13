/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.cobblefurnies.profile;

import de.bluecolored.bluemap.core.util.Key;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

/** Strict loader for the five exact statue identities. */
public final class StatueCatalog {

    private static final int MAX_BYTES = 16 * 1024;

    private StatueCatalog() {
    }

    public static Map<String, StatueDefinition> load(
            String resource, int expectedRows, String expectedSha256
    ) {
        byte[] raw;
        try (InputStream input = StatueCatalog.class.getResourceAsStream(resource)) {
            if (input == null) {
                throw new IllegalStateException("statue catalog is missing");
            }
            raw = input.readNBytes(MAX_BYTES + 1);
        } catch (IOException exception) {
            throw new IllegalStateException("statue catalog is unreadable", exception);
        }
        if (raw.length > MAX_BYTES || !expectedSha256.equals(digest(raw))) {
            throw new IllegalStateException("statue catalog integrity mismatch");
        }
        String text = new String(raw, StandardCharsets.US_ASCII);
        if (!text.endsWith("\n")) {
            throw new IllegalStateException("statue catalog is not LF-terminated");
        }
        Map<String, StatueDefinition> result = new LinkedHashMap<>();
        String previous = null;
        for (String line : text.split("\n", -1)) {
            if (line.isEmpty()) {
                continue;
            }
            String[] fields = line.split("\t", -1);
            if (fields.length != 9) {
                throw new IllegalStateException("statue catalog row shape changed");
            }
            StatueDefinition statue = new StatueDefinition(
                    fields[0], fields[1], fields[2], Key.parse(fields[3]),
                    parse(fields[4]), parse(fields[5]), parse(fields[6]),
                    parse(fields[7]), fields[8]
            );
            if (previous != null && previous.compareTo(statue.blockId()) >= 0) {
                throw new IllegalStateException("statue catalog is not sorted");
            }
            if (result.put(statue.blockId(), statue) != null) {
                throw new IllegalStateException("statue catalog repeats a block");
            }
            previous = statue.blockId();
        }
        if (result.size() != expectedRows) {
            throw new IllegalStateException("statue catalog row count changed");
        }
        return Collections.unmodifiableMap(result);
    }

    private static int parse(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("statue catalog integer is malformed", exception);
        }
    }

    private static String digest(byte[] raw) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(raw));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }
}
