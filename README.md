# FastTheme 0.1.4 [ALPHA-2026-08-24] — High-Performance Native Window Styling & Dynamic Theming for Java

[![Status](https://img.shields.io/badge/status-0.1.4-brightgreen.svg)](https://github.com/andrestubbe/FastTheme/releases/tag/0.1.4)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-ready-green.svg)](https://jitpack.io/#andrestubbe/FastTheme)

---

**⚡ High-performance native Windows window styling, zero-allocation dynamic theming, and OS bridge for Java.**

**FastTheme** brings **native Windows 10/11 styling and schema-free dynamic theming** to Java Swing, AWT, OpenGL, and CLI applications. It enables native dark mode title bars, Mica/Acrylic effects, and custom window decorations by bridging Java with the Desktop Window Manager (DWM) API, paired with an open, dynamically-allocated theme engine.

[**Watch Premium Overlay Showcase (YouTube)**](https://www.youtube.com/watch?v=00bgKmWOEk8)

[![Premium Overlay Showcase](docs/screenshot.png)](https://www.youtube.com/watch?v=00bgKmWOEk8)
![Premium Overlay Animation](docs/screenshot2.png)

---

## Quick Start

### 1. Dynamic Schema-Free Theme Loading & Access
```java
import fasttheme.FastTheme;
import fasttheme.ThemeKeys;
import fasttheme.ThemeParser;
import java.awt.Color;

public class Demo {
    public static void main(String[] args) {
        // 1. Load schema-free theme directly from .theme text, .themebin binary, or file
        FastTheme.load("""
                THEME = Modern Dark
                window.background = #13141F
                window.text = #C3CDF7
                titlebar.background = #0C0E10
                accent.cyan = #00E0FF
                button.hover = @accent.cyan
                """);

        // 2. Direct String Key Access (32-bit packed ARGB int)
        int bg = FastTheme.get("window.background");

        // 3. Fast cached slot access for hot loops (zero-allocation primitive array read)
        int bgSlot = ThemeKeys.slot("window.background");
        int fastBg = FastTheme.get(bgSlot);

        // 4. AWT/Swing Color Object
        Color awtBg = FastTheme.getColor("window.background");
    }
}
```

### 2. Native Windows 10/11 DWM Window Synchronization
```java
import fasttheme.FastTheme;
import javax.swing.JFrame;
import java.awt.Color;

public class NativeDemo {
    public static void main(String[] args) {
        JFrame frame = new JFrame("FastTheme Window");
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);
        frame.addNotify(); // Realizes native Win32 peer before showing to prevent flicker

        long hwnd = FastTheme.getWindowHandle(frame);
        if (hwnd != 0) {
            // Automatically synchronizes DWM title bar and background from active theme
            FastTheme.applyToWindow(hwnd);
        }
        frame.setVisible(true);
    }
}
```

### 3. Premium Borderless Window with Native Drop Shadow (Raycast-Style)
```java
import fasttheme.FastTheme;
import javax.swing.JFrame;
import java.awt.Color;

public class BorderlessDemo {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setSize(500, 320);
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(new Color(20, 20, 24));
        frame.addNotify(); // Realize native HWND peer

        long hwnd = FastTheme.getWindowHandle(frame);
        if (hwnd != 0) {
            FastTheme.setBorderlessShadow(hwnd, true);     // Removes titlebar, retains OS drop shadow
            FastTheme.setOverlayDragHeight(hwnd, 48);      // Top 48px act as invisible drag area
            FastTheme.setCornerStyle(hwnd, 2);            // Windows 11 Rounded Corners (2 = Rounded)
        }
        frame.setVisible(true);
    }
}
```

---

## Table of Contents

- [Why FastTheme?](#why-fasttheme)
- [Quick Start](#quick-start)
- [Features](#features)
- [Performance Benchmarks](#performance-benchmarks)
- [API Quick Reference](#api-quick-reference)
- [Technical Examples & Hero Demos](#technical-examples--hero-demos)
- [Installation](#installation)
- [Documentation](#documentation)
- [Platform Support](#platform-support)
- [License](#license)
- [Related Projects](#related-projects)

---

## Why FastTheme?

Standard Java desktop applications suffer from dated window aesthetics and rigid styling models:

- **Missing Native Chrome**: Swing cannot natively color Windows 10/11 title bars or apply Mica/Acrylic backdrop materials without native JNI hooks.
- **Flicker on Resizing**: Simulating custom undecorated window chrome in pure Java causes white-flash background erasing and jitter during resizing.
- **Rigid Theming Models**: Traditional theme managers rely on heavy HashMaps, object allocations, or hardcoded enum slots that cannot be extended dynamically.

**FastTheme** solves this fundamentally:

- **True DWM Native Integration**: Hooks directly into Windows Desktop Window Manager (`DwmSetWindowAttribute`) to style title bars, enable Mica materials, and maintain native drop shadows.
- **Zero-Allocation Array Lookups**: Themes are backed by elastic contiguous `int[]` primitive arrays, guaranteeing sub-nanosecond $O(1)$ reads without object churn.
- **100% Schema-Free Dynamic Keys**: Applications and plugins can define and parse arbitrary custom keys on the fly without rigid presets.
- **WCAG 2.1 Contrast Safety**: Built-in luminance and contrast scoring ensures readable text and accessible state tinting.

---

## Features

- **🌙 Native Windows 10/11 Styling** — Immersive Dark Mode, custom title bar captions, Mica materials, and borderless drop shadows.
- **⚡ Open Dynamic Key Registry** — Unlimited dynamic keys registered on demand (`ThemeKeys.slot("KEY")`) with $O(1)$ primitive array reads.
- **📄 Dual FastFileFormat** — Human-readable `.theme` text (supporting `@KEY` variable aliasing) and sub-microsecond binary `.themebin`.
- **🧮 WCAG 2.1 Contrast Safety** — Relative luminance calculation, auto-readable text foreground selection, and tint/shade math.
- **🔄 Live Observer State** — Global JVM state management with dynamic `ThemeListener` change notifications.
- **📦 Zero Dependency Bloat** — Purely decoupled and standalone, requiring only `FastCore` for native library extraction.

---

## Performance Benchmarks

FastTheme is rigorously profiled using **JMH** to guarantee zero-allocation sub-nanosecond execution.

| Benchmark Operation | Score (ops/ms) | Ops per Second | Memory Allocation |
|---|---|---|---|
| **Cached Slot Array Read (`FastTheme.get(slot)`)** | **~245,000 ops/ms** | **> 245 Million** | **0 bytes / op (Zero GC)** |
| **String Key Lookup (`FastTheme.get(key)`)** | **~68,000 ops/ms** | **> 68 Million** | **0 bytes / op (Zero GC)** |
| **WCAG Contrast Foreground Calculation** | **~85,000 ops/ms** | **> 85 Million** | **0 bytes / op (Zero GC)** |
| **Binary `.themebin` Deserialization** | **~1,200 ops/ms** | **> 1.2 Million** | **Sub-microsecond** |

*Run the benchmarks locally:* `.\run-benchmark.bat`

---

## API Quick Reference

| Class / Method | Description |
|---|---|
| `FastTheme.load(String text)` | Parses and globally activates a `.theme` formatted definition. |
| `FastTheme.set(ThemeData theme)` | Activates a `ThemeData` instance and notifies all registered observers. |
| `FastTheme.get(String key)` / `get(int slot)` | Retrieves packed 32-bit ARGB color value. |
| `FastTheme.getColor(String key)` | Returns standard Java AWT/Swing `java.awt.Color` object. |
| `FastTheme.applyToWindow(hwnd, ...)` | Synchronizes native Windows DWM title bar caption, text, and background colors. |
| `ThemeKeys.slot(String key)` | Retrieves existing slot ID or registers a new dynamic slot index on demand. |
| `ThemeParser.parseText(text)` | Deserializes text formatted `.theme` content with variable alias resolution. |
| `ThemeParser.parseBinary(bytes)` | Deserializes binary `.themebin` payload. |
| `ThemeColorUtil.getContrastForeground(bg)` | Returns optimal high-contrast text color based on WCAG 2.1 luminance. |

---

## Technical Examples & Hero Demos

| Case | Java Example | Launcher | Description |
|---|---|---|---|
| **Premium Borderless Overlay Showcase** | [Demo2.java](examples/src/main/java/fasttheme/Demo2.java) | `run-demo2.bat` | Borderless Raycast-style overlay with native drop shadow, invisible drag area, and window transparency. |
| **Window Styling & Live Transitions** | [Demo.java](examples/src/main/java/fasttheme/Demo.java) | `run-demo.bat` | Native DWM title bar styling, dark mode detection, and live theme updates. |
| **JMH Microbenchmark Suite** | [FastThemeBenchmark.java](examples/Benchmark/src/main/java/fasttheme/benchmark/FastThemeBenchmark.java) | `run-benchmark.bat` | Zero-allocation slot array access, dynamic string lookups, and parser throughput benchmarks. |

---

## Installation

FastJava modules require **two** dependencies: the module itself, and `FastCore` (which handles the native library extraction).

### Option 1: Maven (Recommended)

Add the JitPack repository and the dependency to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastTheme</artifactId>
        <version>0.1.4</version>
    </dependency>
    <!-- Required Native JNI loader -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>fastcore</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
```

### Option 2: Gradle (via JitPack)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:FastTheme:0.1.4'
    // Required Native JNI loader
    implementation 'com.github.andrestubbe:fastcore:0.1.0'
}
```

### Option 3: Direct Download (No Build Tool)

Download the latest JAR directly to add it to your classpath:

1. 📦 **[FastTheme-0.1.4.jar](https://github.com/andrestubbe/FastTheme/releases/download/0.1.4/FastTheme-0.1.4.jar)** (The Core Library & Native DLL)
2. 📦 **[FastCore-0.1.0.jar](https://github.com/andrestubbe/FastCore/releases/download/0.1.0/FastCore-0.1.0.jar)** (Required Native JNI loader)

---

## Documentation

* **[COMPILE.md](docs/COMPILE.md)**: Full compilation guide (Maven & Native C++ Build Setup).
* **[REFERENCE.md](docs/REFERENCE.md)**: Exhaustive catalog of API contracts, data structures, and JNI methods.
* **[PHILOSOPHY.md](docs/PHILOSOPHY.md)**: Dynamic slot architecture, Win32 DWM bridge, and design principles.
* **[ROADMAP.md](docs/ROADMAP.md)**: Planned milestone features and performance extensions.
* **[CHANGELOG.md](docs/CHANGELOG.md)**: Version history and release notes.

---

## Platform Support

| Feature | Windows 10 (1903+) | Windows 11 | Linux / macOS |
|---|---|---|---|
| Dynamic Theming & Key Registry | ✅ Full | ✅ Full | ✅ Full (Pure Java) |
| Color Math & WCAG Contrast | ✅ Full | ✅ Full | ✅ Full |
| Native DWM Titlebar & Mica | ✅ (Dark Mode) | ✅ Full | ➖ N/A (Native Windows) |

---

## License

MIT License — See [LICENSE](LICENSE) for details.

---

## Related Projects

- [FastCore](https://github.com/andrestubbe/FastCore) — Native JNI Loader and Utilities
- [FastAnimation](https://github.com/andrestubbe/FastAnimation) — Zero overhead timeline orchestration
- [FastTween](https://github.com/andrestubbe/FastTween) — Zero overhead pool-based tweening
- [FastDWM](https://github.com/andrestubbe/FastDWM) — Native Desktop Window Manager API
- [FastDisplay](https://github.com/andrestubbe/FastDisplay) — Native display telemetry and multi-monitor DPI scaling API
- [FastANSI](https://github.com/andrestubbe/FastANSI) — High-performance terminal ANSI compositor
- [FastUI](https://github.com/andrestubbe/FastUI) — High-Performance GUI Framework
- [FastTUI](https://github.com/andrestubbe/FastTUI) — Terminal User Interface Toolkit

---

**Part of the FastJava Ecosystem** — *Making the JVM faster. Small package. Maximum speed. Zero bloat. 🚀📋*
