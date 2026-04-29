# FastTheme Roadmap

## Upcoming Features

### Real-time Runtime Theme Detection
**Status:** Planning / Implementation

While startup detection is already implemented, FastTheme needs a way to react to theme changes **at runtime** (e.g., when the user switches Windows modes while the app is running).

**Implementation Details:**
- **Mechanism:** Listen for `WM_SETTINGCHANGE` native events via a hidden message window or JNI hook.
- **Source of Truth:** Monitor the registry key `HKEY_CURRENT_USER\Software\Microsoft\Windows\CurrentVersion\Themes\Personalize`.

**Goal:**
- [ ] Implement a `ThemeChangeListener` interface.
- [ ] Add native logic to trigger events when `AppsUseLightTheme` changes.

**Priority:** High - Crucial for apps that stay open for long periods.
