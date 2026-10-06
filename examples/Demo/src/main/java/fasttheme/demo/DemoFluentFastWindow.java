package fasttheme.demo;

import fastdisplay.FastDisplay;
import fasttheme.FastTheme;
import fastwindow.FastNativeWindow;
import fastwindow.FastWindow;

/**
 * FastTheme + FastWindow Fluent Mica Demo with 48px Scaled Title Bar.
 */
public class DemoFluentFastWindow {
    public static void main(String[] args) {
        int baseWidth = 900;
        int baseHeight = 560;

        // Get system display scale from FastDisplay
        FastDisplay display = new FastDisplay();
        int scale = 100;
        try {
            scale = display.getScale();
            if (scale <= 0) scale = 100;
        } catch (Throwable t) {
            System.out.println("[DemoFluent] FastDisplay scale fallback to 100%: " + t.getMessage());
        }

        double scaleFactor = scale / 100.0;
        int titleBarHeight = (int) Math.round(48.0 * scaleFactor);
        int buttonWidth    = (int) Math.round(46.0 * scaleFactor);

        System.out.println(String.format("[DemoFluent] DPI Scale: %d%% | TitleBar: %dpx | ButtonWidth: %dpx", 
                scale, titleBarHeight, buttonWidth));

        try (FastNativeWindow window = FastWindow.create("FastTheme — Fluent 48px TitleBar", baseWidth, baseHeight)) {
            long hwnd = window.getHWND();
            System.out.println("[DemoFluent] HWND = " + hwnd);

            if (hwnd != 0) {
                // 1. Dark Mode & Rounded Corners (Windows 11)
                FastTheme.setTitleBarDarkMode(hwnd, true);
                FastTheme.setCornerStyle(hwnd, 2);

                // 2. Full Mica Material
                FastTheme.setSystemBackdropType(hwnd, FastTheme.BACKDROP_MICA);

                // 3. Extend client into title bar + set scaled 48px height
                FastTheme.setBorderlessShadow(hwnd, true);
                FastTheme.setTitleBarHeight(hwnd, titleBarHeight);

                // 4. Enable native button hit-testing (Min, Max, Close + Snap Layouts)
                FastTheme.setNativeTitleBarButtonsEnabled(hwnd, true, buttonWidth);
            }

            // Make window visible once configured
            window.setVisible(true);

            if (hwnd != 0) {
                FastTheme.forceFrameUpdate(hwnd);
            }

            System.out.println("[DemoFluent] Ready. Close window or press ESC to exit.");

            while (window.pollEvents()) {
                try {
                    Thread.sleep(16);
                } catch (InterruptedException ignored) {}
            }
        }
    }
}
