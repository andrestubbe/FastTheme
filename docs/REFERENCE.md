# FastTheme API Reference

This document outlines the native Win32 DWM API contracts, methods, and JNI integrations of the **FastTheme** engine.

---

## 1. Class: `fasttheme.FastTheme`

Primary JNI boundary class for Windows Desktop Window Manager (DWM) and Win32 layered window styling.

### Static Native Methods

*   `public static native long getWindowHandle(java.awt.Component component)`
    *   **Description**: Uses Java AWT Native Interface (JAWT) to extract the native 64-bit Win32 window handle (`HWND`) of a Swing/AWT component or frame.
    *   **Returns**: 64-bit `HWND` address as a `long`, or `0` on failure.

*   `public static native long getConsoleWindowHandle()`
    *   **Description**: Retrieves the native Win32 `HWND` of the active console window (`cmd.exe` / ConHost) with automatic root owner resolution (`GA_ROOTOWNER` / `GA_ROOT`).
    *   **Returns**: 64-bit `HWND` address of the console or its root container.

*   `public static native boolean setWindowTransparency(long hwnd, int alpha)`
    *   **Description**: Enables Win32 `WS_EX_LAYERED` window style and sets window opacity. Automatically applies attributes to parent/root containers and triggers immediate frame invalidation.
    *   **Parameters**:
        *   `hwnd`: 64-bit native window handle.
        *   `alpha`: Alpha opacity from `0` (completely transparent) to `255` (opaque).
    *   **Returns**: `true` on success, `false` on failure.

*   `public static native boolean setTitleBarDarkMode(long hwnd, boolean enabled)`
    *   **Description**: Toggles Windows 10/11 immersive dark mode for the native window title bar using `DWMWA_USE_IMMERSIVE_DARK_MODE` (attribute 20).
    *   **Parameters**: `enabled` — `true` for dark mode, `false` for light mode.

*   `public static native boolean setTitleBarColor(long hwnd, int r, int g, int b)`
    *   **Description**: Sets custom RGB title bar caption color on Windows 11 using `DWMWA_CAPTION_COLOR` (attribute 35).

*   `public static native boolean setTitleBarTextColor(long hwnd, int r, int g, int b)`
    *   **Description**: Sets custom RGB title bar text color on Windows 11 using `DWMWA_TEXT_COLOR` (attribute 36).

*   `public static native boolean enableMica(long hwnd, boolean enabled)`
    *   **Description**: Enables Windows 11 Mica backdrop material effect on the window frame via `DWMWA_MICA_EFFECT` (attribute 38).

*   `public static native boolean setCornerStyle(long hwnd, int style)`
    *   **Description**: Configures window corner preferences on Windows 11 (`DWMWA_WINDOW_CORNER_PREFERENCE` attribute 33).
    *   **Styles**: `0` (Default), `1` (Square), `2` (Rounded), `3` (Small Rounded).

*   `public static native boolean setBorderlessShadow(long hwnd, boolean enabled)`
    *   **Description**: Removes window chrome while extending DWM frame into client area to maintain native Windows drop shadows. Subclasses window procedure to intercept `WM_NCCALCSIZE`, `WM_NCACTIVATE`, and `WM_NCPAINT`.

*   `public static native boolean setOverlayDragHeight(long hwnd, int height)`
    *   **Description**: Configures the invisible drag zone height at the top of borderless windows via `WM_NCHITTEST` interception.

*   `public static native boolean isSystemDarkMode()`
    *   **Description**: Checks global Windows dark mode preferences via the Registry (`AppsUseLightTheme`).

---

## 2. Platform Requirements

- **OS**: Windows 10 (Build 1903+) or Windows 11
- **Architecture**: x86_64 (64-bit)
- **Java Version**: JDK 17+
