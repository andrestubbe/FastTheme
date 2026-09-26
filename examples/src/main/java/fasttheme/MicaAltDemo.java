package fasttheme;

import javax.swing.*;
import java.awt.*;

public class MicaAltDemo {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("FastTheme - Windows 11 Mica Alt (Tabbed) Demo");
            frame.setSize(800, 500);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Transparency on Swing side so the DWM Mica Alt surface shines through
            frame.setBackground(new Color(0, 0, 0, 0));
            JPanel content = new JPanel();
            content.setOpaque(false);
            content.setLayout(new BorderLayout());

            JLabel label = new JLabel("Windows 11 Mica Alt (Tabbed) Active", SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 22));
            label.setForeground(new Color(240, 240, 240));
            content.add(label, BorderLayout.CENTER);

            JLabel hint = new JLabel("Designed for tabbed container windows with stronger tinting than standard Mica", SwingConstants.CENTER);
            hint.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            hint.setForeground(new Color(180, 180, 180));
            hint.setBorder(BorderFactory.createEmptyBorder(0, 0, 40, 0));
            content.add(hint, BorderLayout.SOUTH);

            frame.setContentPane(content);
            frame.setVisible(true);

            long hwnd = FastTheme.getWindowHandle(frame);
            if (hwnd != 0) {
                FastTheme.setTitleBarDarkMode(hwnd, true);
                FastTheme.setCornerStyle(hwnd, 2); // Rounded corners
                FastTheme.setSystemBackdropType(hwnd, FastTheme.BACKDROP_MICA_ALT);
            }
        });
    }
}
