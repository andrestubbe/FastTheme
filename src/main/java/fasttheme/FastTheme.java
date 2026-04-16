package fasttheme;

import fastcore.FastCore;

/**
 * FastTheme - Native Windows Theme, Display & Window Styling API for Java.
 * 
 * <p>Provides real-time monitoring of display changes (resolution, DPI, refresh rate,
 * orientation) and native window styling capabilities (titlebar colors, transparency,
 * dark mode) via JNI.
 * 
 * <p><b>Two main APIs:</b></p>
 * 
 * <p>1. <b>Display Monitoring</b> - Subscribe to system display changes:
 * <pre>
 * FastTheme theme = new FastTheme();
 * theme.setListener(new FastTheme.ThemeListener() {
 *     &#64;Override
 *     public void onInitialState(int width, int height, int dpi, int refreshRate,
 *                                Orientation orientation, boolean isDarkTheme) {
 *         System.out.println("Display: " + width + "x" + height + 
 *                          " | " + dpi + "DPI | " + refreshRate + "Hz");
 *     }
 *     
 *     &#64;Override
 *     public void onResolutionChanged(int w, int h, int dpi, int refresh) {
 *         System.out.println("Resolution changed: " + w + "x" + h);
 *     }
 *     
 *     &#64;Override
 *     public void onThemeChanged(boolean dark) {
 *         System.out.println("Theme: " + (dark ? "DARK" : "LIGHT"));
 *     }
 *     
 *     &#64;Override
 *     public void onOrientationChanged(Orientation o) {}
 * });
 * theme.startMonitoring();
 * </pre>
 * 
 * <p>2. <b>Window Styling</b> - Style any Swing window natively:
 * <pre>
 * JFrame frame = new JFrame("Styled Window");
 * frame.setVisible(true);
 * 
 * long hwnd = FastTheme.getWindowHandle(frame);
 * if (hwnd != 0) {
 *     FastTheme.setTitleBarColor(hwnd, 12, 12, 12);      // Dark gray
 *     FastTheme.setTitleBarDarkMode(hwnd, true);         // Dark mode
 *     FastTheme.setWindowTransparency(hwnd, 204);        // 80% opacity
 * }
 * </pre>
 * 
 * <p><b>Thread Safety:</b> All window styling methods are thread-safe and can be called
 * from any thread. Display monitoring creates a background thread that calls listener
 * methods on the main Java event thread.</p>
 * 
 * <p><b>Platform Support:</b> Windows 10/11 only (uses native DWM APIs).</p>
 * 
 * @author FastJava Team
 * @version 1.3.0
 * @since 1.0.0
 * @see <a href="https://github.com/andrestubbe/FastTheme">GitHub Repository</a>
 */
public class FastTheme {
    static {
        FastCore.loadLibrary("fasttheme");
    }

    /**
     * Display orientation values returned by the display monitoring API.
     * 
     * <p>Windows reports orientation based on the current display settings,
     * which may differ from physical monitor orientation if rotated.</p>
     */
    public enum Orientation {
        /** Normal landscape orientation (0 degrees). */
        LANDSCAPE,
        /** Portrait orientation (90 degrees clockwise). */
        PORTRAIT,
        /** Landscape flipped 180 degrees (upside down). */
        LANDSCAPE_FLIPPED,
        /** Portrait flipped 270 degrees (counter-clockwise 90). */
        PORTRAIT_FLIPPED
    }

    /**
     * Listener interface for receiving display and theme change events.
     * 
     * <p>All methods are called from the background monitoring thread.
     * The implementation should be thread-safe and handle events quickly
     * to avoid blocking the monitoring thread.</p>
     * 
     * <p><b>Event Sequence:</b></p>
     * <ol>
     *   <li>{@link #onInitialState} - Called once immediately after startMonitoring()</li>
     *   <li>{@link #onResolutionChanged} - Called when resolution or DPI changes</li>
     *   <li>{@link #onThemeChanged} - Called when Windows dark/light mode changes</li>
     *   <li>{@link #onOrientationChanged} - Called when display orientation changes</li>
     * </ol>
     */
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

    /**
     * Starts monitoring system display and theme changes.
     * 
     * <p>Creates a hidden message-only window and background thread that listens for:
     * <ul>
     *   <li>WM_DISPLAYCHANGE - Screen resolution changes</li>
     *   <li>WM_DPICHANGED - DPI scaling changes</li>
     *   <li>WM_SETTINGCHANGE - Windows theme changes (dark/light mode)</li>
     * </ul>
     * 
     * <p>Immediately after starting, {@link ThemeListener#onInitialState} is called
     * with the current system configuration.
     * 
     * @return true if monitoring started successfully, false on error
     *         (e.g., JNI library not loaded, listener not set)
     * 
     * @throws IllegalStateException if no listener is set (call setListener() first)
     * 
     * @note Safe to call multiple times - returns true if already running
     * @note Creates one background thread that exists until stopMonitoring() is called
     * @note Thread exits gracefully within ~100ms of stopMonitoring()
     * 
     * @see #stopMonitoring()
     * @see ThemeListener
     */
    public native boolean startMonitoring();
    
    /**
     * Stops monitoring and releases all native resources.
     * 
     * <p>Sends WM_CLOSE to the monitor window, causing the background thread
     * to exit. Releases the global reference to the Java FastTheme object
     * and cleans up JNI resources.</p>
     * 
     * <p><b>Important:</b> After stopping, you can restart monitoring by calling
     * startMonitoring() again with the same or a different listener.</p>
     * 
     * @note Safe to call multiple times - no-op if already stopped
     * @note Thread will exit gracefully within ~100ms
     * @note All pending callbacks complete before resources are released
     * 
     * @see #startMonitoring()
     */
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
