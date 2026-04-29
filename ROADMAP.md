# FastTheme Roadmap

## Upcoming Features

### System Theme Detection at Startup
**Status:** Research Phase

Currently, FastTheme detects the system theme (Dark/Light mode) via JNI calls. However, a more robust approach would be to read the Windows registry key that stores this preference.

**Goal:** Find and implement a reliable registry-based method to detect if the system is in Dark or Light mode immediately at application startup, without relying on real-time JNI polling.

**Registry Target (Research):**
- Investigate `HKEY_CURRENT_USER\Software\Microsoft\Windows\CurrentVersion\Themes\Personalize`
- Key: `AppsUseLightTheme` or `SystemUsesLightTheme`
- Value: `0` = Dark Mode, `1` = Light Mode

**Challenges:**
- Need error-free real-time detection
- Must handle registry access permissions gracefully
- Should fallback to current JNI method if registry read fails

**Priority:** High - This would make theme detection instant and more reliable.
