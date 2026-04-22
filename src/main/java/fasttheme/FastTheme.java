package fasttheme;

import fastcore.FastCore;

/**
 * FastTheme - Native Windows Theme & Window Styling API for Java.
 * 
 * <p>Provides native window styling capabilities (titlebar colors, transparency,
 * dark mode) and system theme detection via JNI.</p>
 * 
 * <p><b>Window Styling Example:</b></p>
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
 * <p><b>Theme Detection Example:</b></p>
 * <pre>
 * boolean isDark = FastTheme.isSystemDarkMode();
 * if (isDark) {
 *     // Apply dark theme to your app
 * }
 * </pre>
 * 
 * <p><b>Thread Safety:</b> All window styling methods are thread-safe and can be called
 * from any thread.</p>
 * 
 * <p><b>Platform Support:</b> Windows 10/11 only (uses native DWM APIs).</p>
 * 
 * @author Andre Stubbe
 * @version 2.0.0
 * @since 2.0.0
 * @see <a href="https://github.com/andrestubbe/FastTheme">GitHub Repository</a>
 * @see <a href="https://github.com/andrestubbe/FastDisplay">FastDisplay (Display Monitoring)</a>
 */
public class FastTheme {
    static {
        FastCore.loadLibrary("fasttheme");
    }

    // ============================================================================
    // WINDOW STYLING API - Static native methods for window customization
    // ============================================================================

    /**
     * Gets the native window handle (HWND) from a Swing/AWT component.
     * 
     * <p>The component must be visible before calling this method.</p>
     * 
     * @param component Usually a JFrame that is already visible
     * @return Native handle as long, or 0 if failed
     */
    public static native long getWindowHandle(java.awt.Component component);

    /**
     * Sets window transparency/opacity.
     * 
     * <p>Values: 0=invisible, 255=fully opaque</p>
     * 
     * @param hwnd Window handle from getWindowHandle()
     * @param alpha 0-255 (0=invisible, 255=fully opaque)
     * @return true on success
     */
    public static native boolean setWindowTransparency(long hwnd, int alpha);

    /**
     * Sets the title bar background color.
     * 
     * <p>Requires Windows 10/11 with DWM (Desktop Window Manager).</p>
     * 
     * @param hwnd Window handle
     * @param r Red (0-255)
     * @param g Green (0-255)
     * @param b Blue (0-255)
     * @return true on success
     */
    public static native boolean setTitleBarColor(long hwnd, int r, int g, int b);

    /**
     * Sets the title bar text color.
     * 
     * <p>Requires Windows 10/11 with DWM.</p>
     * 
     * @param hwnd Window handle
     * @param r Red (0-255)
     * @param g Green (0-255)
     * @param b Blue (0-255)
     * @return true on success
     */
    public static native boolean setTitleBarTextColor(long hwnd, int r, int g, int b);

    /**
     * Enables or disables dark mode for the window title bar.
     * 
     * <p>When enabled, the title bar uses dark colors. This is separate from
     * the system-wide dark mode setting.</p>
     * 
     * @param hwnd Window handle
     * @param enabled true for dark mode
     * @return true on success
     */
    public static native boolean setTitleBarDarkMode(long hwnd, boolean enabled);

    // ============================================================================
    // THEME DETECTION API - System theme information
    // ============================================================================

    /**
     * Checks if Windows is using dark mode globally.
     * 
     * <p>Reads the Windows Registry key "AppsUseLightTheme" to determine
     * the system-wide dark/light mode preference.</p>
     * 
     * @return true if dark mode enabled system-wide
     */
    public static native boolean isSystemDarkMode();
}
