package fasttheme.demo;

import fasttheme.FastTheme;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;

/**
 * FastTheme DemoFluent — 100% Windows 11 Fluent Native Window Showcase:
 * - Full native Windows 11 DWM animations (Minimize to taskbar, Maximize/Zoom, Restore) via WM_SYSCOMMAND
 * - Preserves standard app window semantics (WS_OVERLAPPEDWINDOW + WS_EX_APPWINDOW)
 * - Seamless borderless appearance via WM_NCCALCSIZE = 0
 * - 8px native resize borders and corners
 * - DPI-aware 47px title bar and caption buttons
 * - Immediate Mica backdrop activation with rounded corners and native drop shadow
 */
public class DemoFluent {

    private static final int BASE_TITLE_BAR_HEIGHT = 47;
    private static final int BASE_BTN_WIDTH = 48;
    private static final int BASE_BTN_HEIGHT = 47;

    static {
        // Ensure local build DLL is resolved for demo execution
        java.io.File local = new java.io.File("build/fasttheme.dll");
        java.io.File parent = new java.io.File("../../build/fasttheme.dll");
        java.io.File actualFile = local.exists() ? local : parent;
        if (actualFile.exists()) {
            System.load(actualFile.getAbsolutePath());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("FastTheme - Fluent Window");

            // Detect desktop display DPI scaling factor (e.g. 1.0, 1.25, 1.5, 2.0)
            double scale = 1.0;
            if (frame.getGraphicsConfiguration() != null) {
                AffineTransform transform = frame.getGraphicsConfiguration().getDefaultTransform();
                scale = Math.max(transform.getScaleX(), transform.getScaleY());
            }
            if (scale <= 0.0) {
                scale = Toolkit.getDefaultToolkit().getScreenResolution() / 96.0;
            }
            final double dpiScale = Math.max(1.0, scale);

            int titleBarHeight = (int) Math.round(BASE_TITLE_BAR_HEIGHT * dpiScale);
            int btnWidth = (int) Math.round(BASE_BTN_WIDTH * dpiScale);
            int btnHeight = (int) Math.round(BASE_BTN_HEIGHT * dpiScale);

            frame.setSize((int) Math.round(1020 * dpiScale), (int) Math.round(640 * dpiScale));
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JPanel root = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    // Fill with transparent or subtle tint so Swing repaints cleanly over DWM Mica
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(new Color(20, 20, 24, 180)); // Fluent Mica tint
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.dispose();
                }
            };
            root.setOpaque(false);

            // Custom Title Bar Panel (47px high * dpiScale)
            JPanel titleBar = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(new Color(28, 28, 34, 120)); // Title bar tint
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.dispose();
                }
            };
            titleBar.setPreferredSize(new Dimension(frame.getWidth(), titleBarHeight));
            titleBar.setOpaque(false);

            // Title label
            JLabel titleLabel = new JLabel("  FastTheme — Windows 11 Fluent Mica Material");
            titleLabel.setFont(new Font("Segoe UI Variable Display", Font.PLAIN, (int) Math.round(13 * dpiScale)));
            titleLabel.setForeground(new Color(220, 220, 220));
            titleBar.add(titleLabel, BorderLayout.WEST);

            // System Caption Buttons Panel (Minimize, Maximize/Restore, Close)
            JPanel captionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            captionButtonsPanel.setOpaque(false);
            captionButtonsPanel.setPreferredSize(new Dimension(btnWidth * 3, btnHeight));

            CaptionButton btnMinimize = new CaptionButton("─", btnWidth, btnHeight, dpiScale, false);
            btnMinimize.setToolTipText("Minimize");

            CaptionButton btnMaximize = new CaptionButton("□", btnWidth, btnHeight, dpiScale, false);
            btnMaximize.setToolTipText("Maximize");

            CaptionButton btnClose = new CaptionButton("✕", btnWidth, btnHeight, dpiScale, true);
            btnClose.setToolTipText("Close");

            captionButtonsPanel.add(btnMinimize);
            captionButtonsPanel.add(btnMaximize);
            captionButtonsPanel.add(btnClose);

            titleBar.add(captionButtonsPanel, BorderLayout.EAST);

            root.add(titleBar, BorderLayout.NORTH);

            // Center Content
            JPanel centerPanel = new JPanel(new BorderLayout());
            centerPanel.setOpaque(false);
            JLabel centerLabel = new JLabel("<html><div style='text-align: center;'><span style='font-size: 20px; font-weight: 600;'>Windows 11 Mica Material</span><br><br><span style='color: #888888;'>Native Win32 DWM Backdrop &bull; Preserved Window Animations &bull; Snap Layouts</span></div></html>", SwingConstants.CENTER);
            centerLabel.setForeground(new Color(240, 240, 240));
            centerPanel.add(centerLabel, BorderLayout.CENTER);
            root.add(centerPanel, BorderLayout.CENTER);

            frame.setContentPane(root);
            frame.getRootPane().setOpaque(false);

            // Realize native HWND before making visible
            frame.addNotify();
            long hwnd = FastTheme.getWindowHandle(frame);

            // Wire caption buttons to native WM_SYSCOMMAND (ensures full Windows 11 DWM animations!)
            btnMinimize.addActionListener(e -> {
                if (hwnd != 0) {
                    FastTheme.minimizeWindow(hwnd);
                } else {
                    frame.setState(JFrame.ICONIFIED);
                }
            });

            btnMaximize.addActionListener(e -> {
                if (hwnd != 0) {
                    boolean isMaximized = (frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH;
                    if (isMaximized) {
                        FastTheme.restoreWindow(hwnd);
                    } else {
                        FastTheme.maximizeWindow(hwnd);
                    }
                } else {
                    int state = frame.getExtendedState();
                    if ((state & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) {
                        frame.setExtendedState(JFrame.NORMAL);
                        btnMaximize.setGlyph("□");
                    } else {
                        frame.setExtendedState(state | JFrame.MAXIMIZED_BOTH);
                        btnMaximize.setGlyph("❐");
                    }
                }
            });

            btnClose.addActionListener(e -> {
                if (hwnd != 0) {
                    FastTheme.closeWindow(hwnd);
                } else {
                    frame.dispose();
                }
            });

            Runnable updateHitTest = () -> {
                if (hwnd == 0) return;
                FastTheme.clearTitleBarControlRects(hwnd);

                // Exclude caption buttons (Minimize, Maximize, Close) so clicks and hovers reach Swing
                java.awt.Point pMin = SwingUtilities.convertPoint(btnMinimize, 0, 0, frame);
                FastTheme.addTitleBarControlRect(hwnd, pMin.x, pMin.y, btnMinimize.getWidth(), btnMinimize.getHeight());

                java.awt.Point pMax = SwingUtilities.convertPoint(btnMaximize, 0, 0, frame);
                FastTheme.addTitleBarControlRect(hwnd, pMax.x, pMax.y, btnMaximize.getWidth(), btnMaximize.getHeight());

                java.awt.Point pClose = SwingUtilities.convertPoint(btnClose, 0, 0, frame);
                FastTheme.addTitleBarControlRect(hwnd, pClose.x, pClose.y, btnClose.getWidth(), btnClose.getHeight());
            };

            if (hwnd != 0) {
                // 1. Borderless + Native Shadow (keeps WS_OVERLAPPEDWINDOW for DWM animations)
                FastTheme.setBorderlessShadow(hwnd, true);

                // 2. Title bar height for native dragging & snapping
                FastTheme.setTitleBarHeight(hwnd, titleBarHeight);

                // 3. Rounded corners and Dark mode
                FastTheme.setCornerStyle(hwnd, 2); // Rounded
                FastTheme.setTitleBarDarkMode(hwnd, true);

                // 4. Mica effect
                FastTheme.enableMica(hwnd, true);

                frame.addComponentListener(new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        boolean maximized = (frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH;
                        btnMaximize.setGlyph(maximized ? "❐" : "□");
                        updateHitTest.run();
                    }
                });

                updateHitTest.run();
            }

            frame.setVisible(true);

            if (hwnd != 0) {
                // Force DWM frame re-evaluation and commit once window is visible
                FastTheme.forceFrameUpdate(hwnd);

                // Ensure hit-test rects are matched to final post-realization component layout
                SwingUtilities.invokeLater(() -> {
                    updateHitTest.run();
                    frame.revalidate();
                    frame.repaint();
                });
            }
        });
    }

    /**
     * Modern Windows 11 Photos-Style Caption Button:
     * - Configurable size (48x47px scaled)
     * - High-DPI font rendering
     * - Interactive hover & pressed feedback (subtle dark hover, red for close)
     */
    private static class CaptionButton extends JButton {
        private final boolean isClose;
        private final double dpiScale;
        private String glyph;
        private boolean isHovered = false;
        private boolean isPressed = false;

        public CaptionButton(String glyph, int width, int height, double dpiScale, boolean isClose) {
            this.glyph = glyph;
            this.dpiScale = dpiScale;
            this.isClose = isClose;

            setPreferredSize(new Dimension(width, height));
            setMinimumSize(new Dimension(width, height));
            setMaximumSize(new Dimension(width, height));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getDefaultCursor());

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    isPressed = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        isPressed = true;
                        repaint();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    isPressed = false;
                    repaint();
                }
            });
        }

        public void setGlyph(String glyph) {
            this.glyph = glyph;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Background state
            if (isPressed) {
                g2.setColor(isClose ? new Color(241, 112, 122) : new Color(60, 60, 68));
                g2.fillRect(0, 0, w, h);
            } else if (isHovered) {
                g2.setColor(isClose ? new Color(232, 17, 35) : new Color(48, 48, 56));
                g2.fillRect(0, 0, w, h);
            }

            // Foreground glyph
            g2.setColor(Color.WHITE);
            int fontSize = (int) Math.round(13 * dpiScale);
            g2.setFont(new Font("Segoe UI Symbol", Font.PLAIN, fontSize));

            java.awt.FontMetrics fm = g2.getFontMetrics();
            int strW = fm.stringWidth(glyph);
            int strH = fm.getAscent() - fm.getDescent();
            int x = (w - strW) / 2;
            int y = (h + strH) / 2;

            g2.drawString(glyph, x, y);
            g2.dispose();
        }
    }
}
