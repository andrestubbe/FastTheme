# Changelog: FastTheme

All notable changes to this project will be documented in this file.

## [0.1.9] - 2026-10-10
### Added
- **Native Window State Control Bridge**:
  - `minimizeWindow(long hwnd)`: Native window minimize via `SW_MINIMIZE`.
  - `maximizeWindow(long hwnd)`: Native window maximize via `SW_MAXIMIZE`.
  - `restoreWindow(long hwnd)`: Native window restore via `SW_RESTORE`.
  - `sendSysCommand(long hwnd, int cmd)`: Directly dispatches native `WM_SYSCOMMAND` calls (`SC_CLOSE`, etc.).
- **Fluent TitleBar High-Level Facade**:
  - `applyFluentTitleBar(long hwnd, int titleBarHeight, int buttonWidth)` / `applyFluentTitleBar(..., TitleBarTheme)`: One-line configuration for custom height, dark mode, rounded corners, Mica backdrop, and styled caption buttons.
  - `setTitleBarTheme(long hwnd, TitleBarTheme theme)`: Configures custom active and inactive title bar background and button states.

### Changed
- **DWM Cloaked Transition Handling**: Enhanced `BackdropSubclassProc` to track virtual desktop switches and uncloaking events via `DWMWA_CLOAKED`, automatically kickstarting DWM materials when switching desktops.
- **Defocus/Inactive Backdrop Dimming**: Custom buffered paint occludes Mica with opaque native Windows 11 grey (`#101010`) in inactive state, eliminating milky washed-out additive blends.
- **Accent Color Accuracy**: Improved `getAccentColor()` fallback hierarchy by querying Windows DWM registry `AccentColor` (ABGR to ARGB) followed by `DwmGetColorizationColor` with visible alpha clamp.
- **Demo Renaming**: Symmetrically standardized demos to `Overlay.java` (Raycast overlay) and `Fluent.java` (48px custom Fluent title bar).

## [0.1.8] - 2026-10-09
### Added
- **Native OS Color & Theme State Detection**:
  - `getAccentColor()`: Retrieves native Windows DWM accent/colorization color as packed 32-bit ARGB integer.
  - `getSystemAccentColor()`: Returns Windows DWM accent color as `java.awt.Color`.
  - `isAppDarkMode()`: Queries Windows personal application dark mode setting (`AppsUseLightTheme`).
  - `isHighContrast()`: Detects whether Windows accessibility High Contrast mode is currently enabled.
  - `isTransparencyEnabled()`: Queries system-wide transparency effects toggle from Windows personalization.
  - `isColorizationOpaque()`: Checks whether DWM colorization is set to opaque.

## [0.1.7] - 2026-10-05
### Added
- **Modern Windows 11 Photos-Style Chrome (Demo 3)**: Added full custom title bar support (`Demo3.java`) with high-DPI scaled interactive caption buttons (Minimize, Maximize/Restore, Close), native Mica backdrop, and drop shadow.
- **Custom Interactive Title Bar Height**: Added `setTitleBarHeight(long hwnd, int height)` allowing dynamic window drag areas without losing native window resizing borders.
- **Hit-Test Exclusion Control Rectangles**: Added `addTitleBarControlRect(long hwnd, int x, int y, int w, int h)` and `clearTitleBarControlRects(long hwnd)` enabling interactive Swing controls (buttons, tabs, search inputs) to receive clicks and hovers inside native drag caption zones.
- **Native Caption Button Delegation**: Added `setNativeTitleBarButtonsEnabled(long hwnd, boolean enabled, int buttonWidth)` allowing Windows DWM to manage caption buttons with customizable button widths.
- **DWM Frame Re-evaluation**: Added `forceFrameUpdate(long hwnd)` to immediately trigger non-client frame recalculation on visible windows.

## [0.1.6] - 2026-09-27
### Fixed
- **Mica & Acrylic Background Frame Refresh**: Resolved white window body flashes and maximized inset borders by introducing native `BackdropSubclassProc` that continuously clears update rectangles with a transparent/DWM-permeable brush across initial display, resizing, and window maximizing.
- **Transparent Titlebar Flow**: Integrated `DWMWA_CAPTION_COLOR = DWMWA_COLOR_NONE` and `DwmExtendFrameIntoClientArea` to allow Windows 11 Mica, Mica Alt, and Acrylic materials to flow seamlessly across both title bar and window client area.
- **Standalone FastWindow Integration**: Updated all material demos (`MicaDemo`, `MicaAltDemo`, `AcrylicDemo`) to run on pure Win32 `FastWindow` contexts without AWT/Swing frame lockups.

## [0.1.5] - 2026-09-26
### Added
- **Windows 11 System Backdrop Engine**: Added `setSystemBackdropType(long hwnd, int type)` with support for `BACKDROP_MICA`, `BACKDROP_ACRYLIC`, and `BACKDROP_MICA_ALT` via official DWM attribute 38 and frame extension.
- **Dedicated Material Demos**: Added `MicaDemo`, `AcrylicDemo`, and `MicaAltDemo` showcasing transparent Swing client areas with hardware-accelerated DWM backgrounds.
- **Native Window Chrome Controls**: Added `setWindowButtonsVisible(hwnd, min, max)` to dynamically toggle minimize/maximize buttons and `setAlwaysOnTop(hwnd, bool)` for native topmost z-order control.
- **Self-Describing Binary Format (V2)**: `.themebin` serializes key names alongside slot values, enabling robust persistence across JVM sessions while maintaining backward-compatible V1 decoding.
- **Iterative Multi-Hop Alias Resolution**: `ThemeParser.parseText` resolves nested variable chains (e.g., `A = @B`, `B = @C`) with cycle detection.
- **Native Status Indicator**: Added `isNativeAvailable()` to safely verify JNI DLL presence before calling platform-specific methods.

### Fixed
- **GDI Handle Leak**: Prevented unbounded `HBRUSH` allocation in `setWindowBackgroundColor` via window property tracking and `DeleteObject()` cleanup on overwrite and `WM_NCDESTROY`.
- **JAWT Crash Protection**: Added null verification for `dsi` and `dsi->platformInfo` in `getWindowHandle`.
- **Thread Safety in `ThemeKeys`**: Replaced race-prone check-then-act registration with synchronized locking and `Locale.ROOT` string normalization.
- **Buffer Safety in `ThemeParser`**: Added bounds validation for `nameLen` and `slotCount` in `parseBinary`.
- **Contrast Accuracy**: Corrected `ThemeColorUtil.getContrastForeground` to compare genuine WCAG contrast ratios.
- **Encapsulation**: Guarded `ThemeData.getRawValues()` with defensive copying.

## [0.1.4] - 2026-08-24

### Changed
- **100% Schema-Free Pure Dynamic Registry (`ThemeKeys`)**: Removed all hardcoded slot constants and presets; any string key is dynamically allocated an integer slot ID on demand.
- **Pure Format Deserializer (`ThemeParser`)**: Streamlined parser dedicated purely to text (`.theme`) and binary (`.themebin`) formats and file loading.
- **Flexible OS Titlebar Synchronization**: Added `applyToWindow(hwnd, titleBgKey, titleFgKey, winBgKey)` supporting custom key names.
- **Decoupled ANSI Generation**: Terminal Truecolor formatting is handled externally via `FastANSI` (`FastANSI.fgArgb()`).

## [0.1.3] - 2026-08-24
### Added
- **Open Dynamic Key Registry (`ThemeKeys`)**: Fully elastic, thread-safe dynamic key allocator (`ThemeKeys.slot("KEY")`, `ThemeKeys.register("KEY")`) supporting arbitrary custom keys with $O(1)$ primitive array reads.
- **Elastic Theme Storage (`ThemeData`)**: Contiguous `int[]` primitive array that automatically expands on demand for 32-bit ARGB packed colors with `.toBinary()` and `.toText()` export.
- **Dual Text/Binary Theme Parser (`ThemeParser`)**: High-speed parser for `.theme` (supporting `@KEY` variable aliasing and automatic registration of unknown keys) and `.themebin` binary streams.
- **Embedded Default Presets**: Built-in zero-dependency presets (`loadDefaultDark()`, `loadDefaultLight()`, `loadDefaultCream()`).
- **Color Mathematics & WCAG Metrics (`ThemeColorUtil`)**: WCAG 2.1 relative luminance, contrast ratio calculation, auto-readable foreground determination, tint/shade state generation, and color string parsing.
- **Live Theme State & OS Sync (`FastTheme`)**: Global state management (`FastTheme.set()`, `FastTheme.load()`, `FastTheme.current()`), dynamic observer notifications (`ThemeListener`), and automatic native DWM window color synchronization (`FastTheme.applyToWindow()`).
- **JitPack Configuration (`jitpack.yml`)**: Added OpenJDK 17 build profile.

### Changed
- **Decoupled ANSI Generation**: Relocated Truecolor terminal escape sequences to `FastANSI` (`FastANSI.fgArgb()`, `FastANSI.bgArgb()`) for clean modular separation.

## [0.1.2] - 2026-07-26
### Added
- **Native Console Window Support**: Added `getConsoleWindowHandle()` to query the native Win32 `HWND` of Windows console windows (`cmd.exe` / ConHost) with automatic root owner resolution (`GA_ROOTOWNER` / `GA_ROOT`).
- **Enhanced Transparency Handling**: Updated `setWindowTransparency()` to automatically target parent/root window containers and trigger immediate frame invalidation and redraws (`SetWindowPos` + `RedrawWindow`).

## [0.1.0] - 2026-05-11
### Added
- **First public release of FastTheme via JitPack.**
- **Premium Borderless Mode**: Added `setBorderlessShadow(long hwnd, boolean enabled)` for Raycast-style overlays.
- **Adjustable Drag Zone**: Added `setOverlayDragHeight(long hwnd, int pixels)` for invisible grab areas.
- **Native Resizing Control**: Borderless mode now automatically suppresses resize cursors via `WM_NCHITTEST`.
- **Ecosystem Integration**: Added dependencies for `FastAnimation` and `FastTween` in the demo modules.
- **Focus Stability**: Added `WM_NCACTIVATE` and `WM_NCPAINT` overrides to prevent flicker and margins on focus change.
- **Native Mica Support**: Added `enableMica(long hwnd, boolean enabled)` for Windows 11 material effects.
- **Corner Styling**: Added `setCornerStyle(long hwnd, int style)` (Rounded, Small Rounded, Square).
- **Dark Mode Detection**: Added `isSystemDarkMode()` to check global Windows theme state.
- **Titlebar Styling**: Added `setTitleBarColor` and `setTitleBarTextColor` for Windows 11.
- **Immersive Dark Mode**: Integrated `setTitleBarDarkMode` for professional titlebar aesthetics.
- **Transparency**: Added `setWindowTransparency` for window-wide alpha blending.

---
**Part of the FastJava Ecosystem**
