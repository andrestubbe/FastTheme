# FastTheme — Native Windows Theme, Titlebar Styling & Display API for Java

**Lightweight alternative to FlatLAF when you only need real OS theme events, titlebar styling, opacity, and display metrics.**

[![WIP](https://img.shields.io/badge/status-WIP-yellow.svg)]()
[![Build](https://img.shields.io/badge/build-passing-brightgreen.svg)]()
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

FastTheme brings real Windows theming to Java applications: **Dark/Light Mode, Titlebar Styling, Window Opacity, and Display DPI** — all native, without replacing your Look-and-Feel.

```java
// Quick Start — Native Window Styling
FastThemeTerminal terminal = new FastThemeTerminal();
terminal.setVisible(true);

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

## Window Styling Demo

FastTheme now includes **native window styling** capabilities via JNI, allowing you to create seamless, transparent windows that blend with Windows 11's design language.

```java
import fasttheme.FastThemeTerminal;

public class StyledWindow {
    public static void main(String[] args) {
        // Create a terminal-like window with:
        // - Seamless titlebar (matches content color)
        // - 80% transparency
        // - Native dark mode
        // - Real-time system detection
        new FastThemeTerminal().setVisible(true);
    }
}
```

**Features:**
- 🎨 **Titlebar Color** — Set any RGB color for the window titlebar
- 🔲 **Transparency** — 0-100% opacity control via `SetLayeredWindowAttributes`
- 🌙 **Dark Mode** — Native Windows dark/light mode support
- 📊 **System Detection** — Real-time resolution, DPI, refresh rate, theme
- 🎯 **Custom Icon** — Programmatic window icon generation

**Native APIs Used:**
- `DwmSetWindowAttribute` (DWMWA_CAPTION_COLOR, DWMWA_TEXT_COLOR, DWMWA_USE_IMMERSIVE_DARK_MODE)
- `SetLayeredWindowAttributes` (WS_EX_LAYERED)
- `EnumDisplaySettings` (Resolution, DPI, Refresh Rate)

---

## API Reference

### FastTheme Class

| Method | Description |
|--------|-------------|
| `void setListener(ThemeListener listener)` | Set the event listener |
| `boolean startMonitoring()` | Start monitoring (returns true on success) |
| `void stopMonitoring()` | Stop monitoring |

### ThemeListener Interface

| Method | Description |
|--------|-------------|
| `void onInitialState(...)` | Called once on startup with current state |
| `void onResolutionChanged(...)` | Called when resolution or DPI changes |
| `void onOrientationChanged(...)` | Called when display orientation changes |
| `void onThemeChanged(boolean)` | Called when Windows theme changes (TODO) |

### Orientation Enum

- `LANDSCAPE` — Normal landscape orientation
- `PORTRAIT` — Portrait (90° rotated)
- `LANDSCAPE_FLIPPED` — Landscape flipped 180°
- `PORTRAIT_FLIPPED` — Portrait flipped 180°

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
├── src/
│   └── main/
│       ├── java/fasttheme/       # Java sources
│       │   ├── FastTheme.java    # Main class
│       │   ├── FastThemeTerminal.java  # 🆕 Window styling demo
│       │   └── Demo.java         # Demo application
│       └── resources/            # Native DLL
├── native/
│   ├── FastTheme.cpp             # JNI implementation
│   ├── FastThemeJNI.cpp          # 🆕 Window styling JNI
│   └── fasttheme_FastTheme.h     # JNI header
├── lib/                          # 🆕 DLL output
├── build/                        # Build output
├── compile.bat                   # Windows build script
├── pom.xml                       # Maven configuration
└── README.md                     # This file
```

---

## TODO

- [ ] **Real-time Theme Change Events** — Currently only initial theme is detected. Runtime theme change detection (dark/light mode switching) is being investigated. Windows sends `WM_SETTINGCHANGE` before registry updates, making this challenging.
- [x] **TitleBar Color Control** — ✅ Done v1.1 — Set custom titlebar colors via DWM
- [x] **Window Transparency** — ✅ Done v1.1 — Layered window with alpha blending
- [ ] **Button Colors** — Control Windows caption button colors
- [ ] **Button Existence** — Detect if Windows has specific buttons (close, minimize, maximize)
- [ ] **Linux Support** — Add X11/Wayland display monitoring
- [ ] **macOS Support** — Add macOS display configuration monitoring
- [ ] **Multi-Monitor Support** — Detect per-display settings

---

## License

MIT License — See [LICENSE](LICENSE) file for details.

---

## Related Projects

- [FastRobot](https://github.com/andrestubbe/fastrobot) — High-performance Java automation & screen capture

---

**Made with ⚡ by the FastJava Team**
