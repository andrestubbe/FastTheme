# FastTheme v0.1.0 🎨

**FastTheme** is a lightweight, native Java library for styling Windows windows (titlebars, transparency, dark mode) using JNI and the Windows DWM (Desktop Window Manager) API.


## ✨ Features
- **Native Transparency**: Apply glass/acrylic effects to any Swing window.
- **Custom Titlebars**: Set RGB colors for the titlebar background and text (Windows 11+).
- **Immersive Dark Mode**: Toggle native dark mode for window borders and system menus.
- **Theme Detection**: Check if the system is currently in Dark or Light mode.
- **Zero Overhead**: Direct DWM attribute manipulation with no performance cost.

## 🚀 Quick Start

```bash
# Clone the repository
git clone https://github.com/andrestubbe/fasttheme.git
cd fasttheme

# Build and register locally
.\compile.bat

# Run the Comparison Demo
.\run-demo.bat
```

## 📦 Installation

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
If you aren't using a build tool, download the **FatJar** from [Releases](https://github.com/andrestubbe/fasttheme/releases):
- `fasttheme-0.1.0-fat.jar` — Includes everything (Library + FastCore + DLLs).

Just add this single file to your project's classpath and you're ready to go.

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

## 🎮 Running the Demo
We've included a sleek terminal-style demo to showcase the styling capabilities. To run it:
1. Run `compile.bat` to build the native library.
2. Run `run-demo.bat` to launch the example window.

---

## 🛠️ Build Requirements
- **JDK 17+**
- **Windows 10/11** (DWM API required)
- **Visual Studio 2022/2019** (if building from source)

## 📦 Project Structure

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

## ✅ Release Checklist
- [ ] Version updated in `pom.xml`
- [ ] `CHANGELOG.md` updated
- [ ] Native DLLs built: `.\compile.bat`
- [ ] Comparison Demo certified: `.\run-demo.bat`
- [ ] Git tag created (e.g., `v0.1.0`)
- [ ] GitHub Release created with FatJar asset

## 📄 License
MIT License — See [LICENSE](LICENSE) for details.

---
**Part of the FastJava Ecosystem** — *Making the JVM faster.*
