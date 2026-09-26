package fasttheme;

import fastwindow.FastNativeWindow;
import fastwindow.FastWindow;

/**
 * Windows 11 Acrylic Blur Demo powered by FastWindow and FastTheme.
 * Creates a standalone native Win32 window with real Windows 11 Acrylic backdrop and dark titlebar.
 */
public class AcrylicDemo {
    public static void main(String[] args) {
        // Auto-hide console if started via batch script
        long consoleHwnd = FastTheme.getConsoleWindowHandle();
        if (consoleHwnd != 0) {
            FastTheme.setWindowTransparency(consoleHwnd, 0);
        }

        try (FastNativeWindow window = FastWindow.create("FastTheme — Windows 11 Acrylic Transient Blur", 900, 560)) {
            long hwnd = window.getHWND();
            if (hwnd != 0) {
                // Apply Windows 11 Acrylic material and dark title bar
                FastTheme.setTitleBarDarkMode(hwnd, true);
                FastTheme.setCornerStyle(hwnd, 2); // Windows 11 Rounded corners
                FastTheme.setSystemBackdropType(hwnd, FastTheme.BACKDROP_ACRYLIC);
            }

            // Display window seamlessly
            window.setVisible(true);

            long lastFpsTime = System.nanoTime();
            int frames = 0;

            while (window.pollEvents()) {
                frames++;
                long now = System.nanoTime();
                if (now - lastFpsTime >= 1_000_000_000L) {
                    window.setTitle("FastTheme Acrylic Backdrop (Win11) - FPS: " + frames);
                    frames = 0;
                    lastFpsTime = now;
                }

                try {
                    Thread.sleep(16); // ~60 Hz event tick
                } catch (InterruptedException ignored) {}
            }
        }

        if (consoleHwnd != 0) {
            FastTheme.setWindowTransparency(consoleHwnd, 255);
        }
    }
}
