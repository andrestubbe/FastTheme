#include <jni.h>
#include <windows.h>
#include <dwmapi.h>
#include <windowsx.h>
#include <jawt.h>
#include <jawt_md.h>
#include <stdio.h>

#pragma comment(lib, "dwmapi.lib")
#pragma comment(lib, "user32.lib")
#pragma comment(lib, "gdi32.lib")
#pragma comment(lib, "advapi32.lib")

// DWM attribute constants (Win 11+)
#ifndef DWMWA_CAPTION_COLOR
#define DWMWA_CAPTION_COLOR 35
#endif

#ifndef DWMWA_TEXT_COLOR
#define DWMWA_TEXT_COLOR 36
#endif

#ifndef DWMWA_BORDER_COLOR
#define DWMWA_BORDER_COLOR 34
#endif

#ifndef DWMWA_USE_IMMERSIVE_DARK_MODE
#define DWMWA_USE_IMMERSIVE_DARK_MODE 20
#endif

// Undocumented DWM attributes for button colors (Win 11 22H2+)
#ifndef DWMWA_CAPTION_BUTTON_COLOR
#define DWMWA_CAPTION_BUTTON_COLOR 37
#endif

#ifndef DWMWA_CAPTION_BUTTON_HOVER_COLOR
#define DWMWA_CAPTION_BUTTON_HOVER_COLOR 38
#endif

#ifndef DWMWA_CAPTION_BUTTON_PRESSED_COLOR
#define DWMWA_CAPTION_BUTTON_PRESSED_COLOR 39
#endif

#ifndef DWMWA_CAPTION_BUTTON_ICON_COLOR
#define DWMWA_CAPTION_BUTTON_ICON_COLOR 40
#endif

// Helper function to get HWND from AWT Component
HWND GetHwndFromComponent(JNIEnv* env, jobject component) {
    JAWT awt;
    JAWT_DrawingSurface* ds = NULL;
    JAWT_DrawingSurfaceInfo* dsi = NULL;
    HWND hwnd = NULL;
    
    // Use a higher version for better compatibility with modern Java
    awt.version = JAWT_VERSION_9;
    
    // Get AWT
    if (!JAWT_GetAWT(env, &awt)) {
        return NULL;
    }
    
    // Check if function pointers are valid
    if (awt.GetDrawingSurface == NULL) {
        return NULL;
    }
    
    // Get Drawing Surface
    ds = awt.GetDrawingSurface(env, component);
    if (ds == NULL) {
        return NULL;
    }
    
    // Check if GetDrawingSurfaceInfo is valid
    if (ds->GetDrawingSurfaceInfo == NULL) {
        awt.FreeDrawingSurface(ds);
        return NULL;
    }
    
    // Lock the surface (required before GetDrawingSurfaceInfo)
    jint lock = ds->Lock(ds);
    if ((lock & JAWT_LOCK_ERROR) != 0) {
        awt.FreeDrawingSurface(ds);
        return NULL;
    }
    
    // Get Drawing Surface Info
    dsi = ds->GetDrawingSurfaceInfo(ds);
    
    // Unlock the surface
    ds->Unlock(ds);
    
    if (dsi == NULL) {
        awt.FreeDrawingSurface(ds);
        return NULL;
    }
    
    // Get HWND from platform info
    JAWT_Win32DrawingSurfaceInfo* win32Info = (JAWT_Win32DrawingSurfaceInfo*)dsi->platformInfo;
    if (win32Info != NULL) {
        hwnd = win32Info->hwnd;
    }
    
    // Cleanup
    ds->FreeDrawingSurfaceInfo(dsi);
    awt.FreeDrawingSurface(ds);
    
    return hwnd;
}

// Alternative: Get HWND from Window - walks up to find the real frame window
HWND GetHwndFromWindow(JNIEnv* env, jobject window) {
    // Get the native handle through AWT
    HWND hwnd = GetHwndFromComponent(env, window);
    if (hwnd != NULL) {
        // For JFrame, we need the actual frame window, not the canvas
        // Walk up to find the real window
        HWND parent = GetParent(hwnd);
        while (parent != NULL) {
            char className[256];
            GetClassNameA(parent, className, sizeof(className));
            // SunAwtFrame is the class name for AWT frames
            if (strstr(className, "SunAwtFrame") != NULL) {
                return parent;
            }
            parent = GetParent(parent);
        }
    }
    return hwnd;
}

extern "C" {

JNIEXPORT jlong JNICALL Java_TitleBarJNI_getWindowHandle(JNIEnv* env, jobject obj, jobject component) {
    return (jlong)GetHwndFromWindow(env, component);
}

JNIEXPORT jboolean JNICALL Java_TitleBarJNI_setTitleBarColor(JNIEnv* env, jobject obj, jlong hwndLong, jint r, jint g, jint b) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;
    
    // Windows 11+ only
    COLORREF color = RGB(r, g, b);
    HRESULT hr = DwmSetWindowAttribute(hwnd, DWMWA_CAPTION_COLOR, &color, sizeof(color));
    
    // Force redraw
    SetWindowPos(hwnd, NULL, 0, 0, 0, 0, 
        SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_FRAMECHANGED | SWP_DRAWFRAME);
    InvalidateRect(hwnd, NULL, TRUE);
    UpdateWindow(hwnd);
    
    return SUCCEEDED(hr) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_TitleBarJNI_setTitleBarDarkMode(JNIEnv* env, jobject obj, jlong hwndLong, jboolean enabled) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;
    
    BOOL darkMode = enabled ? TRUE : FALSE;
    
    // Try Windows 11 attribute first
    HRESULT hr = DwmSetWindowAttribute(hwnd, DWMWA_USE_IMMERSIVE_DARK_MODE, &darkMode, sizeof(darkMode));
    
    // Fallback for Windows 10
    if (FAILED(hr)) {
        // Try alternative attribute number for Win10
        const DWORD DWMWA_USE_IMMERSIVE_DARK_MODE_OLD = 19;
        hr = DwmSetWindowAttribute(hwnd, DWMWA_USE_IMMERSIVE_DARK_MODE_OLD, &darkMode, sizeof(darkMode));
    }
    
    // Force redraw
    SetWindowPos(hwnd, NULL, 0, 0, 0, 0, 
        SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_FRAMECHANGED | SWP_DRAWFRAME);
    InvalidateRect(hwnd, NULL, TRUE);
    UpdateWindow(hwnd);
    
    return SUCCEEDED(hr) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_TitleBarJNI_setBorderColor(JNIEnv* env, jobject obj, jlong hwndLong, jint r, jint g, jint b) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;
    
    // Windows 11+ only
    COLORREF color = RGB(r, g, b);
    HRESULT hr = DwmSetWindowAttribute(hwnd, DWMWA_BORDER_COLOR, &color, sizeof(color));
    
    SetWindowPos(hwnd, NULL, 0, 0, 0, 0, 
        SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_FRAMECHANGED);
    
    return SUCCEEDED(hr) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_TitleBarJNI_setTextColor(JNIEnv* env, jobject obj, jlong hwndLong, jint r, jint g, jint b) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;
    
    // Windows 11+ only
    COLORREF color = RGB(r, g, b);
    HRESULT hr = DwmSetWindowAttribute(hwnd, DWMWA_TEXT_COLOR, &color, sizeof(color));
    
    SetWindowPos(hwnd, NULL, 0, 0, 0, 0, 
        SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_FRAMECHANGED);
    
    return SUCCEEDED(hr) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_TitleBarJNI_hideCaptionButtons(JNIEnv* env, jobject obj, jlong hwndLong) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;
    
    // Remove window styles for min/max/close buttons
    LONG style = GetWindowLong(hwnd, GWL_STYLE);
    style &= ~(WS_MINIMIZEBOX | WS_MAXIMIZEBOX | WS_SYSMENU);
    SetWindowLong(hwnd, GWL_STYLE, style);
    
    SetWindowPos(hwnd, NULL, 0, 0, 0, 0, 
        SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_FRAMECHANGED | SWP_DRAWFRAME);
    
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_TitleBarJNI_showCaptionButtons(JNIEnv* env, jobject obj, jlong hwndLong) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;
    
    // Restore window styles
    LONG style = GetWindowLong(hwnd, GWL_STYLE);
    style |= (WS_MINIMIZEBOX | WS_MAXIMIZEBOX | WS_SYSMENU);
    SetWindowLong(hwnd, GWL_STYLE, style);
    
    SetWindowPos(hwnd, NULL, 0, 0, 0, 0, 
        SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_FRAMECHANGED | SWP_DRAWFRAME);
    
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_TitleBarJNI_extendFrameIntoClientArea(
    JNIEnv* env, jobject obj, jlong hwndLong, jint left, jint top, jint right, jint bottom) {
    
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;
    
    MARGINS margins;
    
    if (left == -1 && top == -1 && right == -1 && bottom == -1) {
        // Full window glass effect
        margins.cxLeftWidth = -1;
        margins.cxRightWidth = -1;
        margins.cyTopHeight = -1;
        margins.cyBottomHeight = -1;
    } else {
        margins.cxLeftWidth = left;
        margins.cxRightWidth = right;
        margins.cyTopHeight = top;
        margins.cyBottomHeight = bottom;
    }
    
    HRESULT hr = DwmExtendFrameIntoClientArea(hwnd, &margins);
    
    return SUCCEEDED(hr) ? JNI_TRUE : JNI_FALSE;
}

// Undocumented button color methods (Win 11 22H2+)
JNIEXPORT jboolean JNICALL Java_TitleBarJNI_setButtonColor(JNIEnv* env, jobject obj, jlong hwndLong, jint r, jint g, jint b) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;
    
    COLORREF color = RGB(r, g, b);
    HRESULT hr = DwmSetWindowAttribute(hwnd, DWMWA_CAPTION_BUTTON_COLOR, &color, sizeof(color));
    
    SetWindowPos(hwnd, NULL, 0, 0, 0, 0, SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_FRAMECHANGED);
    
    return SUCCEEDED(hr) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_TitleBarJNI_setButtonHoverColor(JNIEnv* env, jobject obj, jlong hwndLong, jint r, jint g, jint b) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;
    
    COLORREF color = RGB(r, g, b);
    HRESULT hr = DwmSetWindowAttribute(hwnd, DWMWA_CAPTION_BUTTON_HOVER_COLOR, &color, sizeof(color));
    
    SetWindowPos(hwnd, NULL, 0, 0, 0, 0, SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_FRAMECHANGED);
    
    return SUCCEEDED(hr) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_TitleBarJNI_setButtonPressedColor(JNIEnv* env, jobject obj, jlong hwndLong, jint r, jint g, jint b) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;
    
    COLORREF color = RGB(r, g, b);
    HRESULT hr = DwmSetWindowAttribute(hwnd, DWMWA_CAPTION_BUTTON_PRESSED_COLOR, &color, sizeof(color));
    
    SetWindowPos(hwnd, NULL, 0, 0, 0, 0, SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_FRAMECHANGED);
    
    return SUCCEEDED(hr) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_TitleBarJNI_setButtonIconColor(JNIEnv* env, jobject obj, jlong hwndLong, jint r, jint g, jint b) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;
    
    COLORREF color = RGB(r, g, b);
    HRESULT hr = DwmSetWindowAttribute(hwnd, DWMWA_CAPTION_BUTTON_ICON_COLOR, &color, sizeof(color));
    
    SetWindowPos(hwnd, NULL, 0, 0, 0, 0, SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_FRAMECHANGED);
    
    return SUCCEEDED(hr) ? JNI_TRUE : JNI_FALSE;
}

// Convenience method: Set up a blue close button theme
JNIEXPORT jboolean JNICALL Java_TitleBarJNI_setBlueCloseButtonTheme(JNIEnv* env, jobject obj, jlong hwndLong) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;
    
    // Button normal: blue
    COLORREF normal = RGB(0, 120, 212);
    DwmSetWindowAttribute(hwnd, DWMWA_CAPTION_BUTTON_COLOR, &normal, sizeof(normal));
    
    // Button hover: darker blue
    COLORREF hover = RGB(0, 90, 158);
    DwmSetWindowAttribute(hwnd, DWMWA_CAPTION_BUTTON_HOVER_COLOR, &hover, sizeof(hover));
    
    // Button pressed: even darker
    COLORREF pressed = RGB(0, 60, 100);
    DwmSetWindowAttribute(hwnd, DWMWA_CAPTION_BUTTON_PRESSED_COLOR, &pressed, sizeof(pressed));
    
    // Icon: white
    COLORREF icon = RGB(255, 255, 255);
    DwmSetWindowAttribute(hwnd, DWMWA_CAPTION_BUTTON_ICON_COLOR, &icon, sizeof(icon));
    
    SetWindowPos(hwnd, NULL, 0, 0, 0, 0, SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_FRAMECHANGED);
    
    return JNI_TRUE;
}

// Window transparency via Layered Window (0-255, 255=opaque)
JNIEXPORT jboolean JNICALL Java_FastThemeTerminal_setWindowTransparency(JNIEnv* env, jobject obj, jlong hwndLong, jint alpha) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;
    
    // Clamp alpha to 0-255
    if (alpha < 0) alpha = 0;
    if (alpha > 255) alpha = 255;
    
    // Get current extended style
    LONG exStyle = GetWindowLong(hwnd, GWL_EXSTYLE);
    
    // Add WS_EX_LAYERED if not present
    if (!(exStyle & WS_EX_LAYERED)) {
        SetWindowLong(hwnd, GWL_EXSTYLE, exStyle | WS_EX_LAYERED);
    }
    
    // Set transparency
    // LWA_ALPHA = use alpha for transparency
    // LWA_COLORKEY = use color key (we don't use this)
    BOOL result = SetLayeredWindowAttributes(hwnd, 0, (BYTE)alpha, LWA_ALPHA);
    
    // Force redraw
    RedrawWindow(hwnd, NULL, NULL, RDW_ERASE | RDW_INVALIDATE | RDW_FRAME | RDW_ALLCHILDREN);
    
    return result ? JNI_TRUE : JNI_FALSE;
}

// Also export for TitleBarJNI class (backward compat)
JNIEXPORT jboolean JNICALL Java_TitleBarJNI_setWindowTransparency(JNIEnv* env, jobject obj, jlong hwndLong, jint alpha) {
    return Java_FastThemeTerminal_setWindowTransparency(env, obj, hwndLong, alpha);
}

// FastThemeTerminal exports for all methods
JNIEXPORT jlong JNICALL Java_FastThemeTerminal_getWindowHandle(JNIEnv* env, jobject obj, jobject component) {
    return (jlong)GetHwndFromWindow(env, component);
}

JNIEXPORT jboolean JNICALL Java_FastThemeTerminal_setTitleBarColor(JNIEnv* env, jobject obj, jlong hwndLong, jint r, jint g, jint b) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;
    COLORREF color = RGB(r, g, b);
    HRESULT hr = DwmSetWindowAttribute(hwnd, DWMWA_CAPTION_COLOR, &color, sizeof(color));
    SetWindowPos(hwnd, NULL, 0, 0, 0, 0, SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_FRAMECHANGED);
    return SUCCEEDED(hr) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_FastThemeTerminal_setTitleBarTextColor(JNIEnv* env, jobject obj, jlong hwndLong, jint r, jint g, jint b) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;
    COLORREF color = RGB(r, g, b);
    HRESULT hr = DwmSetWindowAttribute(hwnd, DWMWA_TEXT_COLOR, &color, sizeof(color));
    SetWindowPos(hwnd, NULL, 0, 0, 0, 0, SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_FRAMECHANGED);
    return SUCCEEDED(hr) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_FastThemeTerminal_setTitleBarDarkMode(JNIEnv* env, jobject obj, jlong hwndLong, jboolean enabled) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;
    BOOL darkMode = enabled ? TRUE : FALSE;
    HRESULT hr = DwmSetWindowAttribute(hwnd, DWMWA_USE_IMMERSIVE_DARK_MODE, &darkMode, sizeof(darkMode));
    if (FAILED(hr)) {
        const DWORD DWMWA_USE_IMMERSIVE_DARK_MODE_OLD = 19;
        hr = DwmSetWindowAttribute(hwnd, DWMWA_USE_IMMERSIVE_DARK_MODE_OLD, &darkMode, sizeof(darkMode));
    }
    SetWindowPos(hwnd, NULL, 0, 0, 0, 0, SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_FRAMECHANGED);
    return SUCCEEDED(hr) ? JNI_TRUE : JNI_FALSE;
}

// FastTheme System Detection Methods
JNIEXPORT jstring JNICALL Java_FastThemeTerminal_getSystemResolution(JNIEnv* env, jobject obj) {
    int width = GetSystemMetrics(SM_CXSCREEN);
    int height = GetSystemMetrics(SM_CYSCREEN);
    char buffer[64];
    sprintf_s(buffer, sizeof(buffer), "%dx%d", width, height);
    return env->NewStringUTF(buffer);
}

JNIEXPORT jint JNICALL Java_FastThemeTerminal_getSystemDPI(JNIEnv* env, jobject obj) {
    HDC hdc = GetDC(NULL);
    if (hdc) {
        int dpi = GetDeviceCaps(hdc, LOGPIXELSX);
        ReleaseDC(NULL, hdc);
        return dpi;
    }
    return 96; // Default
}

JNIEXPORT jboolean JNICALL Java_FastThemeTerminal_isSystemDarkMode(JNIEnv* env, jobject obj) {
    HKEY hKey;
    DWORD value = 0;
    DWORD dataSize = sizeof(value);
    
    if (RegOpenKeyExA(HKEY_CURRENT_USER, 
        "Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize", 
        0, KEY_READ, &hKey) == ERROR_SUCCESS) {
        
        if (RegQueryValueExA(hKey, "AppsUseLightTheme", NULL, NULL, 
            (LPBYTE)&value, &dataSize) == ERROR_SUCCESS) {
            RegCloseKey(hKey);
            return (value == 0) ? JNI_TRUE : JNI_FALSE; // 0 = dark mode
        }
        RegCloseKey(hKey);
    }
    return JNI_FALSE; // Default to light
}

JNIEXPORT jint JNICALL Java_FastThemeTerminal_getSystemRefreshRate(JNIEnv* env, jobject obj) {
    // Use DEVMODE (not DEVMODEA) and C++11 {} initialization like FastTheme.cpp
    DEVMODE dm = {};
    dm.dmSize = sizeof(dm);
    
    // Get current settings for the primary monitor
    // ENUM_CURRENT_SETTINGS = -1 gives current settings, not registry settings
    if (EnumDisplaySettings(NULL, ENUM_CURRENT_SETTINGS, &dm)) {
        // Check if refresh rate field is valid using dmFields
        if (dm.dmFields & DM_DISPLAYFREQUENCY) {
            if (dm.dmDisplayFrequency > 0 && dm.dmDisplayFrequency < 1000) {
                return dm.dmDisplayFrequency;
            }
        }
    }
    
    return 60; // Default fallback
}

// Set window icon to white circle with black dot
JNIEXPORT jboolean JNICALL Java_FastThemeTerminal_setWindowIcon(JNIEnv* env, jobject obj, jlong hwndLong) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;
    
    const int size = 32;
    const int center = size / 2;
    const int radius = 14;
    const int dotRadius = 4;
    
    // Create memory DC
    HDC hdcScreen = GetDC(NULL);
    HDC hdcMem = CreateCompatibleDC(hdcScreen);
    
    // Create 32-bit bitmap with alpha
    BITMAPINFO bmi;
    ZeroMemory(&bmi, sizeof(bmi));
    bmi.bmiHeader.biSize = sizeof(BITMAPINFOHEADER);
    bmi.bmiHeader.biWidth = size;
    bmi.bmiHeader.biHeight = -size; // Top-down
    bmi.bmiHeader.biPlanes = 1;
    bmi.bmiHeader.biBitCount = 32;
    bmi.bmiHeader.biCompression = BI_RGB;
    
    void* bits = NULL;
    HBITMAP hbmColor = CreateDIBSection(hdcMem, &bmi, DIB_RGB_COLORS, &bits, NULL, 0);
    HBITMAP hbmMask = CreateBitmap(size, size, 1, 1, NULL);
    
    if (!hbmColor || !hbmMask) {
        DeleteDC(hdcMem);
        ReleaseDC(NULL, hdcScreen);
        return JNI_FALSE;
    }
    
    HBITMAP hbmOld = (HBITMAP)SelectObject(hdcMem, hbmColor);
    
    // Fill transparent
    ZeroMemory(bits, size * size * 4);
    
    // Draw white circle
    HBRUSH whiteBrush = CreateSolidBrush(RGB(255, 255, 255));
    HBRUSH blackBrush = CreateSolidBrush(RGB(0, 0, 0));
    HPEN nullPen = CreatePen(PS_NULL, 0, 0);
    
    HGDIOBJ oldBrush = SelectObject(hdcMem, whiteBrush);
    HGDIOBJ oldPen = SelectObject(hdcMem, nullPen);
    
    Ellipse(hdcMem, center - radius, center - radius, center + radius, center + radius);
    
    // Draw black dot in center
    SelectObject(hdcMem, blackBrush);
    Ellipse(hdcMem, center - dotRadius, center - dotRadius, center + dotRadius, center + dotRadius);
    
    // Cleanup drawing
    SelectObject(hdcMem, oldBrush);
    SelectObject(hdcMem, oldPen);
    DeleteObject(whiteBrush);
    DeleteObject(blackBrush);
    DeleteObject(nullPen);
    
    // Create mask (all white for full opacity)
    HDC hdcMask = CreateCompatibleDC(hdcScreen);
    HBITMAP hbmMaskOld = (HBITMAP)SelectObject(hdcMask, hbmMask);
    RECT rc = {0, 0, size, size};
    FillRect(hdcMask, &rc, (HBRUSH)GetStockObject(WHITE_BRUSH));
    // Cut out circle
    SelectObject(hdcMask, GetStockObject(BLACK_BRUSH));
    SelectObject(hdcMask, GetStockObject(NULL_PEN));
    Ellipse(hdcMask, center - radius, center - radius, center + radius, center + radius);
    
    SelectObject(hdcMask, hbmMaskOld);
    DeleteDC(hdcMask);
    
    SelectObject(hdcMem, hbmOld);
    DeleteDC(hdcMem);
    ReleaseDC(NULL, hdcScreen);
    
    // Create icon
    ICONINFO ii;
    ii.fIcon = TRUE;
    ii.xHotspot = center;
    ii.yHotspot = center;
    ii.hbmMask = hbmMask;
    ii.hbmColor = hbmColor;
    
    HICON hIcon = CreateIconIndirect(&ii);
    
    // Cleanup bitmaps
    DeleteObject(hbmColor);
    DeleteObject(hbmMask);
    
    if (!hIcon) return JNI_FALSE;
    
    // Set icon
    SendMessage(hwnd, WM_SETICON, ICON_BIG, (LPARAM)hIcon);
    SendMessage(hwnd, WM_SETICON, ICON_SMALL, (LPARAM)hIcon);
    
    // Note: We don't destroy hIcon here - Windows keeps it for the window
    return JNI_TRUE;
}

} // extern "C"
