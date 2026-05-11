# FastTheme v0.3.0 — High-Performance Native Window Styling for Java

**Modern Windows 10/11 window decorations and dark mode for Java applications.**

[![Build](https://img.shields.io/github/actions/workflow/status/andrestubbe/FastTheme/maven.yml?branch=main)](https://github.com/andrestubbe/FastTheme/actions)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![JitPack](https://jitpack.io/v/andrestubbe/FastTheme.svg)](https://jitpack.io/#andrestubbe/FastTheme)

FastTheme brings **native Windows styling** to Swing and AWT. It enables dark mode title bars, Mica/Acrylic effects, and custom window decorations by bridging Java with the DWM (Desktop Window Manager) API.

[![Premium Overlay Showcase](docs/screenshot.png)
![Premium Overlay Animation](docs/screenshot2.png)
](https://www.youtube.com/watch?v=00bgKmWOEk8)

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
- **🪟 Premium Overlays** — Borderless windows with native shadows and custom drag zones.
- **🚀 Zero Lag** — Direct DWM attribute manipulation via native calls.

---

## Quick Start

```java
// Quick Start — Enabling Premium Overlay Mode
import fasttheme.FastTheme;
import javax.swing.JFrame;

public class Demo {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setSize(600, 400);
        
        // Apply Premium Overlay Styling
        long hwnd = FastTheme.getWindowHandle(frame);
        FastTheme.setBorderlessShadow(hwnd, true);
        FastTheme.setOverlayDragHeight(hwnd, 6); // 6px drag zone
        
        frame.setVisible(true);
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
        <version>0.3.0</version>
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
    implementation 'com.github.andrestubbe:fasttheme:0.3.0'
    implementation 'com.github.andrestubbe:fastcore:0.1.0'
}
```

### Option 3: Direct Download (No Build Tool)
Download the latest JARs directly to add them to your classpath:

1. 📦 **[fasttheme-v0.3.0.jar](https://github.com/andrestubbe/FastTheme/releases)** (The Core Library)
2. ⚙️ **[fastcore-v0.1.0.jar](https://github.com/andrestubbe/FastCore/releases)** (The Mandatory Native Loader)

> [!IMPORTANT]
> Both JARs must be in your classpath for the native JNI calls to function correctly.

---

## Try the Demo

Want to see the high-performance transition engine in action?

1. Clone this repository: `git clone https://github.com/andrestubbe/FastTheme.git`
2. Run the premium showcase: `.\run-demo2.bat`

*Note: The demo showcases the v0.3.0 Borderless Shadow and Drag-Zone logic.*

---

## API Reference

| Method | Description |
|--------|-------------|
| `void setBorderlessShadow(long hwnd, boolean enable)` | Enables borderless mode with native shadows (Raycast-style). |
| `void setOverlayDragHeight(long hwnd, int pixels)` | Sets the height of the invisible drag zone. |
| `void setTitleBarDarkMode(long hwnd, boolean enable)` | Toggles the native system dark mode for the title bar. |
| `void setWindowTransparency(long hwnd, int alpha)` | Sets native window transparency (0-255). |
| `void setTitleBarColor(long hwnd, int r, int g, int b)` | Sets a custom native title bar background color. |

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
