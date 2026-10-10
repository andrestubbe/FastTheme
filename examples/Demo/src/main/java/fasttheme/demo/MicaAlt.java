package fasttheme.demo;

import fasttheme.FastTheme;
import fasttheme.ThemeKeys;
import fasttheme.ThemeData;
import fasttheme.ThemeParser;
import fasttheme.ThemeColorUtil;

import fastwindow.FastNativeWindow;
import fastwindow.FastWindow;

/**
 * Windows 11 Mica Alt (Tabbed) Demo powered by FastWindow and FastTheme.
 * Creates a standalone native Win32 window with real Windows 11 Mica Alt backdrop and dark titlebar.
 */
public class MicaAlt {
    public static void main(String[] args) {
        try (FastNativeWindow window = FastWindow.create("FastTheme — Windows 11 Mica Alt (Tabbed)", 900, 560)) {
            long hwnd = window.getHWND();
            System.out.println("[MicaAltDemo] Window created successfully, HWND = " + hwnd);

            if (hwnd != 0) {
                // Let Windows 11 Mica Alt material flow across both titlebar and body
                FastTheme.setTitleBarDarkMode(hwnd, true);
                FastTheme.setCornerStyle(hwnd, 2); // Windows 11 Rounded corners
                FastTheme.setSystemBackdropType(hwnd, FastTheme.BACKDROP_MICA_ALT);
            }

            // Display window only after styling is applied to prevent initial white flash
            window.setVisible(true);

            System.out.println("[MicaAltDemo] Window is now visible. Close window to exit.");

            while (window.pollEvents()) {
                try {
                    Thread.sleep(16); // Event tick (~60 Hz)
                } catch (InterruptedException ignored) {}
            }
        }
    }
}
