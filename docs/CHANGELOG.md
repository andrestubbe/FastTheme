# Changelog: FastTheme

All notable changes to this project will be documented in this file.

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
