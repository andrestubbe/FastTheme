package fasttheme;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.Timer;

/**
 * FastTheme Demo 2 - Premium Overlay Showcase (Raycast-Style).
 */
public class Demo2 {
    
    // --- UI CONFIGURATION ---
    private static final int WINDOW_WIDTH      = 700;
    private static final int HEIGHT_COLLAPSED  = 60;
    private static final int HEIGHT_EXPANDED   = 300;
    
    private static final int DRAG_ZONE_HEIGHT  = 6;
    private static final int WINDOW_ALPHA      = 230; // 0-255
    private static final int CORNER_STYLE      = 2;   // 2 = Rounded (Win11)
    
    private static final int ANIM_DURATION_MS  = 50;
    private static final int REFRESH_RATE_MS   = 10;
    
    // --- STYLING ---
    private static final Color COLOR_BG        = new Color(44, 50, 46);
    private static final Color COLOR_ACCENT    = new Color(223, 145, 70);
    private static final Color COLOR_BORDER    = new Color(40, 40, 40);
    private static final Color COLOR_TITLEBAR  = new Color(15, 15, 15);
    
    private static final Font FONT_MAIN        = new Font("Inter", Font.PLAIN, 14);

    static {
        // Force loading the local build for development/demo
        java.io.File local = new java.io.File("build/fasttheme.dll");
        java.io.File parent = new java.io.File("../build/fasttheme.dll");
        java.io.File actualFile = local.exists() ? local : parent;
        
        System.out.println("[Debug] Searching for DLL in: " + local.getAbsolutePath() + " (exists: " + local.exists() + ")");
        System.out.println("[Debug] Searching for DLL in: " + parent.getAbsolutePath() + " (exists: " + parent.exists() + ")");
        
        System.load(actualFile.getAbsolutePath());
    }

    public static void main(String[] args) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("FastTheme Premium Overlay");
            frame.setSize(WINDOW_WIDTH, HEIGHT_COLLAPSED);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // 1. Dark Theme UI (Matched to FastFileSearch Demo)
            frame.setBackground(COLOR_BG);
            
            JPanel panel = new JPanel();
            panel.setBackground(COLOR_BG);
            panel.setDoubleBuffered(true);
            panel.setLayout(new BorderLayout());
            panel.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
            
            // Ensure all layers are the same color to prevent flickering
            frame.getContentPane().setBackground(COLOR_BG);
            frame.getRootPane().setBackground(COLOR_BG);
            
            JLabel label = new JLabel("FastTheme v0.3.0 - Press ENTER to rescale", SwingConstants.LEFT);
            label.setForeground(COLOR_ACCENT);
            label.setFont(FONT_MAIN);
            label.setBorder(new EmptyBorder(20, 20, 0, 0));
            panel.add(label, BorderLayout.NORTH);
            
            frame.setContentPane(panel);

            // 2. Premium Native Styling
            frame.addNotify();
            long hwnd = FastTheme.getWindowHandle(frame);
            
            if (hwnd != 0) {
                FastTheme.setBorderlessShadow(hwnd, true);
                FastTheme.setOverlayDragHeight(hwnd, DRAG_ZONE_HEIGHT);
                FastTheme.setTitleBarDarkMode(hwnd, true);
                FastTheme.setTitleBarColor(hwnd, COLOR_TITLEBAR.getRed(), COLOR_TITLEBAR.getGreen(), COLOR_TITLEBAR.getBlue());
                FastTheme.setWindowTransparency(hwnd, WINDOW_ALPHA);
                FastTheme.setCornerStyle(hwnd, CORNER_STYLE); 
            }

            frame.setVisible(true);

            // 3. Animation Logic (ENTER)
            final boolean[] expanded = {false};
            frame.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        int startH = frame.getHeight();
                        int targetH = expanded[0] ? HEIGHT_COLLAPSED : HEIGHT_EXPANDED;
                        expanded[0] = !expanded[0];

                        final fasttween.Tween tween = fasttween.FastTween.to(startH, targetH, ANIM_DURATION_MS)
                            .ease(fasttween.Ease.QUAD_IN_OUT)
                            .onUpdate(v -> frame.setSize(frame.getWidth(), v.intValue()))
                            .start();

                        new Timer(REFRESH_RATE_MS, e2 -> {
                            if (!tween.update()) {
                                ((Timer)e2.getSource()).stop();
                            }
                        }).start();
                    }
                }
            });
            
            frame.setFocusable(true);
            frame.requestFocus();

            // 4. Close on Focus Loss
            frame.addWindowFocusListener(new WindowFocusListener() {
                @Override
                public void windowGainedFocus(WindowEvent e) {}

                @Override
                public void windowLostFocus(WindowEvent e) {
                    System.exit(0);
                }
            });
        });
    }
}
