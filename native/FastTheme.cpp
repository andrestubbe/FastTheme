/**
 * @file FastTheme.cpp
 * @brief Native Windows Theme & Styling Engine for Java (FastTheme).
 * 
 * This module utilizes the Windows Desktop Window Manager (DWM) API and 
 * the Win32 Layered Window API to apply modern visual styles to Java windows.
 * 
 * Key responsibilities:
 * - Toggling Immersive Dark Mode.
 * - Applying Windows 11 materials (Mica).
 * - Configuring Window Corner Styles.
 * - Managing window-wide transparency.
 * 
 * @author FastJava Team
 * @version 0.2.0
 */

#include <jni.h>
#include <windows.h>
#include <dwmapi.h>
#include <jawt_md.h>

#pragma comment(lib, "dwmapi.lib")
#pragma comment(lib, "jawt.lib")

/**
 * @brief DWM Attribute constants for Windows 11 features.
 */
#ifndef DWMWA_USE_IMMERSIVE_DARK_MODE
#define DWMWA_USE_IMMERSIVE_DARK_MODE 20
#endif

#ifndef DWMWA_WINDOW_CORNER_PREFERENCE
#define DWMWA_WINDOW_CORNER_PREFERENCE 33
#endif

#ifndef DWMWA_MICA_EFFECT
#define DWMWA_MICA_EFFECT 38
#endif

/**
 * @brief Helper to check system dark mode via the Windows Registry.
 * 
 * @return true if 'AppsUseLightTheme' is set to 0.
 */
bool IsDarkModeEnabled() {
    HKEY hKey;
    if (RegOpenKeyExA(HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize", 0, KEY_READ, &hKey) == ERROR_SUCCESS) {
        DWORD value;
        DWORD size = sizeof(value);
        if (RegQueryValueExA(hKey, "AppsUseLightTheme", NULL, NULL, (LPBYTE)&value, &size) == ERROR_SUCCESS) {
            RegCloseKey(hKey);
            return value == 0;
        }
        RegCloseKey(hKey);
    }
    return false;
}

extern "C" {

/**
 * @brief Native implementation of HWND extraction.
 * 
 * Walks up the component hierarchy to find the top-level AWT Frame.
 */
JNIEXPORT jlong JNICALL Java_fasttheme_FastTheme_getWindowHandle(JNIEnv* env, jclass clazz, jobject component) {
    JAWT awt;
    awt.version = JAWT_VERSION_1_7;
    if (JAWT_GetAWT(env, &awt) == JNI_FALSE) return 0;

    JAWT_DrawingSurface* ds = awt.GetDrawingSurface(env, component);
    if (!ds) return 0;
    ds->Lock(ds);
    JAWT_DrawingSurfaceInfo* dsi = ds->GetDrawingSurfaceInfo(ds);
    HWND hwnd = ((JAWT_Win32DrawingSurfaceInfo*)dsi->platformInfo)->hwnd;
    ds->FreeDrawingSurfaceInfo(dsi);
    ds->Unlock(ds);
    awt.FreeDrawingSurface(ds);

    // Walk up to find the actual top-level frame window
    if (hwnd != NULL) {
        HWND parent = GetParent(hwnd);
        while (parent != NULL) {
            char className[256];
            GetClassNameA(parent, className, sizeof(className));
            if (strstr(className, "SunAwtFrame") != NULL) return (jlong)parent;
            parent = GetParent(parent);
        }
    }
    return (jlong)hwnd;
}

/**
 * @brief Sets window transparency using SetLayeredWindowAttributes.
 */
JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setWindowTransparency(JNIEnv* env, jclass clazz, jlong hwndLong, jint alpha) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;
    LONG exStyle = GetWindowLong(hwnd, GWL_EXSTYLE);
    SetWindowLong(hwnd, GWL_EXSTYLE, exStyle | WS_EX_LAYERED);
    return SetLayeredWindowAttributes(hwnd, 0, (BYTE)alpha, LWA_ALPHA) ? JNI_TRUE : JNI_FALSE;
}

/**
 * @brief Toggles Immersive Dark Mode via DwmSetWindowAttribute.
 */
JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setTitleBarDarkMode(JNIEnv* env, jclass clazz, jlong hwndLong, jboolean enabled) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;
    BOOL darkMode = enabled ? TRUE : FALSE;
    HRESULT hr = DwmSetWindowAttribute(hwnd, DWMWA_USE_IMMERSIVE_DARK_MODE, &darkMode, sizeof(darkMode));
    return SUCCEEDED(hr) ? JNI_TRUE : JNI_FALSE;
}

/**
 * @brief Sets the title bar color using DWMWA_CAPTION_COLOR (Windows 11+).
 */
JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setTitleBarColor(JNIEnv* env, jclass clazz, jlong hwndLong, jint r, jint g, jint b) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;
    COLORREF color = RGB(r, g, b);
    HRESULT hr = DwmSetWindowAttribute(hwnd, 35, &color, sizeof(color));
    return SUCCEEDED(hr) ? JNI_TRUE : JNI_FALSE;
}

/**
 * @brief Sets the title bar text color using DWMWA_TEXT_COLOR (Windows 11+).
 */
JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setTitleBarTextColor(JNIEnv* env, jclass clazz, jlong hwndLong, jint r, jint g, jint b) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;
    COLORREF color = RGB(r, g, b);
    HRESULT hr = DwmSetWindowAttribute(hwnd, 36, &color, sizeof(color));
    return SUCCEEDED(hr) ? JNI_TRUE : JNI_FALSE;
}

/**
 * @brief Enables the Mica material effect (Windows 11+).
 */
JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_enableMica(JNIEnv* env, jclass clazz, jlong hwndLong, jboolean enabled) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;
    int backdrop = enabled ? 2 : 0; // 2 = Mica Backdrop
    HRESULT hr = DwmSetWindowAttribute(hwnd, DWMWA_MICA_EFFECT, &backdrop, sizeof(backdrop));
    return SUCCEEDED(hr) ? JNI_TRUE : JNI_FALSE;
}

/**
 * @brief Sets the window corner style preference (Windows 11+).
 */
JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setCornerStyle(JNIEnv* env, jclass clazz, jlong hwndLong, jint style) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;
    HRESULT hr = DwmSetWindowAttribute(hwnd, DWMWA_WINDOW_CORNER_PREFERENCE, &style, sizeof(style));
    return SUCCEEDED(hr) ? JNI_TRUE : JNI_FALSE;
}

/**
 * @brief Subclass procedure to handle premium overlay behavior.
 */
LRESULT CALLBACK OverlaySubclassProc(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam) {
    WNDPROC oldProc = (WNDPROC)GetPropW(hwnd, L"FastTheme_OldProc");
    if (!oldProc) return DefWindowProc(hwnd, msg, wParam, lParam);

    switch (msg) {
        case WM_NCCALCSIZE:
            // Returning 0 removes the standard window chrome (title bar) 
            // and the 12px top margin while keeping the frame for the shadow.
            return 0;

        case WM_NCACTIVATE:
            // Prevents Windows from drawing the active/inactive title bar background
            return TRUE;

        case WM_NCPAINT:
            // Prevents Windows from painting the non-client area (borders)
            return 0;

        case WM_ERASEBKGND:
            // Prevents the "black/white flash" before Swing's first paint
            return 1;

        case WM_NCHITTEST: {
            // Get the custom drag height (default to 6 if not set)
            int dragHeight = (int)(INT_PTR)GetPropW(hwnd, L"FastTheme_DragHeight");
            if (dragHeight == 0) dragHeight = 6; 

            POINT pt = { (short)LOWORD(lParam), (short)HIWORD(lParam) };
            ScreenToClient(hwnd, &pt);

            // Drag Zone: Adjustable height
            if (pt.y >= 0 && pt.y < dragHeight) return HTCAPTION;

            // Prevent all resizing by treating borders as client area
            LRESULT hit = CallWindowProc(oldProc, hwnd, msg, wParam, lParam);
            if (hit >= HTLEFT && hit <= HTBOTTOMRIGHT) return HTCLIENT;
            
            return hit;
        }

        case WM_NCDESTROY:
            SetWindowLongPtr(hwnd, GWLP_WNDPROC, (LONG_PTR)oldProc);
            RemovePropW(hwnd, L"FastTheme_OldProc");
            RemovePropW(hwnd, L"FastTheme_DragHeight");
            break;
    }
    return CallWindowProc(oldProc, hwnd, msg, wParam, lParam);
}

/**
 * @brief Enables a borderless window that retains its native system shadow.
 */
JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setBorderlessShadow(JNIEnv* env, jclass clazz, jlong hwndLong, jboolean enabled) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;

    if (enabled) {
        // 0. Apply Dark Mode FIRST to ensure DWM knows it's a dark window from Frame #0
        BOOL darkMode = TRUE;
        DwmSetWindowAttribute(hwnd, DWMWA_USE_IMMERSIVE_DARK_MODE, &darkMode, sizeof(darkMode));

        // 1. Force Borderless: Remove ALL overlapped window chrome
        LONG style = GetWindowLong(hwnd, GWL_STYLE);
        style &= ~WS_OVERLAPPEDWINDOW; // Removes Caption, Menu, Border, Thickframe, etc.
        style |= WS_POPUP | WS_THICKFRAME; // Restore only Popup and Thickframe (for shadow)
        SetWindowLong(hwnd, GWL_STYLE, style);

        LONG exStyle = GetWindowLong(hwnd, GWL_EXSTYLE);
        exStyle |= WS_EX_TOOLWINDOW; 
        SetWindowLong(hwnd, GWL_EXSTYLE, exStyle);

        // 2. Neutralize the Window Class Brush to prevent the white flash
        // Using BLACK_BRUSH ensures that if there's a flicker, it's dark
        SetClassLongPtr(hwnd, GCLP_HBRBACKGROUND, (LONG_PTR)GetStockObject(BLACK_BRUSH));

        // 3. Install Subclass for NCCALCSIZE, NCACTIVATE, NCPAINT, ERASEBKGND and NCHITTEST
        if (!GetPropW(hwnd, L"FastTheme_OldProc")) {
            WNDPROC oldProc = (WNDPROC)SetWindowLongPtr(hwnd, GWLP_WNDPROC, (LONG_PTR)OverlaySubclassProc);
            SetPropW(hwnd, L"FastTheme_OldProc", (HANDLE)oldProc);
        }

        // 3. Extend frame for shadow (-1 = full window glass/shadow)
        MARGINS margins = { -1, -1, -1, -1 };
        DwmExtendFrameIntoClientArea(hwnd, &margins);
    } else {
        WNDPROC oldProc = (WNDPROC)GetPropW(hwnd, L"FastTheme_OldProc");
        if (oldProc) {
            SetWindowLongPtr(hwnd, GWLP_WNDPROC, (LONG_PTR)oldProc);
            RemovePropW(hwnd, L"FastTheme_OldProc");
        }
        LONG style = GetWindowLong(hwnd, GWL_STYLE);
        style |= WS_CAPTION | WS_SYSMENU;
        SetWindowLong(hwnd, GWL_STYLE, style);

        LONG exStyle = GetWindowLong(hwnd, GWL_EXSTYLE);
        exStyle &= ~WS_EX_TOOLWINDOW;
        SetWindowLong(hwnd, GWL_EXSTYLE, exStyle);
    }

    SetWindowPos(hwnd, NULL, 0, 0, 0, 0, 
                 SWP_FRAMECHANGED | SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_NOACTIVATE);

    return JNI_TRUE;
}

/**
 * @brief Sets the height of the invisible drag zone at the top of the window.
 */
JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setOverlayDragHeight(JNIEnv* env, jclass clazz, jlong hwndLong, jint height) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;
    SetPropW(hwnd, L"FastTheme_DragHeight", (HANDLE)(INT_PTR)height);
    return JNI_TRUE;
}

/**
 * @brief Checks if the system is in dark mode.
 */
JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_isSystemDarkMode(JNIEnv* env, jclass clazz) {
    return IsDarkModeEnabled() ? JNI_TRUE : JNI_FALSE;
}

} // extern "C"
