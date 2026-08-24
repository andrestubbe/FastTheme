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

    private ThemeParser() {}

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
                int c = ThemeColorUtil.parseColor(val);
                resolvedColors.put(key, c);
                int slot = ThemeKeys.getOrRegister(key);
                theme.set(slot, c);
            }
        }

        // Pass 2: Resolve @KEY aliases
        for (Map.Entry<String, String> entry : rawMap.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue();
            if (val.startsWith("@")) {
                String aliasKey = val.substring(1).trim().toUpperCase();
                Integer resolved = resolvedColors.get(aliasKey);
                if (resolved == null) {
                    int aliasSlot = ThemeKeys.indexOf(aliasKey);
                    if (aliasSlot != -1) {
                        resolved = theme.get(aliasSlot);
                    }
                }

                if (resolved != null) {
                    resolvedColors.put(key, resolved);
                    int targetSlot = ThemeKeys.getOrRegister(key);
                    theme.set(targetSlot, resolved);
                }
            }
        }

        return theme;
    }

    /**
     * Parses a binary .themebin byte payload into a ThemeData instance.
     *
     * @param bytes Serialized byte array containing the binary format.
     * @return Deserialized {@link ThemeData} instance.
     */
    public static ThemeData parseBinary(byte[] bytes) {
        if (bytes == null || bytes.length < 10) {
            throw new IllegalArgumentException("Invalid binary theme data");
        }

        ByteBuffer buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        int magic = buf.getInt();
        if (magic != ThemeData.MAGIC) {
            throw new IllegalArgumentException("Invalid magic header for .themebin");
        }

        short version = buf.getShort();
        if (version > ThemeData.FORMAT_VERSION) {
            throw new IllegalArgumentException("Unsupported .themebin format version: " + version);
        }

        short nameLen = buf.getShort();
        byte[] nameBytes = new byte[nameLen];
        buf.get(nameBytes);
        String name = new String(nameBytes, java.nio.charset.StandardCharsets.UTF_8);

        short slotCount = buf.getShort();
        ThemeData theme = new ThemeData(name, slotCount);
        for (int i = 0; i < slotCount; i++) {
            int val = buf.getInt();
            theme.set(i, val);
        }

        return theme;
    }

    /**
     * Loads a theme from a file path (supports both .theme text and .themebin binary).
     *
     * @param filePath Absolute or relative path to the theme file.
     * @return Loaded {@link ThemeData} instance.
     * @throws IOException If an I/O error occurs reading the file.
     */
    public static ThemeData loadFromFile(String filePath) throws IOException {
        if (filePath == null) throw new IllegalArgumentException("File path cannot be null");
        return loadFromFile(Paths.get(filePath));
    }

    /**
     * Loads a theme from a Path.
     *
     * @param path Path to the theme file.
     * @return Loaded {@link ThemeData} instance.
     * @throws IOException If an I/O error occurs reading the file.
     */
    public static ThemeData loadFromFile(Path path) throws IOException {
        byte[] bytes = Files.readAllBytes(path);
        String fileName = path.getFileName().toString().toLowerCase();

        if (fileName.endsWith(".themebin") || (bytes.length >= 4 && ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt() == ThemeData.MAGIC)) {
            return parseBinary(bytes);
        } else {
            String text = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
            return parseText(text);
        }
    }

    /**
     * Loads a theme from a File.
     *
     * @param file Theme file.
     * @return Loaded {@link ThemeData} instance.
     * @throws IOException If an I/O error occurs reading the file.
     */
    public static ThemeData loadFromFile(File file) throws IOException {
        if (file == null) throw new IllegalArgumentException("File cannot be null");
        return loadFromFile(file.toPath());
    }
}
