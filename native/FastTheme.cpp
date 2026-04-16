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

/**
 * @brief Dynamically loads JAWT (Java AWT Native Interface)
 * 
 * Loads jawt.dll and JAWT_GetAWT function at runtime. Using dynamic
 * loading avoids requiring jawt.lib at link time, making the DLL
 * work with any JRE without build-time dependencies.
 * 
 * @param env JNI environment pointer
 * @return Pointer to initialized JAWT structure, or NULL on failure
 * 
 * @note Caches result - subsequent calls return cached JAWT*
 * @warning Must be called before any GetHwndFromComponent calls
 */
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

/**
 * @brief Extracts native Windows HWND from Java AWT Component
 * 
 * Uses JAWT to lock the drawing surface and extract the platform-specific
 * window handle. Walks up parent chain to find the actual frame window.
 * 
 * @param env JNI environment pointer
 * @param component Java AWT Component (e.g., JFrame)
 * @return Native HWND handle, or NULL if extraction failed
 * 
 * @note Component must be displayable (addNotify() called)
 * @note Caller must NOT free or destroy the returned HWND
 * @see LoadJAWT, GetHwndFromWindow
 */
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

/**
 * @brief Finds the top-level frame window HWND from any component
 * 
 * Walks up the window parent chain looking for "SunAwtFrame" class,
 * which is the native window class for Java Swing JFrame windows.
 * 
 * @param env JNI environment pointer
 * @param window Java Window (typically JFrame)
 * @return HWND of the actual frame window, or component's HWND if no frame found
 * 
 * @note This finds the root window suitable for DWM API calls
 * @see GetHwndFromComponent
 */
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

/**
 * @brief Checks if Windows is currently using dark mode for apps
 * 
 * Reads the registry value AppsUseLightTheme from the Personalize key.
 * Value 0 = Dark mode enabled, 1 = Light mode enabled.
 * 
 * @return true if dark mode is active, false for light mode or on error
 * 
 * @note Registry path: HKCU\Software\Microsoft\Windows\CurrentVersion\Themes\Personalize
 * @note Falls back to false (light mode) if registry access fails
 */
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

/**
 * @brief Starts monitoring system display and theme changes
 * 
 * Creates a hidden message-only window and background thread that listens for:
 * - WM_DISPLAYCHANGE (resolution changes)
 * - WM_DPICHANGED (DPI scaling changes)  
 * - WM_SETTINGCHANGE (theme changes, including dark mode toggle)
 * 
 * Calls back into Java via notifyResolutionChanged, notifyThemeChanged,
 * and notifyInitialState methods on the FastTheme instance.
 * 
 * @param env JNI environment pointer
 * @param obj FastTheme instance (this) - stored as global reference
 * @return JNI_TRUE if monitoring started successfully, JNI_FALSE on error
 * 
 * @note Safe to call multiple times - returns JNI_TRUE if already running
 * @see Java_fasttheme_FastTheme_stopMonitoring
 */
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

/**
 * @brief Stops monitoring and cleans up resources
 * 
 * Sends WM_CLOSE to the monitor window, which causes the background thread
 * to exit. Releases the global reference to the Java FastTheme object.
 * 
 * @param env JNI environment pointer
 * @param obj FastTheme instance (this) - ignored, uses stored global ref
 * 
 * @note Safe to call multiple times - no-op if already stopped
 * @note Thread will exit gracefully within ~100ms
 * @see Java_fasttheme_FastTheme_startMonitoring
 */
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
// JNI EXPORTS - WINDOW STYLING API (Generic, usable by any demo/app)
// ============================================================================

/**
 * @brief Gets the native Windows HWND handle from a Java AWT Component
 * 
 * Uses JAWT (Java AWT Native Interface) to extract the native window handle
 * from a Swing/AWT component. Required for all subsequent window styling calls.
 * 
 * @param env JNI environment pointer
 * @param obj JNI object reference (this, ignored for static)
 * @param component The Java AWT/Swing Component (usually JFrame)
 * @return jlong containing HWND handle, or 0 if extraction failed
 * 
 * @note The caller must ensure the window is visible before calling this method
 * @see setWindowTransparency, setTitleBarColor, setTitleBarDarkMode
 */
JNIEXPORT jlong JNICALL Java_fasttheme_FastTheme_getWindowHandle(JNIEnv* env, jobject obj, jobject component) {
    printf("[DEBUG C++] getWindowHandle called\n");
    jlong result = (jlong)GetHwndFromWindow(env, component);
    printf("[DEBUG C++] Window handle: %lld\n", result);
    return result;
}

/**
 * @brief Sets window transparency/opacity using layered window attributes
 * 
 * Makes the entire window (including titlebar) semi-transparent.
 * Automatically adds WS_EX_LAYERED style if not present.
 * 
 * @param env JNI environment pointer
 * @param obj JNI object reference (this, ignored for static)
 * @param hwndLong Native window handle (HWND) from getWindowHandle()
 * @param alpha Transparency level (0-255, where 255 = fully opaque, 0 = invisible)
 * @return JNI_TRUE on success, JNI_FALSE if window handle invalid
 * 
 * @warning Values outside 0-255 will be clamped to valid range
 */
JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setWindowTransparency(JNIEnv* env, jobject obj, jlong hwndLong, jint alpha) {
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

/**
 * @brief Sets the title bar (caption) background color
 * 
 * Uses Windows 11 DWM API (DWMWA_CAPTION_COLOR) to customize the title bar.
 * Falls back gracefully on older Windows versions.
 * 
 * @param env JNI environment pointer
 * @param obj JNI object reference (this, ignored for static)
 * @param hwndLong Native window handle (HWND) from getWindowHandle()
 * @param r Red component (0-255)
 * @param g Green component (0-255)
 * @param b Blue component (0-255)
 * @return JNI_TRUE on success, JNI_FALSE if window invalid or API unavailable
 * 
 * @note Forces window frame redraw to apply changes immediately
 */
JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setTitleBarColor(JNIEnv* env, jobject obj, jlong hwndLong, jint r, jint g, jint b) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;
    
    COLORREF color = RGB(r, g, b);
    HRESULT hr = DwmSetWindowAttribute(hwnd, DWMWA_CAPTION_COLOR, &color, sizeof(color));
    SetWindowPos(hwnd, NULL, 0, 0, 0, 0, SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_FRAMECHANGED);
    return SUCCEEDED(hr) ? JNI_TRUE : JNI_FALSE;
}

/**
 * @brief Sets the title bar text color
 * 
 * Uses Windows 11 DWM API (DWMWA_TEXT_COLOR) for caption text color.
 * Requires Windows 11 Build 22000 or later for full support.
 * 
 * @param env JNI environment pointer
 * @param obj JNI object reference (this, ignored for static)
 * @param hwndLong Native window handle (HWND) from getWindowHandle()
 * @param r Red component (0-255)
 * @param g Green component (0-255)
 * @param b Blue component (0-255)
 * @return JNI_TRUE on success, JNI_FALSE if window invalid or API unavailable
 */
JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setTitleBarTextColor(JNIEnv* env, jobject obj, jlong hwndLong, jint r, jint g, jint b) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;
    
    COLORREF color = RGB(r, g, b);
    HRESULT hr = DwmSetWindowAttribute(hwnd, DWMWA_TEXT_COLOR, &color, sizeof(color));
    SetWindowPos(hwnd, NULL, 0, 0, 0, 0, SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_FRAMECHANGED);
    return SUCCEEDED(hr) ? JNI_TRUE : JNI_FALSE;
}

/**
 * @brief Enables/disables dark mode for the window title bar
 * 
 * Uses Windows 10/11 DWM API (DWMWA_USE_IMMERSIVE_DARK_MODE).
 * This affects the title bar, borders, and system menu appearance.
 * 
 * @param env JNI environment pointer
 * @param obj JNI object reference (this, ignored for static)
 * @param hwndLong Native window handle (HWND) from getWindowHandle()
 * @param enabled JNI_TRUE for dark mode, JNI_FALSE for light mode
 * @return JNI_TRUE on success, JNI_FALSE if window invalid
 * 
 * @note This is the system dark mode, not the app content theme
 */
JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setTitleBarDarkMode(JNIEnv* env, jobject obj, jlong hwndLong, jboolean enabled) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;
    
    BOOL darkMode = enabled ? TRUE : FALSE;
    HRESULT hr = DwmSetWindowAttribute(hwnd, DWMWA_USE_IMMERSIVE_DARK_MODE, &darkMode, sizeof(darkMode));
    SetWindowPos(hwnd, NULL, 0, 0, 0, 0, SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_FRAMECHANGED);
    return SUCCEEDED(hr) ? JNI_TRUE : JNI_FALSE;
}

/**
 * @brief Gets the primary display resolution
 * 
 * Returns the current screen resolution as "WIDTHxHEIGHT" string.
 * Uses GetSystemMetrics which respects DPI scaling.
 * 
 * @param env JNI environment pointer
 * @param obj JNI object reference (this, ignored for static)
 * @return jstring in format "1920x1080", or "Unknown" on error
 */
JNIEXPORT jstring JNICALL Java_fasttheme_FastTheme_getSystemResolution(JNIEnv* env, jobject obj) {
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

/**
 * @brief Gets the system DPI scaling value
 * 
 * Returns the DPI of the primary monitor. Standard is 96 DPI (100% scaling).
 * 144 DPI = 150% scaling, 192 DPI = 200% scaling, etc.
 * 
 * @param env JNI environment pointer
 * @param obj JNI object reference (this, ignored for static)
 * @return jint DPI value, or 96 as fallback
 */
JNIEXPORT jint JNICALL Java_fasttheme_FastTheme_getSystemDPI(JNIEnv* env, jobject obj) {
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

/**
 * @brief Detects if Windows is in dark mode
 * 
 * Reads the registry key AppsUseLightTheme from Personalize settings.
 * This reflects the user's system-wide app theme preference.
 * 
 * @param env JNI environment pointer
 * @param obj JNI object reference (this, ignored for static)
 * @return JNI_TRUE if dark mode is enabled, JNI_FALSE for light mode
 * 
 * @note Checks HKEY_CURRENT_USER\Software\Microsoft\Windows\CurrentVersion\Themes\Personalize
 */
JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_isSystemDarkMode(JNIEnv* env, jobject obj) {
    printf("[DEBUG C++] isSystemDarkMode called\n");
    fflush(stdout);
    jboolean result = IsDarkModeEnabled() ? JNI_TRUE : JNI_FALSE;
    printf("[DEBUG C++] Dark mode: %s\n", result ? "true" : "false");
    fflush(stdout);
    return result;
}

/**
 * @brief Gets the primary display refresh rate
 * 
 * Queries the current display settings for refresh rate in Hz.
 * Common values: 60, 75, 120, 144, 165, 240 Hz.
 * 
 * @param env JNI environment pointer
 * @param obj JNI object reference (this, ignored for static)
 * @return jint refresh rate in Hz, or 60 as fallback
 */
JNIEXPORT jint JNICALL Java_fasttheme_FastTheme_getSystemRefreshRate(JNIEnv* env, jobject obj) {
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
