# The Philosophy of FastTheme

> [!IMPORTANT]
> **"Native Windows Elegance. Zero Flicker. Deep OS Integration. Kernel-Level Window Control."**

FastTheme is built on the principle that modern Java desktop applications require **first-class native window decorations** without sacrificing the cross-platform speed and stability of Java Swing and AWT.

## Core Tenets

### 1. Direct DWM Manipulation
Instead of simulating window title bars using custom Java component trees (which often suffer from resizing jitter, non-standard window snapping, and missing OS animations), FastTheme communicates directly with the Windows Desktop Window Manager (DWM) API via C++ JNI calls.

### 2. Flicker-Free Subclassing
Standard Swing windows flicker white or black when resized or focused due to background erasing cycles. FastTheme intercepts native message loops (`WM_ERASEBKGND`, `WM_NCCALCSIZE`, `WM_NCPAINT`) at the Win32 window procedure level, guaranteeing smooth visual transitions.

### 3. Native Glass & Backdrop Materials
FastTheme unlocks Windows 11 Mica, Mica Alt, and Acrylic materials natively. Windows rendered with FastTheme reflect wallpaper accents and system light/dark settings dynamically.

### 4. Seamless Terminal & GUI Compatibility
Whether styling a Swing `JFrame` or turning a native console window (`cmd.exe` / ConHost) semi-transparent, FastTheme provides unified, high-performance handle extraction and styling logic.

---

**⚡ FastTheme — Bringing native Windows elegance to Java.**
