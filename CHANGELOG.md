# Changelog: FastTheme

All notable changes to this project will be documented in this file.

## [0.1.0] - 2026-05-02
### Added
- **Native Mica Support**: Added `enableMica(long hwnd, boolean enabled)` for Windows 11 material effects.
- **Corner Styling**: Added `setCornerStyle(long hwnd, int style)` (Rounded, Small Rounded, Square).
- **Dark Mode Detection**: Added `isSystemDarkMode()` to check global Windows theme state.
- **Titlebar Styling**: Added `setTitleBarColor` and `setTitleBarTextColor` for Windows 11.
- **Immersive Dark Mode**: Integrated `setTitleBarDarkMode` for professional titlebar aesthetics.
- **Transparency**: Added `setWindowTransparency` for window-wide alpha blending.

### Changed
- **Architectural Shift**: Redefined FastTheme as a pure **Appearance Layer**. Window management logic (constraints, sizing) is now delegated to **FastWindow**.
- **Unified JNI**: Standardized naming conventions to match the FastJava ecosystem.
- **Updated README**: Added modern badges, Table of Contents, and ecosystem branding.

### Fixed
- Improved HWND extraction logic for nested Swing components.
- Fixed DWM attribute conflicts on Windows 10 versions.

---
**Part of the FastJava Ecosystem**
