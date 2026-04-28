#include <jni.h>
#include <windows.h>
#include <dwmapi.h>
#include <stdio.h>
#include <jawt.h>
#include <jawt_md.h>

#pragma comment(lib, "user32.lib")
#pragma comment(lib, "dwmapi.lib")
#pragma comment(lib, "advapi32.lib")

// DWM constants for Windows 11 titlebar styling
#ifndef DWMWA_CAPTION_COLOR
#define DWMWA_CAPTION_COLOR 35
#endif
#ifndef DWMWA_TEXT_COLOR
#define DWMWA_TEXT_COLOR 36
#endif
#ifndef DWMWA_USE_IMMERSIVE_DARK_MODE
#define DWMWA_USE_IMMERSIVE_DARK_MODE 20
#endif

// ============================================================================
// HELPER FUNCTIONS
// ============================================================================

/**
 * @brief Extracts native Windows HWND from Java AWT Component
 */
HWND GetHwndFromComponent(JNIEnv* env, jobject component) {
    JAWT awt;
    awt.version = JAWT_VERSION_9;
    if (!JAWT_GetAWT(env, &awt)) return NULL;

    JAWT_DrawingSurface* ds = awt.GetDrawingSurface(env, component);
    if (ds == NULL) return NULL;

    jint lock = ds->Lock(ds);
    if ((lock & JAWT_LOCK_ERROR) != 0) {
        awt.FreeDrawingSurface(ds);
        return NULL;
    }

    JAWT_DrawingSurfaceInfo* dsi = ds->GetDrawingSurfaceInfo(ds);
    if (dsi == NULL) {
        ds->Unlock(ds);
        awt.FreeDrawingSurface(ds);
        return NULL;
    }

    JAWT_Win32DrawingSurfaceInfo* win32Info = (JAWT_Win32DrawingSurfaceInfo*)dsi->platformInfo;
    HWND hwnd = (win32Info != NULL) ? win32Info->hwnd : NULL;

    ds->FreeDrawingSurfaceInfo(dsi);
    ds->Unlock(ds);
    awt.FreeDrawingSurface(ds);

    // Walk up to find the actual frame window
    if (hwnd != NULL) {
        HWND parent = GetParent(hwnd);
        while (parent != NULL) {
            char className[256];
            GetClassNameA(parent, className, sizeof(className));
            if (strstr(className, "SunAwtFrame") != NULL) return parent;
            parent = GetParent(parent);
        }
    }
    return hwnd;
}

/**
 * @brief Checks if Windows is currently using dark mode for apps
 */
static bool IsDarkModeEnabled() {
    HKEY hKey;
    DWORD value = 1; // Default to Light
    DWORD dataSize = sizeof(value);
    if (RegOpenKeyExA(HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize", 0, KEY_READ, &hKey) == ERROR_SUCCESS) {
        RegQueryValueExA(hKey, "AppsUseLightTheme", NULL, NULL, (LPBYTE)&value, &dataSize);
        RegCloseKey(hKey);
    }
    return (value == 0);
}

// ============================================================================
// JNI EXPORTS
// ============================================================================

extern "C" {

JNIEXPORT jlong JNICALL Java_fasttheme_FastTheme_getWindowHandle(JNIEnv* env, jclass clazz, jobject component) {
    return (jlong)GetHwndFromComponent(env, component);
}

JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setWindowTransparency(JNIEnv* env, jclass clazz, jlong hwndLong, jint alpha) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;
    if (alpha < 0) alpha = 0; if (alpha > 255) alpha = 255;
    LONG exStyle = GetWindowLong(hwnd, GWL_EXSTYLE);
    if (!(exStyle & WS_EX_LAYERED)) SetWindowLong(hwnd, GWL_EXSTYLE, exStyle | WS_EX_LAYERED);
    return SetLayeredWindowAttributes(hwnd, 0, (BYTE)alpha, LWA_ALPHA) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setTitleBarColor(JNIEnv* env, jclass clazz, jlong hwndLong, jint r, jint g, jint b) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;
    COLORREF color = RGB(r, g, b);
    HRESULT hr = DwmSetWindowAttribute(hwnd, DWMWA_CAPTION_COLOR, &color, sizeof(color));
    SetWindowPos(hwnd, NULL, 0, 0, 0, 0, SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_FRAMECHANGED);
    return SUCCEEDED(hr) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setTitleBarTextColor(JNIEnv* env, jclass clazz, jlong hwndLong, jint r, jint g, jint b) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;
    COLORREF color = RGB(r, g, b);
    HRESULT hr = DwmSetWindowAttribute(hwnd, DWMWA_TEXT_COLOR, &color, sizeof(color));
    SetWindowPos(hwnd, NULL, 0, 0, 0, 0, SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_FRAMECHANGED);
    return SUCCEEDED(hr) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setTitleBarDarkMode(JNIEnv* env, jclass clazz, jlong hwndLong, jboolean enabled) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;
    BOOL darkMode = enabled ? TRUE : FALSE;
    HRESULT hr = DwmSetWindowAttribute(hwnd, DWMWA_USE_IMMERSIVE_DARK_MODE, &darkMode, sizeof(darkMode));
    SetWindowPos(hwnd, NULL, 0, 0, 0, 0, SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_FRAMECHANGED);
    return SUCCEEDED(hr) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_isSystemDarkMode(JNIEnv* env, jclass clazz) {
    return IsDarkModeEnabled() ? JNI_TRUE : JNI_FALSE;
}

} // extern "C"
