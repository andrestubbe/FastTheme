# FastTheme API Reference

This document outlines the API contracts, data structures, and methods of the **FastTheme** engine (version 0.1.4).

---

## 1. Class: `fasttheme.FastTheme`

Primary facade for both the Dynamic Theme State Engine and the native Windows DWM styling bridge.

### Dynamic Theme State Methods

*   `public static void load(String text)`
    *   **Description**: Parses and globally activates a `.theme` formatted text definition.
*   `public static void load(byte[] binaryData)`
    *   **Description**: Deserializes and globally activates a `.themebin` binary payload.
*   `public static void set(ThemeData theme)`
    *   **Description**: Activates a `ThemeData` instance globally across the JVM and dispatches notifications to all registered listeners.
*   `public static ThemeData current()`
    *   **Description**: Returns the currently active `ThemeData` instance.
*   `public static int get(String keyName)`
    *   **Description**: Retrieves the 32-bit packed ARGB color integer for the given key name.
*   `public static int get(int slotIndex)`
    *   **Description**: Direct $O(1)$ primitive array read returning the 32-bit packed ARGB integer.
*   `public static java.awt.Color getColor(String keyName)` / `getColor(int slotIndex)`
    *   **Description**: Converts the resolved color to an AWT/Swing `java.awt.Color` object.
*   `public static void addListener(ThemeListener listener)` / `removeListener(ThemeListener listener)`
    *   **Description**: Subscribes/unsubscribes an observer for live theme change events.
*   `public static void applyToWindow(long hwnd)` / `applyToWindow(java.awt.Component component)`
    *   **Description**: Synchronizes native Windows DWM title bar caption, text, and background colors with the active theme.

### Native Win32 DWM Methods

*   `public static native long getWindowHandle(java.awt.Component component)`
*   `public static native long getConsoleWindowHandle()`
*   `public static native boolean setWindowTransparency(long hwnd, int alpha)`
*   `public static native boolean setWindowBackgroundColor(long hwnd, int r, int g, int b)`
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

Open dynamic key registry managing slot allocations on-the-fly.

*   `public static int slot(String keyName)`: Retrieves existing slot ID or registers a new slot index on demand.
*   `public static int register(String keyName)`: Explicit dynamic registration of a key name.
*   `public static int indexOf(String keyName)`: Translates key name to allocated slot ID, or `-1` if unregistered.
*   `public static String nameOf(int slotIndex)`: Translates slot ID to string key name.
*   `public static int count()`: Returns total number of currently registered dynamic slots.
*   `public static Map<String, Integer> getAllKeys()`: Unmodifiable map of all registered keys.

---

## 3. Class: `fasttheme.ThemeData`

Elastic in-memory primitive array storage for theme colors.

*   `public int get(String keyName)` / `get(int slotIndex)`: Returns 32-bit ARGB color value.
*   `public void set(String keyName, int argb)` / `set(int slotIndex, int argb)`: Updates color, expanding storage dynamically if needed.
*   `public int[] getRawValues()`: Direct access to underlying `int[]` array.
*   `public int capacity()`: Current allocated capacity.
*   `public byte[] toBinary()`: Serializes to `.themebin` format (magic `0x4654484D`).
*   `public String toText()`: Serializes to human-readable `.theme` format.
*   `public ThemeData copy()`: Deep clone.

---

## 4. Class: `fasttheme.ThemeParser`

*   `public static ThemeData parseText(String textContent)`: Parses `.theme` text, auto-registering any custom keys, and resolving variable aliases (`@KEY`).
*   `public static ThemeData parseBinary(byte[] bytes)`: Deserializes binary `.themebin` payload.
*   `public static ThemeData loadFromFile(String filePath)` / `loadFromFile(Path path)` / `loadFromFile(File file)`: Loads and auto-detects text vs. binary files.

---

## 5. Class: `fasttheme.ThemeColorUtil`

*   `public static int rgb(int r, int g, int b)` / `argb(int a, int r, int g, int b)`: Packs channels into ARGB int.
*   `public static int alpha(int argb)` / `red(int argb)` / `green(int argb)` / `blue(int argb)`: Unpacks channels.
*   `public static double luminance(int argb)`: WCAG 2.1 relative luminance score (0.0 to 1.0).
*   `public static double contrastRatio(int c1, int c2)`: Contrast score between two colors (1.0 to 21.0).
*   `public static int getContrastForeground(int bgArgb)`: Returns dark or white for guaranteed WCAG readability.
*   `public static int lighten(int argb, float amount)` / `darken(int argb, float amount)`: Mathematical tinting/shading.
*   `public static int blend(int c1, int c2, float t)`: Linear color interpolation.
*   `public static int parseColor(String str)`: Parses hex (`#RGB`, `#RRGGBB`, `#AARRGGBB`, `0xRRGGBB`) and comma-separated RGB(A).
