package fasttheme;

import javax.swing.*;
import java.awt.*;

/**
 * Demo - Side-by-side comparison of Standard Swing vs FastTheme.
 * Part of the FastJava Ecosystem.
 */
public class Demo {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // 1. STANDARD SWING WINDOW
            createStandardWindow();

            // 2. FASTTHEME STYLED WINDOW
            createStyledWindow();
        });
    }

    private static void createStandardWindow() {
        JFrame frame = new JFrame("Standard Swing Window");
        frame.setSize(600, 450);
        frame.setLocation(200, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        // Standard window remains empty for comparison
        frame.add(panel);
        
        frame.setVisible(true);
    }

    private static void createStyledWindow() {
        JFrame frame = new JFrame("FastTheme Styled Window");
        frame.setSize(600, 450);
        frame.setLocation(850, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Content Area with Terminal Styling
        JTextArea console = new JTextArea();
        console.setBackground(new Color(15, 15, 15));
        console.setForeground(Color.WHITE);
        console.setFont(new Font("Consolas", Font.PLAIN, 13));
        console.setEditable(false);
        console.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Add Feature List to Console
        console.append("FastTheme v2.0.0-alpha | Feature Overview\n");
        console.append("=========================================\n\n");
        console.append("[+] Native Titlebar Styling (Windows 11)\n");
        console.append("[+] Real Glass/Acrylic Transparency\n");
        console.append("[+] Immersive Dark Mode Integration\n");
        console.append("[+] Native HWND Handle Extraction\n");
        console.append("[+] Zero-Allocation JNI Engine\n\n");
        console.append("Ready. Applied via Direct Memory Access.");

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(15, 15, 15));
        
        JScrollPane scrollPane = new JScrollPane(console);
        scrollPane.setBorder(null); // Seamless look
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        frame.add(panel);

        // APPLY NATIVE STYLING
        frame.setVisible(true); // Must be visible for HWND
        
        long hwnd = FastTheme.getWindowHandle(frame);
        if (hwnd != 0) {
            FastTheme.setWindowTransparency(hwnd, 204);
            FastTheme.setTitleBarDarkMode(hwnd, true);
            FastTheme.setTitleBarColor(hwnd, 15, 15, 15);
            FastTheme.setTitleBarTextColor(hwnd, 255, 255, 255);
        }
    }
}
