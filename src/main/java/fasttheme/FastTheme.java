package fasttheme;

import fastcore.FastCore;

/**
 * FastTheme - Native Windows Window Styling and Theme API for Java.
 * 
 * <p>Provides native window styling capabilities (titlebar colors, transparency,
 * dark mode) via JNI/DWM API.</p>
 * 
 * <p><b>Usage Example:</b></p>
 * <pre>
 * JFrame frame = new JFrame("Styled Window");
 * frame.setVisible(true);
 * 
 * long hwnd = FastTheme.getWindowHandle(frame);
 * if (hwnd != 0) {
 *     FastTheme.setTitleBarColor(hwnd, 12, 12, 12);      // Dark gray titlebar
 *     FastTheme.setTitleBarDarkMode(hwnd, true);         // Enable immersive dark mode
 *     FastTheme.setWindowTransparency(hwnd, 204);        // 80% opacity
 * }
 * </pre>
 * 
 * @author FastJava Team
 * @version 2.0.0-alpha
 */
public class FastTheme {
    /**
     * Default constructor for FastTheme.
     */
    public FastTheme() {}

    static {
        FastCore.loadLibrary("fasttheme");
    }

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
     * Sets the title bar background color. (Windows 11+)
     * @param hwnd Window handle
     * @param r Red (0-255)
     * @param g Green (0-255)
     * @param b Blue (0-255)
     * @return true on success
     */
    public static native boolean setTitleBarColor(long hwnd, int r, int g, int b);

    /**
     * Sets the title bar text color. (Windows 11+)
     * @param hwnd Window handle
     * @param r Red (0-255)
     * @param g Green (0-255)
     * @param b Blue (0-255)
     * @return true on success
     */
    public static native boolean setTitleBarTextColor(long hwnd, int r, int g, int b);

    /**
     * Enables or disables immersive dark mode for the window title bar.
     * @param hwnd Window handle
     * @param enabled true for dark mode
     * @return true on success
     */
    public static native boolean setTitleBarDarkMode(long hwnd, boolean enabled);

    /**
     * Enables or disables the Windows 11 Mica material effect.
     * @param hwnd Window handle
     * @param enabled true to enable Mica
     * @return true on success
     */
    public static native boolean enableMica(long hwnd, boolean enabled);

    /**
     * Sets the Windows 11 corner style for the window.
     * @param hwnd Window handle
     * @param style 0=Default, 1=None, 2=Rounded, 3=Small Rounded
     * @return true on success
     */
    public static native boolean setCornerStyle(long hwnd, int style);

    /**
     * Checks if Windows is currently using dark mode for applications.
     * @return true if dark mode is enabled in Windows settings
     */
    public static native boolean isSystemDarkMode();
}
