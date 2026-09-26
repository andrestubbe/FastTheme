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

            if (hwnd != 0) {
                // Apply Windows 11 Dark Mode, Titlebar, and matching Body Background
                FastTheme.setTitleBarDarkMode(hwnd, true);
                FastTheme.setTitleBarColor(hwnd, 20, 20, 20);
                FastTheme.setTitleBarTextColor(hwnd, 240, 240, 240);
                FastTheme.setWindowBackgroundColor(hwnd, 20, 20, 20);
                FastTheme.setCornerStyle(hwnd, 2); // Windows 11 Rounded corners
                FastTheme.setSystemBackdropType(hwnd, FastTheme.BACKDROP_ACRYLIC);
            }

            // Display window seamlessly
            window.setVisible(true);
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
