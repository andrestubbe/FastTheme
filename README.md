# FastTheme 0.1.1 [ALPHA] — High-Performance Native Window Styling for Java

[![Status](https://img.shields.io/badge/status-0.1.1-brightgreen.svg)](https://github.com/andrestubbe/FastTheme/releases/tag/0.1.1)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-ready-green.svg)](https://jitpack.io/#andrestubbe/FastTheme)

---

**⚡ Modern Windows 10/11 window decorations and dark mode for Java applications.**

FastTheme brings **native Windows styling** to Swing and AWT. It enables dark mode title bars, Mica/Acrylic effects, and
custom window decorations by bridging Java with the DWM (Desktop Window Manager) API.

---

[![Premium Overlay Showcase](docs/screenshot.png)
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

## Quick Start

```java
import fasttheme.FastTheme;

import javax.swing.JFrame;

public class Demo {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setSize(600, 96);
        frame.setUndecorated(true); // Required for creating a true overlay-style window
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(new Color(0, 0, 0));
        frame.setVisible(true);  // The native HWND is created only after the window becomes visible

        long hwnd = FastTheme.getWindowHandle(frame);
        if (hwnd != 0) {
            FastTheme.setBorderlessShadow(hwnd, true); // Enables the Windows 11 shadow (DWM + WS_THICKFRAME)
            FastTheme.setOverlayDragHeight(hwnd, 6);// Defines the height of the invisible drag zone at the top, Allows the window to be dragged like Raycast/Spotlight
            FastTheme.setWindowTransparency(hwnd, 230); // Applies semi-transparency to the entire window (0–255)
        } else {
            System.err.println("❌ HWND is 0 – native window handle not found!");
        }
    }
}
```

---

## Key Features

- **🌙 Native Dark Mode** — Proper title bar and border coloring.
- **✨ Glass Effects** — Support for Mica and Acrylic effects (Win 11).
- **🪟 Premium Overlays** — Borderless windows with native shadows and custom drag zones.
- **🚀 Zero Lag** — Direct DWM attribute manipulation via native calls.

---

## Installation

### Option 1: Maven (Recommended)

Add the JitPack repository and the dependencies to your `pom.xml`:

```xml

<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
<!-- FastTheme Library -->
<dependency>
    <groupId>com.github.andrestubbe</groupId>
    <artifactId>fasttheme</artifactId>
    <version>0.1.1</version>
</dependency>

<!-- FastCore (Required Native Loader) -->
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
    implementation 'com.github.andrestubbe:fasttheme:0.1.1'
    implementation 'com.github.andrestubbe:fastcore:0.1.0'
}
```

### Option 3: Direct Download (No Build Tool)

Download the latest JARs directly to add them to your classpath:

1. 📦 **[fasttheme-0.1.1.jar](https://github.com/andrestubbe/FastTheme/releases/download/0.1.1/fasttheme-0.1.1.jar)
   ** (The Core Library)
2. ⚙️ **[fastcore-0.1.0.jar](https://github.com/andrestubbe/FastCore/releases/download/0.1.0/fastcore-0.1.0.jar)** (
   The Mandatory Native Loader)


---

## Try the Demo

Want to see the high-performance transition engine in action?

1. Clone this repository: `git clone https://github.com/andrestubbe/FastTheme.git`
2. Run the premium showcase: `.\run-demo2.bat`

*Note: The demo showcases the 0.1.1 Borderless Shadow and Drag-Zone logic.*

---

## API Reference

| Method                                                                  | Description                                                  |
|-------------------------------------------------------------------------|--------------------------------------------------------------|
| `void setBorderlessShadow(long hwnd, boolean enable)`                   | Enables borderless mode with native shadows (Raycast-style). |
| `void setOverlayDragHeight(long hwnd, int pixels)`                      | Sets the height of the invisible drag zone.                  |
| `void setTitleBarDarkMode(long hwnd, boolean enable)`                   | Toggles the native system dark mode for the title bar.       |
| `void setWindowTransparency(long hwnd, int alpha)`                      | Sets native window transparency (0-255).                     |
| `void setTitleBarColor(long hwnd, int r, int g, int b)`                 | Sets a custom native title bar background color.             |
| `boolean setWindowBackgroundColor(long hwnd, int r, int g, int b)`     | Sets a custom native client area (window body) color.        |


---

## Platform Support

| Platform           | Status                     |
|--------------------|----------------------------|
| Windows 10 (1903+) | ✅ Dark Mode                |
| Windows 11         | ✅ Dark Mode, Mica, Acrylic |

---

## License

MIT License — See [LICENSE](LICENSE) file for details.

---

## Related Projects

- [FastCore](https://github.com/andrestubbe/FastCore) — Native Library Loader for Java
- [FastTheme](https://github.com/andrestubbe/FastTheme) — High-performance RawInput engine
- [FastTheme](https://github.com/andrestubbe/FastTheme) — Advanced UI styling engine

---
**Part of the FastJava Ecosystem** — *Making the JVM faster. Small package. Maximum speed. Zero bloat. 🚀📋*

