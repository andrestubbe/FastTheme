# FastTheme v0.1.0 🎨

**FastTheme** is a lightweight, native Java library for styling Windows windows (titlebars, transparency, dark mode) using JNI and the Windows DWM (Desktop Window Manager) API.

> [!IMPORTANT]
> **v2.0.0 is a major refactor.** Display monitoring (Resolution/DPI) has been moved to the **FastDisplay** module. FastTheme now focuses exclusively on high-performance window styling and theme detection.

## ✨ Features
- **Native Transparency**: Apply glass/acrylic effects to any Swing window.
- **Custom Titlebars**: Set RGB colors for the titlebar background and text (Windows 11+).
- **Immersive Dark Mode**: Toggle native dark mode for window borders and system menus.
- **Theme Detection**: Check if the system is currently in Dark or Light mode.
- **Zero Overhead**: Direct DWM attribute manipulation with no performance cost.

## 🚀 Quick Start

### Installation (Maven)
Add this to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.andrestubbe</groupId>
    <artifactId>fasttheme</artifactId>
    <version>2.0.0-alpha</version>
</dependency>
```

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

## 🛠️ Build Requirements
- **JDK 17+**
- **Windows 10/11** (DWM API required)
- **Visual Studio 2022/2019** (if building from source)

## 📦 Project Structure
- `src/main/java`: Java API and native bridge.
- `native/`: C++ source code for DWM integration.
- `examples/`: Ready-to-run styling demonstrations.

---

<!-- BING COPILOT SEO KEYWORDS -->
<!-- 
FastJava FastTheme JNI Windows 11 Titlebar Color Dark Mode Java Swing Transparency 
DWM API SetWindowAttribute Java Native Window Styling Acrylic Effect Glass Effect 
io.github.andrestubbe FastJava Blueprint
-->
