# FastTheme 0.1.3 — Universal Color-Matrix & High-Performance Window Styling for Java

**Universal, zero-allocation theme engine, color matrix, and native Windows 10/11 DWM window styling for Java applications.**

[![Build](https://img.shields.io/github/actions/workflow/status/andrestubbe/FastTheme/release.yml)](https://github.com/andrestubbe/FastTheme/actions)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![JitPack](https://img.shields.io/badge/JitPack-v0.1.3-brightgreen.svg)](https://jitpack.io/#andrestubbe/FastTheme)

FastTheme combines **zero-allocation in-memory theme management** with **native Windows DWM styling**. It powers Swing, AWT, OpenGL, and CLI/TUI applications with consistent palettes, WCAG contrast safety, fast text/binary loading, and dark mode title bar integration.

[![Premium Overlay Showcase](docs/screenshot.png)
![Premium Overlay Animation](docs/screenshot2.png)
](https://www.youtube.com/watch?v=00bgKmWOEk8)

---

## Quick Start

### 1. Zero-Allocation Color Lookup & Theming
```java
import fasttheme.FastTheme;
import fasttheme.ThemeKeys;
import fasttheme.ThemeParser;

public class Demo {
    public static void main(String[] args) {
        // Activate embedded preset (or load from .theme / .themebin)
        FastTheme.set(ThemeParser.loadDefaultDark());

        // Zero-allocation direct slot read (32-bit packed ARGB int)
        int bg = FastTheme.get(ThemeKeys.WINDOW_BACKGROUND);
        
        // AWT/Swing Color
        java.awt.Color titleColor = FastTheme.getColor(ThemeKeys.TITLE_BAR_BACKGROUND);
        
        // 24-bit Truecolor ANSI for Terminal/TUI
        System.out.println(FastTheme.getAnsiFg(ThemeKeys.ACCENT_PRIMARY) + "⚡ FastTheme Loaded" + "\u001B[0m");
    }
}
```

### 2. Native Windows 10/11 Styling Bridge
```java
import fasttheme.FastTheme;
import javax.swing.JFrame;
import java.awt.Color;

public class NativeDemo {
    public static void main(String[] args) {
        JFrame frame = new JFrame("FastTheme Window");
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        long hwnd = FastTheme.getWindowHandle(frame);
        if (hwnd != 0) {
            // Automatically synchronizes DWM title bar and background from active theme
            FastTheme.applyToWindow(hwnd);
        }
    }
}
```

---

## Key Features

- **⚡ Zero-Allocation Color Matrix** — Contiguous `int[]` storage indexed via 49 static slot constants (`ThemeKeys`).
- **📄 Dual FastFileFormat** — Human-readable `.theme` text (with `@KEY` variable aliasing) and lightning-fast binary `.themebin`.
- **🧮 WCAG 2.1 Contrast Safety** — Automatic high-contrast text foreground selection and mathematical tint/shade state generation.
- **💻 CLI & TUI Ready** — Native 24-bit Truecolor ANSI escape sequences for terminal apps.
- **🌙 Native Windows DWM Styling** — Immersive Dark Mode, custom title bar colors, Mica/Acrylic effects, and borderless drop shadows.
- **🔄 Live State & Observers** — Dynamic notifications to registered listeners on theme switch.

---

## Installation

FastJava modules require **two** dependencies: the module itself, and `FastCore` (which handles the native library extraction).

### Maven (JitPack)
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
        <version>0.1.3</version>
    </dependency>
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>fastcore</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
```

### Gradle (JitPack)
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:FastTheme:0.1.3'
    implementation 'com.github.andrestubbe:fastcore:0.1.0'
}
```

---

## API Overview

| Class | Purpose |
|-------|---------|
| `FastTheme` | Central facade for theme state, color lookups, listeners, and native DWM synchronization. |
| `ThemeKeys` | 49 indexed static slots (`TITLE_BAR_BACKGROUND`, `ACCENT_PRIMARY`, etc.) with $O(1)$ bidirectional lookup. |
| `ThemeData` | In-memory primitive array storage with `.toBinary()` and `.toText()` export. |
| `ThemeParser` | Deserializer for text (`.theme`) and binary (`.themebin`) formats with embedded default presets. |
| `ThemeColorUtil` | WCAG luminance/contrast math, state generator (tint/shade), and ANSI terminal formatting. |
| `ThemeListener` | Observer interface for live theme change notifications. |

---

## Documentation

- **[REFERENCE.md](docs/REFERENCE.md)**: Exhaustive API contracts and data structure reference.
- **[PHILOSOPHY.md](docs/PHILOSOPHY.md)**: Zero-allocation architecture, Win32 DWM bridge, and theme design principles.
- **[CHANGELOG.md](docs/CHANGELOG.md)**: Version history and release notes.
- **[ROADMAP.md](docs/ROADMAP.md)**: Future milestones.
- **[COMPILE.md](docs/COMPILE.md)**: Native C++ JNI build instructions.

---

## Platform Support

| Feature | Windows 10 (1903+) | Windows 11 | Linux / macOS |
|---------|-------------------|------------|---------------|
| Color Matrix & Theming | ✅ Full | ✅ Full | ✅ Full (Pure Java) |
| ANSI CLI Sequences | ✅ Full | ✅ Full | ✅ Full |
| Native DWM Titlebar & Mica | ✅ (Dark Mode) | ✅ Full | ➖ N/A (Native Windows) |

---

## License
MIT License — See [LICENSE](LICENSE) file for details.

---

## Related Projects
- [FastCore](https://github.com/andrestubbe/FastCore) — Unified Native Loader
- [FastUI](https://github.com/andrestubbe/FastUI) — High-Performance GUI Framework
- [FastTUI](https://github.com/andrestubbe/FastTUI) — Terminal User Interface Toolkit
- [FastAnimation](https://github.com/andrestubbe/FastAnimation) — High-precision animation engine

---
**Made with ⚡ by Andre Stubbe**
