package fasttheme;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Universal, schema-free parser and deserializer for text (.theme) and binary (.themebin) formats.
 * Automatically allocates dynamic slots for any keys defined in the input and resolves variable aliases.
 */
public final class ThemeParser {

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================

    private ThemeParser() {}

    // =========================================================================
    // METHODS (Actions & Operations)
    // =========================================================================

    /**
     * Parses a human-readable .theme formatted string into a ThemeData instance,
     * automatically registering any encountered keys on the fly.
     *
     * @param text Raw .theme formatted string content.
     * @return Fully populated {@link ThemeData} instance.
     */
    public static ThemeData parseText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ThemeData("Empty");
        }

        String themeName = "CustomTheme";
        Map<String, String> rawMap = new HashMap<>(64);

        String[] lines = text.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("//")) {
                continue;
            }

            int eqIdx = trimmed.indexOf('=');
            if (eqIdx == -1) continue;

            String key = trimmed.substring(0, eqIdx).trim();
            String val = trimmed.substring(eqIdx + 1).trim();

            if (key.equalsIgnoreCase("THEME") || key.equalsIgnoreCase("NAME")) {
                themeName = val;
            } else {
                rawMap.put(key.toUpperCase(), val);
            }
        }

        ThemeData theme = new ThemeData(themeName);

        // Pass 1: Parse direct colors and auto-register custom keys
        Map<String, Integer> resolvedColors = new HashMap<>(rawMap.size() * 2);
        for (Map.Entry<String, String> entry : rawMap.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue();

            if (!val.startsWith("@")) {
                int parsed = ThemeColorUtil.parseColor(val);
                resolvedColors.put(key, parsed);
                theme.set(key, parsed);
            }
        }

        // Pass 2: Iterative multi-hop alias resolution (e.g. A = @B, B = @C)
        Map<String, String> pendingAliases = new HashMap<>();
        for (Map.Entry<String, String> entry : rawMap.entrySet()) {
            if (entry.getValue().startsWith("@")) {
                pendingAliases.put(entry.getKey(), entry.getValue().substring(1).trim().toUpperCase());
            }
        }

        boolean progress = true;
        while (!pendingAliases.isEmpty() && progress) {
            progress = false;
            java.util.Iterator<Map.Entry<String, String>> it = pendingAliases.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> aliasEntry = it.next();
                String targetKey = aliasEntry.getValue();
                if (resolvedColors.containsKey(targetKey)) {
                    int resolvedColor = resolvedColors.get(targetKey);
                    String srcKey = aliasEntry.getKey();
                    resolvedColors.put(srcKey, resolvedColor);
                    theme.set(srcKey, resolvedColor);
                    it.remove();
                    progress = true;
                }
            }
        }

        // Any leftover aliases are cyclic or refer to missing keys
        for (Map.Entry<String, String> unresolved : pendingAliases.entrySet()) {
            System.err.println("[FastTheme] Warning: Unresolved or cyclic alias @" + unresolved.getValue() + " for key " + unresolved.getKey());
        }

        return theme;
    }

    /**
     * Deserializes a binary .themebin payload into a ThemeData instance.
     * Supports V2 self-describing key names and V1 legacy format.
     *
     * @param bytes Serialized byte array.
     * @return Fully populated {@link ThemeData} instance.
     */
    public static ThemeData parseBinary(byte[] bytes) {
        if (bytes == null || bytes.length < 8) {
            throw new IllegalArgumentException("Invalid binary theme data: payload too short");
        }

        ByteBuffer buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        int magic = buf.getInt();
        if (magic != ThemeData.MAGIC) {
            throw new IllegalArgumentException(String.format("Invalid theme magic: 0x%08X (expected 0x%08X)", magic, ThemeData.MAGIC));
        }

        short version = buf.getShort();
        if (version != 1 && version != 2) {
            throw new IllegalArgumentException("Unsupported theme binary version: " + version);
        }

        short nameLen = buf.getShort();
        if (nameLen < 0 || buf.remaining() < nameLen + 2) {
            throw new IllegalArgumentException("Corrupt theme binary: invalid name length (" + nameLen + ")");
        }
        byte[] nameBytes = new byte[nameLen];
        buf.get(nameBytes);
        String name = new String(nameBytes, java.nio.charset.StandardCharsets.UTF_8);

        short slotCount = buf.getShort();
        if (slotCount < 0) {
            throw new IllegalArgumentException("Corrupt theme binary: invalid slot count (" + slotCount + ")");
        }

        ThemeData theme = new ThemeData(name, slotCount);

        if (version == 2) {
            // V2: Self-describing key name + ARGB per slot
            for (int i = 0; i < slotCount; i++) {
                if (buf.remaining() < 2) {
                    throw new IllegalArgumentException("Corrupt V2 theme binary: truncated key length at slot " + i);
                }
                short keyLen = buf.getShort();
                if (keyLen < 0 || buf.remaining() < keyLen + 4) {
                    throw new IllegalArgumentException("Corrupt V2 theme binary: truncated key data at slot " + i);
                }
                String keyName = null;
                if (keyLen > 0) {
                    byte[] kb = new byte[keyLen];
                    buf.get(kb);
                    keyName = new String(kb, java.nio.charset.StandardCharsets.UTF_8);
                }
                int argb = buf.getInt();
                if (keyName != null && !keyName.isEmpty()) {
                    theme.set(keyName, argb);
                } else {
                    theme.set(i, argb);
                }
            }
        } else {
            // V1: Legacy raw slot positions
            if (buf.remaining() < slotCount * 4) {
                throw new IllegalArgumentException("Corrupt V1 theme binary: payload shorter than slot array (" + buf.remaining() + " < " + (slotCount * 4) + ")");
            }
            for (int i = 0; i < slotCount; i++) {
                theme.set(i, buf.getInt());
            }
        }

        return theme;
    }

    /**
     * Loads and auto-detects either a .theme or .themebin file from a Path.
     *
     * @param path File system Path.
     * @return Populated {@link ThemeData}.
     * @throws IOException If file reading fails.
     */
    public static ThemeData loadFromFile(Path path) throws IOException {
        byte[] allBytes = Files.readAllBytes(path);
        if (allBytes.length >= 4) {
            int magic = ((allBytes[0] & 0xFF)) |
                        ((allBytes[1] & 0xFF) << 8) |
                        ((allBytes[2] & 0xFF) << 16) |
                        ((allBytes[3] & 0xFF) << 24);
            if (magic == ThemeData.MAGIC) {
                return parseBinary(allBytes);
            }
        }
        return parseText(new String(allBytes, java.nio.charset.StandardCharsets.UTF_8));
    }

    /**
     * Loads and auto-detects either a .theme or .themebin file from a File instance.
     *
     * @param file File instance.
     * @return Populated {@link ThemeData}.
     * @throws IOException If file reading fails.
     */
    public static ThemeData loadFromFile(File file) throws IOException {
        return loadFromFile(file.toPath());
    }

    /**
     * Loads and auto-detects either a .theme or .themebin file from a string file path.
     *
     * @param filePath String path.
     * @return Populated {@link ThemeData}.
     * @throws IOException If file reading fails.
     */
    public static ThemeData loadFromFile(String filePath) throws IOException {
        return loadFromFile(Paths.get(filePath));
    }
}
