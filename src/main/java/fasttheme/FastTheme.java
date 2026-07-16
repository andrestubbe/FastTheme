package fasttheme;

import fastcore.FastCore;

public class FastTheme {
    public FastTheme() {}

    static {
        FastCore.loadLibrary("fasttheme");
    }

    public static native long getWindowHandle(java.awt.Component component);

    public static native boolean setWindowTransparency(long hwnd, int alpha);

    public static native boolean setTitleBarColor(long hwnd, int r, int g, int b);

    public static native boolean setTitleBarTextColor(long hwnd, int r, int g, int b);

    public static native boolean setTitleBarDarkMode(long hwnd, boolean enabled);

    public static native boolean enableMica(long hwnd, boolean enabled);

    public static native boolean setCornerStyle(long hwnd, int style);

    public static native boolean setBorderlessShadow(long hwnd, boolean enabled);

    public static native boolean setOverlayDragHeight(long hwnd, int height);

    public static native boolean isSystemDarkMode();
}
