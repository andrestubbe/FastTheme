package fasttheme.demo;

import fastdirectx.DirectXBackend;
import fastdisplay.FastDisplay;
import fastgraphics.backend.BackendTexture;
import fastgraphics.g2d.FastGraphics2D;
import fastgraphics.util.ImageUtil;
import fasttheme.FastTheme;
import fastwindow.FastNativeWindow;
import fastwindow.FastWindow;

import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

/**
 * <h1>FastGraphics Full — Full-Bleed Desktop Image with 32px Buttons</h1>
 *
 * <p>
 * <ul>
 *   <li>100% full-bleed Desktop-Screenshot via FastGraphics</li>
 *   <li>Min, Max, Close Buttons (32px) floating directly on top of the DirectX image</li>
 *   <li>Native Win32 window dragging outside buttons</li>
 * </ul>
 * </p>
 */
public class DemoFull {

    private static final MethodHandle MH_GET_CURSOR_POS;
    private static final MethodHandle MH_SCREEN_TO_CLIENT;
    private static final MethodHandle MH_GET_ASYNC_KEY_STATE;
    private static final MethodHandle MH_RELEASE_CAPTURE;
    private static final MethodHandle MH_SEND_MESSAGE;
    private static final MethodHandle MH_IS_ZOOMED;

    static {
        Linker linker = Linker.nativeLinker();
        SymbolLookup u32 = SymbolLookup.libraryLookup("user32.dll", Arena.global());
        MH_GET_CURSOR_POS = linker.downcallHandle(u32.find("GetCursorPos").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
        MH_SCREEN_TO_CLIENT = linker.downcallHandle(u32.find("ScreenToClient").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
        MH_GET_ASYNC_KEY_STATE = linker.downcallHandle(u32.find("GetAsyncKeyState").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_SHORT, ValueLayout.JAVA_INT));
        MH_RELEASE_CAPTURE = linker.downcallHandle(u32.find("ReleaseCapture").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_INT));
        MH_SEND_MESSAGE = linker.downcallHandle(u32.find("SendMessageW").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG));
        MH_IS_ZOOMED = linker.downcallHandle(u32.find("IsZoomed").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG));
    }

    public static void main(String[] args) throws Exception {
        int width = 1100;
        int height = 700;

        FastDisplay display = new FastDisplay();
        int scale = Math.max(100, display.getScale());
        double scaleFactor = scale / 100.0;
        int btnH = (int) Math.round(32.0 * scaleFactor);
        int btnW = (int) Math.round(46.0 * scaleFactor);

        Rectangle screenBounds = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration().getBounds();
        BufferedImage screenshot = new Robot().createScreenCapture(screenBounds);

        try (FastNativeWindow window = FastWindow.create("FastGraphics Full", width, height);
             DirectXBackend backend = new DirectXBackend()) {

            window.centerOnScreen();
            long hwnd = window.getHWND();

            // Entfernt Standard-OS-Titelleiste (WM_NCCALCSIZE = 0) und aktiviert DWM-Frame
            FastTheme.applyFluentTitleBar(hwnd, btnH, btnW);

            backend.initialize(hwnd, width, height);
            FastGraphics2D g2d = new FastGraphics2D(backend, width, height);
            BackendTexture bgTexture = backend.createTexture(screenshot.getWidth(), screenshot.getHeight(),
                    ImageUtil.toRgbaBuffer(screenshot));

            final int[] dims = new int[] { width, height };
            final boolean[] prevLDown = new boolean[] { false };

            fastwindow.WindowPaintListener renderFrame = (curW, curH) -> {
                if (curW <= 0 || curH <= 0) return;

                if (curW != dims[0] || curH != dims[1]) {
                    backend.resize(curW, curH);
                    g2d.updateDimensions(curW, curH);
                    dims[0] = curW;
                    dims[1] = curH;
                }

                // Maus-Position ermitteln
                int mx = -1, my = -1;
                try (Arena arena = Arena.ofConfined()) {
                    MemorySegment pt = arena.allocate(8, 4);
                    if ((int) MH_GET_CURSOR_POS.invokeExact(pt) != 0 && (int) MH_SCREEN_TO_CLIENT.invokeExact(hwnd, pt) != 0) {
                        mx = pt.get(ValueLayout.JAVA_INT, 0);
                        my = pt.get(ValueLayout.JAVA_INT, 4);
                    }
                } catch (Throwable ignored) {}

                boolean lDown = false;
                try {
                    lDown = (((short) MH_GET_ASYNC_KEY_STATE.invokeExact(0x01)) & 0x8000) != 0;
                } catch (Throwable ignored) {}

                boolean inHeaderY = (my >= 0 && my < btnH);
                boolean hoverClose = inHeaderY && (mx >= curW - btnW && mx < curW);
                boolean hoverMax   = inHeaderY && (mx >= curW - 2 * btnW && mx < curW - btnW);
                boolean hoverMin   = inHeaderY && (mx >= curW - 3 * btnW && mx < curW - 2 * btnW);

                boolean isZoomed = false;
                try {
                    isZoomed = ((int) MH_IS_ZOOMED.invokeExact(hwnd)) != 0;
                } catch (Throwable ignored) {}

                // Klick auf Buttons oder Titelleiste zum Ziehen
                if (lDown && !prevLDown[0] && inHeaderY) {
                    if (hoverClose) {
                        window.close();
                        return;
                    } else if (hoverMax) {
                        if (isZoomed) window.restore(); else window.maximize();
                    } else if (hoverMin) {
                        window.minimize();
                    } else if (mx < curW - 3 * btnW) {
                        // Dragging via Win32 WM_NCLBUTTONDOWN (HTCAPTION)
                        try {
                            MH_RELEASE_CAPTURE.invokeExact();
                            MH_SEND_MESSAGE.invokeExact(hwnd, 0x00A1, 2L, 0L);
                        } catch (Throwable ignored) {}
                    }
                }
                prevLDown[0] = lDown;

                // ── FastGraphics Frame ──────────────────────────────
                g2d.begin();

                // 1. Screenshot über 100% des Fensters
                g2d.clear(0, 0, 0, 1);
                g2d.drawImage(bgTexture, 0, 0, (float) curW, (float) curH);

                // 2. Schwebende Caption-Buttons (Min, Max, Close)
                // Minimieren (―)
                float minX = curW - 3 * btnW;
                if (hoverMin) {
                    g2d.setColor(1.0f, 1.0f, 1.0f, 0.22f);
                    g2d.fillRect(minX, 0, (float) btnW, (float) btnH);
                }
                g2d.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                float midMinX = minX + btnW * 0.5f;
                float midY = btnH * 0.5f;
                g2d.drawLine(midMinX - 5.0f, midY, midMinX + 5.0f, midY, 1.0f);

                // Maximieren / Restore (□ / ❐)
                float maxX = curW - 2 * btnW;
                if (hoverMax) {
                    g2d.setColor(1.0f, 1.0f, 1.0f, 0.22f);
                    g2d.fillRect(maxX, 0, (float) btnW, (float) btnH);
                }
                g2d.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                float midMaxX = maxX + btnW * 0.5f;
                if (isZoomed) {
                    g2d.drawRect(midMaxX - 3.0f, midY - 5.0f, 7.0f, 7.0f);
                    g2d.drawRect(midMaxX - 5.0f, midY - 3.0f, 7.0f, 7.0f);
                } else {
                    g2d.drawRect(midMaxX - 5.0f, midY - 5.0f, 10.0f, 10.0f);
                }

                // Schließen (✕)
                float closeX = curW - btnW;
                if (hoverClose) {
                    g2d.setColor(0.91f, 0.07f, 0.14f, 0.95f); // Windows 11 Rot
                    g2d.fillRect(closeX, 0, (float) btnW, (float) btnH);
                    g2d.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                } else {
                    g2d.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                }
                float midCloseX = closeX + btnW * 0.5f;
                g2d.drawLine(midCloseX - 4.5f, midY - 4.5f, midCloseX + 4.5f, midY + 4.5f, 1.2f);
                g2d.drawLine(midCloseX - 4.5f, midY + 4.5f, midCloseX + 4.5f, midY - 4.5f, 1.2f);

                g2d.end();
            };

            window.setPaintListener(renderFrame);
            renderFrame.onPaint(width, height);
            window.setVisible(true);

            while (window.isOpen() && window.pollEvents()) {
                renderFrame.onPaint(window.getWidth(), window.getHeight());
                try {
                    Thread.sleep(12);
                } catch (InterruptedException ignored) {}
            }

            backend.destroyTexture(bgTexture);
            g2d.dispose();
        }
    }
}
