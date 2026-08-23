package fasttheme;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * High-performance Key Matrix defining slot indices and names for FastTheme.
 */
public final class ThemeKeys {

    // --- Window & Frame ---
    public static final int TITLE_BAR_BACKGROUND = 0;
    public static final int TITLE_BAR_TEXT = 1;
    public static final int TITLE_BAR_BORDER = 2;
    public static final int WINDOW_BACKGROUND = 3;
    public static final int WINDOW_BORDER = 4;
    public static final int CONTENT_BACKGROUND = 5;

    // --- Text & Typography ---
    public static final int TEXT_PRIMARY = 6;
    public static final int TEXT_SECONDARY = 7;
    public static final int TEXT_MUTED = 8;
    public static final int TEXT_PLACEHOLDER = 9;
    public static final int TEXT_INVERSE = 10;

    // --- Accent & Brand ---
    public static final int ACCENT_PRIMARY = 11;
    public static final int ACCENT_SECONDARY = 12;
    public static final int ACCENT_HOVER = 13;
    public static final int ACCENT_PRESSED = 14;

    // --- Button States ---
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

    // --- Input & Editor ---
    public static final int INPUT_BACKGROUND = 26;
    public static final int INPUT_FOREGROUND = 27;
    public static final int INPUT_BORDER = 28;
    public static final int INPUT_BORDER_FOCUS = 29;
    public static final int EDITOR_LINE_NUMBER = 30;
    public static final int EDITOR_SELECTION = 31;

    // --- Navigation, Tabs & Splits ---
    public static final int TAB_BACKGROUND_NORMAL = 32;
    public static final int TAB_BACKGROUND_SELECTED = 33;
    public static final int TAB_BACKGROUND_HOVER = 34;
    public static final int TAB_FOREGROUND_NORMAL = 35;
    public static final int TAB_FOREGROUND_SELECTED = 36;
    public static final int SPLIT_PANE_BAR = 37;
    public static final int SCROLLBAR_THUMB = 38;
    public static final int SCROLLBAR_TRACK = 39;

    // --- Feedback & Status ---
    public static final int STATUS_SUCCESS = 40;
    public static final int STATUS_WARNING = 41;
    public static final int STATUS_ERROR = 42;
    public static final int STATUS_INFO = 43;

    // --- Popups & Tooltips ---
    public static final int TOOLTIP_BACKGROUND = 44;
    public static final int TOOLTIP_FOREGROUND = 45;
    public static final int POPUP_BACKGROUND = 46;
    public static final int POPUP_BORDER = 47;
    public static final int POPUP_SHADOW = 48;

    public static final int COUNT = 49;

    private static final String[] NAMES = new String[COUNT];
    private static final Map<String, Integer> NAME_TO_INDEX = new HashMap<>(COUNT * 2);

    static {
        register(TITLE_BAR_BACKGROUND, "TITLE_BAR_BACKGROUND");
        register(TITLE_BAR_TEXT, "TITLE_BAR_TEXT");
        register(TITLE_BAR_BORDER, "TITLE_BAR_BORDER");
        register(WINDOW_BACKGROUND, "WINDOW_BACKGROUND");
        register(WINDOW_BORDER, "WINDOW_BORDER");
        register(CONTENT_BACKGROUND, "CONTENT_BACKGROUND");

        register(TEXT_PRIMARY, "TEXT_PRIMARY");
        register(TEXT_SECONDARY, "TEXT_SECONDARY");
        register(TEXT_MUTED, "TEXT_MUTED");
        register(TEXT_PLACEHOLDER, "TEXT_PLACEHOLDER");
        register(TEXT_INVERSE, "TEXT_INVERSE");

        register(ACCENT_PRIMARY, "ACCENT_PRIMARY");
        register(ACCENT_SECONDARY, "ACCENT_SECONDARY");
        register(ACCENT_HOVER, "ACCENT_HOVER");
        register(ACCENT_PRESSED, "ACCENT_PRESSED");

        register(BUTTON_NORMAL_BACKGROUND, "BUTTON_NORMAL_BACKGROUND");
        register(BUTTON_NORMAL_FOREGROUND, "BUTTON_NORMAL_FOREGROUND");
        register(BUTTON_NORMAL_BORDER, "BUTTON_NORMAL_BORDER");
        register(BUTTON_HOVER_BACKGROUND, "BUTTON_HOVER_BACKGROUND");
        register(BUTTON_HOVER_FOREGROUND, "BUTTON_HOVER_FOREGROUND");
        register(BUTTON_HOVER_BORDER, "BUTTON_HOVER_BORDER");
        register(BUTTON_PRESSED_BACKGROUND, "BUTTON_PRESSED_BACKGROUND");
        register(BUTTON_PRESSED_FOREGROUND, "BUTTON_PRESSED_FOREGROUND");
        register(BUTTON_PRESSED_BORDER, "BUTTON_PRESSED_BORDER");
        register(BUTTON_DISABLED_BACKGROUND, "BUTTON_DISABLED_BACKGROUND");
        register(BUTTON_DISABLED_FOREGROUND, "BUTTON_DISABLED_FOREGROUND");

        register(INPUT_BACKGROUND, "INPUT_BACKGROUND");
        register(INPUT_FOREGROUND, "INPUT_FOREGROUND");
        register(INPUT_BORDER, "INPUT_BORDER");
        register(INPUT_BORDER_FOCUS, "INPUT_BORDER_FOCUS");
        register(EDITOR_LINE_NUMBER, "EDITOR_LINE_NUMBER");
        register(EDITOR_SELECTION, "EDITOR_SELECTION");

        register(TAB_BACKGROUND_NORMAL, "TAB_BACKGROUND_NORMAL");
        register(TAB_BACKGROUND_SELECTED, "TAB_BACKGROUND_SELECTED");
        register(TAB_BACKGROUND_HOVER, "TAB_BACKGROUND_HOVER");
        register(TAB_FOREGROUND_NORMAL, "TAB_FOREGROUND_NORMAL");
        register(TAB_FOREGROUND_SELECTED, "TAB_FOREGROUND_SELECTED");
        register(SPLIT_PANE_BAR, "SPLIT_PANE_BAR");
        register(SCROLLBAR_THUMB, "SCROLLBAR_THUMB");
        register(SCROLLBAR_TRACK, "SCROLLBAR_TRACK");

        register(STATUS_SUCCESS, "STATUS_SUCCESS");
        register(STATUS_WARNING, "STATUS_WARNING");
        register(STATUS_ERROR, "STATUS_ERROR");
        register(STATUS_INFO, "STATUS_INFO");

        register(TOOLTIP_BACKGROUND, "TOOLTIP_BACKGROUND");
        register(TOOLTIP_FOREGROUND, "TOOLTIP_FOREGROUND");
        register(POPUP_BACKGROUND, "POPUP_BACKGROUND");
        register(POPUP_BORDER, "POPUP_BORDER");
        register(POPUP_SHADOW, "POPUP_SHADOW");
    }

    private static void register(int index, String name) {
        NAMES[index] = name;
        NAME_TO_INDEX.put(name.toUpperCase(), index);
    }

    public static int indexOf(String keyName) {
        if (keyName == null) return -1;
        Integer idx = NAME_TO_INDEX.get(keyName.trim().toUpperCase());
        return idx != null ? idx : -1;
    }

    public static String nameOf(int index) {
        if (index >= 0 && index < COUNT) {
            return NAMES[index];
        }
        return null;
    }

    public static Map<String, Integer> getAllKeys() {
        return Collections.unmodifiableMap(NAME_TO_INDEX);
    }

    private ThemeKeys() {}
}
