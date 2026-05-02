package fasttheme;

import fastcore.FastCore;

/**
 * FastTheme — Native Windows Window Styling and Theme API for Java.
 * 
 * <p>Provides native window styling capabilities (titlebar colors, materials,
 * dark mode) via JNI and the Windows Desktop Window Manager (DWM) API.</p>
 * 
 * <p><b>Usage:</b></p>
 * <pre>
 * long hwnd = FastWindow.attach(myJFrame).getHWND();
 * FastTheme.setTitleBarDarkMode(hwnd, true);
 * FastTheme.enableMica(hwnd, true);
 * </pre>
 * 
 * @author FastJava Team
 * @version 0.2.0
 * @see <a href="https://github.com/andrestubbe/FastTheme">FastTheme GitHub</a>
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
     * <p>Note: In the FastJava ecosystem, it is recommended to use 
     * {@code FastWindow.attach(component).getHWND()} for better lifecycle management.</p>
     * 
     * @param component The Java component (usually a JFrame or JDialog).
     * @return The native window handle, or 0 if extraction failed.
     */
    public static native long getWindowHandle(java.awt.Component component);

    /**
     * Sets window transparency/opacity via the layered window API.
     * 
     * @param hwnd  The native window handle.
     * @param alpha The opacity value from 0 (fully transparent) to 255 (fully opaque).
     * @return true if the transparency was successfully applied.
     */
    public static native boolean setWindowTransparency(long hwnd, int alpha);

    /**
     * Sets the title bar background color (Windows 11+).
     * 
     * @param hwnd The native window handle.
     * @param r    Red component (0-255).
     * @param g    Green component (0-255).
     * @param b    Blue component (0-255).
     * @return true if successful.
     */
    public static native boolean setTitleBarColor(long hwnd, int r, int g, int b);

    /**
     * Sets the title bar text color (Windows 11+).
     * 
     * @param hwnd The native window handle.
     * @param r    Red component (0-255).
     * @param g    Green component (0-255).
     * @param b    Blue component (0-255).
     * @return true if successful.
     */
    public static native boolean setTitleBarTextColor(long hwnd, int r, int g, int b);

    /**
     * Enables or disables Immersive Dark Mode for the window title bar.
     * <p>This ensures the window frame matches the dark theme of the application contents.</p>
     * 
     * @param hwnd    The native window handle.
     * @param enabled true for dark mode, false for light mode.
     * @return true if successful.
     */
    public static native boolean setTitleBarDarkMode(long hwnd, boolean enabled);

    /**
     * Enables or disables the Windows 11 Mica material effect.
     * <p>Mica is a translucent material that incorporates the user's desktop wallpaper 
     * into the window background.</p>
     * 
     * @param hwnd    The native window handle.
     * @param enabled true to enable Mica material.
     * @return true if successful.
     */
    public static native boolean enableMica(long hwnd, boolean enabled);

    /**
     * Sets the Windows 11 corner style for the window.
     * 
     * @param hwnd  The native window handle.
     * @param style 0=Default, 1=None, 2=Rounded, 3=Small Rounded.
     * @return true if successful.
     */
    public static native boolean setCornerStyle(long hwnd, int style);

    /**
     * Checks if the Windows system is currently set to Dark Mode for applications.
     * 
     * @return true if system-wide dark mode is enabled.
     */
    public static native boolean isSystemDarkMode();
}
