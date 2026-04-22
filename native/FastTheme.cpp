#include "FastTheme.h"
#include <windows.h>
#include <dwmapi.h>
#include <stdio.h>

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
#ifndef DWMWA_BORDER_COLOR
#define DWMWA_BORDER_COLOR 34
#endif
#ifndef DWMWA_USE_IMMERSIVE_DARK_MODE
#define DWMWA_USE_IMMERSIVE_DARK_MODE 20
#endif

// ============================================================================
// HELPER FUNCTIONS
// ============================================================================

typedef jboolean (JNICALL *GetAWT_t)(JavaVM*, void**);

JAWT* LoadJAWT(JNIEnv* env) {
    static JAWT awt;
    static jboolean loaded = JNI_FALSE;
    if (loaded) return &awt;
    
    JavaVM* vm = NULL;
    if (env->GetJavaVM(&vm) != 0) return NULL;
    
    HMODULE hJawt = LoadLibraryA("jawt.dll");
    if (!hJawt) return NULL;
    
    GetAWT_t getAWT = (GetAWT_t)GetProcAddress(hJawt, "JAWT_GetAWT");
    if (!getAWT) return NULL;
    
    awt.version = JAWT_VERSION_9;
    if (!getAWT(vm, (void**)&awt)) return NULL;
    
    loaded = JNI_TRUE;
    return &awt;
}

HWND GetHwndFromComponent(JNIEnv* env, jobject component) {
    JAWT* awt = LoadJAWT(env);
    if (!awt) return NULL;
    
    JAWT_DrawingSurface* ds = awt->GetDrawingSurface(env, component);
    if (!ds) return NULL;
    
    jint lock = ds->Lock(ds);
    if ((lock & JAWT_LOCK_ERROR) != 0) {
        awt->FreeDrawingSurface(ds);
        return NULL;
    }
    
    JAWT_DrawingSurfaceInfo* dsi = ds->GetDrawingSurfaceInfo(ds);
    ds->Unlock(ds);
    
    if (!dsi) {
        awt->FreeDrawingSurface(ds);
        return NULL;
    }
    
    JAWT_Win32DrawingSurfaceInfo* win32Info = (JAWT_Win32DrawingSurfaceInfo*)dsi->platformInfo;
    HWND hwnd = win32Info ? win32Info->hwnd : NULL;
    
    ds->FreeDrawingSurfaceInfo(dsi);
    awt->FreeDrawingSurface(ds);
    
    return hwnd;
}

HWND GetHwndFromWindow(JNIEnv* env, jobject window) {
    HWND hwnd = GetHwndFromComponent(env, window);
    if (hwnd) {
        HWND parent = GetParent(hwnd);
        while (parent) {
            char className[256];
            GetClassNameA(parent, className, sizeof(className));
            if (strstr(className, "SunAwtFrame")) return parent;
            parent = GetParent(parent);
        }
    }
    return hwnd;
}

static bool IsDarkModeEnabled() {
    HKEY hKey;
    DWORD value = 0;
    DWORD dataSize = sizeof(value);
    
    if (RegOpenKeyExA(HKEY_CURRENT_USER,
        "Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
        0, KEY_READ, &hKey) == ERROR_SUCCESS) {
        if (RegQueryValueExA(hKey, "AppsUseLightTheme", NULL, NULL,
            (LPBYTE)&value, &dataSize) == ERROR_SUCCESS) {
            RegCloseKey(hKey);
            return (value == 0);
        }
        RegCloseKey(hKey);
    }
    return false;
}

// ============================================================================
// JNI EXPORTS - WINDOW STYLING
// ============================================================================

JNIEXPORT jlong JNICALL Java_fasttheme_FastTheme_getWindowHandle(JNIEnv* env, jclass, jobject component) {
    return (jlong)GetHwndFromWindow(env, component);
}

JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setWindowTransparency(JNIEnv*, jclass, jlong hwnd, jint alpha) {
    if (!hwnd) return JNI_FALSE;
    return SetLayeredWindowAttributes((HWND)hwnd, 0, (BYTE)alpha, LWA_ALPHA) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setTitleBarColor(JNIEnv*, jclass, jlong hwnd, jint r, jint g, jint b) {
    if (!hwnd) return JNI_FALSE;
    COLORREF color = RGB(r, g, b);
    return SUCCEEDED(DwmSetWindowAttribute((HWND)hwnd, DWMWA_CAPTION_COLOR, &color, sizeof(COLORREF))) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setTitleBarTextColor(JNIEnv*, jclass, jlong hwnd, jint r, jint g, jint b) {
    if (!hwnd) return JNI_FALSE;
    COLORREF color = RGB(r, g, b);
    return SUCCEEDED(DwmSetWindowAttribute((HWND)hwnd, DWMWA_TEXT_COLOR, &color, sizeof(COLORREF))) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setTitleBarDarkMode(JNIEnv*, jclass, jlong hwnd, jboolean enabled) {
    if (!hwnd) return JNI_FALSE;
    BOOL darkMode = enabled ? TRUE : FALSE;
    return SUCCEEDED(DwmSetWindowAttribute((HWND)hwnd, DWMWA_USE_IMMERSIVE_DARK_MODE, &darkMode, sizeof(BOOL))) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_isSystemDarkMode(JNIEnv*, jclass) {
    return IsDarkModeEnabled() ? JNI_TRUE : JNI_FALSE;
}
