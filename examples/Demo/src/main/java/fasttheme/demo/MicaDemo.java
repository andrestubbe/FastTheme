package fasttheme.demo;

import fasttheme.FastTheme;
import fasttheme.ThemeKeys;
import fasttheme.ThemeData;
import fasttheme.ThemeParser;
import fasttheme.ThemeColorUtil;

import fastwindow.FastNativeWindow;
import fastwindow.FastWindow;

/**
 * Windows 11 Mica Material Demo powered by FastWindow and FastTheme.
 * Creates a standalone native Win32 window with real Windows 11 Mica backdrop and dark titlebar.
 */
public class MicaDemo {
    public static void main(String[] args) {
        // Console stays visible (transparent hiding removed so errors/logs remain readable)

        try (FastNativeWindow window = FastWindow.create("FastTheme — Windows 11 Mica Material", 900, 560)) {
            long hwnd = window.getHWND();
            System.out.println("[MicaDemo] Window created successfully, HWND = " + hwnd);

            // Display window first so DWM surface exists
            window.setVisible(true);

            if (hwnd != 0) {
                // Let Windows 11 Mica material flow across both titlebar and body
                FastTheme.setTitleBarDarkMode(hwnd, true);
                FastTheme.setCornerStyle(hwnd, 2); // Windows 11 Rounded corners
                FastTheme.setSystemBackdropType(hwnd, FastTheme.BACKDROP_MICA);
            }

            System.out.println("[MicaDemo] Window is now visible. Close window to exit.");

            while (window.pollEvents()) {
                try {
                    Thread.sleep(16); // Event tick (~60 Hz)
                } catch (InterruptedException ignored) {}
            }
        }
    }
}
