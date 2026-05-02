# FastTheme — Native Windows Theme & Titlebar Styling for Java [v0.2.0]

**Lightweight alternative to FlatLAF when you only need real OS titlebar styling, window opacity, and modern Windows 11 materials.**

[![Status](https://img.shields.io/badge/status-v0.2.0--stable-green.svg)]()
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

---

**FastTheme** brings real Windows theming to Java applications: **Dark/Light Mode, Mica & Acrylic Materials, Titlebar Styling, and Window Opacity** — all native, without replacing your Look-and-Feel.

## Features
- **🎨 Native Window Styling**: Apply high-end Windows 11 materials like Mica and Acrylic.
- **🌓 Theme Detection**: Check if the system is currently in Dark or Light mode.
- **🛡️ Corner Control**: Force rounded, small-rounded, or square corners on any window.
- **🌓 Dark Mode Titlebar**: Flip the immersive dark mode bit for a seamless professional look.
- **⚡ Zero Overhead**: Direct DWM attribute manipulation with no performance cost.

---

## Table of Contents
- [Quick Start](#quick-start)
- [Installation](#installation)
- [API Reference](#api-reference)
- [Try the Demo](#try-the-demo)
- [Build from Source](#build-from-source)
- [License](#license)

---

## Quick Start

```java
JFrame frame = new JFrame("Styled Window");
frame.setVisible(true); 

long hwnd = FastWindow.attach(frame).getHWND(); // Get HWND via FastWindow

// Apply high-end aesthetics
FastTheme.setTitleBarDarkMode(hwnd, true);
FastTheme.enableMica(hwnd, true);
FastTheme.setCornerStyle(hwnd, 2); // Rounded
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
    <dependency>
        <groupId>io.github.andrestubbe</groupId>
        <artifactId>fasttheme</artifactId>
        <version>0.2.0</version>
    </dependency>
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>fastcore</artifactId>
        <version>v1.0.0</version>
    </dependency>
</dependencies>
```

### Option 2: Gradle (via JitPack)
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'io.github.andrestubbe:fasttheme:0.2.0'
    implementation 'com.github.andrestubbe:fastcore:v1.0.0'
}
```

### Option 3: Direct Download (No Build Tool)
Download the latest JARs directly to add them to your classpath:

1.  📦 **[fasttheme-v0.2.0.jar](https://github.com/andrestubbe/fasttheme/releases)**
2.  ⚙️ **[fastcore-v1.0.0.jar](https://github.com/andrestubbe/FastCore/releases)**

---

## API Reference

| Method | Description |
|--------|-------------|
| `static long getWindowHandle(Component)` | Helper to get the HWND (Preferred: use FastWindow). |
| `boolean enableMica(hwnd, boolean)` | Enables/Disables Windows 11 Mica backdrop. |
| `boolean setCornerStyle(hwnd, style)` | Sets rounded or square corner styles. |
| `boolean setTitleBarDarkMode(hwnd, boolean)` | Toggles immersive dark mode on titlebar. |
| `boolean setTitleBarColor(hwnd, r, g, b)` | Sets custom titlebar background color. |
| `boolean setWindowTransparency(hwnd, alpha)`| Sets window-wide opacity (0-255). |
| `boolean isSystemDarkMode()` | Returns the global Windows theme state. |

---

## Try the Demo
1. Run `compile.bat` to build the native library.
2. Run `run-demo.bat` to launch the ecosystem comparison.

---

## License
MIT License — See [LICENSE](LICENSE) for details.

---
**Part of the FastJava Ecosystem** — *Making the JVM faster.*

<!-- SEO KEYWORDS -->
<!-- 
FastJava FastTheme JNI Windows 11 Mica Dark Mode Java Swing Acrylic 
DWM API SetWindowAttribute Java Native Window Styling 
-->
