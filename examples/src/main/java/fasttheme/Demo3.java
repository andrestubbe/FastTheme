package fasttheme;

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
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;

/**
 * FastTheme Demo 3 — Windows 11 Modern Photos-Style Chrome Showcase:
 * - 94px custom interactive title bar (DPI scaled)
 * - Seamless native window resizing across all borders and corners
 * - Native desktop DPI scaling awareness (e.g. 100%, 150%, 200%)
 * - 96x94px interactive caption buttons (Minimize, Maximize/Restore, Close)
 * - Hit-test exclusion zones for controls (buttons, tabs, search)
 * - Native Mica backdrop with rounded corners and drop shadow
 */
public class Demo3 {

    private static final int BASE_TITLE_BAR_HEIGHT = 47;
    private static final int BASE_BTN_WIDTH = 48;
    private static final int BASE_BTN_HEIGHT = 47;

    static {
        // Ensure local build DLL is resolved for demo execution
        java.io.File local = new java.io.File("build/fasttheme.dll");
        java.io.File parent = new java.io.File("../build/fasttheme.dll");
        java.io.File actualFile = local.exists() ? local : parent;
        if (actualFile.exists()) {
            System.load(actualFile.getAbsolutePath());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("FastTheme Demo 3 - Modern Chrome");

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

            JPanel root = new JPanel(new BorderLayout());
            root.setBackground(new Color(24, 24, 28));

            // Custom Title Bar Panel (47px high * dpiScale)
            JPanel titleBar = new JPanel(new BorderLayout());
            titleBar.setPreferredSize(new Dimension(frame.getWidth(), titleBarHeight));
            titleBar.setBackground(new Color(32, 32, 38));

            // System Caption Buttons Panel (Minimize, Maximize/Restore, Close)
            JPanel captionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            captionButtonsPanel.setOpaque(false);
            captionButtonsPanel.setPreferredSize(new Dimension(btnWidth * 3, btnHeight));

            CaptionButton btnMinimize = new CaptionButton("─", btnWidth, btnHeight, dpiScale, false);
            btnMinimize.setToolTipText("Minimize");
            btnMinimize.addActionListener(e -> frame.setState(JFrame.ICONIFIED));

            CaptionButton btnMaximize = new CaptionButton("□", btnWidth, btnHeight, dpiScale, false);
            btnMaximize.setToolTipText("Maximize");
            btnMaximize.addActionListener(e -> {
                int state = frame.getExtendedState();
                if ((state & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) {
                    frame.setExtendedState(JFrame.NORMAL);
                    btnMaximize.setGlyph("□");
                } else {
                    frame.setExtendedState(state | JFrame.MAXIMIZED_BOTH);
                    btnMaximize.setGlyph("❐");
                }
            });

            CaptionButton btnClose = new CaptionButton("✕", btnWidth, btnHeight, dpiScale, true);
            btnClose.setToolTipText("Close");
            btnClose.addActionListener(e -> frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING)));

            captionButtonsPanel.add(btnMinimize);
            captionButtonsPanel.add(btnMaximize);
            captionButtonsPanel.add(btnClose);

            titleBar.add(captionButtonsPanel, BorderLayout.EAST);

            root.add(titleBar, BorderLayout.NORTH);
            frame.setContentPane(root);

            // Realize native HWND before making visible
            frame.addNotify();
            long hwnd = FastTheme.getWindowHandle(frame);

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
                // Apply FastTheme 0.1.7 Modern Architecture:
                FastTheme.setBorderlessShadow(hwnd, true);
                FastTheme.setTitleBarHeight(hwnd, titleBarHeight);
                FastTheme.setCornerStyle(hwnd, 2); // Rounded
                FastTheme.setTitleBarDarkMode(hwnd, true);
                FastTheme.enableMica(hwnd, true);

                frame.addComponentListener(new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        int state = frame.getExtendedState();
                        if ((state & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) {
                            btnMaximize.setGlyph("❐");
                        } else {
                            btnMaximize.setGlyph("□");
                        }
                        updateHitTest.run();
                    }
                });

                updateHitTest.run();
            }

            frame.setVisible(true);
            if (hwnd != 0) {
                // Critical: Force DWM frame re-evaluation and commit once window is visible
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
     * - Configurable size (96x94px scaled)
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
            int fontSize = (int) Math.round(15 * dpiScale);
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
