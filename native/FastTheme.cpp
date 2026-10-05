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
 * @version 0.1.4
 */

#include <jni.h>
#include <windows.h>
#include <windowsx.h>
#include <dwmapi.h>
#include <jawt_md.h>
#include <vector>
#include <unordered_map>
#include <mutex>

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
#define DWMWA_MICA_EFFECT 1029
#endif

#ifndef DWMWA_SYSTEMBACKDROP_TYPE
#define DWMWA_SYSTEMBACKDROP_TYPE 38
#endif

#ifndef DWMWA_CAPTION_COLOR
#define DWMWA_CAPTION_COLOR 35
#endif

#ifndef DWMWA_TEXT_COLOR
#define DWMWA_TEXT_COLOR 36
#endif

#ifndef DWMWA_COLOR_NONE
#define DWMWA_COLOR_NONE 0xFFFFFFFE
#endif

#ifndef DWMSBT_AUTO
#define DWMSBT_AUTO 0
#define DWMSBT_NONE 1
#define DWMSBT_MAINWINDOW 2      // Mica
#define DWMSBT_TRANSIENTWINDOW 3 // Acrylic
#define DWMSBT_TABBEDWINDOW 4    // Mica Alt
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
    if ((ds->Lock(ds) & JAWT_LOCK_ERROR) != 0) {
        awt.FreeDrawingSurface(ds);
        return 0;
    }

    JAWT_DrawingSurfaceInfo* dsi = ds->GetDrawingSurfaceInfo(ds);
    HWND hwnd = NULL;
    if (dsi != NULL) {
        if (dsi->platformInfo != NULL) {
            hwnd = ((JAWT_Win32DrawingSurfaceInfo*)dsi->platformInfo)->hwnd;
        }
        ds->FreeDrawingSurfaceInfo(dsi);
    }
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
 * @brief Retrieves the native window handle (HWND) of the current Windows Console.
 */
JNIEXPORT jlong JNICALL Java_fasttheme_FastTheme_getConsoleWindowHandle(JNIEnv* env, jclass clazz) {
    HWND hwnd = GetConsoleWindow();
    if (hwnd != NULL) {
        HWND root = GetAncestor(hwnd, GA_ROOTOWNER);
        if (root != NULL && IsWindow(root)) return (jlong)root;
        root = GetAncestor(hwnd, GA_ROOT);
        if (root != NULL && IsWindow(root)) return (jlong)root;
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
    BOOL res = SetLayeredWindowAttributes(hwnd, 0, (BYTE)alpha, LWA_ALPHA);

    HWND root = GetAncestor(hwnd, GA_ROOTOWNER);
    if (root != NULL && root != hwnd && IsWindow(root)) {
        LONG rootEx = GetWindowLong(root, GWL_EXSTYLE);
        SetWindowLong(root, GWL_EXSTYLE, rootEx | WS_EX_LAYERED);
        SetLayeredWindowAttributes(root, 0, (BYTE)alpha, LWA_ALPHA);
        SetWindowPos(root, NULL, 0, 0, 0, 0, SWP_FRAMECHANGED | SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_NOACTIVATE);
        RedrawWindow(root, NULL, NULL, RDW_ERASE | RDW_INVALIDATE | RDW_FRAME | RDW_ALLCHILDREN);
    }

    SetWindowPos(hwnd, NULL, 0, 0, 0, 0, SWP_FRAMECHANGED | SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_NOACTIVATE);
    RedrawWindow(hwnd, NULL, NULL, RDW_ERASE | RDW_INVALIDATE | RDW_FRAME | RDW_ALLCHILDREN);

    return res ? JNI_TRUE : JNI_FALSE;
}

/**
 * @brief Sets the native window background brush color to eliminate white window creation flash.
 */
JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setWindowBackgroundColor(JNIEnv* env, jclass clazz, jlong hwndLong, jint r, jint g, jint b) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;

    HBRUSH hNewBrush = CreateSolidBrush(RGB(r, g, b));
    if (!hNewBrush) return JNI_FALSE;

    // Free previously stored custom brush for this window to prevent GDI handle leak
    HBRUSH hOldCustomBrush = (HBRUSH)GetPropW(hwnd, L"FastTheme_CustomBgBrush");
    if (hOldCustomBrush != NULL) {
        DeleteObject(hOldCustomBrush);
    }
    SetPropW(hwnd, L"FastTheme_CustomBgBrush", (HANDLE)hNewBrush);

    // Apply brush to window class for early initialization paints
    HBRUSH hPrevClassBrush = (HBRUSH)SetClassLongPtr(hwnd, GCLP_HBRBACKGROUND, (LONG_PTR)hNewBrush);
    // If the previous class brush was a custom created brush not attached to this window, delete it
    if (hPrevClassBrush != NULL && hPrevClassBrush != hOldCustomBrush && hPrevClassBrush != (HBRUSH)GetStockObject(BLACK_BRUSH) && hPrevClassBrush != (HBRUSH)GetStockObject(WHITE_BRUSH)) {
        DeleteObject(hPrevClassBrush);
    }

    RedrawWindow(hwnd, NULL, NULL, RDW_ERASE | RDW_INVALIDATE | RDW_FRAME | RDW_ALLCHILDREN);
    return JNI_TRUE;
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
 * @brief Sets the system backdrop type on Windows 11 (Build 22621+).
 * 
 * @param type 0 = Auto, 1 = None, 2 = Mica, 3 = Acrylic, 4 = Mica Alt (Tabbed)
 */
JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setSystemBackdropType(JNIEnv* env, jclass clazz, jlong hwndLong, jint type) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;

    int backdrop = type;
    HRESULT hr = DwmSetWindowAttribute(hwnd, DWMWA_SYSTEMBACKDROP_TYPE, &backdrop, sizeof(backdrop));
    if (FAILED(hr)) {
        // Fallback for older Windows 11 builds (22000) if Mica is requested
        if (type == DWMSBT_MAINWINDOW) {
            BOOL micaVal = TRUE;
            hr = DwmSetWindowAttribute(hwnd, DWMWA_MICA_EFFECT, &micaVal, sizeof(micaVal));
        } else if (type == DWMSBT_NONE) {
            BOOL micaVal = FALSE;
            hr = DwmSetWindowAttribute(hwnd, DWMWA_MICA_EFFECT, &micaVal, sizeof(micaVal));
        }
    }

    if (SUCCEEDED(hr) && type != DWMSBT_NONE) {
        // Set transparent caption color so title bar seamlessly displays Mica material
        COLORREF transparentColor = DWMWA_COLOR_NONE;
        DwmSetWindowAttribute(hwnd, DWMWA_CAPTION_COLOR, &transparentColor, sizeof(transparentColor));

        // Extend DWM frame into full client area
        MARGINS margins = { -1, -1, -1, -1 };
        DwmExtendFrameIntoClientArea(hwnd, &margins);

        // Reset any opaque GDI window class background brush to null/stock so Mica is not blocked
        SetClassLongPtr(hwnd, GCLP_HBRBACKGROUND, (LONG_PTR)GetStockObject(NULL_BRUSH));
    }

    SetWindowPos(hwnd, NULL, 0, 0, 0, 0, SWP_FRAMECHANGED | SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_NOACTIVATE);
    DwmFlush();
    RedrawWindow(hwnd, NULL, NULL, RDW_ERASE | RDW_INVALIDATE | RDW_FRAME | RDW_ALLCHILDREN | RDW_UPDATENOW);
    return SUCCEEDED(hr) ? JNI_TRUE : JNI_FALSE;
}

/**
 * @brief Forces a complete DWM frame re-evaluation and redraw on a visible window.
 */
JNIEXPORT void JNICALL Java_fasttheme_FastTheme_forceFrameUpdate(JNIEnv* env, jclass clazz, jlong hwndLong) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return;

    MARGINS margins = { -1, -1, -1, -1 };
    DwmExtendFrameIntoClientArea(hwnd, &margins);

    SetWindowPos(hwnd, NULL, 0, 0, 0, 0,
                 SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_NOACTIVATE | SWP_FRAMECHANGED);

    DwmFlush();
    RedrawWindow(hwnd, NULL, NULL,
                 RDW_INVALIDATE | RDW_ERASE | RDW_FRAME | RDW_ALLCHILDREN | RDW_UPDATENOW);
}

/**
 * @brief Enables the Mica material effect (Windows 11+).
 */
JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_enableMica(JNIEnv* env, jclass clazz, jlong hwndLong, jboolean enabled) {
    return Java_fasttheme_FastTheme_setSystemBackdropType(env, clazz, hwndLong, enabled ? DWMSBT_MAINWINDOW : DWMSBT_NONE);
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

// Structure for TitleBar Hit-Test Layout
struct TitleBarLayout {
    int height = 6;
    bool nativeButtonsEnabled = false;
    int buttonWidth = 96;
    std::vector<RECT> controlRects;
};

static std::unordered_map<HWND, TitleBarLayout> g_titleBarLayouts;
static std::mutex g_titleBarMutex;

/**
 * @brief Subclass procedure to handle premium overlay behavior.
 */
LRESULT CALLBACK OverlaySubclassProc(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam) {
    WNDPROC oldProc = (WNDPROC)GetPropW(hwnd, L"FastTheme_OldProc");
    if (!oldProc) return DefWindowProc(hwnd, msg, wParam, lParam);

    switch (msg) {
        case WM_NCCALCSIZE: {
            if (wParam) {
                // If maximized, adjust the client rect to avoid extending onto other monitors
                if (IsZoomed(hwnd)) {
                    LPNCCALCSIZE_PARAMS pParams = (LPNCCALCSIZE_PARAMS)lParam;
                    HMONITOR hMonitor = MonitorFromWindow(hwnd, MONITOR_DEFAULTTONEAREST);
                    if (hMonitor) {
                        MONITORINFO mi = { sizeof(mi) };
                        if (GetMonitorInfo(hMonitor, &mi)) {
                            pParams->rgrc[0] = mi.rcWork;
                        }
                    }
                }
                return 0;
            }
            return 0;
        }

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
            // 1. Calculate resize borders and corners manually if window is resizable and not zoomed
            if (!IsZoomed(hwnd)) {
                POINT ptScreen = { GET_X_LPARAM(lParam), GET_Y_LPARAM(lParam) };
                RECT rcWin;
                GetWindowRect(hwnd, &rcWin);

                // Border thickness in screen pixels (standard Windows resize border: 8px)
                int border = GetSystemMetrics(SM_CXSIZEFRAME) + GetSystemMetrics(SM_CXPADDEDBORDER);
                if (border < 8) border = 8;

                bool onLeft   = (ptScreen.x >= rcWin.left && ptScreen.x < rcWin.left + border);
                bool onRight  = (ptScreen.x >= rcWin.right - border && ptScreen.x < rcWin.right);
                bool onTop    = (ptScreen.y >= rcWin.top && ptScreen.y < rcWin.top + border);
                bool onBottom = (ptScreen.y >= rcWin.bottom - border && ptScreen.y < rcWin.bottom);

                if (onTop && onLeft)     return HTTOPLEFT;
                if (onTop && onRight)    return HTTOPRIGHT;
                if (onBottom && onLeft)  return HTBOTTOMLEFT;
                if (onBottom && onRight) return HTBOTTOMRIGHT;
                if (onLeft)              return HTLEFT;
                if (onRight)             return HTRIGHT;
                if (onTop)               return HTTOP;
                if (onBottom)            return HTBOTTOM;
            }

            // 2. Client area / Caption handling in title bar zone
            POINT pt = { GET_X_LPARAM(lParam), GET_Y_LPARAM(lParam) };
            ScreenToClient(hwnd, &pt);

            int titleBarHeight = (int)(INT_PTR)GetPropW(hwnd, L"FastTheme_DragHeight");
            if (titleBarHeight <= 0) titleBarHeight = 6;

            std::lock_guard<std::mutex> lock(g_titleBarMutex);
            auto it = g_titleBarLayouts.find(hwnd);
            if (it != g_titleBarLayouts.end() && it->second.height > 0) {
                titleBarHeight = it->second.height;
            }

            if (pt.y >= 0 && pt.y < titleBarHeight) {
                // 2a. Native System Caption Buttons delegation (Top-Right):
                // If nativeButtonsEnabled is set, delegate top-right areas to Windows:
                // [Close: right - btnW to right], [Maximize: right - 2*btnW to right - btnW], [Minimize: right - 3*btnW to right - 2*btnW]
                if (it != g_titleBarLayouts.end() && it->second.nativeButtonsEnabled) {
                    RECT clientRc;
                    GetClientRect(hwnd, &clientRc);
                    int btnW = it->second.buttonWidth > 0 ? it->second.buttonWidth : 96;
                    int right = clientRc.right;

                    if (pt.x >= right - btnW && pt.x < right) {
                        return HTCLOSE;
                    } else if (pt.x >= right - 2 * btnW && pt.x < right - btnW) {
                        return HTMAXBUTTON;
                    } else if (pt.x >= right - 3 * btnW && pt.x < right - 2 * btnW) {
                        return HTMINBUTTON;
                    }
                }

                // 2b. Check if cursor is over any registered interactive control rect
                if (it != g_titleBarLayouts.end()) {
                    for (const auto& rc : it->second.controlRects) {
                        if (pt.x >= rc.left && pt.x < rc.right &&
                            pt.y >= rc.top && pt.y < rc.bottom) {
                            return HTCLIENT; // Interactive UI element: dispatch clicks to app
                        }
                    }
                }
                // Free caption area: enable window dragging & snapping
                return HTCAPTION;
            }

            return HTCLIENT;
        }

        case WM_NCDESTROY: {
            SetWindowLongPtr(hwnd, GWLP_WNDPROC, (LONG_PTR)oldProc);
            RemovePropW(hwnd, L"FastTheme_OldProc");
            RemovePropW(hwnd, L"FastTheme_DragHeight");
            {
                std::lock_guard<std::mutex> lock(g_titleBarMutex);
                g_titleBarLayouts.erase(hwnd);
            }
            HBRUSH hBrush = (HBRUSH)GetPropW(hwnd, L"FastTheme_CustomBgBrush");
            if (hBrush != NULL) {
                DeleteObject(hBrush);
                RemovePropW(hwnd, L"FastTheme_CustomBgBrush");
            }
            break;
        }

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

        // 1. Force Modern Borderless: Keep WS_THICKFRAME, WS_MINIMIZEBOX, WS_MAXIMIZEBOX
        LONG style = GetWindowLong(hwnd, GWL_STYLE);
        style &= ~WS_CAPTION;
        style &= ~WS_SYSMENU;
        style |= WS_POPUP | WS_THICKFRAME | WS_MINIMIZEBOX | WS_MAXIMIZEBOX;
        SetWindowLong(hwnd, GWL_STYLE, style);

        // Keep normal app window style (do NOT add WS_EX_TOOLWINDOW)

        // 2. Neutralize the Window Class Brush to prevent the white flash
        SetClassLongPtr(hwnd, GCLP_HBRBACKGROUND, (LONG_PTR)GetStockObject(BLACK_BRUSH));

        // 3. Install Subclass for NCCALCSIZE, NCACTIVATE, NCPAINT, ERASEBKGND and NCHITTEST
        if (!GetPropW(hwnd, L"FastTheme_OldProc")) {
            WNDPROC oldProc = (WNDPROC)SetWindowLongPtr(hwnd, GWLP_WNDPROC, (LONG_PTR)OverlaySubclassProc);
            SetPropW(hwnd, L"FastTheme_OldProc", (HANDLE)oldProc);
        }

        // 4. Extend frame for shadow (-1 = full window glass/shadow)
        MARGINS margins = { -1, -1, -1, -1 };
        DwmExtendFrameIntoClientArea(hwnd, &margins);
    } else {
        WNDPROC oldProc = (WNDPROC)GetPropW(hwnd, L"FastTheme_OldProc");
        if (oldProc) {
            SetWindowLongPtr(hwnd, GWLP_WNDPROC, (LONG_PTR)oldProc);
            RemovePropW(hwnd, L"FastTheme_OldProc");
        }
        LONG style = GetWindowLong(hwnd, GWL_STYLE);
        style |= WS_CAPTION | WS_SYSMENU | WS_OVERLAPPEDWINDOW;
        SetWindowLong(hwnd, GWL_STYLE, style);
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
    {
        std::lock_guard<std::mutex> lock(g_titleBarMutex);
        g_titleBarLayouts[hwnd].height = height;
    }
    return JNI_TRUE;
}

/**
 * @brief Sets the height of the interactive title bar zone.
 */
JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setTitleBarHeight(JNIEnv* env, jclass clazz, jlong hwndLong, jint height) {
    return Java_fasttheme_FastTheme_setOverlayDragHeight(env, clazz, hwndLong, height);
}

/**
 * @brief Registers an exclusion rectangle for interactive UI controls in the title bar.
 */
JNIEXPORT void JNICALL Java_fasttheme_FastTheme_addTitleBarControlRect(JNIEnv* env, jclass clazz, jlong hwndLong, jint x, jint y, jint w, jint h) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return;
    RECT rc = { x, y, x + w, y + h };
    std::lock_guard<std::mutex> lock(g_titleBarMutex);
    g_titleBarLayouts[hwnd].controlRects.push_back(rc);
}

/**
 * @brief Clears all registered exclusion rectangles for a window.
 */
JNIEXPORT void JNICALL Java_fasttheme_FastTheme_clearTitleBarControlRects(JNIEnv* env, jclass clazz, jlong hwndLong) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return;
    std::lock_guard<std::mutex> lock(g_titleBarMutex);
    auto it = g_titleBarLayouts.find(hwnd);
    if (it != g_titleBarLayouts.end()) {
        it->second.controlRects.clear();
    }
}

/**
 * @brief Enables or disables delegation of top-right caption area to Windows native system buttons.
 *
 * @param hwnd 64-bit native window handle.
 * @param enabled True to let Windows handle Minimize, Maximize, and Close.
 * @param buttonWidth Width per button in pixels (e.g. 96 for modern Windows 11 Photos-style).
 */
JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setNativeTitleBarButtonsEnabled(JNIEnv* env, jclass clazz, jlong hwndLong, jboolean enabled, jint buttonWidth) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;
    std::lock_guard<std::mutex> lock(g_titleBarMutex);
    auto& layout = g_titleBarLayouts[hwnd];
    layout.nativeButtonsEnabled = (enabled == JNI_TRUE);
    if (buttonWidth > 0) {
        layout.buttonWidth = buttonWidth;
    }
    return JNI_TRUE;
}

/**
 * @brief Checks if the system is in dark mode.
 */
JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_isSystemDarkMode(JNIEnv* env, jclass clazz) {
    return IsDarkModeEnabled() ? JNI_TRUE : JNI_FALSE;
}

/**
 * @brief Toggles minimize and maximize buttons on standard native window title bar.
 * 
 * @param showMinimize True to display the minimize button, false to remove.
 * @param showMaximize True to display the maximize button, false to remove.
 */
JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setWindowButtonsVisible(JNIEnv* env, jclass clazz, jlong hwndLong, jboolean showMinimize, jboolean showMaximize) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;

    LONG_PTR style = GetWindowLongPtr(hwnd, GWL_STYLE);
    if (showMinimize) {
        style |= WS_MINIMIZEBOX;
    } else {
        style &= ~WS_MINIMIZEBOX;
    }

    if (showMaximize) {
        style |= WS_MAXIMIZEBOX;
    } else {
        style &= ~WS_MAXIMIZEBOX;
    }

    SetWindowLongPtr(hwnd, GWL_STYLE, style);
    SetWindowPos(hwnd, NULL, 0, 0, 0, 0,
                 SWP_FRAMECHANGED | SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_NOACTIVATE);
    return JNI_TRUE;
}

/**
 * @brief Toggles whether the native window stays always on top.
 * 
 * @param alwaysOnTop True to make topmost, false for normal z-order.
 */
JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setAlwaysOnTop(JNIEnv* env, jclass clazz, jlong hwndLong, jboolean alwaysOnTop) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;

    HWND insertAfter = alwaysOnTop ? HWND_TOPMOST : HWND_NOTOPMOST;
    BOOL res = SetWindowPos(hwnd, insertAfter, 0, 0, 0, 0,
                            SWP_NOMOVE | SWP_NOSIZE | SWP_NOACTIVATE);
    return res ? JNI_TRUE : JNI_FALSE;
}

} // extern "C"
