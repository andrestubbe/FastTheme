# FastTheme — OS-Aware Display & Theme Monitor for Java

**⚡ Real-time OS resolution, DPI scale, orientation, refresh rate, and theme detection for Java**

[![Build](https://img.shields.io/badge/build-passing-brightgreen.svg)]()
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

```java
// Quick Start — Monitor display changes
FastTheme theme = new FastTheme();
theme.setListener(new FastTheme.ThemeListener() {
    @Override
    public void onInitialState(int width, int height, int dpi, int refreshRate, 
                               FastTheme.Orientation orientation, boolean isDarkTheme) {
        System.out.println("Display: " + width + "x" + height + " @ " + dpi + "DPI, " + 
                          refreshRate + "Hz, " + orientation + ", Theme: " + 
                          (isDarkTheme ? "DARK" : "LIGHT"));
    }
    
    @Override
    public void onResolutionChanged(int width, int height, int dpi, int refreshRate) {
        System.out.println("Resolution changed to " + width + "x" + height);
    }
    
    @Override
    public void onThemeChanged(boolean isDarkTheme) {
        System.out.println("Theme changed to " + (isDarkTheme ? "DARK" : "LIGHT"));
    }
});
theme.startMonitoring();
```

FastTheme is a **lightweight Java library** that monitors OS display settings through a **native Windows backend** using JNI. It provides real-time notifications for resolution changes, DPI scaling, display orientation, refresh rate, and system theme (dark/light mode).

**Keywords:** java display monitor, screen resolution detection, dpi scaling detection, windows theme detection, dark mode detection java, display orientation monitor, refresh rate detection, jni native windows

---

## Table of Contents

- [Features](#features)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [API Reference](#api-reference)
- [Build from Source](#build-from-source)
- [TODO](#todo)
- [License](#license)

---

## Features

- ✅ **Initial State Detection** — Get current resolution, DPI, orientation, refresh rate, and theme on startup
- ✅ **Resolution Change Events** — Detect when display resolution changes
- ✅ **DPI Scale Detection** — Monitor Windows DPI/scaling changes (100%, 125%, 150%, etc.)
- ✅ **Orientation Support** — Detect LANDSCAPE, PORTRAIT, LANDSCAPE_FLIPPED, PORTRAIT_FLIPPED
- ✅ **Refresh Rate Detection** — Get current display refresh rate (60Hz, 120Hz, 144Hz, etc.)
- ✅ **Theme Detection** — Detect Windows dark/light mode (initial state)
- ✅ **Lightweight** — Minimal overhead, native Windows API via JNI
- ✅ **Cross-platform Ready** — Windows implementation, extensible for Linux/macOS

---

## Installation

### Maven

```xml
<dependency>
    <groupId>io.github.andrestubbe</groupId>
    <artifactId>fasttheme</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Manual

Download the `FastTheme.jar` from releases and include it in your classpath:

```bash
java -cp FastTheme.jar:your-app.jar YourMainClass
```

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
│       ├── java/fasttheme/     # Java sources
│       │   ├── FastTheme.java  # Main class
│       │   └── demo.java       # Demo application
│       └── resources/          # Native DLL
├── native/
│   ├── FastTheme.cpp           # JNI implementation
│   └── fasttheme_FastTheme.h   # JNI header
├── build/                      # Build output
├── compile.bat                 # Windows build script
├── pom.xml                     # Maven configuration
└── README.md                   # This file
```

---

## TODO

- [ ] **Real-time Theme Change Events** — Currently only initial theme is detected. Runtime theme change detection (dark/light mode switching) is being investigated. Windows sends `WM_SETTINGCHANGE` before registry updates, making this challenging.
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
