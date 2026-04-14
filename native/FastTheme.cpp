#include "FastTheme.h"
#include <windows.h>
#include <dwmapi.h>
#include <shellscalingapi.h>
#include <stdio.h>

#pragma comment(lib, "user32.lib")
#pragma comment(lib, "gdi32.lib")
#pragma comment(lib, "dwmapi.lib")
#pragma comment(lib, "shcore.lib")
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

// Dynamically load JAWT functions (to avoid linking against jawt.lib)
typedef jboolean (JNICALL *GetAWT_t)(JavaVM*, void**);

JAWT* LoadJAWT(JNIEnv* env) {
    static JAWT awt;
    static jboolean loaded = JNI_FALSE;
    
    if (loaded) return &awt;
    
    // Get JavaVM
    JavaVM* vm = NULL;
    if (env->GetJavaVM(&vm) != 0) {
        printf("[DEBUG C++] Failed to get JavaVM\n");
        return NULL;
    }
    
    // Load jawt.dll
    HMODULE hJawt = LoadLibraryA("jawt.dll");
    if (!hJawt) {
        printf("[DEBUG C++] Failed to load jawt.dll\n");
        return NULL;
    }
    
    // Get JAWT_GetAWT function
    GetAWT_t getAWT = (GetAWT_t)GetProcAddress(hJawt, "JAWT_GetAWT");
    if (!getAWT) {
        printf("[DEBUG C++] Failed to get JAWT_GetAWT\n");
        return NULL;
    }
    
    // Get JAWT
    awt.version = JAWT_VERSION_9;
    if (!getAWT(vm, (void**)&awt)) {
        printf("[DEBUG C++] JAWT_GetAWT failed\n");
        return NULL;
    }
    
    printf("[DEBUG C++] JAWT loaded successfully\n");
    loaded = JNI_TRUE;
    return &awt;
}

// Helper function to get HWND from AWT Component
HWND GetHwndFromComponent(JNIEnv* env, jobject component) {
    JAWT* awt = LoadJAWT(env);
    if (!awt) return NULL;
    
    JAWT_DrawingSurface* ds = NULL;
    JAWT_DrawingSurfaceInfo* dsi = NULL;
    HWND hwnd = NULL;
    
    if (awt->GetDrawingSurface == NULL) {
        printf("[DEBUG C++] GetDrawingSurface is NULL\n");
        return NULL;
    }
    
    ds = awt->GetDrawingSurface(env, component);
    if (ds == NULL) {
        printf("[DEBUG C++] GetDrawingSurface returned NULL\n");
        return NULL;
    }
    
    if (ds->GetDrawingSurfaceInfo == NULL) {
        printf("[DEBUG C++] GetDrawingSurfaceInfo is NULL\n");
        awt->FreeDrawingSurface(ds);
        return NULL;
    }
    
    jint lock = ds->Lock(ds);
    if ((lock & JAWT_LOCK_ERROR) != 0) {
        printf("[DEBUG C++] Lock failed\n");
        awt->FreeDrawingSurface(ds);
        return NULL;
    }
    
    dsi = ds->GetDrawingSurfaceInfo(ds);
    ds->Unlock(ds);
    
    if (dsi == NULL) {
        printf("[DEBUG C++] GetDrawingSurfaceInfo returned NULL\n");
        awt->FreeDrawingSurface(ds);
        return NULL;
    }
    
    JAWT_Win32DrawingSurfaceInfo* win32Info = (JAWT_Win32DrawingSurfaceInfo*)dsi->platformInfo;
    if (win32Info != NULL) {
        hwnd = win32Info->hwnd;
    }
    
    ds->FreeDrawingSurfaceInfo(dsi);
    awt->FreeDrawingSurface(ds);
    
    return hwnd;
}

// Get HWND from Window - walks up to find the real frame window
HWND GetHwndFromWindow(JNIEnv* env, jobject window) {
    HWND hwnd = GetHwndFromComponent(env, window);
    if (hwnd != NULL) {
        HWND parent = GetParent(hwnd);
        while (parent != NULL) {
            char className[256];
            GetClassNameA(parent, className, sizeof(className));
            if (strstr(className, "SunAwtFrame") != NULL) {
                return parent;
            }
            parent = GetParent(parent);
        }
    }
    return hwnd;
}

// ============================================================================
// FASTTHEME DISPLAY MONITORING API
// ============================================================================

static JavaVM* g_jvm = nullptr;
static jobject g_themeObj = nullptr;
static jmethodID g_notifyMethodId = nullptr;
static jmethodID g_notifyThemeMethodId = nullptr;
static jmethodID g_notifyInitialStateMethodId = nullptr;
static HWND g_hwnd = nullptr;
static DWORD g_threadId = 0;

static int lastWidth = 0;
static int lastHeight = 0;
static int lastDpi = 96;
static bool lastTheme = false;

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

static void sendInitialState() {
    if (g_jvm == nullptr || g_themeObj == nullptr) return;
    
    JNIEnv* env;
    jint attachResult = g_jvm->GetEnv((void**)&env, JNI_VERSION_1_6);
    bool didAttach = false;
    
    if (attachResult == JNI_EDETACHED) {
        if (g_jvm->AttachCurrentThread((void**)&env, nullptr) != 0) {
            return;
        }
        didAttach = true;
    } else if (attachResult != JNI_OK) {
        return;
    }
    
    // Get current display settings
    DEVMODE dm = {};
    dm.dmSize = sizeof(dm);
    int width = GetSystemMetrics(SM_CXSCREEN);
    int height = GetSystemMetrics(SM_CYSCREEN);
    int refreshRate = 60;
    int orientation = 0;
    int dpi = 96;
    
    if (EnumDisplaySettings(NULL, ENUM_CURRENT_SETTINGS, &dm)) {
        if (dm.dmFields & DM_PELSWIDTH) width = dm.dmPelsWidth;
        if (dm.dmFields & DM_PELSHEIGHT) height = dm.dmPelsHeight;
        if (dm.dmFields & DM_DISPLAYFREQUENCY) refreshRate = dm.dmDisplayFrequency;
        if (dm.dmFields & DM_DISPLAYORIENTATION) {
            switch (dm.dmDisplayOrientation) {
                case DMDO_DEFAULT: orientation = 0; break;
                case DMDO_90: orientation = 1; break;
                case DMDO_180: orientation = 2; break;
                case DMDO_270: orientation = 3; break;
            }
        }
    }
    
    // Get DPI
    HMONITOR hMonitor = MonitorFromWindow(GetDesktopWindow(), MONITOR_DEFAULTTOPRIMARY);
    if (hMonitor) {
        UINT dpiX = 96, dpiY = 96;
        if (SUCCEEDED(GetDpiForMonitor(hMonitor, MDT_EFFECTIVE_DPI, &dpiX, &dpiY))) {
            dpi = (int)dpiX;
        }
    }
    
    bool isDark = IsDarkModeEnabled();
    
    env->CallVoidMethod(g_themeObj, g_notifyInitialStateMethodId, 
        width, height, dpi, orientation, refreshRate, isDark ? JNI_TRUE : JNI_FALSE);

    if (didAttach) {
        g_jvm->DetachCurrentThread();
    }
}

static LRESULT CALLBACK MonitorWindowProc(HWND hwnd, UINT uMsg, WPARAM wParam, LPARAM lParam) {
    switch (uMsg) {
        case WM_CREATE:
            return 0;
            
        case WM_DISPLAYCHANGE:
        case WM_DPICHANGED:
            if (g_jvm && g_themeObj && g_notifyMethodId) {
                JNIEnv* env;
                jint attachResult = g_jvm->GetEnv((void**)&env, JNI_VERSION_1_6);
                bool didAttach = false;
                
                if (attachResult == JNI_EDETACHED) {
                    if (g_jvm->AttachCurrentThread((void**)&env, nullptr) == 0) {
                        didAttach = true;
                    } else {
                        break;
                    }
                } else if (attachResult != JNI_OK) {
                    break;
                }
                
                int width = GetSystemMetrics(SM_CXSCREEN);
                int height = GetSystemMetrics(SM_CYSCREEN);
                
                DEVMODE dm = {};
                dm.dmSize = sizeof(dm);
                int refreshRate = 60;
                int orientation = 0;
                if (EnumDisplaySettings(NULL, ENUM_CURRENT_SETTINGS, &dm)) {
                    if (dm.dmFields & DM_PELSWIDTH) width = dm.dmPelsWidth;
                    if (dm.dmFields & DM_PELSHEIGHT) height = dm.dmPelsHeight;
                    if (dm.dmFields & DM_DISPLAYFREQUENCY) refreshRate = dm.dmDisplayFrequency;
                    if (dm.dmFields & DM_DISPLAYORIENTATION) {
                        switch (dm.dmDisplayOrientation) {
                            case DMDO_DEFAULT: orientation = 0; break;
                            case DMDO_90: orientation = 1; break;
                            case DMDO_180: orientation = 2; break;
                            case DMDO_270: orientation = 3; break;
                        }
                    }
                }
                
                int dpi = 96;
                HMONITOR hMonitor = MonitorFromWindow(GetDesktopWindow(), MONITOR_DEFAULTTOPRIMARY);
                if (hMonitor) {
                    UINT dpiX = 96, dpiY = 96;
                    if (SUCCEEDED(GetDpiForMonitor(hMonitor, MDT_EFFECTIVE_DPI, &dpiX, &dpiY))) {
                        dpi = (int)dpiX;
                    }
                }
                
                env->CallVoidMethod(g_themeObj, g_notifyMethodId, width, height, dpi, orientation, refreshRate);
                
                if (didAttach) {
                    g_jvm->DetachCurrentThread();
                }
            }
            return 0;
            
        case WM_SETTINGCHANGE:
            if (g_jvm && g_themeObj && g_notifyThemeMethodId) {
                bool currentTheme = IsDarkModeEnabled();
                if (currentTheme != lastTheme) {
                    lastTheme = currentTheme;
                    
                    JNIEnv* env;
                    jint attachResult = g_jvm->GetEnv((void**)&env, JNI_VERSION_1_6);
                    bool didAttach = false;
                    
                    if (attachResult == JNI_EDETACHED) {
                        if (g_jvm->AttachCurrentThread((void**)&env, nullptr) == 0) {
                            didAttach = true;
                        } else {
                            break;
                        }
                    } else if (attachResult != JNI_OK) {
                        break;
                    }
                    
                    env->CallVoidMethod(g_themeObj, g_notifyThemeMethodId, currentTheme ? JNI_TRUE : JNI_FALSE);
                    
                    if (didAttach) {
                        g_jvm->DetachCurrentThread();
                    }
                }
            }
            return 0;
            
        case WM_CLOSE:
            DestroyWindow(hwnd);
            return 0;
            
        case WM_DESTROY:
            PostQuitMessage(0);
            return 0;
    }
    return DefWindowProcA(hwnd, uMsg, wParam, lParam);
}

static DWORD WINAPI MonitorThread(LPVOID lpParam) {
    SetThreadDpiAwarenessContext(DPI_AWARENESS_CONTEXT_PER_MONITOR_AWARE_V2);
    
    WNDCLASSA wc = {};
    wc.lpfnWndProc = MonitorWindowProc;
    wc.hInstance = GetModuleHandle(nullptr);
    wc.lpszClassName = "FastThemeMonitor";
    
    if (!RegisterClassA(&wc)) {
        return 1;
    }
    
    g_hwnd = CreateWindowExA(
        0,
        "FastThemeMonitor",
        "FastTheme Monitor",
        0, 0, 0, 0, 0,
        HWND_MESSAGE, nullptr, GetModuleHandle(nullptr), nullptr
    );
    
    if (!g_hwnd) {
        return 1;
    }
    
    MSG msg;
    while (GetMessage(&msg, nullptr, 0, 0)) {
        TranslateMessage(&msg);
        DispatchMessage(&msg);
    }
    
    return 0;
}

// ============================================================================
// JNI EXPORTS - FASTTHEME MONITORING
// ============================================================================

extern "C" {

JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_startMonitoring(JNIEnv* env, jobject obj) {
    if (g_hwnd != nullptr) {
        return JNI_TRUE;
    }

    env->GetJavaVM(&g_jvm);
    g_themeObj = env->NewGlobalRef(obj);
    jclass cls = env->GetObjectClass(obj);
    g_notifyMethodId = env->GetMethodID(cls, "notifyResolutionChanged", "(IIIII)V");
    g_notifyThemeMethodId = env->GetMethodID(cls, "notifyThemeChanged", "(Z)V");
    g_notifyInitialStateMethodId = env->GetMethodID(cls, "notifyInitialState", "(IIIIIZ)V");

    if (!g_notifyMethodId || !g_notifyThemeMethodId || !g_notifyInitialStateMethodId) {
         return JNI_FALSE;
    }

    HANDLE hThread = CreateThread(nullptr, 0, MonitorThread, nullptr, 0, &g_threadId);
    if (hThread) {
        CloseHandle(hThread);
        int attempts = 0;
        while (g_hwnd == nullptr && attempts < 50) {
            Sleep(10);
            attempts++;
        }
        if (g_hwnd != nullptr) {
            sendInitialState();
        }
        return JNI_TRUE;
    }

    return JNI_FALSE;
}

JNIEXPORT void JNICALL Java_fasttheme_FastTheme_stopMonitoring(JNIEnv* env, jobject obj) {
    if (g_hwnd) {
        PostMessageA(g_hwnd, WM_CLOSE, 0, 0);
        g_hwnd = nullptr;
    }

    if (g_themeObj) {
        env->DeleteGlobalRef(g_themeObj);
        g_themeObj = nullptr;
    }
    g_jvm = nullptr;
}

// ============================================================================
// JNI EXPORTS - FASTTHEME TERMINAL (WINDOW STYLING)
// ============================================================================

JNIEXPORT jlong JNICALL Java_fasttheme_FastThemeTerminal_getWindowHandle(JNIEnv* env, jobject obj, jobject component) {
    printf("[DEBUG C++] getWindowHandle called\n");
    jlong result = (jlong)GetHwndFromWindow(env, component);
    printf("[DEBUG C++] Window handle: %lld\n", result);
    return result;
}

JNIEXPORT jboolean JNICALL Java_fasttheme_FastThemeTerminal_setWindowTransparency(JNIEnv* env, jobject obj, jlong hwndLong, jint alpha) {
    printf("[DEBUG C++] setWindowTransparency called, alpha=%d\n", alpha);
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;

    if (alpha < 0) alpha = 0;
    if (alpha > 255) alpha = 255;

    LONG exStyle = GetWindowLong(hwnd, GWL_EXSTYLE);
    if (!(exStyle & WS_EX_LAYERED)) {
        SetWindowLong(hwnd, GWL_EXSTYLE, exStyle | WS_EX_LAYERED);
    }
    BOOL result = SetLayeredWindowAttributes(hwnd, 0, (BYTE)alpha, LWA_ALPHA);
    RedrawWindow(hwnd, NULL, NULL, RDW_ERASE | RDW_INVALIDATE | RDW_FRAME | RDW_ALLCHILDREN);
    return result ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_fasttheme_FastThemeTerminal_setTitleBarColor(JNIEnv* env, jobject obj, jlong hwndLong, jint r, jint g, jint b) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;
    
    COLORREF color = RGB(r, g, b);
    HRESULT hr = DwmSetWindowAttribute(hwnd, DWMWA_CAPTION_COLOR, &color, sizeof(color));
    SetWindowPos(hwnd, NULL, 0, 0, 0, 0, SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_FRAMECHANGED);
    return SUCCEEDED(hr) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_fasttheme_FastThemeTerminal_setTitleBarTextColor(JNIEnv* env, jobject obj, jlong hwndLong, jint r, jint g, jint b) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;
    
    COLORREF color = RGB(r, g, b);
    HRESULT hr = DwmSetWindowAttribute(hwnd, DWMWA_TEXT_COLOR, &color, sizeof(color));
    SetWindowPos(hwnd, NULL, 0, 0, 0, 0, SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_FRAMECHANGED);
    return SUCCEEDED(hr) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_fasttheme_FastThemeTerminal_setTitleBarDarkMode(JNIEnv* env, jobject obj, jlong hwndLong, jboolean enabled) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;
    
    BOOL darkMode = enabled ? TRUE : FALSE;
    HRESULT hr = DwmSetWindowAttribute(hwnd, DWMWA_USE_IMMERSIVE_DARK_MODE, &darkMode, sizeof(darkMode));
    SetWindowPos(hwnd, NULL, 0, 0, 0, 0, SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_FRAMECHANGED);
    return SUCCEEDED(hr) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jstring JNICALL Java_fasttheme_FastThemeTerminal_getSystemResolution(JNIEnv* env, jobject obj) {
    printf("[DEBUG C++] getSystemResolution called\n");
    fflush(stdout);
    int width = GetSystemMetrics(SM_CXSCREEN);
    int height = GetSystemMetrics(SM_CYSCREEN);
    char buffer[64];
    sprintf_s(buffer, sizeof(buffer), "%dx%d", width, height);
    printf("[DEBUG C++] Resolution: %s\n", buffer);
    fflush(stdout);
    return env->NewStringUTF(buffer);
}

JNIEXPORT jint JNICALL Java_fasttheme_FastThemeTerminal_getSystemDPI(JNIEnv* env, jobject obj) {
    printf("[DEBUG C++] getSystemDPI called\n");
    fflush(stdout);
    HDC hdc = GetDC(NULL);
    if (hdc) {
        int dpi = GetDeviceCaps(hdc, LOGPIXELSX);
        ReleaseDC(NULL, hdc);
        printf("[DEBUG C++] DPI: %d\n", dpi);
        fflush(stdout);
        return dpi;
    }
    printf("[DEBUG C++] DPI fallback: 96\n");
    fflush(stdout);
    return 96;
}

JNIEXPORT jboolean JNICALL Java_fasttheme_FastThemeTerminal_isSystemDarkMode(JNIEnv* env, jobject obj) {
    printf("[DEBUG C++] isSystemDarkMode called\n");
    fflush(stdout);
    jboolean result = IsDarkModeEnabled() ? JNI_TRUE : JNI_FALSE;
    printf("[DEBUG C++] Dark mode: %s\n", result ? "true" : "false");
    fflush(stdout);
    return result;
}

JNIEXPORT jint JNICALL Java_fasttheme_FastThemeTerminal_getSystemRefreshRate(JNIEnv* env, jobject obj) {
    printf("[DEBUG C++] getSystemRefreshRate called\n");
    fflush(stdout);
    // Use EXACT same code as working Monitor Thread from Demo.java
    SetThreadDpiAwarenessContext(DPI_AWARENESS_CONTEXT_PER_MONITOR_AWARE_V2);
    
    DEVMODE dm = {};
    dm.dmSize = sizeof(dm);
    int refreshRate = 60;
    
    // Get current display settings - ENUM_CURRENT_SETTINGS = -1
    if (EnumDisplaySettings(NULL, ENUM_CURRENT_SETTINGS, &dm)) {
        if (dm.dmFields & DM_DISPLAYFREQUENCY) {
            int freq = dm.dmDisplayFrequency;
            if (freq > 0 && freq < 1000) {
                refreshRate = freq;
            }
        }
    }
    
    printf("[DEBUG C++] Refresh rate: %d\n", refreshRate);
    fflush(stdout);
    return refreshRate;
}

} // extern "C"
