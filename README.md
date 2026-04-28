# FastTheme — Native Windows Theme & Titlebar Styling for Java [v0.1.0]

**Lightweight alternative to FlatLAF when you only need real OS titlebar styling, window opacity, and theme detection.**

[![Status](https://img.shields.io/badge/status-v0.1.0--alpha-orange.svg)]()
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

---

**FastTheme** brings real Windows theming to Java applications: **Dark/Light Mode, Titlebar Styling, and Window Opacity** — all native, without replacing your Look-and-Feel.

## 📋 Table of Contents
- [Features](#features)
- [Quick Start](#quick-start)
- [Installation](#installation)
- [Running the Demo](#running-the-demo)
- [Build Requirements](#build-requirements)
- [Project Structure](#project-structure)
- [Release Checklist](#release-checklist)
- [License](#license)

## Features
- **🎨 Native Window Styling**: Apply glass/acrylic effects and custom titlebars.
- **🌓 Theme Detection**: Check if the system is currently in Dark or Light mode.
- **⚡ Zero Overhead**: Direct DWM attribute manipulation with no performance cost.
- **📦 Ecosystem Ready**: Built for the FastJava Blueprint architecture.

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

### Maven (via JitPack)
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

### Gradle (via JitPack)
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'io.github.andrestubbe:fasttheme:0.1.0'
    implementation 'com.github.andrestubbe:fastcore:v1.0.0'
}
```

### Direct Download (No Build Tool)
Download these JARs from [Releases](https://github.com/andrestubbe/fasttheme/releases):

1. **`fasttheme-0.1.0.jar`** — Main Styling Library (includes native DLLs)
2. **`fastcore-v1.0.0.jar`** — [Native JNI Loader](https://github.com/andrestubbe/FastCore/releases) (Required)

Just add both files to your project's classpath and you're ready to go.

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

## Running the Demo
We've included a sleek terminal-style demo to showcase the styling capabilities. To run it:
1. Run `compile.bat` to build the native library.
2. Run `run-demo.bat` to launch the example window.

---

## Build Requirements
- **JDK 17+**
- **Windows 10/11** (DWM API required)
- **Visual Studio 2022/2019** (if building from source)

## Project Structure

```text
fasttheme/
├── examples/               # ⭐ Side-by-side comparison demo
├── native/                 # C++ JNI source (DWM styling engine)
├── src/
│   └── main/java/          # Java API and JNI bridge
├── compile.bat             # Native build & install script
├── run-demo.bat            # One-click demo launcher
├── pom.xml                 # Maven configuration
├── LICENSE                 # MIT License
└── README.md               # You are here
```

## Release Checklist
- [ ] Version updated in `pom.xml`
- [ ] `CHANGELOG.md` updated
- [ ] Native DLLs built: `.\compile.bat`
- [ ] Comparison Demo certified: `.\run-demo.bat`
- [ ] Git tag created (e.g., `v0.1.0`)
- [ ] GitHub Release created with FatJar asset

---

## 🛠️ Build from Source
See [COMPILE.md](COMPILE.md) for detailed instructions on building the JNI bridge and Java library from source.

## 📄 License
MIT License — See [LICENSE](LICENSE) for details.

---
**Part of the FastJava Ecosystem** — *Making the JVM faster.*

<!-- BING COPILOT SEO KEYWORDS -->
<!-- 
FastJava FastTheme JNI Windows 11 Titlebar Color Dark Mode Java Swing Transparency 
DWM API SetWindowAttribute Java Native Window Styling Acrylic Effect Glass Effect 
io.github.andrestubbe FastJava Blueprint
-->
