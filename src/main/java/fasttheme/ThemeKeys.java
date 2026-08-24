package fasttheme;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * High-performance, dynamic Key Matrix defining slot indices and names for FastTheme.
 * Supports standard predefined slots (0..48) as well as on-the-fly registration of arbitrary custom keys.
 */
public final class ThemeKeys {

    // --- Standard Window & Frame Slots (0..5) ---
    public static final int TITLE_BAR_BACKGROUND = 0;
    public static final int TITLE_BAR_TEXT = 1;
    public static final int TITLE_BAR_BORDER = 2;
    public static final int WINDOW_BACKGROUND = 3;
    public static final int WINDOW_BORDER = 4;
    public static final int CONTENT_BACKGROUND = 5;

    // --- Standard Typography Slots (6..10) ---
    public static final int TEXT_PRIMARY = 6;
    public static final int TEXT_SECONDARY = 7;
    public static final int TEXT_MUTED = 8;
    public static final int TEXT_PLACEHOLDER = 9;
    public static final int TEXT_INVERSE = 10;

    // --- Standard Accent Slots (11..14) ---
    public static final int ACCENT_PRIMARY = 11;
    public static final int ACCENT_SECONDARY = 12;
    public static final int ACCENT_HOVER = 13;
    public static final int ACCENT_PRESSED = 14;

    // --- Standard Button Slots (15..25) ---
    public static final int BUTTON_NORMAL_BACKGROUND = 15;
    public static final int BUTTON_NORMAL_FOREGROUND = 16;
    public static final int BUTTON_NORMAL_BORDER = 17;
    public static final int BUTTON_HOVER_BACKGROUND = 18;
    public static final int BUTTON_HOVER_FOREGROUND = 19;
    public static final int BUTTON_HOVER_BORDER = 20;
    public static final int BUTTON_PRESSED_BACKGROUND = 21;
    public static final int BUTTON_PRESSED_FOREGROUND = 22;
    public static final int BUTTON_PRESSED_BORDER = 23;
    public static final int BUTTON_DISABLED_BACKGROUND = 24;
    public static final int BUTTON_DISABLED_FOREGROUND = 25;

    // --- Standard Input & Editor Slots (26..31) ---
    public static final int INPUT_BACKGROUND = 26;
    public static final int INPUT_FOREGROUND = 27;
    public static final int INPUT_BORDER = 28;
    public static final int INPUT_BORDER_FOCUS = 29;
    public static final int EDITOR_LINE_NUMBER = 30;
    public static final int EDITOR_SELECTION = 31;

    // --- Standard Navigation, Tabs & Splits (32..39) ---
    public static final int TAB_BACKGROUND_NORMAL = 32;
    public static final int TAB_BACKGROUND_SELECTED = 33;
    public static final int TAB_BACKGROUND_HOVER = 34;
    public static final int TAB_FOREGROUND_NORMAL = 35;
    public static final int TAB_FOREGROUND_SELECTED = 36;
    public static final int SPLIT_PANE_BAR = 37;
    public static final int SCROLLBAR_THUMB = 38;
    public static final int SCROLLBAR_TRACK = 39;

    // --- Standard Status Slots (40..43) ---
    public static final int STATUS_SUCCESS = 40;
    public static final int STATUS_WARNING = 41;
    public static final int STATUS_ERROR = 42;
    public static final int STATUS_INFO = 43;

    // --- Standard Popups & Tooltips (44..48) ---
    public static final int TOOLTIP_BACKGROUND = 44;
    public static final int TOOLTIP_FOREGROUND = 45;
    public static final int POPUP_BACKGROUND = 46;
    public static final int POPUP_BORDER = 47;
    public static final int POPUP_SHADOW = 48;

    public static final int STANDARD_COUNT = 49;

    private static final CopyOnWriteArrayList<String> NAMES = new CopyOnWriteArrayList<>();
    private static final ConcurrentHashMap<String, Integer> NAME_TO_INDEX = new ConcurrentHashMap<>(128);

    static {
        registerStandard(TITLE_BAR_BACKGROUND, "TITLE_BAR_BACKGROUND");
        registerStandard(TITLE_BAR_TEXT, "TITLE_BAR_TEXT");
        registerStandard(TITLE_BAR_BORDER, "TITLE_BAR_BORDER");
        registerStandard(WINDOW_BACKGROUND, "WINDOW_BACKGROUND");
        registerStandard(WINDOW_BORDER, "WINDOW_BORDER");
        registerStandard(CONTENT_BACKGROUND, "CONTENT_BACKGROUND");

        registerStandard(TEXT_PRIMARY, "TEXT_PRIMARY");
        registerStandard(TEXT_SECONDARY, "TEXT_SECONDARY");
        registerStandard(TEXT_MUTED, "TEXT_MUTED");
        registerStandard(TEXT_PLACEHOLDER, "TEXT_PLACEHOLDER");
        registerStandard(TEXT_INVERSE, "TEXT_INVERSE");

        registerStandard(ACCENT_PRIMARY, "ACCENT_PRIMARY");
        registerStandard(ACCENT_SECONDARY, "ACCENT_SECONDARY");
        registerStandard(ACCENT_HOVER, "ACCENT_HOVER");
        registerStandard(ACCENT_PRESSED, "ACCENT_PRESSED");

        registerStandard(BUTTON_NORMAL_BACKGROUND, "BUTTON_NORMAL_BACKGROUND");
        registerStandard(BUTTON_NORMAL_FOREGROUND, "BUTTON_NORMAL_FOREGROUND");
        registerStandard(BUTTON_NORMAL_BORDER, "BUTTON_NORMAL_BORDER");
        registerStandard(BUTTON_HOVER_BACKGROUND, "BUTTON_HOVER_BACKGROUND");
        registerStandard(BUTTON_HOVER_FOREGROUND, "BUTTON_HOVER_FOREGROUND");
        registerStandard(BUTTON_HOVER_BORDER, "BUTTON_HOVER_BORDER");
        registerStandard(BUTTON_PRESSED_BACKGROUND, "BUTTON_PRESSED_BACKGROUND");
        registerStandard(BUTTON_PRESSED_FOREGROUND, "BUTTON_PRESSED_FOREGROUND");
        registerStandard(BUTTON_PRESSED_BORDER, "BUTTON_PRESSED_BORDER");
        registerStandard(BUTTON_DISABLED_BACKGROUND, "BUTTON_DISABLED_BACKGROUND");
        registerStandard(BUTTON_DISABLED_FOREGROUND, "BUTTON_DISABLED_FOREGROUND");

        registerStandard(INPUT_BACKGROUND, "INPUT_BACKGROUND");
        registerStandard(INPUT_FOREGROUND, "INPUT_FOREGROUND");
        registerStandard(INPUT_BORDER, "INPUT_BORDER");
        registerStandard(INPUT_BORDER_FOCUS, "INPUT_BORDER_FOCUS");
        registerStandard(EDITOR_LINE_NUMBER, "EDITOR_LINE_NUMBER");
        registerStandard(EDITOR_SELECTION, "EDITOR_SELECTION");

        registerStandard(TAB_BACKGROUND_NORMAL, "TAB_BACKGROUND_NORMAL");
        registerStandard(TAB_BACKGROUND_SELECTED, "TAB_BACKGROUND_SELECTED");
        registerStandard(TAB_BACKGROUND_HOVER, "TAB_BACKGROUND_HOVER");
        registerStandard(TAB_FOREGROUND_NORMAL, "TAB_FOREGROUND_NORMAL");
        registerStandard(TAB_FOREGROUND_SELECTED, "TAB_FOREGROUND_SELECTED");
        registerStandard(SPLIT_PANE_BAR, "SPLIT_PANE_BAR");
        registerStandard(SCROLLBAR_THUMB, "SCROLLBAR_THUMB");
        registerStandard(SCROLLBAR_TRACK, "SCROLLBAR_TRACK");

        registerStandard(STATUS_SUCCESS, "STATUS_SUCCESS");
        registerStandard(STATUS_WARNING, "STATUS_WARNING");
        registerStandard(STATUS_ERROR, "STATUS_ERROR");
        registerStandard(STATUS_INFO, "STATUS_INFO");

        registerStandard(TOOLTIP_BACKGROUND, "TOOLTIP_BACKGROUND");
        registerStandard(TOOLTIP_FOREGROUND, "TOOLTIP_FOREGROUND");
        registerStandard(POPUP_BACKGROUND, "POPUP_BACKGROUND");
        registerStandard(POPUP_BORDER, "POPUP_BORDER");
        registerStandard(POPUP_SHADOW, "POPUP_SHADOW");
    }

    private static void registerStandard(int expectedIndex, String name) {
        NAMES.add(name);
        NAME_TO_INDEX.put(name.toUpperCase(), expectedIndex);
    }

    /**
     * Registers a custom key name dynamically and returns its unique allocated slot ID.
     * Thread-safe. If the key is already registered, returns the existing slot ID.
     */
    public static synchronized int register(String keyName) {
        if (keyName == null || keyName.trim().isEmpty()) {
            throw new IllegalArgumentException("Key name cannot be null or empty");
        }
        String normalized = keyName.trim().toUpperCase();
        Integer existing = NAME_TO_INDEX.get(normalized);
        if (existing != null) {
            return existing;
        }

        int newIndex = NAMES.size();
        NAMES.add(normalized);
        NAME_TO_INDEX.put(normalized, newIndex);
        return newIndex;
    }

    /**
     * Retrieves the slot index for a given key, or dynamically registers it on the fly.
     */
    public static int getOrRegister(String keyName) {
        if (keyName == null || keyName.trim().isEmpty()) return -1;
        String normalized = keyName.trim().toUpperCase();
        Integer idx = NAME_TO_INDEX.get(normalized);
        if (idx != null) {
            return idx;
        }
        return register(keyName);
    }

    /**
     * Returns the slot index for a given key name, or -1 if not registered.
     */
    public static int indexOf(String keyName) {
        if (keyName == null) return -1;
        Integer idx = NAME_TO_INDEX.get(keyName.trim().toUpperCase());
        return idx != null ? idx : -1;
    }

    /**
     * Returns the string name for a given slot index, or null if out of range.
     */
    public static String nameOf(int index) {
        if (index >= 0 && index < NAMES.size()) {
            return NAMES.get(index);
        }
        return null;
    }

    /**
     * Returns the total count of currently registered slots (standard + custom).
     */
    public static int count() {
        return NAMES.size();
    }

    public static Map<String, Integer> getAllKeys() {
        return Collections.unmodifiableMap(NAME_TO_INDEX);
    }

    private ThemeKeys() {}
}
