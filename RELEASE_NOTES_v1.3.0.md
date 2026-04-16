# FastTheme v1.3.0 - Generic Window Styling API

## What's New

### Generic JNI API
- Renamed all JNI methods from `TerminalDemo_*` to `FastTheme_*`
- Window styling API is now generic and usable by any application
- No longer tied to specific demo classes

### New Static Native Methods in `FastTheme.java`
- `getWindowHandle(Component)` - Extract native HWND
- `setWindowTransparency(hwnd, alpha)` - Window opacity
- `setTitleBarColor(hwnd, r, g, b)` - Titlebar background
- `setTitleBarTextColor(hwnd, r, g, b)` - Titlebar text
- `setTitleBarDarkMode(hwnd, enabled)` - Dark mode toggle
- `getSystemResolution()` - Screen resolution
- `getSystemDPI()` - DPI scaling
- `isSystemDarkMode()` - Dark mode detection
- `getSystemRefreshRate()` - Display refresh rate

### Documentation
- Complete Doxygen documentation for all C++ JNI exports
- Added documentation for helper functions (JAWT loading, HWND extraction)

### Updated Examples
- `TerminalDemo` now uses `FastTheme.*` static methods
- Updated to v1.3.0 dependency

## Installation

### Maven
```xml
<dependency>
    <groupId>io.github.andrestubbe</groupId>
    <artifactId>fasttheme</artifactId>
    <version>1.3.0</version>
</dependency>
```

### Gradle
```kotlin
implementation("io.github.andrestubbe:fasttheme:1.3.0")
```

## Changes

**Breaking Changes:**
- JNI method names changed from `Java_fasttheme_TerminalDemo_*` to `Java_fasttheme_FastTheme_*`
- `FastThemeTerminal.java` removed (functionality merged into `FastTheme.java`)

**Migration:**
Replace native method declarations in your code with calls to `FastTheme.*` static methods:
```java
// Old (v1.2):
public native long getWindowHandle(Component c);
long hwnd = getWindowHandle(frame);

// New (v1.3):
long hwnd = FastTheme.getWindowHandle(frame);
```
