# FastTheme — Native Windows Theme, Titlebar Styling & Display API for Java [ALPHA]

**Lightweight alternative to FlatLAF when you only need real OS theme events, titlebar styling, opacity, and display metrics.**

[![WIP](https://img.shields.io/badge/status-WIP-yellow.svg)]()
[![Build](https://img.shields.io/badge/build-passing-brightgreen.svg)]()
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

FastTheme brings real Windows theming to Java applications: **Dark/Light Mode, Titlebar Styling, Window Opacity, and Display DPI** — all native, without replacing your Look-and-Feel.

![FastTheme Terminal Demo](docs/screenshot.png)

```java
// Quick Start — Window Styling with FastTheme API
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
            FastTheme.setTitleBarColor(hwnd, 12, 12, 12);    // Dark gray
            FastTheme.setTitleBarDarkMode(hwnd, true);       // Dark mode
            FastTheme.setWindowTransparency(hwnd, 230);      // 90% opacity
        }
    }
}

// Or: Monitor display changes
FastTheme theme = new FastTheme();
theme.setListener(new FastTheme.ThemeListener() {
    @Override
    public void onInitialState(int width, int height, int dpi, int refreshRate, 
                               FastTheme.Orientation orientation, boolean isDarkTheme) {
        System.out.println("Display: " + width + "x" + height + " @ " + dpi + "DPI, " + 
                          refreshRate + "Hz, " + (isDarkTheme ? "DARK" : "LIGHT"));
    }
    
    @Override
    public void onResolutionChanged(int width, int height, int dpi, int refreshRate) {
        System.out.println("Resolution changed: " + width + "x" + height);
    }
    
    @Override
    public void onOrientationChanged(FastTheme.Orientation o) {}
    
    @Override
    public void onThemeChanged(boolean dark) {
        System.out.println("Theme: " + (dark ? "DARK" : "LIGHT"));
    }
});
theme.startMonitoring();
```

FastTheme is a **minimal, native, fast** library that provides:

- **🎨 Native Window Styling** — Custom titlebar colors, transparency, and icons via DWM APIs
- **📊 Display Monitoring** — Real-time notifications for resolution, DPI, orientation, refresh rate changes
- **🌓 Theme Detection** — Windows dark/light mode detection

Unlike Swing Look & Feels that replace rendering, FastTheme controls the **native Windows chrome** — giving you true OS integration without sacrificing Swing's capabilities.

**Keywords:** java display monitor, screen resolution detection, dpi scaling detection, windows theme detection, dark mode detection java, display orientation monitor, refresh rate detection, jni native windows

---

## Table of Contents

- [Installation](#installation)
- [Why FastTheme?](#why-fasttheme)
- [Key Features](#key-features)
- [Quick Start](#quick-start)
- [Window Styling Demo](#window-styling-demo)
- [API Overview](#api-overview)
- [Use Cases](#use-cases)
- [Build from Source](#build-from-source)
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
    <version>v1.3.0</version>
</dependency>
```

FastCore is automatically included as a transitive dependency.

### Gradle (JitPack)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'io.github.andrestubbe:fasttheme:v1.3.0'
}
```

### Direct Download (Both JARs Required)

- `fasttheme-1.3.0.jar` — Main library with DLL
- `fastcore-1.0.0.jar` — [JNI loader](https://github.com/andrestubbe/FastCore/releases)

```bash
java -cp "fasttheme-1.3.0.jar:fastcore-1.0.0.jar:." YourApp
```

---

## Why FastTheme?

When searching for **Java Theme**, **Java Dark Mode**, **Java Windows Theme**, **Java Titlebar Styling**, or **FlatLAF alternatives**, you usually need one of two things:

**1. A complete UI framework** (FlatLAF) — replaces Swing's Look-and-Feel entirely

**2. Just real Windows theme info** for your own UI — without replacing anything

FastTheme is **Option 2**:
- **Minimal** — no UI framework, no overhead
- **Native** — real Windows API via JNI
- **Fast** — direct access to OS events
- **Non-intrusive** — works with your existing Swing/AWT code

**Use FastTheme when you want:**
- Native Dark/Light mode detection without changing your LAF
- Custom titlebar colors while keeping standard Swing components
- Real-time display metrics (DPI, resolution, refresh rate)
- Window transparency without complex workarounds

**Use FlatLAF when you want:**
- A complete, polished cross-platform Look-and-Feel
- Consistent UI across Windows, macOS, and Linux
- Pre-built themes and component styling

---

## Key Features

### 🎨 Native Window Styling (v1.1)
- **Titlebar Color** — Set any RGB color, seamless with content
- **Window Opacity** — 0-100% transparency via `SetLayeredWindowAttributes`
- **Dark Mode** — Native Windows dark/light titlebar
- **Custom Icons** — Programmatic icon generation

### 📊 Display Monitoring
- **Real-time detection** — Resolution, DPI, orientation, refresh rate
- **Instant callbacks** — `WM_DISPLAYCHANGE`, `WM_DPICHANGED` events
- **Complete metrics** — Scale %, DPI, orientation, refresh rate

### 🌓 Theme Detection
- **Dark/Light mode** — Windows theme detection via Registry
- **System colors** — Access to Windows accent colors (planned)

### ⚡ Technical
- **Zero dependencies** — Java 17+ and Windows only
- **Lightweight** — Minimal CPU/memory overhead
- **MIT licensed** — free for commercial use
- **Cross-platform Ready** — Windows implementation, extensible for Linux/macOS

---

## Quick Start

```java
import fasttheme.FastTheme;

public class Main {
    public static void main(String[] args) throws Exception {
        FastTheme theme = new FastTheme();
        
        theme.setListener(new FastTheme.ThemeListener() {
            @Override
            public void onInitialState(int width, int height, int dpi, int refreshRate,
                                       FastTheme.Orientation orientation, boolean isDarkTheme) {
                int scalePercent = (dpi * 100) / 96;
                System.out.println("INIT: " + width + "x" + height + 
                                 " | Scale: " + scalePercent + "% (DPI: " + dpi + ")" +
                                 " | " + refreshRate + "Hz" +
                                 " | " + orientation +
                                 " | Theme: " + (isDarkTheme ? "DARK" : "LIGHT"));
            }
            
            @Override
            public void onResolutionChanged(int width, int height, int dpi, int refreshRate) {
                int scalePercent = (dpi * 100) / 96;
                String orientation = (width > height) ? "LANDSCAPE" : "PORTRAIT";
                System.out.println("EVENT: Resolution changed to " + width + "x" + height + 
                                 " | Scale: " + scalePercent + "% (DPI: " + dpi + ")" +
                                 " | " + refreshRate + "Hz" +
                                 " | " + orientation);
            }
            
            @Override
            public void onOrientationChanged(FastTheme.Orientation orientation) {
                // Orientation changes included in resolution event
            }
            
            @Override
            public void onThemeChanged(boolean isDarkTheme) {
                System.out.println("EVENT: Theme changed to " + (isDarkTheme ? "DARK" : "LIGHT") + " mode");
            }
        });

        if (theme.startMonitoring()) {
            System.out.println("Monitoring started...");
            System.in.read(); // Press Enter to stop
            theme.stopMonitoring();
        }
    }
}
```

---

## Window Styling API

FastTheme v1.3.0 provides a **generic window styling API** via static native methods. Works with any Swing/AWT window.

### Basic Window Styling

```java
import fasttheme.FastTheme;
import javax.swing.*;
import java.awt.*;

public class StyledWindow {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Styled Window");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Content color (dark gray)
        JPanel content = new JPanel();
        content.setBackground(new Color(12, 12, 12));
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

### System Information

```java
// Get real system values
String resolution = FastTheme.getSystemResolution();  // "1920x1080"
int dpi = FastTheme.getSystemDPI();                   // 96, 120, 144...
int refresh = FastTheme.getSystemRefreshRate();       // 60, 120, 144...
boolean dark = FastTheme.isSystemDarkMode();          // true/false
```

**Window Styling Features:**
- 🎨 **Titlebar Color** — RGB background color for seamless design
- ✏️ **Titlebar Text** — RGB text color for the window title
- 🌙 **Dark Mode** — Native Windows dark/light titlebar
- 🔲 **Transparency** — 0-100% window opacity

**System Detection Features:**
- 📊 **Resolution** — Current screen resolution
- 🔍 **DPI Scaling** — System DPI value (96 = 100%)
- 🔄 **Refresh Rate** — Display refresh rate in Hz
- � **Theme Detection** — Windows dark/light mode

**Native APIs Used:**
- `DwmSetWindowAttribute` (DWMWA_CAPTION_COLOR, DWMWA_TEXT_COLOR, DWMWA_USE_IMMERSIVE_DARK_MODE)
- `SetLayeredWindowAttributes` (WS_EX_LAYERED)
- `GetDeviceCaps` / `EnumDisplaySettings` (DPI, Resolution, Refresh Rate)
- `RegQueryValueEx` (Theme detection)

---

## API Reference

### Display Monitoring

| Method | Description |
|--------|-------------|
| `void setListener(ThemeListener listener)` | Set the event listener for display/theme changes |
| `boolean startMonitoring()` | Start monitoring (creates background thread) |
| `void stopMonitoring()` | Stop monitoring and release resources |

### Window Styling (Static Methods)

All window styling methods are `public static native` and can be called from any thread:

| Method | Parameters | Returns | Description |
|--------|-----------|---------|-------------|
| `getWindowHandle` | `Component component` | `long` | Extract native HWND from Swing window |
| `setWindowTransparency` | `long hwnd, int alpha` | `boolean` | Set window opacity (0-255) |
| `setTitleBarColor` | `long hwnd, int r, int g, int b` | `boolean` | Set titlebar background color |
| `setTitleBarTextColor` | `long hwnd, int r, int g, int b` | `boolean` | Set titlebar text color |
| `setTitleBarDarkMode` | `long hwnd, boolean enabled` | `boolean` | Enable/disable dark mode |
| `getSystemResolution` | — | `String` | Current resolution (e.g., "1920x1080") |
| `getSystemDPI` | — | `int` | System DPI (96 = 100% scaling) |
| `getSystemRefreshRate` | — | `int` | Display refresh rate in Hz |
| `isSystemDarkMode` | — | `boolean` | Windows dark mode status |

### ThemeListener Interface

| Method | Description |
|--------|-------------|
| `void onInitialState(int w, int h, int dpi, int refresh, Orientation o, boolean dark)` | Called once on startup |
| `void onResolutionChanged(int w, int h, int dpi, int refresh)` | Resolution or DPI changed |
| `void onOrientationChanged(Orientation o)` | Display orientation changed |
| `void onThemeChanged(boolean dark)` | Windows theme changed |

### Orientation Enum

| Value | Description |
|-------|-------------|
| `LANDSCAPE` | Normal landscape (0°) |
| `PORTRAIT` | Portrait (90° clockwise) |
| `LANDSCAPE_FLIPPED` | Flipped landscape (180°) |
| `PORTRAIT_FLIPPED` | Flipped portrait (270°) |

---

## Use Cases

- **Adaptive UI Applications** — Automatically adjust layouts when DPI or resolution changes
- **Multi-Monitor Apps** — Detect display configuration changes
- **Tablet/Convertible Apps** — Handle orientation switches (landscape ↔ portrait)
- **Gaming Overlays** — Match refresh rate for smooth rendering
- **Theme-Aware Apps** — Apply dark/light theme based on OS preference
- **System Monitoring Tools** — Track display configuration over time

---

## Platform Support

| Platform | Status | Notes |
|----------|--------|-------|
| Windows 10/11 | ✅ Supported | Full feature set via JNI |
| Linux | 🚧 Planned | X11/Wayland implementation |
| macOS | 🚧 Planned | macOS display API implementation |

---

## Build from Source

### Prerequisites

- Java 17+ JDK
- Visual Studio 2022 (or Build Tools)
- Maven 3.8+

### Windows Build

```bash
# Using the provided batch file (compiles Java + native DLL)
compile.bat

# Or with Maven
mvn clean package
```

### Project Structure

```
FastTheme/
├── src/main/
│   └── java/fasttheme/
│       └── FastTheme.java           # Main API class
│   └── resources/native/
│       └── fasttheme.dll            # Native JNI library
├── native/
│   ├── FastTheme.cpp                # JNI implementation
│   ├── FastTheme.h                  # JNI header
│   └── FastTheme.def                # DLL exports
├── examples/
│   └── 00-basic-usage/
│       └── src/main/java/fasttheme/
│           ├── WindowStylingDemo.java  # Window styling demo
│           └── ConsoleDemo.java        # Display monitoring demo
├── compile.bat                      # Windows build script
├── pom.xml                          # Maven configuration
└── README.md                        # This file
```

---

## Changelog

### v1.3.0 — Generic Window Styling API
- ✨ **Generic JNI API** — Renamed JNI methods to `FastTheme_*`
- ✨ **Static Native Methods** — All window styling via `FastTheme.*` static methods
- ✨ **Doxygen Documentation** — Complete C++ API documentation
- ✨ **Java Documentation** — Comprehensive Javadoc for all classes

### v1.2.0 — Initial Window Styling
- ✨ **TitleBar Color** — Custom RGB titlebar colors
- ✨ **TitleBar Text** — Custom titlebar text color
- ✨ **Dark Mode** — Native Windows dark/light mode toggle
- ✨ **Transparency** — Window opacity control
- ✨ **System Detection** — Resolution, DPI, refresh rate, theme detection

### v1.0.0 — Display Monitoring
- ✨ **Display Monitoring** — Real-time resolution, DPI, orientation changes
- ✨ **Theme Detection** — Windows dark/light mode detection

## Roadmap

- [ ] **Real-time Theme Change Events** — Runtime dark/light mode switching detection
- [ ] **Button Colors** — Control Windows caption button colors
- [ ] **Multi-Monitor Support** — Per-display settings detection
- [ ] **Linux Support** — X11/Wayland display monitoring
- [ ] **macOS Support** — macOS display configuration monitoring

---

## License

MIT License — See [LICENSE](LICENSE) file for details.

---

## Related Projects

- [FastRobot](https://github.com/andrestubbe/fastrobot) — High-performance Java automation & screen capture

---

**Made with ⚡ by the FastJava Team**
