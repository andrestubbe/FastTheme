package fasttheme.demo;

import fasttheme.FastTheme;
import fasttheme.ThemeKeys;
import fasttheme.ThemeData;
import fasttheme.ThemeParser;
import fasttheme.ThemeColorUtil;

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
                // Let Windows 11 Acrylic material flow across both titlebar and body
                FastTheme.setTitleBarDarkMode(hwnd, true);
                FastTheme.setCornerStyle(hwnd, 2); // Windows 11 Rounded corners
                FastTheme.setSystemBackdropType(hwnd, FastTheme.BACKDROP_ACRYLIC);
            }

            // Display window only after styling is applied to prevent initial white flash
            window.setVisible(true);

            System.out.println("[AcrylicDemo] Window is now visible. Close window to exit.");

            while (window.pollEvents()) {
                try {
                    Thread.sleep(16); // Event tick (~60 Hz)
                } catch (InterruptedException ignored) {}
            }
        }
    }
}
