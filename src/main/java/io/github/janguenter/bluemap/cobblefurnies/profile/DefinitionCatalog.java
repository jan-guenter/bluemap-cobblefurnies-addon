/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.cobblefurnies.profile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/** Strict immutable loader for the packaged 32-row Athena catalog. */
public final class DefinitionCatalog {

    private static final int MAX_BYTES = 64 * 1024;
    private final Map<String, CobbleFurniesDefinition> definitions;
    private final Set<String> textureIds;

    private DefinitionCatalog(
            Map<String, CobbleFurniesDefinition> definitions,
            Set<String> textureIds
    ) {
        this.definitions = Collections.unmodifiableMap(new LinkedHashMap<>(definitions));
        this.textureIds = Collections.unmodifiableSet(new TreeSet<>(textureIds));
    }

    public static DefinitionCatalog load(String resource, int expectedRows, String sha256) {
        byte[] raw;
        try (InputStream input = DefinitionCatalog.class.getResourceAsStream(resource)) {
            if (input == null) {
                throw new IllegalStateException("definition catalog is missing");
            }
            raw = input.readNBytes(MAX_BYTES + 1);
        } catch (IOException exception) {
            throw new IllegalStateException("definition catalog is unreadable", exception);
        }
        if (raw.length > MAX_BYTES || !sha256.equals(digest(raw))) {
            throw new IllegalStateException("definition catalog integrity mismatch");
        }
        String text = new String(raw, StandardCharsets.US_ASCII);
        if (!text.endsWith("\n")) {
            throw new IllegalStateException("definition catalog is not LF-terminated");
        }
        Map<String, CobbleFurniesDefinition> result = new LinkedHashMap<>();
        Set<String> textures = new TreeSet<>();
        String previous = null;
        for (String line : text.split("\n", -1)) {
            if (line.isEmpty()) {
                continue;
            }
            String[] fields = line.split("\t", -1);
            if (fields.length != 7) {
                throw new IllegalStateException("definition catalog row shape changed");
            }
            CobbleFurniesDefinition definition = new CobbleFurniesDefinition(
                    fields[0], LoaderFamily.parse(fields[1]),
                    List.copyOf(Arrays.asList(fields).subList(2, 7))
            );
            if (previous != null && previous.compareTo(definition.blockId()) >= 0) {
                throw new IllegalStateException("definition catalog is not sorted");
            }
            if (result.put(definition.blockId(), definition) != null) {
                throw new IllegalStateException("definition catalog repeats a block");
            }
            textures.addAll(definition.textures());
            previous = definition.blockId();
        }
        if (result.size() != expectedRows) {
            throw new IllegalStateException("definition catalog row count changed");
        }
        return new DefinitionCatalog(result, textures);
    }

    public Map<String, CobbleFurniesDefinition> definitions() {
        return definitions;
    }

    public Set<String> textureIds() {
        return textureIds;
    }

    private static String digest(byte[] raw) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(raw));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }
}
