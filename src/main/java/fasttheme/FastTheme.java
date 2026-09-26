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

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================

    /**
     * Constructs a FastTheme instance.
     */
    public FastTheme() {}

    // =========================================================================
    // METHODS (Actions & Operations)
    // =========================================================================

    /**
     * Parses and activates a theme from a .theme text definition.
     *
     * @param text Raw .theme formatted string.
     */
    public static void load(String text) {
        set(ThemeParser.parseText(text));
    }

    /**
     * Deserializes and activates a theme from a .themebin binary payload.
     *
     * @param binaryData Byte array containing the binary format.
     */
    public static void load(byte[] binaryData) {
        set(ThemeParser.parseBinary(binaryData));
    }

    /**
     * Loads and activates a theme from a file path (.theme or .themebin).
     *
     * @param filePath Path to the theme file.
     * @throws IOException If file reading fails.
     */
    public static void loadFile(String filePath) throws IOException {
        set(ThemeParser.loadFromFile(filePath));
    }

    /**
     * Loads and activates a theme from a Path (.theme or .themebin).
     *
     * @param path Path to the theme file.
     * @throws IOException If file reading fails.
     */
    public static void loadFile(Path path) throws IOException {
        set(ThemeParser.loadFromFile(path));
    }

    /**
     * Loads and activates a theme from a File (.theme or .themebin).
     *
     * @param file Theme file.
     * @throws IOException If file reading fails.
     */
    public static void loadFile(File file) throws IOException {
        set(ThemeParser.loadFromFile(file));
    }

    /**
     * Registers a listener for live theme changes.
     *
     * @param listener Observer callback.
     */
    public static void addListener(ThemeListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    /**
     * Removes a registered theme change listener.
     *
     * @param listener Observer callback.
     */
    public static void removeListener(ThemeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Synchronizes native Windows DWM title bar and background styling
     * using standard key names ("TITLE_BAR_BACKGROUND", "TITLE_BAR_TEXT", "WINDOW_BACKGROUND").
     *
     * @param hwnd 64-bit native window handle.
     */
    public static void applyToWindow(long hwnd) {
        applyToWindow(hwnd, "TITLE_BAR_BACKGROUND", "TITLE_BAR_TEXT", "WINDOW_BACKGROUND");
    }

    /**
     * Synchronizes native Windows DWM title bar and background styling
     * using user-specified key names from the active theme.
     *
     * @param hwnd 64-bit native window handle.
     * @param titleBgKey Key name for title bar background color.
     * @param titleFgKey Key name for title bar text color.
     * @param winBgKey Key name for window client background color.
     */
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

    /**
     * Convenience method to apply theme styling to a Swing/AWT component window.
     *
     * @param component Swing or AWT component.
     */
    public static void applyToWindow(Component component) {
        if (component == null) return;
        try {
            long hwnd = getWindowHandle(component);
            if (hwnd != 0) {
                applyToWindow(hwnd);
            }
        } catch (Throwable ignored) {}
    }

    /**
     * Convenience method to apply theme styling to a Swing/AWT component window with custom key names.
     *
     * @param component Swing or AWT component.
     * @param titleBgKey Key name for title bar background color.
     * @param titleFgKey Key name for title bar text color.
     * @param winBgKey Key name for window client background color.
     */
    public static void applyToWindow(Component component, String titleBgKey, String titleFgKey, String winBgKey) {
        if (component == null) return;
        try {
            long hwnd = getWindowHandle(component);
            if (hwnd != 0) {
                applyToWindow(hwnd, titleBgKey, titleFgKey, winBgKey);
            }
        } catch (Throwable ignored) {}
    }

    /**
     * Enables or disables Windows 11 Mica material effect.
     *
     * @param hwnd 64-bit native window handle.
     * @param enabled True to enable Mica effect.
     * @return True if operation succeeded.
     */
    public static native boolean enableMica(long hwnd, boolean enabled);

    // =========================================================================
    // GETTERS
    // =========================================================================

    /**
     * Checks if the native Windows styling engine (FastTheme DLL) is loaded and available.
     *
     * @return True if native methods can be invoked.
     */
    public static boolean isNativeAvailable() {
        return NATIVE_AVAILABLE;
    }

    /**
     * Retrieves the currently active ThemeData instance.
     *
     * @return Active {@link ThemeData}.
     */
    public static ThemeData current() {
        return currentTheme;
    }

    /**
     * Zero-allocation direct slot access.
     *
     * @param slotIndex Integer slot index.
     * @return Packed 32-bit ARGB color value.
     */
    public static int get(int slotIndex) {
        return currentTheme.get(slotIndex);
    }

    /**
     * String key based color lookup.
     *
     * @param keyName Theme key name.
     * @return Packed 32-bit ARGB color value.
     */
    public static int get(String keyName) {
        return currentTheme.get(keyName);
    }

    /**
     * Returns the AWT/Swing Color object for the requested slot ID.
     *
     * @param slotIndex Integer slot index.
     * @return Corresponding {@link java.awt.Color}.
     */
    public static Color getColor(int slotIndex) {
        return ThemeColorUtil.toAwtColor(get(slotIndex));
    }

    /**
     * Returns the AWT/Swing Color object for the requested key name.
     *
     * @param keyName Theme key name.
     * @return Corresponding {@link java.awt.Color}.
     */
    public static Color getColor(String keyName) {
        return ThemeColorUtil.toAwtColor(get(keyName));
    }

    /**
     * Extracts the native HWND handle for a Swing/AWT component.
     *
     * @param component The AWT or Swing component.
     * @return 64-bit native window handle.
     */
    public static native long getWindowHandle(Component component);

    /**
     * Retrieves the native HWND handle for the active console window.
     *
     * @return 64-bit console window handle.
     */
    public static native long getConsoleWindowHandle();

    /**
     * Queries Windows system dark mode setting.
     *
     * @return True if Windows is in dark mode.
     */
    public static native boolean isSystemDarkMode();

    // =========================================================================
    // SETTERS
    // =========================================================================

    /**
     * Activates a theme globally across the JVM and notifies all registered listeners.
     *
     * @param theme The {@link ThemeData} to set as active.
     */
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

    /**
     * Sets native window transparency.
     *
     * @param hwnd 64-bit native window handle.
     * @param alpha Alpha value from 0 (transparent) to 255 (opaque).
     * @return True if operation succeeded.
     */
    public static native boolean setWindowTransparency(long hwnd, int alpha);

    /**
     * Sets native window background color.
     *
     * @param hwnd 64-bit native window handle.
     * @param r Red component (0..255).
     * @param g Green component (0..255).
     * @param b Blue component (0..255).
     * @return True if operation succeeded.
     */
    public static native boolean setWindowBackgroundColor(long hwnd, int r, int g, int b);

    /**
     * Sets native title bar color on Windows 11.
     *
     * @param hwnd 64-bit native window handle.
     * @param r Red component (0..255).
     * @param g Green component (0..255).
     * @param b Blue component (0..255).
     * @return True if operation succeeded.
     */
    public static native boolean setTitleBarColor(long hwnd, int r, int g, int b);

    /**
     * Sets native title bar text color on Windows 11.
     *
     * @param hwnd 64-bit native window handle.
     * @param r Red component (0..255).
     * @param g Green component (0..255).
     * @param b Blue component (0..255).
     * @return True if operation succeeded.
     */
    public static native boolean setTitleBarTextColor(long hwnd, int r, int g, int b);

    /**
     * Toggles immersive dark mode for native title bar.
     *
     * @param hwnd 64-bit native window handle.
     * @param enabled True for dark mode, false for light mode.
     * @return True if operation succeeded.
     */
    public static native boolean setTitleBarDarkMode(long hwnd, boolean enabled);

    /**
     * Sets the native system backdrop material on Windows 11 (Mica, Acrylic, Mica Alt).
     *
     * @param hwnd 64-bit native window handle.
     * @param type Backdrop type (BACKDROP_NONE, BACKDROP_MICA, BACKDROP_ACRYLIC, BACKDROP_MICA_ALT).
     * @return True if operation succeeded.
     */
    public static native boolean setSystemBackdropType(long hwnd, int type);

    /**
     * Sets window corner style on Windows 11.
     *
     * @param hwnd 64-bit native window handle.
     * @param style Corner style preference (0=Default, 1=Square, 2=Rounded, 3=Small Rounded).
     * @return True if operation succeeded.
     */
    public static native boolean setCornerStyle(long hwnd, int style);

    /**
     * Enables borderless window with native drop shadow.
     *
     * @param hwnd 64-bit native window handle.
     * @param enabled True to enable borderless mode.
     * @return True if operation succeeded.
     */
    public static native boolean setBorderlessShadow(long hwnd, boolean enabled);

    /**
     * Sets height of invisible drag area for borderless windows.
     *
     * @param hwnd 64-bit native window handle.
     * @param height Height in pixels.
     * @return True if operation succeeded.
     */
    public static native boolean setOverlayDragHeight(long hwnd, int height);

    /**
     * Toggles visibility of minimize and maximize buttons in the native title bar.
     *
     * @param hwnd 64-bit native window handle.
     * @param showMinimize True to display minimize button, false to hide.
     * @param showMaximize True to display maximize button, false to hide.
     * @return True if operation succeeded.
     */
    public static native boolean setWindowButtonsVisible(long hwnd, boolean showMinimize, boolean showMaximize);

    /**
     * Toggles whether the native window stays always on top.
     *
     * @param hwnd 64-bit native window handle.
     * @param alwaysOnTop True to make topmost, false for normal z-order.
     * @return True if operation succeeded.
     */
    public static native boolean setAlwaysOnTop(long hwnd, boolean alwaysOnTop);
}
