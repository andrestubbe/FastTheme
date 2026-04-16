package fasttheme;

import fastcore.FastCore;

public class FastTheme {
    static {
        FastCore.loadLibrary("fasttheme");
    }

    public enum Orientation {
        LANDSCAPE, PORTRAIT, LANDSCAPE_FLIPPED, PORTRAIT_FLIPPED
    }

    public interface ThemeListener {
        void onResolutionChanged(int width, int height, int dpi, int refreshRate);
        void onOrientationChanged(Orientation orientation);
        void onThemeChanged(boolean isDarkTheme);
        void onInitialState(int width, int height, int dpi, int refreshRate, Orientation orientation, boolean isDarkTheme);
    }

    private ThemeListener listener;

    public void setListener(ThemeListener listener) {
        this.listener = listener;
    }

    // Called from C++ JNI when WM_DISPLAYCHANGE or WM_DPICHANGED occurs.
    private void notifyResolutionChanged(int width, int height, int dpi, int orientationOrdinal, int refreshRate) {
        if (listener != null) {
            listener.onResolutionChanged(width, height, dpi, refreshRate);
            listener.onOrientationChanged(Orientation.values()[orientationOrdinal]);
        }
    }

    // Called from C++ JNI when theme changes (WM_SETTINGCHANGE with ImmersiveColorSet)
    private void notifyThemeChanged(boolean isDarkTheme) {
        if (listener != null) {
            listener.onThemeChanged(isDarkTheme);
        }
    }

    // Called from C++ JNI on startup to report initial state
    private void notifyInitialState(int width, int height, int dpi, int orientationOrdinal, int refreshRate, boolean isDarkTheme) {
        if (listener != null) {
            listener.onInitialState(width, height, dpi, refreshRate, Orientation.values()[orientationOrdinal], isDarkTheme);
        }
    }

    public native boolean startMonitoring();
    public native void stopMonitoring();

    // ============================================================================
    // WINDOW STYLING API - Static native methods for window customization
    // ============================================================================

    /**
     * Gets the native window handle (HWND) from a Swing/AWT component.
     * @param component Usually a JFrame that is already visible
     * @return Native handle as long, or 0 if failed
     */
    public static native long getWindowHandle(java.awt.Component component);

    /**
     * Sets window transparency/opacity.
     * @param hwnd Window handle from getWindowHandle()
     * @param alpha 0-255 (0=invisible, 255=fully opaque)
     * @return true on success
     */
    public static native boolean setWindowTransparency(long hwnd, int alpha);

    /**
     * Sets the title bar background color.
     * @param hwnd Window handle
     * @param r Red (0-255)
     * @param g Green (0-255)
     * @param b Blue (0-255)
     * @return true on success
     */
    public static native boolean setTitleBarColor(long hwnd, int r, int g, int b);

    /**
     * Sets the title bar text color.
     * @param hwnd Window handle
     * @param r Red (0-255)
     * @param g Green (0-255)
     * @param b Blue (0-255)
     * @return true on success
     */
    public static native boolean setTitleBarTextColor(long hwnd, int r, int g, int b);

    /**
     * Enables or disables dark mode for the window title bar.
     * @param hwnd Window handle
     * @param enabled true for dark mode
     * @return true on success
     */
    public static native boolean setTitleBarDarkMode(long hwnd, boolean enabled);

    /**
     * Gets the current screen resolution.
     * @return String like "1920x1080"
     */
    public static native String getSystemResolution();

    /**
     * Gets the system DPI scaling value (96 = 100%).
     * @return DPI value
     */
    public static native int getSystemDPI();

    /**
     * Checks if Windows is using dark mode.
     * @return true if dark mode enabled
     */
    public static native boolean isSystemDarkMode();

    /**
     * Gets the display refresh rate in Hz.
     * @return Refresh rate (60, 120, 144, etc.)
     */
    public static native int getSystemRefreshRate();
}
