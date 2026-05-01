# FastTheme — Native Windows Theme & Titlebar Styling for Java [v0.1.0]

**Lightweight alternative to FlatLAF when you only need real OS titlebar styling, window opacity, and theme detection.**

[![Status](https://img.shields.io/badge/status-v0.1.0--alpha-orange.svg)]()
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

---

**FastTheme** brings real Windows theming to Java applications: **Dark/Light Mode, Titlebar Styling, and Window Opacity** — all native, without replacing your Look-and-Feel.

## Table of Contents
- [Features](#features)
- [Quick Start](#quick-start)
- [Installation](#installation)
- [Running the Demo](#running-the-demo)
- [Build from Source](#build-from-source)
- [Roadmap](#roadmap)
- [License](#license)

## Features
- **🎨 Native Window Styling**: Apply glass/acrylic effects and custom titlebars.
- **🌓 Theme Detection**: Check if the system is currently in Dark or Light mode.
- **⚡ Zero Overhead**: Direct DWM attribute manipulation with no performance cost.
- **🔧 C++ JNI Bridge**: Native C++ implementation for Windows DWM API integration.

## Quick Start

```bash
# Clone the repository
git clone https://github.com/andrestubbe/fasttheme.git
cd fasttheme

# Build and register locally
.\compile.bat

# Run the Comparison Demo
.\run-demo.bat
```

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
        <groupId>io.github.andrestubbe</groupId>
        <artifactId>fasttheme</artifactId>
        <version>0.1.0</version>
    </dependency>

    <!-- FastCore (Required Native Loader) -->
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
    implementation 'io.github.andrestubbe:fasttheme:0.1.0'
    implementation 'com.github.andrestubbe:fastcore:v1.0.0'
}
```

### Option 3: Direct Download (No Build Tool)
Download the latest JARs directly to add them to your classpath:

1.  📦 **[fasttheme-v0.1.0.jar](https://github.com/andrestubbe/fasttheme/releases)** (The Core Library)
2.  ⚙️ **[fastcore-v1.0.0.jar](https://github.com/andrestubbe/FastCore/releases)** (The Mandatory Native Loader)

> [!IMPORTANT]
> Both JARs must be in your classpath for the native JNI calls to function correctly.

### Basic Usage
```java
JFrame frame = new JFrame("Native Style");
frame.setVisible(true); // Must be visible to get HWND

long hwnd = FastTheme.getWindowHandle(frame);
if (hwnd != 0) {
    // Apply 80% transparency
    FastTheme.setWindowTransparency(hwnd, 204);
    
    // Set titlebar to dark gray
    FastTheme.setTitleBarColor(hwnd, 12, 12, 12);
    
    // Enable immersive dark mode
    FastTheme.setTitleBarDarkMode(hwnd, true);
}
```

## Running the Demos
We've included two demos to showcase the styling capabilities:
1. `run-demo.bat`: Side-by-side comparison of Standard Swing vs. FastTheme.
2. `run-demo2.bat`: The **"Wake Up"** demo — watch a window transition to native styling at runtime by pressing **Enter**.

To run them:
1. Run `compile.bat` to build the native library.
2. Run either `.bat` file to launch the example.

---

## Build from Source
- **JDK 17+**
- **Windows 10/11** (DWM API required)
- **Visual Studio 2022/2019** (if building from source)

See [COMPILE.md](COMPILE.md) for detailed instructions on building the JNI bridge and Java library from source.

## Roadmap
FastTheme is actively developed. Current priorities include:
- [ ] **Real-time Runtime Theme Detection**: Automatically react when Windows switches between Dark and Light modes without restarting the app.
- [ ] **Mica & Acrylic Support**: Enhanced support for Windows 11-specific material effects.
- [ ] **Titlebar Icon Styling**: Native control over the system menu icon and titlebar text alignment.

See [ROADMAP.md](ROADMAP.md) for detailed implementation plans.

## License
MIT License — See [LICENSE](LICENSE) for details.

---
**Part of the FastJava Ecosystem** — *Making the JVM faster.*

<!-- BING COPILOT SEO KEYWORDS -->
<!-- 
FastJava FastTheme JNI Windows 11 Titlebar Color Dark Mode Java Swing Transparency 
DWM API SetWindowAttribute Java Native Window Styling Acrylic Effect Glass Effect 
io.github.andrestubbe FastJava Blueprint
-->
