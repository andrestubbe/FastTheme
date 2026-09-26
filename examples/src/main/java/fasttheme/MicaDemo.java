package fasttheme;

import javax.swing.*;
import java.awt.*;

public class MicaDemo {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("FastTheme - Windows 11 Mica Demo");
            frame.setSize(800, 500);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setUndecorated(true);
            frame.setBackground(new Color(0, 0, 0, 0));

            JPanel content = new JPanel();
            content.setOpaque(false);
            content.setLayout(new BorderLayout());

            JLabel label = new JLabel("Windows 11 Mica Backdrop Active", SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 22));
            label.setForeground(new Color(240, 240, 240));
            content.add(label, BorderLayout.CENTER);

            JLabel hint = new JLabel("The desktop wallpaper color shines subtly through this window", SwingConstants.CENTER);
            hint.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            hint.setForeground(new Color(180, 180, 180));
            hint.setBorder(BorderFactory.createEmptyBorder(0, 0, 40, 0));
            content.add(hint, BorderLayout.SOUTH);

            frame.setContentPane(content);
            frame.setVisible(true);

            long hwnd = FastTheme.getWindowHandle(frame);
            if (hwnd != 0) {
                FastTheme.setBorderlessShadow(hwnd, true);
                FastTheme.setOverlayDragHeight(hwnd, 40);
                FastTheme.setTitleBarDarkMode(hwnd, true);
                FastTheme.setCornerStyle(hwnd, 2); // Rounded corners
                FastTheme.setSystemBackdropType(hwnd, FastTheme.BACKDROP_MICA);
            }

            // Close on ESC key
            frame.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyPressed(java.awt.event.KeyEvent e) {
                    if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
                        frame.dispose();
                        System.exit(0);
                    }
                }
            });
        });
    }
}
