/**
 * @file FastTheme.cpp
 * @brief Native Windows Theme & Styling Engine for Java (FastTheme).
 */

#include "FastTheme.h"
#include <windows.h>
#include <windowsx.h>
#include <dwmapi.h>
#include <jawt_md.h>
#include <vector>
#include <unordered_map>
#include <mutex>

#pragma comment(lib, "dwmapi.lib")
#pragma comment(lib, "jawt.lib")
#pragma comment(lib, "uxtheme.lib")

#include <uxtheme.h>
#include <cstdio>
#include <cstdlib>

// Diagnostic logging, enabled via environment variable FASTTHEME_DEBUG=1
static bool FastThemeDebug() {
    static int enabled = -1;
    if (enabled < 0) {
        char buf[8] = {0};
        enabled = (GetEnvironmentVariableA("FASTTHEME_DEBUG", buf, sizeof(buf)) > 0 && buf[0] == '1') ? 1 : 0;
    }
    return enabled == 1;
}

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

#ifndef DWMWA_BORDER_COLOR
#define DWMWA_BORDER_COLOR 34
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

#ifndef DWMWA_CLOAKED
#define DWMWA_CLOAKED 14
#endif

#ifndef DWMSBT_AUTO
#define DWMSBT_AUTO 0
#define DWMSBT_NONE 1
#define DWMSBT_MAINWINDOW 2      // Mica
#define DWMSBT_TRANSIENTWINDOW 3 // Acrylic
#define DWMSBT_TABBEDWINDOW 4    // Mica Alt
#endif

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

struct TitleBarPalette {
    COLORREF inactiveBg          = RGB(0x10, 0x10, 0x10);
    COLORREF activeBg            = RGB(0x1B, 0x22, 0x23);
    COLORREF glyphActive         = RGB(0xFF, 0xFF, 0xFF);
    COLORREF glyphInactive       = RGB(0x79, 0x79, 0x79);
    COLORREF glyphInactiveHover  = RGB(0xFC, 0xFC, 0xFC);
    COLORREF inactiveBtnHover    = RGB(0x2D, 0x2D, 0x2D);
    COLORREF inactiveBtnPressed  = RGB(0x25, 0x25, 0x25);
    COLORREF activeBtnHover      = RGB(0x28, 0x2F, 0x30);
    COLORREF activeBtnPressed    = RGB(0x25, 0x2C, 0x2C);
    COLORREF activeCloseHover    = RGB(0xC4, 0x2B, 0x1C);
    COLORREF activeClosePressed  = RGB(0xB2, 0x2A, 0x1C);
    COLORREF closeHoverGlyph     = RGB(0xFF, 0xFF, 0xFF);
};

struct TitleBarLayout {
    int height = 6;
    bool nativeButtonsEnabled = false;
    int buttonWidth = 96;
    int hoveredButton = 0; // 0=none, 1=min, 2=max, 3=close
    int pressedButton = 0; // 0=none, 1=min, 2=max, 3=close
    bool isActive = true;
    bool wasCloaked = false;
    TitleBarPalette palette;
    int backdropType = 2; // DWMSBT_MAINWINDOW
    std::vector<RECT> controlRects;
};

static std::unordered_map<HWND, TitleBarLayout> g_titleBarLayouts;
static std::mutex g_titleBarMutex;

static void PaintNativeCaptionButtons(HWND hwnd, HDC hdc, const RECT& clientRc, const TitleBarLayout& layout) {
    if (!layout.nativeButtonsEnabled || layout.height <= 0) return;

    int btnW = layout.buttonWidth > 0 ? layout.buttonWidth : 46;
    int btnH = layout.height;
    int right = clientRc.right;

    RECT rcBtns  = { right - 3 * btnW, 0, right, btnH };
    RECT rcClose = { right - btnW, 0, right, btnH };
    RECT rcMax   = { right - 2 * btnW, 0, right - btnW, btnH };
    RECT rcMin   = { right - 3 * btnW, 0, right - 2 * btnW, btnH };

    const auto& pal = layout.palette;
    bool active = layout.isActive;

    // Use BufferedPaint for the buttons strip to ensure true opaque colors over Mica!
    HDC hdcPaint = hdc;
    HPAINTBUFFER hbp = NULL;
    HDC hdcBuffered = NULL;
    BP_PAINTPARAMS bpParams = { sizeof(bpParams) };
    bpParams.dwFlags = BPPF_NOCLIP;

    hbp = BeginBufferedPaint(hdc, &rcBtns, BPBF_TOPDOWNDIB, &bpParams, &hdcBuffered);
    if (hbp && hdcBuffered) {
        hdcPaint = hdcBuffered;
        if (active) {
            FillRect(hdcPaint, &rcBtns, (HBRUSH)GetStockObject(BLACK_BRUSH));
        } else {
            HBRUSH hInactiveBrush = CreateSolidBrush(pal.inactiveBg);
            FillRect(hdcPaint, &rcBtns, hInactiveBrush);
            DeleteObject(hInactiveBrush);
        }
    }

    // Close button background:
    if (layout.pressedButton == 3) {
        HBRUSH brush = CreateSolidBrush(pal.activeClosePressed);
        FillRect(hdcPaint, &rcClose, brush);
        DeleteObject(brush);
    } else if (layout.hoveredButton == 3) {
        HBRUSH brush = CreateSolidBrush(pal.activeCloseHover);
        FillRect(hdcPaint, &rcClose, brush);
        DeleteObject(brush);
    }

    // Min / Max button background:
    COLORREF btnHoverBg   = active ? pal.activeBtnHover   : pal.inactiveBtnHover;
    COLORREF btnPressedBg = active ? pal.activeBtnPressed : pal.inactiveBtnPressed;

    // Maximize button:
    if (layout.pressedButton == 2) {
        HBRUSH brush = CreateSolidBrush(btnPressedBg);
        FillRect(hdcPaint, &rcMax, brush);
        DeleteObject(brush);
    } else if (layout.hoveredButton == 2) {
        HBRUSH brush = CreateSolidBrush(btnHoverBg);
        FillRect(hdcPaint, &rcMax, brush);
        DeleteObject(brush);
    }

    // Minimize button:
    if (layout.pressedButton == 1) {
        HBRUSH brush = CreateSolidBrush(btnPressedBg);
        FillRect(hdcPaint, &rcMin, brush);
        DeleteObject(brush);
    } else if (layout.hoveredButton == 1) {
        HBRUSH brush = CreateSolidBrush(btnHoverBg);
        FillRect(hdcPaint, &rcMin, brush);
        DeleteObject(brush);
    }

    // Prepare font for Fluent glyphs (Segoe Fluent Icons or fallback Segoe MDL2 Assets)
    HFONT hFont = CreateFontW(
        -10, 0, 0, 0, FW_NORMAL, FALSE, FALSE, FALSE,
        DEFAULT_CHARSET, OUT_DEFAULT_PRECIS, CLIP_DEFAULT_PRECIS,
        CLEARTYPE_QUALITY, DEFAULT_PITCH | FF_DONTCARE,
        L"Segoe Fluent Icons"
    );

    HFONT hOldFont = (HFONT)SelectObject(hdcPaint, hFont);
    SetBkMode(hdcPaint, TRANSPARENT);

    // Glyph definitions
    WCHAR chMin[] = { 0xE921, 0 }; // Minimize (ChromeMinimize)
    WCHAR chMax[] = { IsZoomed(hwnd) ? (WCHAR)0xE923 : (WCHAR)0xE922, 0 }; // Restore / Maximize
    WCHAR chClose[] = { 0xE8BB, 0 }; // Close (ChromeClose)

    // Minimize glyph
    COLORREF minColor = active ? pal.glyphActive : (layout.hoveredButton == 1 ? pal.glyphInactiveHover : pal.glyphInactive);
    SetTextColor(hdcPaint, minColor);
    DrawTextW(hdcPaint, chMin, -1, &rcMin, DT_CENTER | DT_VCENTER | DT_SINGLELINE);

    // Maximize glyph
    COLORREF maxColor = active ? pal.glyphActive : (layout.hoveredButton == 2 ? pal.glyphInactiveHover : pal.glyphInactive);
    SetTextColor(hdcPaint, maxColor);
    DrawTextW(hdcPaint, chMax, -1, &rcMax, DT_CENTER | DT_VCENTER | DT_SINGLELINE);

    // Close glyph (hovering over close button always turns glyph pure white)
    COLORREF closeColor = (layout.hoveredButton == 3 || layout.pressedButton == 3) ? pal.closeHoverGlyph : 
                          (active ? pal.glyphActive : pal.glyphInactive);
    SetTextColor(hdcPaint, closeColor);
    DrawTextW(hdcPaint, chClose, -1, &rcClose, DT_CENTER | DT_VCENTER | DT_SINGLELINE);

    SelectObject(hdcPaint, hOldFont);
    DeleteObject(hFont);

    if (hbp) {
        if (!active) {
            // When inactive, ensure the entire caption button strip has Alpha 255 (true opaque over dark fallback)
            BufferedPaintSetAlpha(hbp, &rcBtns, 255);
        } else {
            // Only hovered/pressed buttons are drawn opaque over Mica
            if (layout.hoveredButton == 1 || layout.pressedButton == 1) {
                BufferedPaintSetAlpha(hbp, &rcMin, 255);
            }
            if (layout.hoveredButton == 2 || layout.pressedButton == 2) {
                BufferedPaintSetAlpha(hbp, &rcMax, 255);
            }
            if (layout.hoveredButton == 3 || layout.pressedButton == 3) {
                BufferedPaintSetAlpha(hbp, &rcClose, 255);
            }
        }
        EndBufferedPaint(hbp, TRUE);
    }
}

static void RefreshBackdrop(HWND hwnd, bool forceReset = false) {
    int backdrop = 2; // DWMSBT_MAINWINDOW
    {
        std::lock_guard<std::mutex> lock(g_titleBarMutex);
        auto it = g_titleBarLayouts.find(hwnd);
        if (it != g_titleBarLayouts.end() && it->second.backdropType > 0) {
            backdrop = it->second.backdropType;
        }
    }

    if (forceReset) {
        // Toggle NONE to kick DWM compositor into re-evaluating backdrop
        int none = DWMSBT_NONE;
        DwmSetWindowAttribute(hwnd, DWMWA_SYSTEMBACKDROP_TYPE, &none, sizeof(none));
    }

    DwmSetWindowAttribute(hwnd, DWMWA_SYSTEMBACKDROP_TYPE, &backdrop, sizeof(backdrop));

    COLORREF noneCol = DWMWA_COLOR_NONE;
    DwmSetWindowAttribute(hwnd, DWMWA_BORDER_COLOR, &noneCol, sizeof(noneCol));
    DwmSetWindowAttribute(hwnd, DWMWA_CAPTION_COLOR, &noneCol, sizeof(noneCol));

    MARGINS margins = { -1, -1, -1, -1 };
    DwmExtendFrameIntoClientArea(hwnd, &margins);
}



static const char* GetMsgName(UINT msg) {
    switch (msg) {
        case WM_ACTIVATE: return "WM_ACTIVATE";
        case WM_ACTIVATEAPP: return "WM_ACTIVATEAPP";
        case WM_NCACTIVATE: return "WM_NCACTIVATE";
        case WM_SETFOCUS: return "WM_SETFOCUS";
        case WM_KILLFOCUS: return "WM_KILLFOCUS";
        case WM_SHOWWINDOW: return "WM_SHOWWINDOW";
        case WM_WINDOWPOSCHANGED: return "WM_WINDOWPOSCHANGED";
        case WM_WINDOWPOSCHANGING: return "WM_WINDOWPOSCHANGING";
        case WM_STYLECHANGED: return "WM_STYLECHANGED";
        case WM_NCPAINT: return "WM_NCPAINT";
        case WM_PAINT: return "WM_PAINT";
        case WM_ERASEBKGND: return "WM_ERASEBKGND";
        case WM_MOUSEACTIVATE: return "WM_MOUSEACTIVATE";
        case WM_LBUTTONDOWN: return "WM_LBUTTONDOWN";
        case WM_LBUTTONUP: return "WM_LBUTTONUP";
        case WM_SIZE: return "WM_SIZE";
        case WM_MOVE: return "WM_MOVE";
        case WM_DWMCOMPOSITIONCHANGED: return "WM_DWMCOMPOSITIONCHANGED";
        case WM_DWMCOLORIZATIONCOLORCHANGED: return "WM_DWMCOLORIZATIONCOLORCHANGED";
        default: return NULL;
    }
}

static LRESULT CALLBACK BackdropSubclassProc(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam) {
    WNDPROC oldProc = (WNDPROC)GetPropW(hwnd, L"FastTheme_BackdropOldProc");
    if (!oldProc) return DefWindowProc(hwnd, msg, wParam, lParam);

    // Track Cloaking State changes cleanly
    DWORD cloaked = 0;
    DwmGetWindowAttribute(hwnd, DWMWA_CLOAKED, &cloaked, sizeof(cloaked));
    bool isCloaked = (cloaked != 0);
    bool cloakChanged = false;
    bool wasCloakedBefore = false;
    {
        std::lock_guard<std::mutex> lock(g_titleBarMutex);
        auto it = g_titleBarLayouts.find(hwnd);
        if (it != g_titleBarLayouts.end() && it->second.wasCloaked != isCloaked) {
            wasCloakedBefore = it->second.wasCloaked;
            it->second.wasCloaked = isCloaked;
            cloakChanged = true;
        }
    }
    if (cloakChanged) {
        if (isCloaked) {
            printf("\n>>> [EVENT: CLOAKED] (Desktop Switch weg | cloaked=%lu)\n", cloaked);
        } else {
            printf("\n>>> [EVENT: UNCLOAKED] (Desktop Switch zurueck | cloaked=0)\n");
        }
        fflush(stdout);
    }

    // Track Focus / Defocus cleanly
    if (msg == WM_ACTIVATE) {
        bool active = (LOWORD(wParam) != WA_INACTIVE);
        HWND fg = GetForegroundWindow();
        if (active) {
            printf(">>> [EVENT: FOCUS] (WM_ACTIVATE state=%d | isFg=%d)\n", (int)LOWORD(wParam), fg == hwnd);
        } else {
            printf(">>> [EVENT: DEFOCUS] (WM_ACTIVATE state=%d | isFg=%d)\n", (int)LOWORD(wParam), fg == hwnd);
        }
        fflush(stdout);
    }

    switch (msg) {
        case WM_NCCALCSIZE: {
            bool customLayout = false;
            {
                std::lock_guard<std::mutex> lock(g_titleBarMutex);
                auto it = g_titleBarLayouts.find(hwnd);
                if (it != g_titleBarLayouts.end() && (it->second.height > 6 || it->second.nativeButtonsEnabled)) {
                    customLayout = true;
                }
            }
            if (customLayout) {
                if (wParam) {
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
                    // Return 0 completely eliminates all white borders on left, bottom and right!
                    return 0;
                } else {
                    // wParam == 0: client rectangle calculation
                    return 0;
                }
            }
            break;
        }

        case WM_NCACTIVATE: {
            bool customLayout = false;
            {
                std::lock_guard<std::mutex> lock(g_titleBarMutex);
                auto it = g_titleBarLayouts.find(hwnd);
                if (it != g_titleBarLayouts.end() && (it->second.height > 6 || it->second.nativeButtonsEnabled)) {
                    customLayout = true;
                }
            }
            if (customLayout) {
                const bool active = (wParam != FALSE);
                bool changed = false;
                {
                    std::lock_guard<std::mutex> lock(g_titleBarMutex);
                    auto it = g_titleBarLayouts.find(hwnd);
                    if (it != g_titleBarLayouts.end() && it->second.isActive != active) {
                        it->second.isActive = active;
                        changed = true;
                    }
                }

                if (active) {
                    // Activate: Let DefWindowProc handle it so Mica initializes properly
                    CallWindowProc(oldProc, hwnd, msg, wParam, (LPARAM)-1);
                    RefreshBackdrop(hwnd, false);
                } else {
                    // Deactivate: Do NOT call oldProc/DefWindowProc — it draws the classic white inactive border!
                    LRESULT dwmResult = 0;
                    DwmDefWindowProc(hwnd, msg, wParam, lParam, &dwmResult);
                }

                if (changed) {
                    InvalidateRect(hwnd, NULL, FALSE);
                }
                return TRUE;
            }
            break;
        }

        case WM_MOUSEACTIVATE: {
            RefreshBackdrop(hwnd, false);
            InvalidateRect(hwnd, NULL, FALSE);
            break;
        }

        case WM_ACTIVATE: {
            bool customLayout = false;
            {
                std::lock_guard<std::mutex> lock(g_titleBarMutex);
                auto it = g_titleBarLayouts.find(hwnd);
                if (it != g_titleBarLayouts.end() && (it->second.height > 6 || it->second.nativeButtonsEnabled)) {
                    customLayout = true;
                }
            }
            if (customLayout) {
                bool active = (LOWORD(wParam) != WA_INACTIVE);

                bool changed = false;
                {
                    std::lock_guard<std::mutex> lock(g_titleBarMutex);
                    auto it = g_titleBarLayouts.find(hwnd);
                    if (it != g_titleBarLayouts.end() && it->second.isActive != active) {
                        it->second.isActive = active;
                        changed = true;
                    }
                }

                if (active) {
                    RefreshBackdrop(hwnd, false);
                }

                CallWindowProc(oldProc, hwnd, msg, wParam, lParam);

                if (changed) {
                    InvalidateRect(hwnd, NULL, FALSE);
                }
                return 0;
            }
            break;
        }

        case WM_NCPAINT: {
            bool customLayout = false;
            {
                std::lock_guard<std::mutex> lock(g_titleBarMutex);
                auto it = g_titleBarLayouts.find(hwnd);
                if (it != g_titleBarLayouts.end() && (it->second.height > 6 || it->second.nativeButtonsEnabled)) {
                    customLayout = true;
                }
            }
            if (customLayout) {
                return 0; // No non-client painting
            }
            break;
        }

        // Window position changing: VERY FIRST event received when returning from another desktop!
        case WM_WINDOWPOSCHANGING: {
            DWORD cloaked = 0;
            if (SUCCEEDED(DwmGetWindowAttribute(hwnd, DWMWA_CLOAKED, &cloaked, sizeof(cloaked)))) {
                bool isCloaked = (cloaked != 0);
                bool wasCloaked = false;
                {
                    std::lock_guard<std::mutex> lock(g_titleBarMutex);
                    auto it = g_titleBarLayouts.find(hwnd);
                    if (it != g_titleBarLayouts.end()) {
                        wasCloaked = it->second.wasCloaked;
                        it->second.wasCloaked = isCloaked;
                    }
                }
                // Transition STRICTLY from cloaked -> uncloaked! (No loop!)
                if (wasCloaked && !isCloaked) {
                    RefreshBackdrop(hwnd, true); // true: force reset via DWMSBT_NONE to kickstart DWM
                    InvalidateRect(hwnd, NULL, FALSE);
                }
            }
            break;
        }

        // BASELINE: Always paint black. On an extended DWM frame GDI writes alpha=0,
        // so DWM composites result = backdrop + gdiColor. Black = pure backdrop.
        // DWM natively handles its own subtle inactive backdrop dimming!
        case WM_ERASEBKGND: {
            // Background is completely handled in WM_PAINT
            return 1;
        }

        case WM_PAINT: {
            PAINTSTRUCT ps;
            HDC hdc = BeginPaint(hwnd, &ps);
            if (hdc) {
                HWND fg = GetForegroundWindow();
                HWND rootFg = fg ? GetAncestor(fg, GA_ROOT) : NULL;
                HWND rootWin = GetAncestor(hwnd, GA_ROOT);
                bool isActive = (fg == hwnd || (rootFg && rootFg == rootWin) || IsChild(hwnd, fg));

                TitleBarLayout layout;
                bool hasLayout = false;
                {
                    std::lock_guard<std::mutex> lock(g_titleBarMutex);
                    auto it = g_titleBarLayouts.find(hwnd);
                    if (it != g_titleBarLayouts.end()) {
                        it->second.isActive = isActive;
                        layout = it->second;
                        hasLayout = it->second.nativeButtonsEnabled;
                    }
                }

                RECT clientRc;
                GetClientRect(hwnd, &clientRc);

                if (isActive) {
                    // Active: Pure black (alpha 0 in DWM frame) lets vibrant Mica shine through!
                    FillRect(hdc, &clientRc, (HBRUSH)GetStockObject(BLACK_BRUSH));
                } else {
                    // Defocused / Inactive: True opaque dark native Windows 11 grey (#101010) with Alpha 255
                    // Using BufferedPaint with Alpha 255 completely occludes Mica so there is NO milky/washed-out additive blend!
                    HDC hdcBuffered = NULL;
                    BP_PAINTPARAMS bpParams = { sizeof(bpParams) };
                    bpParams.dwFlags = BPPF_NOCLIP;
                    HPAINTBUFFER hbp = BeginBufferedPaint(hdc, &clientRc, BPBF_TOPDOWNDIB, &bpParams, &hdcBuffered);
                    COLORREF bgCol = hasLayout ? layout.palette.inactiveBg : RGB(0x10, 0x10, 0x10);
                    if (hbp && hdcBuffered) {
                        HBRUSH hInactiveBrush = CreateSolidBrush(bgCol);
                        FillRect(hdcBuffered, &clientRc, hInactiveBrush);
                        DeleteObject(hInactiveBrush);
                        BufferedPaintSetAlpha(hbp, &clientRc, 255);
                        EndBufferedPaint(hbp, TRUE);
                    } else {
                        HBRUSH hInactiveBrush = CreateSolidBrush(bgCol);
                        FillRect(hdc, &clientRc, hInactiveBrush);
                        DeleteObject(hInactiveBrush);
                    }
                }

                if (hasLayout) {
                    PaintNativeCaptionButtons(hwnd, hdc, clientRc, layout);
                }
            }
            EndPaint(hwnd, &ps);
            return 0;
        }

        case WM_SHOWWINDOW: {
            LRESULT res = CallWindowProc(oldProc, hwnd, msg, wParam, lParam);
            if (wParam) { // Window is being shown (e.g. after desktop switch or unhide)
                RefreshBackdrop(hwnd, true); // Force-flush on desktop switch/show
                InvalidateRect(hwnd, NULL, FALSE);
            }
            return res;
        }

        case WM_WINDOWPOSCHANGED: {
            LRESULT res = CallWindowProc(oldProc, hwnd, msg, wParam, lParam);
            WINDOWPOS* lpwp = (WINDOWPOS*)lParam;
            
            // Query DWM cloaked status directly:
            DWORD cloaked = 0;
            if (SUCCEEDED(DwmGetWindowAttribute(hwnd, DWMWA_CLOAKED, &cloaked, sizeof(cloaked)))) {
                bool isCloaked = (cloaked != 0);
                bool wasCloaked = false;
                {
                    std::lock_guard<std::mutex> lock(g_titleBarMutex);
                    auto it = g_titleBarLayouts.find(hwnd);
                    if (it != g_titleBarLayouts.end()) {
                        wasCloaked = it->second.wasCloaked;
                        it->second.wasCloaked = isCloaked;
                    }
                }
                // Transition from cloaked -> uncloaked (Desktop switch back!)
                if (wasCloaked && !isCloaked) {
                    RefreshBackdrop(hwnd, true);
                    InvalidateRect(hwnd, NULL, FALSE);
                }
            }

            if (lpwp && (lpwp->flags & SWP_SHOWWINDOW)) {
                RefreshBackdrop(hwnd, true); // Un-cloaked/shown by shell
            }
            return res;
        }

        case WM_SIZE: {
            LRESULT res = CallWindowProc(oldProc, hwnd, msg, wParam, lParam);

            // If restored from minimize (SIZE_RESTORED or SIZE_MAXIMIZED), ensure DWM frame & backdrop remain intact!
            if (wParam == SIZE_RESTORED || wParam == SIZE_MAXIMIZED) {
                RefreshBackdrop(hwnd, false);
            }

            InvalidateRect(hwnd, NULL, FALSE);
            return res;
        }

        case WM_NCHITTEST: {
            TitleBarLayout layout;
            bool hasLayout = false;
            {
                std::lock_guard<std::mutex> lock(g_titleBarMutex);
                auto it = g_titleBarLayouts.find(hwnd);
                if (it != g_titleBarLayouts.end() && (it->second.height > 6 || it->second.nativeButtonsEnabled)) {
                    layout = it->second;
                    hasLayout = true;
                }
            }

            if (!hasLayout) {
                break; // Use standard default processing
            }

            // Window resize borders when not maximized
            if (!IsZoomed(hwnd)) {
                POINT ptScreen = { GET_X_LPARAM(lParam), GET_Y_LPARAM(lParam) };
                RECT rcWin;
                GetWindowRect(hwnd, &rcWin);

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

            POINT pt = { GET_X_LPARAM(lParam), GET_Y_LPARAM(lParam) };
            ScreenToClient(hwnd, &pt);

            int titleBarHeight = layout.height > 0 ? layout.height : 48;
            if (pt.y >= 0 && pt.y < titleBarHeight) {
                // If over caption buttons area, return HTCLIENT so we handle mouse & DWM never draws mini-buttons
                if (layout.nativeButtonsEnabled) {
                    RECT clientRc;
                    GetClientRect(hwnd, &clientRc);
                    int btnW = layout.buttonWidth > 0 ? layout.buttonWidth : 46;
                    int right = clientRc.right;

                    if (pt.x >= right - 3 * btnW && pt.x < right) {
                        return HTCLIENT;
                    }
                }

                // If over custom controls (buttons, tabs, etc.), return HTCLIENT
                for (const auto& rc : layout.controlRects) {
                    if (pt.x >= rc.left && pt.x < rc.right &&
                        pt.y >= rc.top && pt.y < rc.bottom) {
                        return HTCLIENT;
                    }
                }

                // Everywhere else in the title bar is draggable caption
                return HTCAPTION;
            }

            return HTCLIENT;
        }

        case WM_MOUSEMOVE: {
            TitleBarLayout layout;
            bool hasLayout = false;
            {
                std::lock_guard<std::mutex> lock(g_titleBarMutex);
                auto it = g_titleBarLayouts.find(hwnd);
                if (it != g_titleBarLayouts.end() && it->second.nativeButtonsEnabled) {
                    layout = it->second;
                    hasLayout = true;
                }
            }

            if (hasLayout) {
                POINT pt = { GET_X_LPARAM(lParam), GET_Y_LPARAM(lParam) };
                RECT clientRc;
                GetClientRect(hwnd, &clientRc);
                int btnW = layout.buttonWidth > 0 ? layout.buttonWidth : 46;
                int right = clientRc.right;

                int newHover = 0;
                if (pt.y >= 0 && pt.y < layout.height) {
                    if (pt.x >= right - btnW && pt.x < right) {
                        newHover = 3; // Close
                    } else if (pt.x >= right - 2 * btnW && pt.x < right - btnW) {
                        newHover = 2; // Max
                    } else if (pt.x >= right - 3 * btnW && pt.x < right - 2 * btnW) {
                        newHover = 1; // Min
                    }
                }

                bool changed = false;
                {
                    std::lock_guard<std::mutex> lock(g_titleBarMutex);
                    auto it = g_titleBarLayouts.find(hwnd);
                    if (it != g_titleBarLayouts.end() && it->second.hoveredButton != newHover) {
                        it->second.hoveredButton = newHover;
                        changed = true;
                    }
                }

                if (changed) {
                    RECT rcBtns = { clientRc.right - 3 * btnW, 0, clientRc.right, layout.height };
                    InvalidateRect(hwnd, &rcBtns, FALSE);
                    UpdateWindow(hwnd);
                }

                // Track mouse leave so hover resets when mouse moves away
                TRACKMOUSEEVENT tme = { sizeof(tme) };
                tme.dwFlags = TME_LEAVE;
                tme.hwndTrack = hwnd;
                TrackMouseEvent(&tme);
            }
            break;
        }

        case WM_MOUSELEAVE: {
            bool changed = false;
            TitleBarLayout layout;
            {
                std::lock_guard<std::mutex> lock(g_titleBarMutex);
                auto it = g_titleBarLayouts.find(hwnd);
                if (it != g_titleBarLayouts.end() && it->second.hoveredButton != 0) {
                    it->second.hoveredButton = 0;
                    layout = it->second;
                    changed = true;
                }
            }
            if (changed) {
                RECT clientRc;
                GetClientRect(hwnd, &clientRc);
                int btnW = layout.buttonWidth > 0 ? layout.buttonWidth : 46;
                RECT rcBtns = { clientRc.right - 3 * btnW, 0, clientRc.right, layout.height };
                InvalidateRect(hwnd, &rcBtns, FALSE);
                UpdateWindow(hwnd);
            }
            break;
        }

        case WM_LBUTTONDOWN: {
            TitleBarLayout layout;
            bool hasLayout = false;
            {
                std::lock_guard<std::mutex> lock(g_titleBarMutex);
                auto it = g_titleBarLayouts.find(hwnd);
                if (it != g_titleBarLayouts.end() && it->second.nativeButtonsEnabled) {
                    layout = it->second;
                    hasLayout = true;
                }
            }

            if (hasLayout) {
                POINT pt = { GET_X_LPARAM(lParam), GET_Y_LPARAM(lParam) };
                RECT clientRc;
                GetClientRect(hwnd, &clientRc);
                int btnW = layout.buttonWidth > 0 ? layout.buttonWidth : 46;
                int right = clientRc.right;

                int pressed = 0;
                if (pt.y >= 0 && pt.y < layout.height) {
                    if (pt.x >= right - btnW && pt.x < right) {
                        pressed = 3; // Close
                    } else if (pt.x >= right - 2 * btnW && pt.x < right - btnW) {
                        pressed = 2; // Max
                    } else if (pt.x >= right - 3 * btnW && pt.x < right - 2 * btnW) {
                        pressed = 1; // Min
                    }
                }

                if (pressed != 0) {
                    SetCapture(hwnd);
                    {
                        std::lock_guard<std::mutex> lock(g_titleBarMutex);
                        g_titleBarLayouts[hwnd].pressedButton = pressed;
                    }
                    RECT rcBtns = { clientRc.right - 3 * btnW, 0, clientRc.right, layout.height };
                    InvalidateRect(hwnd, &rcBtns, FALSE);
                    UpdateWindow(hwnd);
                    return 0; // Handled! Do not pass to DefWindowProc / parent
                }
            }
            break;
        }

        case WM_LBUTTONUP: {
            TitleBarLayout layout;
            bool hasLayout = false;
            int pressed = 0;
            {
                std::lock_guard<std::mutex> lock(g_titleBarMutex);
                auto it = g_titleBarLayouts.find(hwnd);
                if (it != g_titleBarLayouts.end() && it->second.nativeButtonsEnabled) {
                    layout = it->second;
                    pressed = layout.pressedButton;
                    it->second.pressedButton = 0;
                    hasLayout = true;
                }
            }

            if (GetCapture() == hwnd) {
                ReleaseCapture();
            }

            if (hasLayout) {
                POINT pt = { GET_X_LPARAM(lParam), GET_Y_LPARAM(lParam) };
                RECT clientRc;
                GetClientRect(hwnd, &clientRc);
                int btnW = layout.buttonWidth > 0 ? layout.buttonWidth : 46;
                int right = clientRc.right;

                // Repaint to clear pressed state cleanly via InvalidateRect
                RECT rcBtns = { clientRc.right - 3 * btnW, 0, clientRc.right, layout.height };
                InvalidateRect(hwnd, &rcBtns, FALSE);
                UpdateWindow(hwnd);

                // Check if released within the button that was pressed
                if (pressed != 0 && pt.y >= 0 && pt.y < layout.height) {
                    if (pressed == 3 && pt.x >= right - btnW && pt.x < right) {
                        SendMessage(hwnd, WM_SYSCOMMAND, SC_CLOSE, 0);
                        return 0;
                    } else if (pressed == 2 && pt.x >= right - 2 * btnW && pt.x < right - btnW) {
                        if (IsZoomed(hwnd)) {
                            SendMessage(hwnd, WM_SYSCOMMAND, SC_RESTORE, 0);
                        } else {
                            SendMessage(hwnd, WM_SYSCOMMAND, SC_MAXIMIZE, 0);
                        }
                        return 0;
                    } else if (pressed == 1 && pt.x >= right - 3 * btnW && pt.x < right - 2 * btnW) {
                        SendMessage(hwnd, WM_SYSCOMMAND, SC_MINIMIZE, 0);
                        return 0;
                    }
                }
            }
            break;
        }

        case WM_NCDESTROY: {
            BufferedPaintUnInit();
            SetWindowLongPtr(hwnd, GWLP_WNDPROC, (LONG_PTR)oldProc);
            RemovePropW(hwnd, L"FastTheme_BackdropOldProc");
            {
                std::lock_guard<std::mutex> lock(g_titleBarMutex);
                g_titleBarLayouts.erase(hwnd);
            }
            break;
        }
    }
    return CallWindowProc(oldProc, hwnd, msg, wParam, lParam);
}

LRESULT CALLBACK OverlaySubclassProc(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam) {
    WNDPROC oldProc = (WNDPROC)GetPropW(hwnd, L"FastTheme_OldProc");
    if (!oldProc) return DefWindowProc(hwnd, msg, wParam, lParam);

    switch (msg) {
        case WM_NCCALCSIZE: {
            if (wParam) {
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
            CallWindowProc(oldProc, hwnd, msg, wParam, (LPARAM)-1);
            return TRUE;

        case WM_NCPAINT:
            return 0;

        case WM_PAINT: {
            PAINTSTRUCT ps;
            HDC hdc = BeginPaint(hwnd, &ps);
            if (hdc) {
                TitleBarLayout layout;
                bool hasLayout = false;
                {
                    std::lock_guard<std::mutex> lock(g_titleBarMutex);
                    auto it = g_titleBarLayouts.find(hwnd);
                    if (it != g_titleBarLayouts.end()) {
                        layout = it->second;
                        hasLayout = true;
                    }
                }
                if (hasLayout) {
                    RECT clientRc;
                    GetClientRect(hwnd, &clientRc);
                    PaintNativeCaptionButtons(hwnd, hdc, clientRc, layout);
                }
            }
            EndPaint(hwnd, &ps);
            return 0;
        }

        case WM_NCMOUSEMOVE: {
            int newHover = 0;
            if (wParam == HTCLOSE) newHover = 3;
            else if (wParam == HTMAXBUTTON) newHover = 2;
            else if (wParam == HTMINBUTTON) newHover = 1;

            bool changed = false;
            {
                std::lock_guard<std::mutex> lock(g_titleBarMutex);
                auto it = g_titleBarLayouts.find(hwnd);
                if (it != g_titleBarLayouts.end() && it->second.hoveredButton != newHover) {
                    it->second.hoveredButton = newHover;
                    changed = true;
                }
            }
            if (changed) {
                RECT clientRc;
                GetClientRect(hwnd, &clientRc);
                HDC hdc = GetDC(hwnd);
                if (hdc) {
                    TitleBarLayout layout;
                    {
                        std::lock_guard<std::mutex> lock(g_titleBarMutex);
                        layout = g_titleBarLayouts[hwnd];
                    }
                    // Repaint buttons area with black brush first to keep Mica clear
                    int btnW = layout.buttonWidth > 0 ? layout.buttonWidth : 46;
                    RECT rcBtns = { clientRc.right - 3 * btnW, 0, clientRc.right, layout.height };
                    HBRUSH blackBrush = (HBRUSH)GetStockObject(BLACK_BRUSH);
                    FillRect(hdc, &rcBtns, blackBrush);
                    PaintNativeCaptionButtons(hwnd, hdc, clientRc, layout);
                    ReleaseDC(hwnd, hdc);
                }
            }
            break;
        }

        case WM_NCMOUSELEAVE: {
            bool changed = false;
            {
                std::lock_guard<std::mutex> lock(g_titleBarMutex);
                auto it = g_titleBarLayouts.find(hwnd);
                if (it != g_titleBarLayouts.end() && it->second.hoveredButton != 0) {
                    it->second.hoveredButton = 0;
                    changed = true;
                }
            }
            if (changed) {
                RECT clientRc;
                GetClientRect(hwnd, &clientRc);
                HDC hdc = GetDC(hwnd);
                if (hdc) {
                    TitleBarLayout layout;
                    {
                        std::lock_guard<std::mutex> lock(g_titleBarMutex);
                        layout = g_titleBarLayouts[hwnd];
                    }
                    int btnW = layout.buttonWidth > 0 ? layout.buttonWidth : 46;
                    RECT rcBtns = { clientRc.right - 3 * btnW, 0, clientRc.right, layout.height };
                    HBRUSH blackBrush = (HBRUSH)GetStockObject(BLACK_BRUSH);
                    FillRect(hdc, &rcBtns, blackBrush);
                    PaintNativeCaptionButtons(hwnd, hdc, clientRc, layout);
                    ReleaseDC(hwnd, hdc);
                }
            }
            break;
        }

        case WM_ERASEBKGND:
            return 1;

        case WM_NCHITTEST: {
            if (!IsZoomed(hwnd)) {
                POINT ptScreen = { GET_X_LPARAM(lParam), GET_Y_LPARAM(lParam) };
                RECT rcWin;
                GetWindowRect(hwnd, &rcWin);

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

                if (it != g_titleBarLayouts.end()) {
                    for (const auto& rc : it->second.controlRects) {
                        if (pt.x >= rc.left && pt.x < rc.right &&
                            pt.y >= rc.top && pt.y < rc.bottom) {
                            return HTCLIENT;
                        }
                    }
                }
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

extern "C" {

// Methods
JNIEXPORT void JNICALL Java_fasttheme_FastTheme_addTitleBarControlRect(JNIEnv* env, jclass clazz, jlong hwndLong, jint x, jint y, jint w, jint h) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return;
    RECT rc = { x, y, x + w, y + h };
    std::lock_guard<std::mutex> lock(g_titleBarMutex);
    g_titleBarLayouts[hwnd].controlRects.push_back(rc);
}

JNIEXPORT void JNICALL Java_fasttheme_FastTheme_clearTitleBarControlRects(JNIEnv* env, jclass clazz, jlong hwndLong) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return;
    std::lock_guard<std::mutex> lock(g_titleBarMutex);
    auto it = g_titleBarLayouts.find(hwnd);
    if (it != g_titleBarLayouts.end()) {
        it->second.controlRects.clear();
    }
}

JNIEXPORT void JNICALL Java_fasttheme_FastTheme_closeWindow(JNIEnv* env, jclass clazz, jlong hwndLong) {
    HWND hwnd = (HWND)hwndLong;
    if (IsWindow(hwnd)) {
        SendMessage(hwnd, WM_CLOSE, 0, 0);
    }
}

JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_enableMica(JNIEnv* env, jclass clazz, jlong hwndLong, jboolean enabled) {
    return Java_fasttheme_FastTheme_setSystemBackdropType(env, clazz, hwndLong, enabled ? DWMSBT_MAINWINDOW : DWMSBT_NONE);
}

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

JNIEXPORT void JNICALL Java_fasttheme_FastTheme_maximizeWindow(JNIEnv* env, jclass clazz, jlong hwndLong) {
    HWND hwnd = (HWND)hwndLong;
    if (IsWindow(hwnd)) {
        SendMessage(hwnd, WM_SYSCOMMAND, SC_MAXIMIZE, 0);
    }
}

JNIEXPORT void JNICALL Java_fasttheme_FastTheme_minimizeWindow(JNIEnv* env, jclass clazz, jlong hwndLong) {
    HWND hwnd = (HWND)hwndLong;
    if (IsWindow(hwnd)) {
        SendMessage(hwnd, WM_SYSCOMMAND, SC_MINIMIZE, 0);
    }
}

JNIEXPORT void JNICALL Java_fasttheme_FastTheme_restoreWindow(JNIEnv* env, jclass clazz, jlong hwndLong) {
    HWND hwnd = (HWND)hwndLong;
    if (IsWindow(hwnd)) {
        SendMessage(hwnd, WM_SYSCOMMAND, SC_RESTORE, 0);
    }
}

JNIEXPORT void JNICALL Java_fasttheme_FastTheme_sendSysCommand(JNIEnv* env, jclass clazz, jlong hwndLong, jint cmd) {
    HWND hwnd = (HWND)hwndLong;
    if (IsWindow(hwnd)) {
        SendMessage(hwnd, WM_SYSCOMMAND, (WPARAM)cmd, 0);
    }
}

// Getters
JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_isSystemDarkMode(JNIEnv* env, jclass clazz) {
    HKEY hKey;
    if (RegOpenKeyExA(HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize", 0, KEY_READ, &hKey) == ERROR_SUCCESS) {
        DWORD value = 1;
        DWORD size = sizeof(value);
        if (RegQueryValueExA(hKey, "SystemUsesLightTheme", NULL, NULL, (LPBYTE)&value, &size) == ERROR_SUCCESS) {
            RegCloseKey(hKey);
            return (value == 0) ? JNI_TRUE : JNI_FALSE;
        }
        RegCloseKey(hKey);
    }
    return IsDarkModeEnabled() ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_isAppDarkMode(JNIEnv* env, jclass clazz) {
    return IsDarkModeEnabled() ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jint JNICALL Java_fasttheme_FastTheme_getAccentColor(JNIEnv* env, jclass clazz) {
    // 1. Try Windows 10/11 DWM AccentColor (ABGR DWORD in registry)
    HKEY hKey;
    if (RegOpenKeyExA(HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\DWM", 0, KEY_READ, &hKey) == ERROR_SUCCESS) {
        DWORD accentColor = 0;
        DWORD size = sizeof(accentColor);
        if (RegQueryValueExA(hKey, "AccentColor", NULL, NULL, (LPBYTE)&accentColor, &size) == ERROR_SUCCESS && accentColor != 0) {
            RegCloseKey(hKey);
            // AccentColor is stored as 0xAABBGGRR. Convert to ARGB: 0xAARRGGBB
            DWORD a = (accentColor >> 24) & 0xFF;
            if (a == 0) a = 0xFF;
            DWORD b = (accentColor >> 16) & 0xFF;
            DWORD g = (accentColor >> 8) & 0xFF;
            DWORD r = accentColor & 0xFF;
            return (jint)((a << 24) | (r << 16) | (g << 8) | b);
        }
        RegCloseKey(hKey);
    }

    // 2. Try native DwmGetColorizationColor (returns ARGB)
    DWORD color = 0;
    BOOL opaque = FALSE;
    HRESULT hr = DwmGetColorizationColor(&color, &opaque);
    if (SUCCEEDED(hr) && color != 0) {
        // Ensure alpha channel is visible (DwmGetColorizationColor often returns 0x00RRGGBB or low alpha)
        DWORD a = (color >> 24) & 0xFF;
        if (a < 50) color |= 0xFF000000;
        return (jint)color;
    }

    // 3. Fallback to DWM ColorizationColor
    if (RegOpenKeyExA(HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\DWM", 0, KEY_READ, &hKey) == ERROR_SUCCESS) {
        DWORD regColor = 0;
        DWORD size = sizeof(regColor);
        if (RegQueryValueExA(hKey, "ColorizationColor", NULL, NULL, (LPBYTE)&regColor, &size) == ERROR_SUCCESS && regColor != 0) {
            RegCloseKey(hKey);
            if (((regColor >> 24) & 0xFF) < 50) regColor |= 0xFF000000;
            return (jint)regColor;
        }
        RegCloseKey(hKey);
    }

    return (jint)0xFF0078D7; // Default Windows Blue
}

JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_isColorizationOpaque(JNIEnv* env, jclass clazz) {
    DWORD color = 0;
    BOOL opaque = FALSE;
    HRESULT hr = DwmGetColorizationColor(&color, &opaque);
    if (SUCCEEDED(hr)) {
        return opaque ? JNI_TRUE : JNI_FALSE;
    }
    return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_isHighContrast(JNIEnv* env, jclass clazz) {
    HIGHCONTRASTA hc;
    hc.cbSize = sizeof(HIGHCONTRASTA);
    if (SystemParametersInfoA(SPI_GETHIGHCONTRAST, sizeof(HIGHCONTRASTA), &hc, 0)) {
        return (hc.dwFlags & HCF_HIGHCONTRASTON) ? JNI_TRUE : JNI_FALSE;
    }
    return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_isTransparencyEnabled(JNIEnv* env, jclass clazz) {
    HKEY hKey;
    if (RegOpenKeyExA(HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize", 0, KEY_READ, &hKey) == ERROR_SUCCESS) {
        DWORD value = 1;
        DWORD size = sizeof(value);
        if (RegQueryValueExA(hKey, "EnableTransparency", NULL, NULL, (LPBYTE)&value, &size) == ERROR_SUCCESS) {
            RegCloseKey(hKey);
            return (value != 0) ? JNI_TRUE : JNI_FALSE;
        }
        RegCloseKey(hKey);
    }
    return JNI_TRUE;
}

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

// Setters
JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setAlwaysOnTop(JNIEnv* env, jclass clazz, jlong hwndLong, jboolean alwaysOnTop) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;

    HWND insertAfter = alwaysOnTop ? HWND_TOPMOST : HWND_NOTOPMOST;
    BOOL res = SetWindowPos(hwnd, insertAfter, 0, 0, 0, 0,
                            SWP_NOMOVE | SWP_NOSIZE | SWP_NOACTIVATE);
    return res ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setBorderlessShadow(JNIEnv* env, jclass clazz, jlong hwndLong, jboolean enabled) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;

    if (enabled) {
        BOOL darkMode = TRUE;
        DwmSetWindowAttribute(hwnd, DWMWA_USE_IMMERSIVE_DARK_MODE, &darkMode, sizeof(darkMode));

        LONG style = GetWindowLong(hwnd, GWL_STYLE);
        style &= ~WS_POPUP;
        style |= WS_OVERLAPPEDWINDOW;
        SetWindowLong(hwnd, GWL_STYLE, style);

        LONG exStyle = GetWindowLong(hwnd, GWL_EXSTYLE);
        exStyle |= WS_EX_APPWINDOW;
        exStyle &= ~WS_EX_TOOLWINDOW;
        SetWindowLong(hwnd, GWL_EXSTYLE, exStyle);

        SetClassLongPtr(hwnd, GCLP_HBRBACKGROUND, (LONG_PTR)GetStockObject(BLACK_BRUSH));

        if (!GetPropW(hwnd, L"FastTheme_OldProc")) {
            WNDPROC oldProc = (WNDPROC)SetWindowLongPtr(hwnd, GWLP_WNDPROC, (LONG_PTR)OverlaySubclassProc);
            SetPropW(hwnd, L"FastTheme_OldProc", (HANDLE)oldProc);
        }

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

JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setCornerStyle(JNIEnv* env, jclass clazz, jlong hwndLong, jint style) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;
    HRESULT hr = DwmSetWindowAttribute(hwnd, DWMWA_WINDOW_CORNER_PREFERENCE, &style, sizeof(style));
    return SUCCEEDED(hr) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setTitleBarThemeColors(JNIEnv* env, jclass clazz, jlong hwndLong,
        jint inactiveBg, jint activeBg,
        jint glyphActive, jint glyphInactive, jint glyphInactiveHover,
        jint inactiveBtnHover, jint inactiveBtnPressed,
        jint activeBtnHover, jint activeBtnPressed,
        jint closeHover, jint closePressed, jint closeHoverGlyph) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;

    auto toColorRef = [](jint c) -> COLORREF {
        return RGB((c >> 16) & 0xFF, (c >> 8) & 0xFF, c & 0xFF);
    };

    {
        std::lock_guard<std::mutex> lock(g_titleBarMutex);
        auto& pal = g_titleBarLayouts[hwnd].palette;
        pal.inactiveBg         = toColorRef(inactiveBg);
        pal.activeBg           = toColorRef(activeBg);
        pal.glyphActive        = toColorRef(glyphActive);
        pal.glyphInactive      = toColorRef(glyphInactive);
        pal.glyphInactiveHover = toColorRef(glyphInactiveHover);
        pal.inactiveBtnHover   = toColorRef(inactiveBtnHover);
        pal.inactiveBtnPressed = toColorRef(inactiveBtnPressed);
        pal.activeBtnHover     = toColorRef(activeBtnHover);
        pal.activeBtnPressed   = toColorRef(activeBtnPressed);
        pal.activeCloseHover   = toColorRef(closeHover);
        pal.activeClosePressed = toColorRef(closePressed);
        pal.closeHoverGlyph    = toColorRef(closeHoverGlyph);
    }

    RedrawWindow(hwnd, NULL, NULL, RDW_INVALIDATE | RDW_ERASE | RDW_UPDATENOW);
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setNativeTitleBarButtonsEnabled(JNIEnv* env, jclass clazz, jlong hwndLong, jboolean enabled, jint buttonWidth) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;
    {
        std::lock_guard<std::mutex> lock(g_titleBarMutex);
        auto& layout = g_titleBarLayouts[hwnd];
        layout.nativeButtonsEnabled = (enabled == JNI_TRUE);
        if (buttonWidth > 0) {
            layout.buttonWidth = buttonWidth;
        }
    }
    SetWindowPos(hwnd, NULL, 0, 0, 0, 0,
                 SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_NOACTIVATE | SWP_FRAMECHANGED);
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setOverlayDragHeight(JNIEnv* env, jclass clazz, jlong hwndLong, jint height) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;
    SetPropW(hwnd, L"FastTheme_DragHeight", (HANDLE)(INT_PTR)height);
    {
        std::lock_guard<std::mutex> lock(g_titleBarMutex);
        g_titleBarLayouts[hwnd].height = height;
    }
    SetWindowPos(hwnd, NULL, 0, 0, 0, 0,
                 SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_NOACTIVATE | SWP_FRAMECHANGED);
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setSystemBackdropType(JNIEnv* env, jclass clazz, jlong hwndLong, jint type) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;

    int backdrop = type;
    HRESULT hr = DwmSetWindowAttribute(hwnd, DWMWA_SYSTEMBACKDROP_TYPE, &backdrop, sizeof(backdrop));
    if (FAILED(hr)) {
        if (type == DWMSBT_MAINWINDOW) {
            BOOL micaVal = TRUE;
            hr = DwmSetWindowAttribute(hwnd, DWMWA_MICA_EFFECT, &micaVal, sizeof(micaVal));
        } else if (type == DWMSBT_NONE) {
            BOOL micaVal = FALSE;
            hr = DwmSetWindowAttribute(hwnd, DWMWA_MICA_EFFECT, &micaVal, sizeof(micaVal));
        }
    }

    if (SUCCEEDED(hr) && type != DWMSBT_NONE) {
        COLORREF transparentColor = DWMWA_COLOR_NONE;
        DwmSetWindowAttribute(hwnd, DWMWA_CAPTION_COLOR, &transparentColor, sizeof(transparentColor));
        DwmSetWindowAttribute(hwnd, DWMWA_BORDER_COLOR, &transparentColor, sizeof(transparentColor));

        if (FastThemeDebug()) {
            BOOL dark = FALSE;
            DwmGetWindowAttribute(hwnd, DWMWA_USE_IMMERSIVE_DARK_MODE, &dark, sizeof(dark));
            fprintf(stderr, "[FastTheme] setSystemBackdropType hwnd=%p type=%d hr=0x%08lX darkMode=%d\n",
                    (void*)hwnd, type, (unsigned long)hr, (int)dark);
            fflush(stderr);
        }

        MARGINS margins = { -1, -1, -1, -1 };
        DwmExtendFrameIntoClientArea(hwnd, &margins);

        SetClassLongPtr(hwnd, GCLP_HBRBACKGROUND, (LONG_PTR)GetStockObject(NULL_BRUSH));

        BufferedPaintInit();

        {
            std::lock_guard<std::mutex> lock(g_titleBarMutex);
            g_titleBarLayouts[hwnd].backdropType = type;
        }

        if (!GetPropW(hwnd, L"FastTheme_BackdropOldProc")) {
            WNDPROC oldProc = (WNDPROC)SetWindowLongPtr(hwnd, GWLP_WNDPROC, (LONG_PTR)BackdropSubclassProc);
            SetPropW(hwnd, L"FastTheme_BackdropOldProc", (HANDLE)oldProc);
        }

        HDC hdc = GetDC(hwnd);
        if (hdc) {
            RECT rc;
            GetClientRect(hwnd, &rc);
            HBRUSH blackBrush = (HBRUSH)GetStockObject(BLACK_BRUSH);
            FillRect(hdc, &rc, blackBrush);
            ReleaseDC(hwnd, hdc);
        }
    } else if (type == DWMSBT_NONE) {
        WNDPROC oldProc = (WNDPROC)GetPropW(hwnd, L"FastTheme_BackdropOldProc");
        if (oldProc) {
            SetWindowLongPtr(hwnd, GWLP_WNDPROC, (LONG_PTR)oldProc);
            RemovePropW(hwnd, L"FastTheme_BackdropOldProc");
        }
    }

    SetWindowPos(hwnd, NULL, 0, 0, 0, 0, SWP_FRAMECHANGED | SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_NOACTIVATE);
    DwmFlush();
    RedrawWindow(hwnd, NULL, NULL, RDW_ERASE | RDW_INVALIDATE | RDW_FRAME | RDW_ALLCHILDREN | RDW_UPDATENOW);
    return SUCCEEDED(hr) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setTitleBarColor(JNIEnv* env, jclass clazz, jlong hwndLong, jint r, jint g, jint b) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;
    COLORREF color = RGB(r, g, b);
    HRESULT hr = DwmSetWindowAttribute(hwnd, 35, &color, sizeof(color));
    return SUCCEEDED(hr) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setTitleBarDarkMode(JNIEnv* env, jclass clazz, jlong hwndLong, jboolean enabled) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;
    BOOL darkMode = enabled ? TRUE : FALSE;
    HRESULT hr = DwmSetWindowAttribute(hwnd, DWMWA_USE_IMMERSIVE_DARK_MODE, &darkMode, sizeof(darkMode));
    return SUCCEEDED(hr) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setTitleBarHeight(JNIEnv* env, jclass clazz, jlong hwndLong, jint height) {
    return Java_fasttheme_FastTheme_setOverlayDragHeight(env, clazz, hwndLong, height);
}

JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setTitleBarTextColor(JNIEnv* env, jclass clazz, jlong hwndLong, jint r, jint g, jint b) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;
    COLORREF color = RGB(r, g, b);
    HRESULT hr = DwmSetWindowAttribute(hwnd, 36, &color, sizeof(color));
    return SUCCEEDED(hr) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setWindowBackgroundColor(JNIEnv* env, jclass clazz, jlong hwndLong, jint r, jint g, jint b) {
    HWND hwnd = (HWND)hwndLong;
    if (!IsWindow(hwnd)) return JNI_FALSE;

    HBRUSH hNewBrush = CreateSolidBrush(RGB(r, g, b));
    if (!hNewBrush) return JNI_FALSE;

    HBRUSH hOldCustomBrush = (HBRUSH)GetPropW(hwnd, L"FastTheme_CustomBgBrush");
    if (hOldCustomBrush != NULL) {
        DeleteObject(hOldCustomBrush);
    }
    SetPropW(hwnd, L"FastTheme_CustomBgBrush", (HANDLE)hNewBrush);

    HBRUSH hPrevClassBrush = (HBRUSH)SetClassLongPtr(hwnd, GCLP_HBRBACKGROUND, (LONG_PTR)hNewBrush);
    if (hPrevClassBrush != NULL && hPrevClassBrush != hOldCustomBrush && hPrevClassBrush != (HBRUSH)GetStockObject(BLACK_BRUSH) && hPrevClassBrush != (HBRUSH)GetStockObject(WHITE_BRUSH)) {
        DeleteObject(hPrevClassBrush);
    }

    RedrawWindow(hwnd, NULL, NULL, RDW_ERASE | RDW_INVALIDATE | RDW_FRAME | RDW_ALLCHILDREN);
    return JNI_TRUE;
}

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

} // extern "C"
