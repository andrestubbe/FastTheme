package fasttheme;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Ultra-fast parser and deserializer for text (.theme) and binary (.themebin) formats,
 * supporting dynamic custom keys, variable aliasing, and embedded presets.
 */
public final class ThemeParser {

    /**
     * Parses a human-readable .theme formatted string into a ThemeData instance,
     * automatically registering any unknown/custom keys on the fly.
     */
    public static ThemeData parseText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return loadDefaultDark();
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
     */
    public static ThemeData loadFromFile(String filePath) throws IOException {
        Path p = Paths.get(filePath);
        byte[] bytes = Files.readAllBytes(p);

        if (filePath.endsWith(".themebin") || (bytes.length >= 4 && ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt() == ThemeData.MAGIC)) {
            return parseBinary(bytes);
        } else {
            String text = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
            return parseText(text);
        }
    }

    // --- Built-in Default Presets ---

    public static ThemeData loadDefaultDark() {
        ThemeData t = new ThemeData("Default Dark");

        int bg = ThemeColorUtil.rgb(30, 30, 30);
        int titleBg = ThemeColorUtil.rgb(24, 24, 24);
        int contentBg = ThemeColorUtil.rgb(38, 38, 38);
        int textPrimary = ThemeColorUtil.rgb(230, 230, 230);
        int textSecondary = ThemeColorUtil.rgb(160, 160, 160);
        int textMuted = ThemeColorUtil.rgb(110, 110, 110);
        int accent = ThemeColorUtil.rgb(0, 122, 204);

        t.set(ThemeKeys.TITLE_BAR_BACKGROUND, titleBg);
        t.set(ThemeKeys.TITLE_BAR_TEXT, textPrimary);
        t.set(ThemeKeys.TITLE_BAR_BORDER, ThemeColorUtil.rgb(45, 45, 45));
        t.set(ThemeKeys.WINDOW_BACKGROUND, bg);
        t.set(ThemeKeys.WINDOW_BORDER, ThemeColorUtil.rgb(60, 60, 60));
        t.set(ThemeKeys.CONTENT_BACKGROUND, contentBg);

        t.set(ThemeKeys.TEXT_PRIMARY, textPrimary);
        t.set(ThemeKeys.TEXT_SECONDARY, textSecondary);
        t.set(ThemeKeys.TEXT_MUTED, textMuted);
        t.set(ThemeKeys.TEXT_PLACEHOLDER, textMuted);
        t.set(ThemeKeys.TEXT_INVERSE, ThemeColorUtil.rgb(20, 20, 20));

        t.set(ThemeKeys.ACCENT_PRIMARY, accent);
        t.set(ThemeKeys.ACCENT_SECONDARY, ThemeColorUtil.rgb(28, 151, 234));
        t.set(ThemeKeys.ACCENT_HOVER, ThemeColorUtil.lighten(accent, 0.15f));
        t.set(ThemeKeys.ACCENT_PRESSED, ThemeColorUtil.darken(accent, 0.15f));

        t.set(ThemeKeys.BUTTON_NORMAL_BACKGROUND, ThemeColorUtil.rgb(48, 48, 48));
        t.set(ThemeKeys.BUTTON_NORMAL_FOREGROUND, textPrimary);
        t.set(ThemeKeys.BUTTON_NORMAL_BORDER, ThemeColorUtil.rgb(65, 65, 65));
        t.set(ThemeKeys.BUTTON_HOVER_BACKGROUND, ThemeColorUtil.rgb(62, 62, 62));
        t.set(ThemeKeys.BUTTON_HOVER_FOREGROUND, ThemeColorUtil.rgb(255, 255, 255));
        t.set(ThemeKeys.BUTTON_HOVER_BORDER, ThemeColorUtil.rgb(85, 85, 85));
        t.set(ThemeKeys.BUTTON_PRESSED_BACKGROUND, ThemeColorUtil.rgb(35, 35, 35));
        t.set(ThemeKeys.BUTTON_PRESSED_FOREGROUND, textPrimary);
        t.set(ThemeKeys.BUTTON_PRESSED_BORDER, ThemeColorUtil.rgb(75, 75, 75));
        t.set(ThemeKeys.BUTTON_DISABLED_BACKGROUND, ThemeColorUtil.rgb(35, 35, 35));
        t.set(ThemeKeys.BUTTON_DISABLED_FOREGROUND, textMuted);

        t.set(ThemeKeys.INPUT_BACKGROUND, ThemeColorUtil.rgb(25, 25, 25));
        t.set(ThemeKeys.INPUT_FOREGROUND, textPrimary);
        t.set(ThemeKeys.INPUT_BORDER, ThemeColorUtil.rgb(60, 60, 60));
        t.set(ThemeKeys.INPUT_BORDER_FOCUS, accent);
        t.set(ThemeKeys.EDITOR_LINE_NUMBER, textMuted);
        t.set(ThemeKeys.EDITOR_SELECTION, ThemeColorUtil.argb(100, 38, 79, 120));

        t.set(ThemeKeys.TAB_BACKGROUND_NORMAL, titleBg);
        t.set(ThemeKeys.TAB_BACKGROUND_SELECTED, contentBg);
        t.set(ThemeKeys.TAB_BACKGROUND_HOVER, ThemeColorUtil.rgb(45, 45, 45));
        t.set(ThemeKeys.TAB_FOREGROUND_NORMAL, textSecondary);
        t.set(ThemeKeys.TAB_FOREGROUND_SELECTED, textPrimary);
        t.set(ThemeKeys.SPLIT_PANE_BAR, ThemeColorUtil.rgb(45, 45, 45));
        t.set(ThemeKeys.SCROLLBAR_THUMB, ThemeColorUtil.rgb(80, 80, 80));
        t.set(ThemeKeys.SCROLLBAR_TRACK, bg);

        t.set(ThemeKeys.STATUS_SUCCESS, ThemeColorUtil.rgb(78, 201, 176));
        t.set(ThemeKeys.STATUS_WARNING, ThemeColorUtil.rgb(220, 160, 60));
        t.set(ThemeKeys.STATUS_ERROR, ThemeColorUtil.rgb(241, 76, 76));
        t.set(ThemeKeys.STATUS_INFO, ThemeColorUtil.rgb(117, 190, 255));

        t.set(ThemeKeys.TOOLTIP_BACKGROUND, ThemeColorUtil.rgb(40, 40, 40));
        t.set(ThemeKeys.TOOLTIP_FOREGROUND, textPrimary);
        t.set(ThemeKeys.POPUP_BACKGROUND, ThemeColorUtil.rgb(35, 35, 35));
        t.set(ThemeKeys.POPUP_BORDER, ThemeColorUtil.rgb(65, 65, 65));
        t.set(ThemeKeys.POPUP_SHADOW, ThemeColorUtil.argb(128, 0, 0, 0));

        return t;
    }

    public static ThemeData loadDefaultLight() {
        ThemeData t = new ThemeData("Default Light");

        int bg = ThemeColorUtil.rgb(245, 245, 245);
        int titleBg = ThemeColorUtil.rgb(230, 230, 230);
        int contentBg = ThemeColorUtil.rgb(255, 255, 255);
        int textPrimary = ThemeColorUtil.rgb(30, 30, 30);
        int textSecondary = ThemeColorUtil.rgb(90, 90, 90);
        int textMuted = ThemeColorUtil.rgb(140, 140, 140);
        int accent = ThemeColorUtil.rgb(0, 120, 215);

        t.set(ThemeKeys.TITLE_BAR_BACKGROUND, titleBg);
        t.set(ThemeKeys.TITLE_BAR_TEXT, textPrimary);
        t.set(ThemeKeys.TITLE_BAR_BORDER, ThemeColorUtil.rgb(215, 215, 215));
        t.set(ThemeKeys.WINDOW_BACKGROUND, bg);
        t.set(ThemeKeys.WINDOW_BORDER, ThemeColorUtil.rgb(200, 200, 200));
        t.set(ThemeKeys.CONTENT_BACKGROUND, contentBg);

        t.set(ThemeKeys.TEXT_PRIMARY, textPrimary);
        t.set(ThemeKeys.TEXT_SECONDARY, textSecondary);
        t.set(ThemeKeys.TEXT_MUTED, textMuted);
        t.set(ThemeKeys.TEXT_PLACEHOLDER, textMuted);
        t.set(ThemeKeys.TEXT_INVERSE, ThemeColorUtil.rgb(250, 250, 250));

        t.set(ThemeKeys.ACCENT_PRIMARY, accent);
        t.set(ThemeKeys.ACCENT_SECONDARY, ThemeColorUtil.rgb(30, 144, 255));
        t.set(ThemeKeys.ACCENT_HOVER, ThemeColorUtil.lighten(accent, 0.15f));
        t.set(ThemeKeys.ACCENT_PRESSED, ThemeColorUtil.darken(accent, 0.15f));

        t.set(ThemeKeys.BUTTON_NORMAL_BACKGROUND, ThemeColorUtil.rgb(240, 240, 240));
        t.set(ThemeKeys.BUTTON_NORMAL_FOREGROUND, textPrimary);
        t.set(ThemeKeys.BUTTON_NORMAL_BORDER, ThemeColorUtil.rgb(205, 205, 205));
        t.set(ThemeKeys.BUTTON_HOVER_BACKGROUND, ThemeColorUtil.rgb(225, 225, 225));
        t.set(ThemeKeys.BUTTON_HOVER_FOREGROUND, ThemeColorUtil.rgb(0, 0, 0));
        t.set(ThemeKeys.BUTTON_HOVER_BORDER, ThemeColorUtil.rgb(180, 180, 180));
        t.set(ThemeKeys.BUTTON_PRESSED_BACKGROUND, ThemeColorUtil.rgb(210, 210, 210));
        t.set(ThemeKeys.BUTTON_PRESSED_FOREGROUND, textPrimary);
        t.set(ThemeKeys.BUTTON_PRESSED_BORDER, ThemeColorUtil.rgb(160, 160, 160));
        t.set(ThemeKeys.BUTTON_DISABLED_BACKGROUND, ThemeColorUtil.rgb(235, 235, 235));
        t.set(ThemeKeys.BUTTON_DISABLED_FOREGROUND, textMuted);

        t.set(ThemeKeys.INPUT_BACKGROUND, ThemeColorUtil.rgb(255, 255, 255));
        t.set(ThemeKeys.INPUT_FOREGROUND, textPrimary);
        t.set(ThemeKeys.INPUT_BORDER, ThemeColorUtil.rgb(200, 200, 200));
        t.set(ThemeKeys.INPUT_BORDER_FOCUS, accent);
        t.set(ThemeKeys.EDITOR_LINE_NUMBER, textMuted);
        t.set(ThemeKeys.EDITOR_SELECTION, ThemeColorUtil.argb(100, 173, 214, 255));

        t.set(ThemeKeys.TAB_BACKGROUND_NORMAL, titleBg);
        t.set(ThemeKeys.TAB_BACKGROUND_SELECTED, contentBg);
        t.set(ThemeKeys.TAB_BACKGROUND_HOVER, ThemeColorUtil.rgb(220, 220, 220));
        t.set(ThemeKeys.TAB_FOREGROUND_NORMAL, textSecondary);
        t.set(ThemeKeys.TAB_FOREGROUND_SELECTED, textPrimary);
        t.set(ThemeKeys.SPLIT_PANE_BAR, ThemeColorUtil.rgb(220, 220, 220));
        t.set(ThemeKeys.SCROLLBAR_THUMB, ThemeColorUtil.rgb(180, 180, 180));
        t.set(ThemeKeys.SCROLLBAR_TRACK, bg);

        t.set(ThemeKeys.STATUS_SUCCESS, ThemeColorUtil.rgb(40, 167, 69));
        t.set(ThemeKeys.STATUS_WARNING, ThemeColorUtil.rgb(255, 153, 0));
        t.set(ThemeKeys.STATUS_ERROR, ThemeColorUtil.rgb(220, 53, 69));
        t.set(ThemeKeys.STATUS_INFO, ThemeColorUtil.rgb(23, 162, 184));

        t.set(ThemeKeys.TOOLTIP_BACKGROUND, ThemeColorUtil.rgb(250, 250, 250));
        t.set(ThemeKeys.TOOLTIP_FOREGROUND, textPrimary);
        t.set(ThemeKeys.POPUP_BACKGROUND, ThemeColorUtil.rgb(255, 255, 255));
        t.set(ThemeKeys.POPUP_BORDER, ThemeColorUtil.rgb(210, 210, 210));
        t.set(ThemeKeys.POPUP_SHADOW, ThemeColorUtil.argb(64, 0, 0, 0));

        return t;
    }

    public static ThemeData loadDefaultCream() {
        ThemeData t = new ThemeData("Cream Synthwave");

        int bg = 0xFF13141f; // Obsidian Blue / Midnight Indigo
        int titleBg = 0xFF0c0e10; // Blackened Navy
        int contentBg = 0xFF171a29; // Shadow Navy
        int textPrimary = 0xFFc3cdf7;
        int textSecondary = 0xFF95a8f1;
        int textMuted = 0xFF596491;
        int accentCyan = 0xFF00e0ff; // Electric Cyan
        int accentMagenta = 0xFFe40373; // Neon Magenta

        t.set(ThemeKeys.TITLE_BAR_BACKGROUND, titleBg);
        t.set(ThemeKeys.TITLE_BAR_TEXT, textSecondary);
        t.set(ThemeKeys.TITLE_BAR_BORDER, 0xFF22293e);
        t.set(ThemeKeys.WINDOW_BACKGROUND, bg);
        t.set(ThemeKeys.WINDOW_BORDER, 0xFF373e59);
        t.set(ThemeKeys.CONTENT_BACKGROUND, contentBg);

        t.set(ThemeKeys.TEXT_PRIMARY, textPrimary);
        t.set(ThemeKeys.TEXT_SECONDARY, textSecondary);
        t.set(ThemeKeys.TEXT_MUTED, textMuted);
        t.set(ThemeKeys.TEXT_PLACEHOLDER, textMuted);
        t.set(ThemeKeys.TEXT_INVERSE, 0xFF0c0e10);

        t.set(ThemeKeys.ACCENT_PRIMARY, accentCyan);
        t.set(ThemeKeys.ACCENT_SECONDARY, accentMagenta);
        t.set(ThemeKeys.ACCENT_HOVER, ThemeColorUtil.lighten(accentCyan, 0.2f));
        t.set(ThemeKeys.ACCENT_PRESSED, ThemeColorUtil.darken(accentCyan, 0.2f));

        t.set(ThemeKeys.BUTTON_NORMAL_BACKGROUND, 0xFF1e283a);
        t.set(ThemeKeys.BUTTON_NORMAL_FOREGROUND, textPrimary);
        t.set(ThemeKeys.BUTTON_NORMAL_BORDER, 0xFF373e59);
        t.set(ThemeKeys.BUTTON_HOVER_BACKGROUND, 0xFF24293e);
        t.set(ThemeKeys.BUTTON_HOVER_FOREGROUND, 0xFFFFFFFF);
        t.set(ThemeKeys.BUTTON_HOVER_BORDER, accentCyan);
        t.set(ThemeKeys.BUTTON_PRESSED_BACKGROUND, 0xFF13141f);
        t.set(ThemeKeys.BUTTON_PRESSED_FOREGROUND, accentCyan);
        t.set(ThemeKeys.BUTTON_PRESSED_BORDER, accentCyan);
        t.set(ThemeKeys.BUTTON_DISABLED_BACKGROUND, 0xFF111727);
        t.set(ThemeKeys.BUTTON_DISABLED_FOREGROUND, textMuted);

        t.set(ThemeKeys.INPUT_BACKGROUND, 0xFF0c0e10);
        t.set(ThemeKeys.INPUT_FOREGROUND, textPrimary);
        t.set(ThemeKeys.INPUT_BORDER, 0xFF373e59);
        t.set(ThemeKeys.INPUT_BORDER_FOCUS, accentCyan);
        t.set(ThemeKeys.EDITOR_LINE_NUMBER, textMuted);
        t.set(ThemeKeys.EDITOR_SELECTION, ThemeColorUtil.argb(120, 228, 3, 115));

        t.set(ThemeKeys.TAB_BACKGROUND_NORMAL, titleBg);
        t.set(ThemeKeys.TAB_BACKGROUND_SELECTED, contentBg);
        t.set(ThemeKeys.TAB_BACKGROUND_HOVER, 0xFF1e283a);
        t.set(ThemeKeys.TAB_FOREGROUND_NORMAL, textMuted);
        t.set(ThemeKeys.TAB_FOREGROUND_SELECTED, accentCyan);
        t.set(ThemeKeys.SPLIT_PANE_BAR, 0xFF22293e);
        t.set(ThemeKeys.SCROLLBAR_THUMB, 0xFF373e59);
        t.set(ThemeKeys.SCROLLBAR_TRACK, bg);

        t.set(ThemeKeys.STATUS_SUCCESS, 0xFF4acdb8);
        t.set(ThemeKeys.STATUS_WARNING, 0xFFf0be75);
        t.set(ThemeKeys.STATUS_ERROR, accentMagenta);
        t.set(ThemeKeys.STATUS_INFO, accentCyan);

        t.set(ThemeKeys.TOOLTIP_BACKGROUND, 0xFF171a29);
        t.set(ThemeKeys.TOOLTIP_FOREGROUND, 0xFFFFFFFF);
        t.set(ThemeKeys.POPUP_BACKGROUND, 0xFF171a29);
        t.set(ThemeKeys.POPUP_BORDER, 0xFF373e59);
        t.set(ThemeKeys.POPUP_SHADOW, ThemeColorUtil.argb(180, 0, 0, 0));

        return t;
    }

    private ThemeParser() {}
}
