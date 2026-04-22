# FastTheme — Native Windows Theme & Window Styling API for Java

**Lightweight native Windows window styling for Java applications.**

[![Build](https://img.shields.io/badge/build-passing-brightgreen.svg)]()
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

FastTheme provides **native Windows window styling** for Java applications: **Titlebar Coloring, Dark Mode, Window Transparency** — all native via JNI, without replacing your Look-and-Feel.

```java
// Quick Start — Window Styling with FastTheme v2.0
import fasttheme.FastTheme;
import javax.swing.*;

public class StyledWindow {
    public static void main(String[] args) {
        JFrame frame = new JFrame("FastTheme Window");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true); // Must be visible first!
        
        // Apply native styling
        long hwnd = FastTheme.getWindowHandle(frame);
        if (hwnd != 0) {
            FastTheme.setTitleBarColor(hwnd, 12, 12, 12);      // Dark gray
            FastTheme.setTitleBarDarkMode(hwnd, true);         // Dark mode
            FastTheme.setWindowTransparency(hwnd, 230);        // 90% opacity
        }
    }
}
```

FastTheme v2.0 is a **minimal, native, fast** library that provides:

- **🎨 Native Window Styling** — Custom titlebar colors, transparency, and dark mode via DWM APIs
- **🌓 Theme Detection** — Windows dark/light mode detection
- **⚡ Zero Dependencies** — Java 17+ and Windows only

> **Note:** Display monitoring (DPI, resolution, refresh rate) has been moved to [FastDisplay](https://github.com/andrestubbe/FastDisplay).

---

## Table of Contents

- [Installation](#installation)
- [Key Features](#key-features)
- [Quick Start](#quick-start)
- [API Reference](#api-reference)
- [Platform Support](#platform-support)
- [Roadmap](#roadmap)
- [License](#license)

---

## Installation

### Maven (JitPack)

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>io.github.andrestubbe</groupId>
    <artifactId>fasttheme</artifactId>
    <version>v2.0.0</version>
</dependency>
<dependency>
    <groupId>io.github.andrestubbe</groupId>
    <artifactId>fastcore</artifactId>
    <version>v1.0.0</version>
</dependency>
```

> **Note:** FastCore handles native library loading from the JAR automatically. Both dependencies are required.

### Gradle (JitPack)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'io.github.andrestubbe:fasttheme:v2.0.0'
    implementation 'io.github.andrestubbe:fastcore:v1.0.0'
}
```

---

## Key Features

### 🎨 Native Window Styling
- **Titlebar Color** — Set any RGB color, seamless with content
- **Window Opacity** — 0-100% transparency via `SetLayeredWindowAttributes`
- **Dark Mode** — Native Windows dark/light titlebar
- **Titlebar Text** — Custom titlebar text color

### 🌓 Theme Detection
- **Dark/Light mode** — Windows theme detection via Registry

### ⚡ Technical
- **Zero dependencies** — Java 17+ and Windows only
- **Lightweight** — Minimal CPU/memory overhead
- **MIT licensed** — Free for commercial use
- **Thread-safe** — All methods can be called from any thread

---

## Quick Start

```java
import fasttheme.FastTheme;
import javax.swing.*;

public class StyledWindow {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Styled Window");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Content color (dark gray)
        JPanel content = new JPanel();
        content.setBackground(new java.awt.Color(12, 12, 12));
        frame.setContentPane(content);
        
        frame.setVisible(true); // Must be visible first!
        
        // Apply native styling
        long hwnd = FastTheme.getWindowHandle(frame);
        if (hwnd != 0) {
            FastTheme.setTitleBarColor(hwnd, 12, 12, 12);      // Match content
            FastTheme.setTitleBarTextColor(hwnd, 255, 255, 255); // White text
            FastTheme.setTitleBarDarkMode(hwnd, true);         // Dark mode
            FastTheme.setWindowTransparency(hwnd, 204);        // 80% opacity
        }
    }
}
```

### Theme Detection

```java
// Check Windows theme
boolean isDark = FastTheme.isSystemDarkMode();
if (isDark) {
    // Apply dark theme to your app
}
```

---

## API Reference

All methods are `public static native` and can be called from any thread:

| Method | Parameters | Returns | Description |
|--------|-----------|---------|-------------|
| `getWindowHandle` | `Component component` | `long` | Extract native HWND from Swing window |
| `setWindowTransparency` | `long hwnd, int alpha` | `boolean` | Set window opacity (0-255) |
| `setTitleBarColor` | `long hwnd, int r, int g, int b` | `boolean` | Set titlebar background color |
| `setTitleBarTextColor` | `long hwnd, int r, int g, int b` | `boolean` | Set titlebar text color |
| `setTitleBarDarkMode` | `long hwnd, boolean enabled` | `boolean` | Enable/disable dark mode |
| `isSystemDarkMode` | — | `boolean` | Windows dark mode status |

**Native APIs Used:**
- `DwmSetWindowAttribute` (DWMWA_CAPTION_COLOR, DWMWA_TEXT_COLOR, DWMWA_USE_IMMERSIVE_DARK_MODE)
- `SetLayeredWindowAttributes` (WS_EX_LAYERED)
- `RegQueryValueEx` (Theme detection)

---

## Platform Support

| Platform | Status | Notes |
|----------|--------|-------|
| Windows 10/11 | ✅ Supported | Full feature set via DWM APIs |
| Linux | 🚧 Planned | GTK/KWin implementation |
| macOS | 🚧 Planned | macOS AppKit implementation |

---

## Roadmap

- [ ] **Accent Color** — Read Windows accent colors
- [ ] **Button Colors** — Control Windows caption button colors
- [ ] **Mica/Acrylic** — Windows 11 material effects
- [ ] **Linux Support** — GTK/KWin window styling
- [ ] **macOS Support** — macOS window styling

---

## License

MIT License — See [LICENSE](LICENSE) file for details.

---

## Related Projects

- [FastDisplay](https://github.com/andrestubbe/FastDisplay) — Display Monitoring (DPI, Resolution, Refresh Rate)
- [FastCore](https://github.com/andrestubbe/FastCore) — Native Library Loader for Java

---

**Made with ⚡ by Andre Stubbe**
