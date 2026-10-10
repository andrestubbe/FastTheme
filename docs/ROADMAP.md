# FastTheme Roadmap

## Milestone Status

### Fluent Material Optimization & Window State Bridge (0.1.9)
**Status:** Released
- [x] Native window state control bridge (`minimizeWindow`, `maximizeWindow`, `restoreWindow`, `sendSysCommand`).
- [x] High-level Fluent title bar helper (`applyFluentTitleBar`, `setTitleBarTheme`).
- [x] Virtual desktop uncloaking DWM recovery (`DWMWA_CLOAKED`).
- [x] Inactive window buffered paint occlusion (`#101010` true opaque dimming).
- [x] Multi-source accent color resolution (Windows DWM registry + `DwmGetColorizationColor`).
- [x] Demo standardization (`Overlay.java`, `Fluent.java`).

### Native OS Color & Telemetry Query Bridge (0.1.8)
**Status:** Released
- [x] Native Windows DWM accent/colorization color retrieval (`getAccentColor`, `getSystemAccentColor`).
- [x] Application personal dark mode query (`isAppDarkMode`).
- [x] Windows accessibility High Contrast detection (`isHighContrast`).
- [x] Windows transparency effects toggle detection (`isTransparencyEnabled`).
- [x] Colorization opacity query (`isColorizationOpaque`).

### Modern Photos-Style Fluent Chrome & Dynamic Hit-Testing (0.1.7)
**Status:** Released
- [x] Custom non-client title bar height configuration (`setTitleBarHeight`).
- [x] Dynamic interactive control exclusion zones in title bar (`addTitleBarControlRect`, `clearTitleBarControlRects`).
- [x] Native title bar button delegation and width configuration (`setNativeTitleBarButtonsEnabled`).
- [x] Non-client frame update trigger (`forceFrameUpdate`).
- [x] Symmetrical `examples/Demo` and `examples/Benchmark` layout.
- [x] Windows Photos-style Fluent chrome showcase (`Demo3.java` / `run-demo-fluent.bat`).

### Mica & Acrylic Seamless Window Flow (0.1.6)
**Status:** Released
- [x] Continuous background repaint subclassing (`BackdropSubclassProc`) eliminating resize flicker and white frame flashes.
- [x] Full-bleed transparent title bar integration (`DWMWA_CAPTION_COLOR = DWMWA_COLOR_NONE`).
- [x] Standalone Win32 `FastWindow` support for hardware-accelerated materials without AWT lockup.

### System Backdrops & Resilient Binary Storage (0.1.5)
**Status:** Released
- [x] Official DWM backdrop material engine (`BACKDROP_MICA`, `BACKDROP_ACRYLIC`, `BACKDROP_MICA_ALT`).
- [x] Dedicated material showcases (`MicaDemo`, `AcrylicDemo`, `MicaAltDemo`).
- [x] Window button visibility toggle (`setWindowButtonsVisible`) and topmost z-order (`setAlwaysOnTop`).
- [x] Self-describing `.themebin` V2 binary format preserving key name bindings across sessions.
- [x] Multi-hop nested alias resolution (`A = @B`, `B = @C`) in `ThemeParser`.
- [x] JNI availability probing (`isNativeAvailable`).

### Schema-Free Dynamic Key Matrix (0.1.4)
**Status:** Released
- [x] 100% schema-free dynamic slot allocation in `ThemeKeys`.
- [x] Streamlined text/binary parser architecture (`ThemeParser`).
- [x] Flexible OS title bar color mapping using arbitrary theme key tokens.

### Elastic Primitive Matrix & Serialization (0.1.3)
**Status:** Released
- [x] Contiguous primitive array in-memory storage (`ThemeData`).
- [x] Text parser supporting variable aliasing (`@KEY`) and sub-microsecond binary deserializer (`.themebin`).
- [x] WCAG 2.1 contrast luminance calculation and auto-readable text foreground.
- [x] Mathematical state generation (tinting/shading).
- [x] Central dynamic state management and `ThemeListener` observer events.

### Native Console Window Transparency (0.1.2)
**Status:** Released
- [x] Console window handle resolution (`getConsoleWindowHandle`).
- [x] Enhanced root-window transparency invalidation (`setWindowTransparency`).

### Premium Borderless Overlays & Win32 DWM Engine (0.1.0)
**Status:** Released
- [x] Non-client frame removal (`WM_NCCALCSIZE`) with native OS drop shadow retention (`setBorderlessShadow`).
- [x] Borderless invisible drag zones (`setOverlayDragHeight`).
- [x] Immersive Windows dark mode title bar control.
- [x] First public release of FastTheme via JitPack.

---

## Upcoming Features

### Real-Time OS Theme Change Listener (Zero-Polling)
**Status:** Planned
- [ ] Message-only window (`HWND_MESSAGE`) or subclassed window listening directly for Windows broadcast message `WM_SETTINGCHANGE` (with `lParam == "ImmersiveColorSet"`).
- [ ] Push-based native event callback into Java (`OnOSThemeChangedListener`) without polling or CPU overhead.
- [ ] Automatic live switching between dark and light `ThemeData` presets upon OS theme changes.

### Extended Non-Client Area (NCA) Controls
**Status:** Backlog
- [ ] Native methods to hide/show the window icon in the title bar.
- [ ] Support for centering title text in native non-client area.
