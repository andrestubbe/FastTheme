package fasttheme;

import fastwindow.FastNativeWindow;
import fastwindow.FastWindow;

/**
 * Windows 11 Acrylic Blur Demo powered by FastWindow and FastTheme.
 * Creates a standalone native Win32 window with real Windows 11 Acrylic backdrop and dark titlebar.
 */
public class AcrylicDemo {
    public static void main(String[] args) {
        try (FastNativeWindow window = FastWindow.create("FastTheme — Windows 11 Acrylic Transient Blur", 900, 560)) {
            long hwnd = window.getHWND();
            System.out.println("[AcrylicDemo] Window created successfully, HWND = " + hwnd);

            // Display window first so DWM surface exists
            window.setVisible(true);

            if (hwnd != 0) {
                // Let Windows 11 Acrylic material flow across both titlebar and body
                FastTheme.setTitleBarDarkMode(hwnd, true);
                FastTheme.setCornerStyle(hwnd, 2); // Windows 11 Rounded corners
                FastTheme.setSystemBackdropType(hwnd, FastTheme.BACKDROP_ACRYLIC);
            }

            System.out.println("[AcrylicDemo] Window is now visible. Close window to exit.");

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
    }
}
