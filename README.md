# FastTheme â€” High-Performance Native Window Styling for Java [v0.3.0]

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

- **ðŸŒ™ Native Dark Mode** â€” Proper title bar and border coloring.
- **âœ¨ Glass Effects** â€” Support for Mica and Acrylic effects (Win 11).
- **ðŸªŸ Premium Overlays** â€” Borderless windows with native shadows and custom drag zones.
- **ðŸš€ Zero Lag** â€” Direct DWM attribute manipulation via native calls.

---

## Quick Start

```java
// Quick Start â€” Enabling Premium Overlay Mode
import fasttheme.FastTheme;
import javax.swing.JFrame;

public class Demo {
    public static void main(String[] args) {
         JFrame frame = new JFrame();
        frame.setSize(600, 96);

        // Remove all Java window decorations (title bar, borders)
        // Required for creating a true overlay-style window
        frame.setUndecorated(true);

        // Center the window on the screen
        frame.setLocationRelativeTo(null);

        // Set the window background to pure black
        // Since the title bar is removed, the content area becomes the visible body
        frame.getContentPane().setBackground(new Color(0, 0, 0));

        // The native HWND is created only after the window becomes visible
        frame.setVisible(true);

        long hwnd = FastTheme.getWindowHandle(frame);
        if (hwnd != 0) {

            // Enables the Windows 11 shadow (DWM + WS_THICKFRAME)
            FastTheme.setBorderlessShadow(hwnd, true);

            // Defines the height of the invisible drag zone at the top
            // Allows the window to be dragged like Raycast/Spotlight
            FastTheme.setOverlayDragHeight(hwnd, 6);

            // Applies semi-transparency to the entire window (0â€“255)
            FastTheme.setWindowTransparency(hwnd, 230);

        } else {
            System.err.println("âŒ HWND is 0 â€“ native window handle not found!");
        }

    }
}
```

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
        <version>v0.1.0</version>
    </dependency>

    <!-- FastCore (Required Native Loader) -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>fastcore</artifactId>
        <version>v0.1.0</version>
    </dependency>
</dependencies>
```

### Option 2: Gradle (via JitPack)
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:fasttheme:v0.1.0'
    implementation 'com.github.andrestubbe:fastcore:v0.1.0'
}
```

### Option 3: Direct Download (No Build Tool)
Download the latest JARs directly to add them to your classpath:

1. 📦 **[fasttheme-v0.1.0.jar](https://github.com/andrestubbe/FastTheme/releases/download/v0.1.0/fasttheme-v0.1.0.jar)** (The Core Library)
2. ⚙️ **[fastcore-v0.1.0.jar](https://github.com/andrestubbe/FastCore/releases/download/v0.1.0/fastcore-v0.1.0.jar)** (The Mandatory Native Loader)

> [!IMPORTANT]
> All JARs must be in your classpath for the native JNI calls to function correctly.


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
| Windows 10 (1903+) | âœ… Dark Mode |
| Windows 11 | âœ… Dark Mode, Mica, Acrylic |

---

## License
MIT License â€” See [LICENSE](LICENSE) file for details.

---

## Related Projects
- [FastCore](https://github.com/andrestubbe/FastCore) â€” Native Library Loader
- [FastAnimation](https://github.com/andrestubbe/FastAnimation) â€” High-precision animation engine
- [FastTween](https://github.com/andrestubbe/FastTween) â€” SIMD-optimized interpolation

---
**Made with âš¡ by Andre Stubbe**

<!-- 
SEO Keywords: java, jni, dark mode, mica, acrylic, dwm, windows 11, fastjava
-->
