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
}
