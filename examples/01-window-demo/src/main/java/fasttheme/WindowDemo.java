package fasttheme;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * WindowDemo - Advanced Window Styling Example for FastTheme v2.0.0-alpha.
 * 
 * <p>Demonstrates:
 * <ul>
 *   <li>Native Windows 11 Titlebar Colors</li>
 *   <li>Native Window Transparency (Glass Effect)</li>
 *   <li>Immersive Dark Mode Integration</li>
 *   <li>Native HWND extraction from Swing</li>
 * </ul>
 */
public class WindowDemo extends JFrame {

    private JTextArea logArea;

    public WindowDemo() {
        setTitle("FastTheme Styling Demo");
        setSize(900, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create terminal-like text area
        logArea = new JTextArea();
        logArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        logArea.setBackground(new Color(0, 0, 0, 0)); // Transparent
        logArea.setForeground(Color.WHITE);
        logArea.setCaretColor(Color.WHITE);
        logArea.setEditable(false);
        logArea.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Main panel with terminal background
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(12, 12, 12)); // Dark content area
        panel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        setContentPane(panel);

        // Set custom icon
        setIconImage(createWindowIcon());
    }

    private void log(String msg) {
        logArea.append(msg + "\n");
    }

    private BufferedImage createWindowIcon() {
        BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillOval(4, 4, 24, 24);
        g2.setColor(Color.BLACK);
        g2.fillOval(12, 12, 8, 8);
        g2.dispose();
        return image;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);

        if (visible) {
            // APPLY NATIVE STYLING
            long hwnd = FastTheme.getWindowHandle(this);
            if (hwnd != 0) {
                // 1. Transparency (80%)
                FastTheme.setWindowTransparency(hwnd, 204);

                // 2. Titlebar matching content (RGB 12, 12, 12)
                FastTheme.setTitleBarColor(hwnd, 12, 12, 12);

                // 3. White titlebar text
                FastTheme.setTitleBarTextColor(hwnd, 255, 255, 255);

                // 4. Immersive Dark Mode
                FastTheme.setTitleBarDarkMode(hwnd, true);

                log("FastTheme v2.0.0-alpha | Styling Engine");
                log("=====================================");
                log("Status: STYLING APPLIED");
                log("Native HWND: 0x" + Long.toHexString(hwnd).toUpperCase());
                log("");
                log("Applied Attributes:");
                log("  [+] Transparency: 80% (Glass Effect)");
                log("  [+] Titlebar Color: RGB(12,12,12)");
                log("  [+] Titlebar Text: RGB(255,255,255)");
                log("  [+] Dark Mode: Enabled");
                log("");
                log("System Theme Check:");
                log("  Windows Dark Mode: " + (FastTheme.isSystemDarkMode() ? "YES" : "NO"));
                log("");
                log("Note: Display monitoring (DPI/Resolution) has been ");
                log("migrated to the FastDisplay module.");
            }
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new WindowDemo().setVisible(true));
    }
}
