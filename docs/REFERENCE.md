# FastTheme API Reference

This document outlines the API contracts, data structures, and methods of the **FastTheme** engine (version 0.1.3).

---

## 1. Class: `fasttheme.FastTheme`

Primary facade for both the Dynamic Theme State Engine and the native Windows DWM styling bridge.

### Dynamic Theme Methods

*   `public static void set(ThemeData theme)`
    *   **Description**: Activates a theme globally across the JVM and dispatches notifications to all registered listeners.
*   `public static ThemeData current()`
    *   **Description**: Returns the active `ThemeData` instance.
*   `public static int get(int slotIndex)`
    *   **Description**: Zero-allocation direct array read returning the 32-bit packed ARGB integer color.
*   `public static int get(String keyName)`
    *   **Description**: Returns 32-bit packed ARGB color by string key name.
*   `public static java.awt.Color getColor(int slotIndex)` / `getColor(String keyName)`
    *   **Description**: Converts the color to an AWT/Swing `java.awt.Color` object.
*   `public static String getAnsiFg(int slotIndex)` / `getAnsiBg(int slotIndex)`
    *   **Description**: Returns 24-bit Truecolor ANSI escape sequence string for CLI/TUI rendering.
*   `public static void addListener(ThemeListener listener)` / `removeListener(ThemeListener listener)`
    *   **Description**: Subscribes/unsubscribes an observer for live theme changes.
*   `public static void applyToWindow(long hwnd)` / `applyToWindow(java.awt.Component component)`
    *   **Description**: Synchronizes native Windows DWM title bar caption, text, and background colors with the active theme.

### Native Win32 DWM Methods

*   `public static native long getWindowHandle(java.awt.Component component)`
*   `public static native long getConsoleWindowHandle()`
*   `public static native boolean setWindowTransparency(long hwnd, int alpha)`
*   `public static native boolean setTitleBarDarkMode(long hwnd, boolean enabled)`
*   `public static native boolean setTitleBarColor(long hwnd, int r, int g, int b)`
*   `public static native boolean setTitleBarTextColor(long hwnd, int r, int g, int b)`
*   `public static native boolean enableMica(long hwnd, boolean enabled)`
*   `public static native boolean setCornerStyle(long hwnd, int style)`
*   `public static native boolean setBorderlessShadow(long hwnd, boolean enabled)`
*   `public static native boolean setOverlayDragHeight(long hwnd, int height)`
*   `public static native boolean isSystemDarkMode()`

---

## 2. Class: `fasttheme.ThemeKeys`

Defines 49 standardized slot IDs for instant $O(1)$ primitive array lookups.

*   `public static int indexOf(String keyName)`: Translates key name to slot ID.
*   `public static String nameOf(int slotIndex)`: Translates slot ID to key name.
*   **Slot Categories**:
    *   *Window & Frame*: `TITLE_BAR_BACKGROUND`, `TITLE_BAR_TEXT`, `TITLE_BAR_BORDER`, `WINDOW_BACKGROUND`, `WINDOW_BORDER`, `CONTENT_BACKGROUND`
    *   *Typography*: `TEXT_PRIMARY`, `TEXT_SECONDARY`, `TEXT_MUTED`, `TEXT_PLACEHOLDER`, `TEXT_INVERSE`
    *   *Accent & Brand*: `ACCENT_PRIMARY`, `ACCENT_SECONDARY`, `ACCENT_HOVER`, `ACCENT_PRESSED`
    *   *Buttons*: `BUTTON_NORMAL_BACKGROUND`, `BUTTON_NORMAL_FOREGROUND`, `BUTTON_NORMAL_BORDER`, `BUTTON_HOVER_*`, `BUTTON_PRESSED_*`, `BUTTON_DISABLED_*`
    *   *Inputs*: `INPUT_BACKGROUND`, `INPUT_FOREGROUND`, `INPUT_BORDER`, `INPUT_BORDER_FOCUS`, `EDITOR_LINE_NUMBER`, `EDITOR_SELECTION`
    *   *Navigation*: `TAB_BACKGROUND_NORMAL`, `TAB_BACKGROUND_SELECTED`, `TAB_BACKGROUND_HOVER`, `TAB_FOREGROUND_*`, `SPLIT_PANE_BAR`, `SCROLLBAR_THUMB`, `SCROLLBAR_TRACK`
    *   *Status*: `STATUS_SUCCESS`, `STATUS_WARNING`, `STATUS_ERROR`, `STATUS_INFO`
    *   *Popups & Tooltips*: `TOOLTIP_BACKGROUND`, `TOOLTIP_FOREGROUND`, `POPUP_BACKGROUND`, `POPUP_BORDER`, `POPUP_SHADOW`

---

## 3. Class: `fasttheme.ThemeData`

Contiguous primitive storage for theme colors.

*   `public int get(int slotIndex)`: Returns 32-bit ARGB value.
*   `public void set(int slotIndex, int argb)`: Updates color slot.
*   `public byte[] toBinary()`: Serializes to `.themebin` format (magic `0x4654484D`).
*   `public String toText()`: Serializes to human-readable `.theme` format.
*   `public ThemeData copy()`: Deep clone.

---

## 4. Class: `fasttheme.ThemeParser`

*   `public static ThemeData parseText(String textContent)`: Parses `.theme` text with variable aliasing (`@KEY`).
*   `public static ThemeData parseBinary(byte[] bytes)`: Deserializes binary `.themebin` payload.
*   `public static ThemeData loadFromFile(String filePath)`: Auto-detects text vs. binary files.
*   `public static ThemeData loadDefaultDark()`: Built-in dark preset.
*   `public static ThemeData loadDefaultLight()`: Built-in light preset.
*   `public static ThemeData loadDefaultCream()`: Built-in synthwave/TUI preset.

---

## 5. Class: `fasttheme.ThemeColorUtil`

*   `public static int rgb(int r, int g, int b)` / `argb(int a, int r, int g, int b)`: Packs components into ARGB int.
*   `public static double luminance(int argb)`: WCAG 2.1 relative luminance.
*   `public static double contrastRatio(int c1, int c2)`: Contrast score between two colors.
*   `public static int getContrastForeground(int bgArgb)`: Returns dark or white for guaranteed readability.
*   `public static int lighten(int argb, float amount)` / `darken(int argb, float amount)`: Mathematical tinting/shading.
*   `public static int blend(int c1, int c2, float t)`: Linear interpolation.
*   `public static String toAnsiForeground(int argb)` / `toAnsiBackground(int argb)`: 24-bit Truecolor terminal codes.
*   `public static int parseColor(String str)`: Parses hex (`#RRGGBB`, `0xRRGGBB`) and comma-separated RGB(A).
