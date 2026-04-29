# FastTheme Roadmap

## Upcoming Features

### Native System Theme Detection (Windows 10/11)
**Status:** Planning / Implementation

Implement a robust, registry-based method to detect the system's Dark/Light mode preference. This ensures instant detection at startup and provides a reliable source of truth.

**Implementation Details:**
- **Registry Key:** `HKEY_CURRENT_USER\Software\Microsoft\Windows\CurrentVersion\Themes\Personalize`
- **Logic:** 
  - `AppsUseLightTheme` (Value `0` = Dark, `1` = Light)
  - `SystemUsesLightTheme` (Value `0` = Dark, `1` = Light)

**Goal:**
- [ ] Implement `isDarkMode()` and `isLightMode()` methods using these registry keys.
- [ ] **Bonus:** Add a Native Event Listener to detect theme changes in real-time without polling (using `WM_SETTINGCHANGE`).

**Priority:** High - Essential for "Immersive Dark Mode" features.
