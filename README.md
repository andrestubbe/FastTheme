# FastTheme — High-Performance Native Window Styling for Java

**Modern Windows 10/11 window decorations and dark mode for Java applications.**

[![Build](https://img.shields.io/github/actions/workflow/status/andrestubbe/FastTheme/maven.yml?branch=main)](https://github.com/andrestubbe/FastTheme/actions)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![JitPack](https://jitpack.io/v/andrestubbe/FastTheme.svg)](https://jitpack.io/#andrestubbe/FastTheme)

FastTheme brings **native Windows styling** to Swing and AWT. It enables dark mode title bars, Mica/Acrylic effects, and custom window decorations by bridging Java with the DWM (Desktop Window Manager) API.

[![Watch the FastTheme Performance & Transitions Showcase](docs/screenshot.png)](https://www.youtube.com/watch?v=6FVXiFB1itw)

---

## Table of Contents
- [Key Features](#key-features)
- [Quick Start](#quick-start)
- [Installation](#installation)
- [Try the Demo](#try-the-demo)
- [API Reference](#api-reference)
- [Platform Support](#platform-support)
- [License](#license)
- [Related Projects](#related-projects)

---

## Key Features

- **🌙 Native Dark Mode** — Proper title bar and border coloring.
- **✨ Glass Effects** — Support for Mica and Acrylic effects (Win 11).
- **🚀 Zero Lag** — Direct DWM attribute manipulation via native calls.

---

## Quick Start

```java
// Quick Start — Enabling Native Dark Mode
import fasttheme.FastTheme;
import javax.swing.JFrame;

public class Demo {
    public static void main(String[] args) {
        JFrame frame = new JFrame("FastTheme Demo");
        frame.setSize(800, 600);
        frame.setVisible(true);
        
        // Apply native dark mode to the title bar with zero overhead
        FastTheme.setDarkMode(frame, true);
    }
}
```

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
        <artifactId>fasttheme</artifactId>
        <version>0.2.0</version>
    </dependency>
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>fastcore</artifactId>
        <version>0.2.0</version>
    </dependency>
</dependencies>
```

### Gradle (JitPack)
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:fasttheme:0.2.0'
    implementation 'com.github.andrestubbe:fastcore:0.2.0'
}
```

### Option 3: Direct Download (No Build Tool)
Download the latest JARs directly to add them to your classpath:

1. 📦 **[fasttheme-v0.2.0.jar](https://github.com/andrestubbe/FastTheme/releases)** (The Core Library)
2. ⚙️ **[fastcore-v0.2.0.jar](https://github.com/andrestubbe/FastCore/releases)** (The Mandatory Native Loader)

> [!IMPORTANT]
> Both JARs must be in your classpath for the native JNI calls to function correctly.

---

## Try the Demo

Want to see the high-performance transition engine in action?

1. Clone this repository: `git clone https://github.com/andrestubbe/FastTheme.git`
2. Run the automated showcase: `.\run-demo.bat`

*Note: The demo showcases 300+ FPS theme transitions using FastAnimation.*

---

## API Reference

| Method | Description |
|--------|-------------|
| `void setDarkMode(Window w, boolean enable)` | Toggles the native system dark mode for the window. |
| `void setWindowTransparency(Window w, int alpha)` | Sets native window transparency (0-255). |
| `void setTitleBarColor(Window w, int r, int g, int b)` | Sets a custom native title bar background color. |

---

## Platform Support

| Platform | Status |
|----------|--------|
| Windows 10 (1903+) | ✅ Dark Mode |
| Windows 11 | ✅ Dark Mode, Mica, Acrylic |

---

## License
MIT License — See [LICENSE](LICENSE) file for details.

---

## Related Projects
- [FastCore](https://github.com/andrestubbe/FastCore) — Native Library Loader
- [FastAnimation](https://github.com/andrestubbe/FastAnimation) — High-precision animation engine
- [FastTween](https://github.com/andrestubbe/FastTween) — SIMD-optimized interpolation

---
**Made with ⚡ by Andre Stubbe**

<!-- 
SEO Keywords: java, jni, dark mode, mica, acrylic, dwm, windows 11, fastjava
-->
