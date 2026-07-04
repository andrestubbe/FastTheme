# Changelog: FastTheme

All notable changes to this project will be documented in this file.

## [0.1.1] - 2026-07-04
### Added
- **Window Body/Client Area Coloring**: Added `setWindowBackgroundColor(long hwnd, int r, int g, int b)` to eliminate the default white frame/background flash during JFrame creation/display.

## [0.1.0] - 2026-05-11
### Added
- **First public release of FastTheme via JitPack.**
- **Premium Borderless Mode**: Added `setBorderlessShadow(long hwnd, boolean enabled)` for Raycast-style overlays.
- **Adjustable Drag Zone**: Added `setOverlayDragHeight(long hwnd, int pixels)` for invisible grab areas.
- **Native Resizing Control**: Borderless mode now automatically suppresses resize cursors via `WM_NCHITTEST`.
- **Ecosystem Integration**: Core dependency for FastUI, FastWindow, FastThumb and other FastJava modules.
- **Focus Stability**: Added `WM_NCACTIVATE` and `WM_NCPAINT` overrides to prevent flicker and margins on focus change.
- **Native Mica Support**: Added `enableMica(long hwnd, boolean enabled)` for Windows 11 material effects.
- **Corner Styling**: Added `setCornerStyle(long hwnd, int style)` (Rounded, Small Rounded, Square).
- **Dark Mode Detection**: Added `isSystemDarkMode()` to check global Windows theme state.
- **Titlebar Styling**: Added `setTitleBarColor` and `setTitleBarTextColor` for Windows 11.
- **Immersive Dark Mode**: Integrated `setTitleBarDarkMode` for professional titlebar aesthetics.
- **Transparency**: Added `setWindowTransparency` for window-wide alpha blending.

---
**Part of the FastJava Ecosystem**
