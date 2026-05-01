package fasttheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Demo2 - "Wake Up" Transition with Smooth Fade.
 * Demonstrates switching from standard Swing to FastTheme with an animated transition.
 */
public class Demo2 {

    // Animation Constants
    private static final int ANIMATION_DURATION_MS = 1000;
    private static final int FPS = 60;
    
    // Color Constants (Target State - Dark Theme)
    private static final Color COLOR_BG_DARK = new Color(22, 22, 22);
    private static final Color COLOR_TEXT_PRIMARY = new Color(0, 210, 255); // Neon Blue
    private static final Color COLOR_TEXT_SECONDARY = new Color(180, 180, 180);
    
    // Transparency Constants (0-255)
    private static final int TRANSPARENCY_OPAQUE = 255;
    private static final int TRANSPARENCY_TARGET = 220; // ~86% opacity

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            createWakeUpWindow();
        });
    }

    private static void createWakeUpWindow() {
        JFrame frame = new JFrame("FastTheme Smooth Transition");
        frame.setSize(700, 500);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Content Area
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        
        JLabel instructionLabel = new JLabel("Press ENTER to Wake Up");
        instructionLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 32));
        instructionLabel.setForeground(new Color(40, 40, 40));
        
        JLabel subLabel = new JLabel("Standard Swing Mode — Static & Opaque");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subLabel.setForeground(new Color(120, 120, 120));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 5, 10);
        panel.add(instructionLabel, gbc);

        gbc.gridy = 1;
        panel.add(subLabel, gbc);

        frame.add(panel);

        // Key Listener for "Enter"
        frame.addKeyListener(new KeyAdapter() {
            private boolean awakened = false;

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && !awakened) {
                    awakened = true;
                    startSmoothTransition(frame, panel, instructionLabel, subLabel);
                }
            }
        });

        frame.setFocusable(true);
        frame.requestFocusInWindow();
        frame.setVisible(true);
    }

    private static void startSmoothTransition(JFrame frame, JPanel panel, JLabel mainLabel, JLabel subLabel) {
        long hwnd = FastTheme.getWindowHandle(frame);
        if (hwnd == 0) return;

        // Initial setup for the "Native Punch"
        mainLabel.setText("Awakening FastTheme...");
        FastTheme.setTitleBarDarkMode(hwnd, true);
        FastTheme.setTitleBarTextColor(hwnd, 255, 255, 255);

        // Animation Parameters
        final int totalFrames = ANIMATION_DURATION_MS / (1000 / FPS);
        final long startTime = System.currentTimeMillis();

        // Colors
        Color startBg = Color.WHITE;
        Color targetBg = COLOR_BG_DARK;
        
        Color startMain = new Color(40, 40, 40);
        Color targetMain = COLOR_TEXT_PRIMARY;
        
        Color startSub = new Color(120, 120, 120);
        Color targetSub = COLOR_TEXT_SECONDARY;

        Timer timer = new Timer(1000 / FPS, null);
        timer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            float progress = Math.min(1.0f, (float) elapsed / ANIMATION_DURATION_MS);
            
            // Ease out cubic for a smoother feel
            float eased = 1 - (float) Math.pow(1 - progress, 3);

            // Interpolate Background
            panel.setBackground(lerpColor(startBg, targetBg, eased));
            
            // Interpolate Labels
            mainLabel.setForeground(lerpColor(startMain, targetMain, eased));
            subLabel.setForeground(lerpColor(startSub, targetSub, eased));

            // Interpolate Native Attributes
            int currentTrans = (int) (TRANSPARENCY_OPAQUE - (eased * (TRANSPARENCY_OPAQUE - TRANSPARENCY_TARGET)));
            FastTheme.setWindowTransparency(hwnd, currentTrans);
            
            Color currentTitleBar = lerpColor(startBg, targetBg, eased);
            FastTheme.setTitleBarColor(hwnd, currentTitleBar.getRed(), currentTitleBar.getGreen(), currentTitleBar.getBlue());

            if (progress >= 1.0f) {
                ((Timer) e.getSource()).stop();
                mainLabel.setText("FastTheme Awakened");
                subLabel.setText("Native Acrylic & Dark Mode applied seamlessly");
            }
            
            panel.repaint();
        });

        timer.start();
    }

    private static Color lerpColor(Color start, Color end, float t) {
        int r = (int) (start.getRed() + t * (end.getRed() - start.getRed()));
        int g = (int) (start.getGreen() + t * (end.getGreen() - start.getGreen()));
        int b = (int) (start.getBlue() + t * (end.getBlue() - start.getBlue()));
        return new Color(r, g, b);
    }
}
