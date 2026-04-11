#include <jni.h>
#include <windows.h>
#include <shellscalingapi.h>

#pragma comment(lib, "shcore.lib")
#pragma comment(lib, "user32.lib")

extern "C" {
    #include "fasttheme_FastTheme.h"
}

static JavaVM* g_jvm = nullptr;
static jobject g_themeObj = nullptr;
static jmethodID g_notifyMethodId = nullptr;
static jmethodID g_notifyThemeMethodId = nullptr;
static jmethodID g_notifyInitialStateMethodId = nullptr;
static HWND g_hwnd = nullptr;
static DWORD g_threadId = 0;

// Helper to check if Windows is in dark mode
static bool IsDarkModeEnabled();

static bool IsDarkModeEnabled() {
    HKEY hKey;
    DWORD value = 0;
    DWORD size = sizeof(value);
    
    LONG openResult = RegOpenKeyExA(HKEY_CURRENT_USER, 
        "Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize", 
        0, KEY_READ, &hKey);
    
    if (openResult == ERROR_SUCCESS) {
        LONG queryResult = RegQueryValueExA(hKey, "AppsUseLightTheme", nullptr, nullptr, 
            (LPBYTE)&value, &size);
        RegCloseKey(hKey);
        
        if (queryResult == ERROR_SUCCESS) {
            return value == 0; // 0 = dark mode, 1 = light mode
        }
    }
    return false; // Default to light if can't read
}

// Notify Java of theme change
void notifyThemeJava() {
    if (!g_jvm || !g_themeObj || !g_notifyThemeMethodId) return;

    JNIEnv* env;
    int getEnvStat = g_jvm->GetEnv((void**)&env, JNI_VERSION_1_8);
    bool didAttach = false;

    if (getEnvStat == JNI_EDETACHED) {
        if (g_jvm->AttachCurrentThread((void**)&env, nullptr) != 0) {
            return;
        }
        didAttach = true;
    } else if (getEnvStat == JNI_EVERSION) {
        return;
    }

    bool isDark = IsDarkModeEnabled();
    env->CallVoidMethod(g_themeObj, g_notifyThemeMethodId, isDark ? JNI_TRUE : JNI_FALSE);

    if (didAttach) {
        g_jvm->DetachCurrentThread();
    }
}

// Call the Java listener when resolution or DPI changes
void notifyJava(HWND hwnd, int overrideDpi = 0) {
    if (!g_jvm || !g_themeObj || !g_notifyMethodId) return;

    JNIEnv* env;
    int getEnvStat = g_jvm->GetEnv((void**)&env, JNI_VERSION_1_8);
    bool didAttach = false;

    if (getEnvStat == JNI_EDETACHED) {
        if (g_jvm->AttachCurrentThread((void**)&env, nullptr) != 0) {
            return; // Failed to attach
        }
        didAttach = true;
    } else if (getEnvStat == JNI_EVERSION) {
        return; // Version not supported
    }

    // Get actual DPI - use overrideDpi from WM_DPICHANGED if available, otherwise query current
    int dpi = 96; // default 100%
    if (overrideDpi > 0) {
        // WM_DPICHANGED provides the new DPI in wParam - use it!
        dpi = overrideDpi;
    } else {
        // Query current DPI from the monitor (for WM_DISPLAYCHANGE, etc.)
        HMONITOR hMonitor = MonitorFromWindow(hwnd ? hwnd : GetDesktopWindow(), MONITOR_DEFAULTTOPRIMARY);
        if (hMonitor) {
            UINT dpiX = 96, dpiY = 96;
            if (SUCCEEDED(GetDpiForMonitor(hMonitor, MDT_EFFECTIVE_DPI, &dpiX, &dpiY))) {
                dpi = (int)dpiX;
            }
        }
    }
    
    // Get actual resolution from EnumDisplaySettings (not GetSystemMetrics which returns virtual coordinates)
    int width = 0, height = 0;
    DEVMODE dm = {};
    dm.dmSize = sizeof(dm);
    if (EnumDisplaySettingsA(NULL, ENUM_CURRENT_SETTINGS, &dm)) {
        width = dm.dmPelsWidth;
        height = dm.dmPelsHeight;
    } else {
        // Fallback to GetSystemMetrics
        width = GetSystemMetrics(SM_CXSCREEN);
        height = GetSystemMetrics(SM_CYSCREEN);
    }

    // Detect orientation from DEVMODE
    int orientation = 0; // 0=LANDSCAPE, 1=PORTRAIT, 2=LANDSCAPE_FLIPPED, 3=PORTRAIT_FLIPPED
    if (dm.dmFields & DM_DISPLAYORIENTATION) {
        switch (dm.dmDisplayOrientation) {
            case DMDO_DEFAULT: orientation = 0; break;
            case DMDO_90: orientation = 1; break;
            case DMDO_180: orientation = 2; break;
            case DMDO_270: orientation = 3; break;
        }
    } else {
        // Fallback: detect from width/height
        orientation = (width > height) ? 0 : 1;
    }

    // Get refresh rate from DEVMODE
    int refreshRate = 60; // default
    if (dm.dmFields & DM_DISPLAYFREQUENCY) {
        refreshRate = dm.dmDisplayFrequency;
    }

    env->CallVoidMethod(g_themeObj, g_notifyMethodId, width, height, dpi, orientation, refreshRate);

    if (didAttach) {
        g_jvm->DetachCurrentThread();
    }
}

// Window Procedure for our invisible window
LRESULT CALLBACK WindowProc(HWND hwnd, UINT uMsg, WPARAM wParam, LPARAM lParam) {
    switch (uMsg) {
        case WM_DISPLAYCHANGE:
            notifyJava(hwnd, 0);
            return 0;
        case WM_DPICHANGED: {
            // DPI scale is changing - Windows provides new DPI in HIWORD(wParam)
            int newDpi = HIWORD(wParam);
            
            // REQUIRED: Must call SetWindowPos with the suggested rect from lParam
            // This tells Windows we're handling the DPI change
            RECT* const prcNewWindow = (RECT*)lParam;
            SetWindowPos(hwnd, NULL, 
                prcNewWindow->left, prcNewWindow->top,
                prcNewWindow->right - prcNewWindow->left,
                prcNewWindow->bottom - prcNewWindow->top,
                SWP_NOZORDER | SWP_NOACTIVATE);
            
            notifyJava(hwnd, newDpi);
            return 0;
        }
        case WM_SETTINGCHANGE:
            // Theme change detection disabled - only initial state is reported
            break;
        case WM_DESTROY:
            PostQuitMessage(0);
            return 0;
    }
    return DefWindowProcA(hwnd, uMsg, wParam, lParam);
}

// The thread that runs the message loop
DWORD WINAPI MonitorThread(LPVOID lpParam) {
    const char CLASS_NAME[] = "FastThemeMonitorClass";

    // Enable per-monitor DPI awareness for this thread BEFORE creating window
    // Windows 10 1607+
    HMODULE hUser32 = GetModuleHandleA("user32.dll");
    typedef DPI_AWARENESS_CONTEXT (WINAPI *SetThreadDpiAwarenessContextProc)(DPI_AWARENESS_CONTEXT);
    SetThreadDpiAwarenessContextProc SetThreadDpiAwarenessContext = 
        (SetThreadDpiAwarenessContextProc)GetProcAddress(hUser32, "SetThreadDpiAwarenessContext");
    if (SetThreadDpiAwarenessContext) {
        SetThreadDpiAwarenessContext(DPI_AWARENESS_CONTEXT_PER_MONITOR_AWARE_V2);
    }

    WNDCLASSA wc = { 0 };
    wc.lpfnWndProc = WindowProc;
    wc.hInstance = GetModuleHandleA(NULL);
    wc.lpszClassName = CLASS_NAME;

    RegisterClassA(&wc);

    g_hwnd = CreateWindowExA(
        0,
        CLASS_NAME,
        "FastThemeMonitorWindow",
        WS_OVERLAPPEDWINDOW, // Normal top-level window, but without WS_VISIBLE
        0, 0, 0, 0,
        NULL, NULL, GetModuleHandleA(NULL), NULL
    );

    if (g_hwnd == nullptr) {
        return 0;
    }

    MSG msg = { 0 };
    while (GetMessage(&msg, nullptr, 0, 0)) {
        TranslateMessage(&msg);
        DispatchMessage(&msg);
    }

    return 0;
}

// Send initial state to Java
static void sendInitialState() {
    if (!g_jvm || !g_themeObj || !g_notifyInitialStateMethodId) {
        return;
    }

    JNIEnv* env;
    int getEnvStat = g_jvm->GetEnv((void**)&env, JNI_VERSION_1_8);
    bool didAttach = false;

    if (getEnvStat == JNI_EDETACHED) {
        if (g_jvm->AttachCurrentThread((void**)&env, nullptr) != 0) {
            return;
        }
        didAttach = true;
    } else if (getEnvStat == JNI_EVERSION) {
        return;
    }

    // Get current display settings
    int width = 0, height = 0, dpi = 96, orientation = 0, refreshRate = 60;
    
    DEVMODE dm = {};
    dm.dmSize = sizeof(dm);
    if (EnumDisplaySettingsA(NULL, ENUM_CURRENT_SETTINGS, &dm)) {
        width = dm.dmPelsWidth;
        height = dm.dmPelsHeight;
        if (dm.dmFields & DM_DISPLAYFREQUENCY) {
            refreshRate = dm.dmDisplayFrequency;
        }
        if (dm.dmFields & DM_DISPLAYORIENTATION) {
            switch (dm.dmDisplayOrientation) {
                case DMDO_DEFAULT: orientation = 0; break;
                case DMDO_90: orientation = 1; break;
                case DMDO_180: orientation = 2; break;
                case DMDO_270: orientation = 3; break;
            }
        } else {
            orientation = (width > height) ? 0 : 1;
        }
    }
    
    // Get DPI
    HMONITOR hMonitor = MonitorFromWindow(GetDesktopWindow(), MONITOR_DEFAULTTOPRIMARY);
    if (hMonitor) {
        UINT dpiX = 96;
        UINT dpiY = 96;
        if (SUCCEEDED(GetDpiForMonitor(hMonitor, MDT_EFFECTIVE_DPI, &dpiX, &dpiY))) {
            dpi = (int)dpiX;
        }
    }
    
    // Get current theme
    bool isDark = IsDarkModeEnabled();
    
    env->CallVoidMethod(g_themeObj, g_notifyInitialStateMethodId, width, height, dpi, orientation, refreshRate, isDark ? JNI_TRUE : JNI_FALSE);

    if (didAttach) {
        g_jvm->DetachCurrentThread();
    }
}

extern "C" JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_startMonitoring(JNIEnv* env, jobject obj) {
    if (g_hwnd != nullptr) {
        return JNI_TRUE; // Already running
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

    // Note: DPI awareness is set per-thread in MonitorThread before window creation

    HANDLE hThread = CreateThread(nullptr, 0, MonitorThread, nullptr, 0, &g_threadId);
    if (hThread) {
        CloseHandle(hThread);
        // Wait for window to be created (g_hwnd set in MonitorThread)
        int attempts = 0;
        while (g_hwnd == nullptr && attempts < 50) {
            Sleep(10);
            attempts++;
        }
        // Now send initial state (JNI is fully initialized here)
        if (g_hwnd != nullptr) {
            sendInitialState();
        }
        return JNI_TRUE;
    }

    return JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL Java_fasttheme_FastTheme_stopMonitoring(JNIEnv* env, jobject obj) {
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
