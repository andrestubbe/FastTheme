package fasttheme;

import fastcore.FastCore;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * FastTheme - Universal, Schema-Free Dynamic Theme Management &amp; OS Window Styling Engine for FastJava.
 */
public class FastTheme {

    // System Backdrop Types (Windows 11 Build 22621+)
    public static final int BACKDROP_AUTO = 0;
    public static final int BACKDROP_NONE = 1;
    public static final int BACKDROP_MICA = 2;
    public static final int BACKDROP_ACRYLIC = 3;
    public static final int BACKDROP_MICA_ALT = 4;

    private static final boolean NATIVE_AVAILABLE;
    private static volatile ThemeData currentTheme = new ThemeData("Default");
    private static final List<ThemeListener> listeners = new CopyOnWriteArrayList<>();

    static {
        boolean loaded = false;
        try {
            FastCore.loadLibrary("fasttheme");
            loaded = true;
        } catch (Throwable t) {
            System.err.println("[FastTheme] Native styling bridge unavailable: " + t.getMessage());
        }
        NATIVE_AVAILABLE = loaded;
    }

    // Constructor
    public FastTheme() {
    }

    // Methods
    public static void addListener(ThemeListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public static native void addTitleBarControlRect(long hwnd, int x, int y, int w, int h);

    public static void applyToWindow(long hwnd) {
        applyToWindow(hwnd, "TITLE_BAR_BACKGROUND", "TITLE_BAR_TEXT", "WINDOW_BACKGROUND");
    }

    public static void applyToWindow(long hwnd, String titleBgKey, String titleFgKey, String winBgKey) {
        if (hwnd == 0 || !isNativeAvailable()) return;
        try {
            int titleBg = get(titleBgKey);
            int titleFg = get(titleFgKey);
            int winBg = get(winBgKey);

            if (titleBg != 0) {
                setTitleBarColor(hwnd, ThemeColorUtil.red(titleBg), ThemeColorUtil.green(titleBg), ThemeColorUtil.blue(titleBg));
            }
            if (titleFg != 0) {
                setTitleBarTextColor(hwnd, ThemeColorUtil.red(titleFg), ThemeColorUtil.green(titleFg), ThemeColorUtil.blue(titleFg));
            }
            if (winBg != 0) {
                setWindowBackgroundColor(hwnd, ThemeColorUtil.red(winBg), ThemeColorUtil.green(winBg), ThemeColorUtil.blue(winBg));
                boolean isDark = ThemeColorUtil.contrastRatio(winBg, 0xFFFFFFFF) >= ThemeColorUtil.contrastRatio(winBg, 0xFF111111);
                setTitleBarDarkMode(hwnd, isDark);
            }
        } catch (Exception e) {
            System.err.println("[FastTheme] Failed to apply window theme: " + e.getMessage());
        }
    }

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

    public static void applyToWindow(Component component, String titleBgKey, String titleFgKey, String winBgKey) {
        if (component == null) return;
        try {
            long hwnd = getWindowHandle(component);
            if (hwnd != 0) {
                applyToWindow(hwnd, titleBgKey, titleFgKey, winBgKey);
            }
        } catch (Throwable ignored) {
        }
    }

    public static native void clearTitleBarControlRects(long hwnd);

    public static native void closeWindow(long hwnd);

    public static native boolean enableMica(long hwnd, boolean enabled);

    public static native void forceFrameUpdate(long hwnd);

    public static void load(String text) {
        set(ThemeParser.parseText(text));
    }

    public static void load(byte[] binaryData) {
        set(ThemeParser.parseBinary(binaryData));
    }

    public static void loadFile(String filePath) throws IOException {
        set(ThemeParser.loadFromFile(filePath));
    }

    public static void loadFile(Path path) throws IOException {
        set(ThemeParser.loadFromFile(path));
    }

    public static void loadFile(File file) throws IOException {
        set(ThemeParser.loadFromFile(file));
    }

    public static native void maximizeWindow(long hwnd);

    public static native void minimizeWindow(long hwnd);

    public static void removeListener(ThemeListener listener) {
        listeners.remove(listener);
    }

    public static native void restoreWindow(long hwnd);

    public static native void sendSysCommand(long hwnd, int cmd);

    // Getters
    public static boolean isNativeAvailable() {
        return NATIVE_AVAILABLE;
    }

    public static native boolean isSystemDarkMode();

    public static native boolean isAppDarkMode();

    /**
     * Retrieves the native Windows DWM accent/colorization color as packed 32-bit ARGB.
     *
     * @return Packed 32-bit ARGB integer.
     */
    public static native int getAccentColor();

    /**
     * Retrieves the native Windows DWM accent color as a java.awt.Color.
     *
     * @return Color object representing the current Windows accent color.
     */
    public static Color getSystemAccentColor() {
        return ThemeColorUtil.toAwtColor(getAccentColor());
    }

    public static native boolean isColorizationOpaque();

    public static native boolean isHighContrast();

    public static native boolean isTransparencyEnabled();

    public static ThemeData current() {
        return currentTheme;
    }

    public static int get(int slotIndex) {
        return currentTheme.get(slotIndex);
    }

    public static int get(String keyName) {
        return currentTheme.get(keyName);
    }

    public static Color getColor(int slotIndex) {
        return ThemeColorUtil.toAwtColor(get(slotIndex));
    }

    public static Color getColor(String keyName) {
        return ThemeColorUtil.toAwtColor(get(keyName));
    }

    public static native long getConsoleWindowHandle();

    public static native long getWindowHandle(Component component);

    // Setters
    public static void set(ThemeData theme) {
        if (theme == null) return;
        currentTheme = theme;
        for (ThemeListener l : listeners) {
            try {
                l.onThemeChanged(theme);
            } catch (Exception e) {
                System.err.println("[FastTheme] Listener failed on theme change: " + e.getMessage());
            }
        }
    }

    public static native boolean setAlwaysOnTop(long hwnd, boolean alwaysOnTop);

    public static native boolean setBorderlessShadow(long hwnd, boolean enabled);

    public static native boolean setCornerStyle(long hwnd, int style);

    public static native boolean setNativeTitleBarButtonsEnabled(long hwnd, boolean enabled, int buttonWidth);

    public static boolean setNativeTitleBarButtonsEnabled(long hwnd, boolean enabled) {
        return setNativeTitleBarButtonsEnabled(hwnd, enabled, 96);
    }

    public static native boolean setOverlayDragHeight(long hwnd, int height);

    public static native boolean setSystemBackdropType(long hwnd, int type);

    public static native boolean setTitleBarColor(long hwnd, int r, int g, int b);

    public static native boolean setTitleBarDarkMode(long hwnd, boolean enabled);

    public static native boolean setTitleBarHeight(long hwnd, int height);

    public static native boolean setTitleBarTextColor(long hwnd, int r, int g, int b);

    public static native boolean setWindowBackgroundColor(long hwnd, int r, int g, int b);

    public static native boolean setWindowButtonsVisible(long hwnd, boolean showMinimize, boolean showMaximize);

    public static native boolean setWindowTransparency(long hwnd, int alpha);

    public static native boolean setTitleBarThemeColors(long hwnd,
            int inactiveBg, int activeBg,
            int glyphActive, int glyphInactive, int glyphInactiveHover,
            int inactiveBtnHover, int inactiveBtnPressed,
            int activeBtnHover, int activeBtnPressed,
            int closeHover, int closePressed, int closeHoverGlyph);

    public static boolean setTitleBarTheme(long hwnd, TitleBarTheme theme) {
        if (hwnd == 0 || theme == null) return false;
        return setTitleBarThemeColors(hwnd,
                theme.inactiveBackgroundColor, theme.activeBackgroundColor,
                theme.glyphColorActive, theme.glyphColorInactive, theme.glyphColorInactiveHover,
                theme.inactiveButtonHoverBg, theme.inactiveButtonPressedBg,
                theme.activeButtonHoverBg, theme.activeButtonPressedBg,
                theme.activeCloseHoverBg, theme.activeClosePressedBg, theme.closeHoverGlyphColor);
    }

    /**
     * Applies full Fluent Mica TitleBar with native caption buttons, dark mode, and custom palette.
     */
    public static void applyFluentTitleBar(long hwnd, int titleBarHeight, int buttonWidth, TitleBarTheme theme) {
        if (hwnd == 0) return;
        setTitleBarDarkMode(hwnd, true);
        setCornerStyle(hwnd, 2); // Rounded
        setSystemBackdropType(hwnd, BACKDROP_MICA);
        setTitleBarHeight(hwnd, titleBarHeight);
        setNativeTitleBarButtonsEnabled(hwnd, true, buttonWidth);
        if (theme != null) {
            setTitleBarTheme(hwnd, theme);
        }
    }

    public static void applyFluentTitleBar(long hwnd, int titleBarHeight, int buttonWidth) {
        applyFluentTitleBar(hwnd, titleBarHeight, buttonWidth, TitleBarTheme.fluentDark());
    }
}
