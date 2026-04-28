package fasttheme;

import javax.swing.*;
import java.awt.*;

/**
 * ComparisonDemo - Side-by-side comparison of Standard Swing vs FastTheme.
 */
public class ComparisonDemo {

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
        frame.setSize(400, 300);
        frame.setLocation(200, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("I am a boring default window.", SwingConstants.CENTER);
        panel.add(label, BorderLayout.CENTER);
        frame.add(panel);
        
        frame.setVisible(true);
    }

    private static void createStyledWindow() {
        JFrame frame = new JFrame("FastTheme Styled Window");
        frame.setSize(400, 300);
        frame.setLocation(650, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Content Area
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(15, 15, 15)); // Dark background
        
        JLabel label = new JLabel("I am a FastTheme styled window!", SwingConstants.CENTER);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        panel.add(label, BorderLayout.CENTER);
        frame.add(panel);

        // APPLY NATIVE STYLING
        frame.setVisible(true); // Must be visible for HWND
        
        long hwnd = FastTheme.getWindowHandle(frame);
        if (hwnd != 0) {
            // 80% Transparency
            FastTheme.setWindowTransparency(hwnd, 204);
            
            // Dark Mode Titlebar
            FastTheme.setTitleBarDarkMode(hwnd, true);
            
            // Titlebar matching background (RGB 15, 15, 15)
            FastTheme.setTitleBarColor(hwnd, 15, 15, 15);
            
            // White Titlebar Text
            FastTheme.setTitleBarTextColor(hwnd, 255, 255, 255);
        }
    }
}
