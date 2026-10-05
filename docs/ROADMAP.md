# FastTheme Roadmap

## Milestone Status

### Modern Photos-Style Fluent Chrome & Dynamic Hit-Testing (v0.1.7)
**Status:** Released
- [x] Custom non-client title bar height configuration (`setTitleBarHeight`).
- [x] Dynamic interactive control exclusion zones in title bar (`addTitleBarControlRect`, `clearTitleBarControlRects`).
- [x] Native title bar button delegation/toggle (`setNativeTitleBarButtonsEnabled`).
- [x] Non-client frame update trigger (`forceFrameUpdate`).
- [x] Symmetrical `examples/Demo` and `examples/Benchmark` layout.
- [x] Windows Photos-style Fluent chrome showcase (`Demo3.java` / `run-demo-fluent.bat`).

### Universal Color-Matrix & FastFileFormat (v0.1.3)
**Status:** Released
- [x] Contiguous primitive array in-memory storage (`ThemeData`).
- [x] Standardized 49-slot key matrix (`ThemeKeys`).
- [x] Text parser supporting variable aliasing (`@KEY`) and sub-microsecond binary deserializer (`.themebin`).
- [x] WCAG 2.1 contrast luminance calculation and auto-readable text foreground.
- [x] Mathematical state generation (tinting/shading).
- [x] 24-bit Truecolor ANSI CLI sequences.
- [x] Central dynamic state management and `ThemeListener` observer events.

### Native Console Window Transparency (v0.1.2)
**Status:** Released
- [x] Implement `getConsoleWindowHandle()` and `setWindowTransparency()` with root window frame traversal and invalidation.

### Mica & Acrylic Material Support (v0.1.0)
**Status:** Released
- [x] Implement `DWM_SYSTEMBACKDROP_TYPE` enumerations.
- [x] Add support for `DWMSBT_MAINWINDOW` (Mica), `DWMSBT_TRANSIENTWINDOW` (Acrylic), and `DWMSBT_TABBEDWINDOW` (Mica Alt).

### Premium Borderless Overlays (v0.1.0)
**Status:** Released
- [x] Implement `WM_NCCALCSIZE` removal.
- [x] Add `WM_NCHITTEST` drag zone logic.
- [x] Lock resizing for fixed overlays.

## Upcoming Features

### Real-time OS Theme Change Detection
**Status:** In Progress
- [ ] Listen for `WM_SETTINGCHANGE` native events via a background message loop.
- [ ] Automatically toggle active `ThemeData` between default dark and light presets when Windows system mode switches.

### Extended Non-Client Area (NCA) Controls
**Status:** In Progress
- [x] Custom titlebar height metrics and control exclusion hit-testing.
- [ ] Native methods to hide/show the window icon.
- [ ] Support for centering title text in native non-client area.
