package fasttheme;

import fastwindow.FastNativeWindow;
import fastwindow.FastWindow;

/**
 * Windows 11 Mica Alt (Tabbed) Demo powered by FastWindow and FastTheme.
 * Creates a standalone native Win32 window with real Windows 11 Mica Alt backdrop and dark titlebar.
 */
public class MicaAltDemo {
    public static void main(String[] args) {
        try (FastNativeWindow window = FastWindow.create("FastTheme — Windows 11 Mica Alt (Tabbed)", 900, 560)) {
            long hwnd = window.getHWND();
            System.out.println("[MicaAltDemo] Window created successfully, HWND = " + hwnd);

            // Display window first so DWM surface exists
            window.setVisible(true);

            if (hwnd != 0) {
                // Let Windows 11 Mica Alt material flow across both titlebar and body
                FastTheme.setTitleBarDarkMode(hwnd, true);
                FastTheme.setCornerStyle(hwnd, 2); // Windows 11 Rounded corners
                FastTheme.setSystemBackdropType(hwnd, FastTheme.BACKDROP_MICA_ALT);
            }

            System.out.println("[MicaAltDemo] Window is now visible. Close window to exit.");

            long lastFpsTime = System.nanoTime();
            int frames = 0;

            while (window.pollEvents()) {
                frames++;
                long now = System.nanoTime();
                if (now - lastFpsTime >= 1_000_000_000L) {
                    window.setTitle("FastTheme Mica Alt Backdrop (Win11) - FPS: " + frames);
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
