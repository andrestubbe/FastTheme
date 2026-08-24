package fasttheme;

import fastcore.FastCore;

import java.awt.Color;
import java.awt.Component;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * FastTheme - Universal Display, OS Window & Dynamic Theme Management Engine for FastJava.
 */
public class FastTheme {

    static {
        try {
            FastCore.loadLibrary("fasttheme");
        } catch (Throwable ignored) {
        }
    }

    private static volatile ThemeData currentTheme = ThemeParser.loadDefaultDark();
    private static final List<ThemeListener> listeners = new CopyOnWriteArrayList<>();

    public FastTheme() {
    }

    // --- Dynamic Theme State Management ---

    /**
     * Registers a listener for live theme changes.
     */
    public static void addListener(ThemeListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    /**
     * Removes a registered theme change listener.
     */
    public static void removeListener(ThemeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Parses and activates a theme from a .theme text definition.
     */
    public static void load(String text) {
        set(ThemeParser.parseText(text));
    }

    /**
     * Deserializes and activates a theme from a .themebin binary payload.
     */
    public static void load(byte[] binaryData) {
        set(ThemeParser.parseBinary(binaryData));
    }

    /**
     * Retrieves the currently active ThemeData instance.
     */
    public static ThemeData current() {
        return currentTheme;
    }

    /**
     * Synchronizes native Windows DWM title bar and background styling
     * using colors from the currently active theme.
     */
    public static void applyToWindow(long hwnd) {
        if (hwnd == 0) return;
        try {
            int titleBg = get(ThemeKeys.TITLE_BAR_BACKGROUND);
            int titleFg = get(ThemeKeys.TITLE_BAR_TEXT);
            int winBg = get(ThemeKeys.WINDOW_BACKGROUND);

            setTitleBarColor(hwnd, ThemeColorUtil.red(titleBg), ThemeColorUtil.green(titleBg), ThemeColorUtil.blue(titleBg));
            setTitleBarTextColor(hwnd, ThemeColorUtil.red(titleFg), ThemeColorUtil.green(titleFg), ThemeColorUtil.blue(titleFg));
            setWindowBackgroundColor(hwnd, ThemeColorUtil.red(winBg), ThemeColorUtil.green(winBg), ThemeColorUtil.blue(winBg));

            boolean isDark = ThemeColorUtil.luminance(winBg) < 0.5;
            setTitleBarDarkMode(hwnd, isDark);
        } catch (Throwable ignored) {
        }
    }

    /**
     * Convenience method to apply theme to a Swing/AWT component window.
     */
    public static void applyToWindow(Component component) {
        if (component == null) return;
        try {
            long hwnd = getWindowHandle(component);
            if (hwnd != 0) {
                applyToWindow(hwnd);
            }
        } catch (Throwable ignored) {
        }
    }

    /**
     * Zero-allocation direct slot access.
     */
    public static int get(int slotIndex) {
        return currentTheme.get(slotIndex);
    }

    /**
     * String key based lookup.
     */
    public static int get(String keyName) {
        return currentTheme.get(keyName);
    }

    /**
     * Returns the AWT/Swing Color object for the requested slot ID.
     */
    public static Color getColor(int slotIndex) {
        return ThemeColorUtil.toAwtColor(get(slotIndex));
    }

    /**
     * Returns the AWT/Swing Color object for the requested key name.
     */
    public static Color getColor(String keyName) {
        return ThemeColorUtil.toAwtColor(get(keyName));
    }


    /**
     * Returns the 24-bit Truecolor ANSI foreground sequence for CLI/TUI.
     */
    public static String getAnsiFg(int slotIndex) {
        return ThemeColorUtil.toAnsiForeground(get(slotIndex));
    }

    /**
     * Returns the 24-bit Truecolor ANSI background sequence for CLI/TUI.
     */
    public static String getAnsiBg(int slotIndex) {
        return ThemeColorUtil.toAnsiBackground(get(slotIndex));
    }

    /**
     * Activates a theme globally and notifies all registered listeners.
     */
    public static void set(ThemeData theme) {
        if (theme == null) return;
        currentTheme = theme;
        for (ThemeListener l : listeners) {
            try {
                l.onThemeChanged(theme);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    // --- Native JNI Methods ---

    public static native boolean enableMica(long hwnd, boolean enabled);

    public static native boolean isSystemDarkMode();

    public static native long getWindowHandle(Component component);

    public static native long getConsoleWindowHandle();

    public static native boolean setWindowTransparency(long hwnd, int alpha);

    public static native boolean setWindowBackgroundColor(long hwnd, int r, int g, int b);

    public static native boolean setTitleBarColor(long hwnd, int r, int g, int b);

    public static native boolean setTitleBarTextColor(long hwnd, int r, int g, int b);

    public static native boolean setTitleBarDarkMode(long hwnd, boolean enabled);

    public static native boolean setCornerStyle(long hwnd, int style);

    public static native boolean setBorderlessShadow(long hwnd, boolean enabled);

    public static native boolean setOverlayDragHeight(long hwnd, int height);
}
