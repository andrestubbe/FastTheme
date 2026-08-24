# Changelog: FastTheme

All notable changes to this project will be documented in this file.

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
