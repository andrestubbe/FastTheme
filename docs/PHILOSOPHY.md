# The Philosophy of FastTheme

> [!IMPORTANT]
> **"Dynamic Elastic State. Zero Allocation. Kernel-Level Window Control. Open-Ended Key Matrix."**

FastTheme is built on the principle that modern Java desktop and CLI applications require **first-class native window decorations and dynamic theming** without sacrificing the cross-platform speed and stability of Java Swing, AWT, and terminal toolkits.

## Core Tenets

### 1. Open Dynamic Slot Matrix
FastTheme rejects rigid, hardcoded theme schemas. Applications, plugins, and custom components can register arbitrary key names at runtime (`ThemeKeys.slot("MY_KEY")`). `ThemeData` automatically resizes its contiguous primitive `int[]` memory array to provide guaranteed $O(1)$ zero-allocation lookups for any standard or custom color.

### 2. WCAG Contrast Safety
Readability is never left to guesswork. FastTheme provides built-in WCAG 2.1 relative luminance and contrast scoring, dynamically choosing high-contrast text foregrounds (`ThemeColorUtil.getContrastForeground(bg)`) and generating mathematical button hover/pressed states deterministically.

### 3. Direct DWM Manipulation
Instead of simulating window title bars using custom Java component trees (which often suffer from resizing jitter, non-standard window snapping, and missing OS animations), FastTheme communicates directly with the Windows Desktop Window Manager (DWM) API via C++ JNI calls.

### 4. Flicker-Free Subclassing
Standard Swing windows flicker white or black when resized or focused due to background erasing cycles. FastTheme intercepts native message loops (`WM_ERASEBKGND`, `WM_NCCALCSIZE`, `WM_NCPAINT`) at the Win32 window procedure level, guaranteeing smooth visual transitions.

### 5. Native Glass & Backdrop Materials
FastTheme unlocks Windows 11 Mica, Mica Alt, and Acrylic materials natively. Windows rendered with FastTheme reflect wallpaper accents and system light/dark settings dynamically.

---

**⚡ FastTheme — Dynamic theming and native Windows elegance for Java.**
