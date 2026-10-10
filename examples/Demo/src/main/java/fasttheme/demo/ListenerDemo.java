package fasttheme.demo;

import fasttheme.FastTheme;
import fasttheme.SystemThemeListener;
import fastwindow.FastNativeWindow;
import fastwindow.FastWindow;

/**
 * Live Windows 11 System Theme & Personalization Change Listener Demo.
 *
 * Demonstrates 100% interrupt-driven, event-based OS theme switching:
 * When you change Windows Light/Dark mode in Windows Settings -> Personalization -> Colors,
 * or change your Windows Accent Color, this demo immediately receives the event
 * and dynamically updates the window titlebar and backdrop!
 */
public class ListenerDemo {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println(" FastTheme - Live OS Theme Change Listener Demo ");
        System.out.println("=================================================");
        System.out.println();
        System.out.println("Switch Dark/Light mode in Windows Settings (Win+I -> Personalization -> Colors)");
        System.out.println("Watch the window and console react instantly!");
        System.out.println();

        try (FastNativeWindow window = FastWindow.create("FastTheme — Live OS Theme Listener", 800, 500)) {
            long hwnd = window.getHWND();
            System.out.println("[ListenerDemo] Window created, HWND = " + hwnd);

            // Initial style
            boolean currentDark = FastTheme.isAppDarkMode();
            int currentAccent = FastTheme.getAccentColor();
            System.out.printf("[ListenerDemo] Initial OS State: Dark=%b, Accent=0x%08X%n", currentDark, currentAccent);

            if (hwnd != 0) {
                FastTheme.setTitleBarDarkMode(hwnd, currentDark);
                FastTheme.setCornerStyle(hwnd, 2); // Rounded
                FastTheme.setSystemBackdropType(hwnd, FastTheme.BACKDROP_MICA);
            }

            window.setVisible(true);

            // Register OS-level event listener (Win32 Push Message Queue)
            SystemThemeListener listener = (isDark, accentColor) -> {
                System.out.println();
                System.out.println(">>> [EVENT] WINDOWS THEME CHANGED! <<<");
                System.out.printf("    Mode   : %s%n", (isDark ? "DARK MODE" : "LIGHT MODE"));
                System.out.printf("    Accent : 0x%08X%n", accentColor);

                if (hwnd != 0) {
                    // Update DWM Titlebar immediately
                    FastTheme.setTitleBarDarkMode(hwnd, isDark);
                    // Re-apply backdrop
                    FastTheme.setSystemBackdropType(hwnd, FastTheme.BACKDROP_MICA);
                    FastTheme.forceFrameUpdate(hwnd);
                }
            };

            FastTheme.addSystemThemeListener(listener);

            try {
                while (window.pollEvents()) {
                    try {
                        Thread.sleep(16);
                    } catch (InterruptedException ignored) {}
                }
            } finally {
                FastTheme.removeSystemThemeListener(listener);
                System.out.println("[ListenerDemo] Unregistered listener and exiting.");
            }
        }
    }
}
