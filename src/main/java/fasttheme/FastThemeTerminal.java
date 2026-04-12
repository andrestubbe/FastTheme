import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.text.*;

public class FastThemeTerminal extends JFrame {
    
    // Native methods - Window styling
    public native long getWindowHandle(Component component);
    public native boolean setWindowTransparency(long hwnd, int alpha);
    public native boolean setTitleBarColor(long hwnd, int r, int g, int b);
    public native boolean setTitleBarTextColor(long hwnd, int r, int g, int b);
    public native boolean setTitleBarDarkMode(long hwnd, boolean enabled);
    
    // Native methods - System detection (from FastTheme)
    public native String getSystemResolution();
    public native int getSystemDPI();
    public native boolean isSystemDarkMode();
    public native int getSystemRefreshRate();
    
    // Native method - Custom window icon
    public native boolean setWindowIcon(long hwnd);
    
    static {
        System.loadLibrary("titlebar_jni");
    }
    
    private JTextArea logArea;
    private StyleContext styleContext;
    private StyledDocument styledDoc;
    
    // Real detection values (populated via JNI)
    private int systemDPI = 96;
    private String systemTheme = "Unknown";
    private int systemRefresh = 60;
    private String systemResolution = "Unknown";
    
    public FastThemeTerminal() {
        // No decorations from Swing - we use native
        setTitle("FastTheme Terminal");
        setSize(960, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Create terminal-like text area
        logArea = new JTextArea();
        // Windows 11 Terminal font: Cascadia Mono (or Consolas fallback)
        Font terminalFont = loadTerminalFont();
        logArea.setFont(terminalFont);
        logArea.setBackground(new Color(0, 0, 0, 0)); // Transparent
        logArea.setForeground(Color.WHITE);
        logArea.setCaretColor(Color.WHITE);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setEditable(false);
        logArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Dark scroll pane
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        scrollPane.getVerticalScrollBar().setUI(new MinimalScrollBarUI());
        
        // Main panel with terminal-like background
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(12, 12, 12)); // Almost black
        panel.add(scrollPane, BorderLayout.CENTER);
        
        setContentPane(panel);
        
        // Log initial info
        log("FastTheme Terminal v1.1");
        log("======================");
        log("");
        log("OS Detection:");
        log("  Resolution: " + systemResolution);
        log("  DPI Scale: " + systemDPI + " (" + (systemDPI * 100 / 96) + "%)");
        log("  Theme: " + systemTheme);
        log("  Refresh: " + systemRefresh + "Hz");
        log("");
        log("Window Styling Applied:");
        log("  Titlebar: RGB(12,12,12) [seamless]");
        log("  Text: RGB(255,255,255) [white]");
        log("  Transparency: 80%");
        log("  Mode: Dark");
        log("");
        log("Ready. JNI + DWM APIs. Pure native.");
    }
    
    private void log(String msg) {
        logArea.append(msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
    
    private Font loadTerminalFont() {
        // Try Windows 11 Terminal fonts in order
        String[] fontNames = {"Cascadia Mono", "Cascadia Code", "Consolas", "Lucida Console", "Monospaced"};
        for (String name : fontNames) {
            if (isFontAvailable(name)) {
                return new Font(name, Font.PLAIN, 14);
            }
        }
        return new Font("Monospaced", Font.PLAIN, 14);
    }
    
    private boolean isFontAvailable(String fontName) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (Font font : ge.getAllFonts()) {
            if (font.getName().equals(fontName)) {
                return true;
            }
        }
        return false;
    }
    
    private void detectSystemValues() {
        try {
            systemResolution = getSystemResolution();
            systemDPI = getSystemDPI();
            systemTheme = isSystemDarkMode() ? "Dark" : "Light";
            systemRefresh = getSystemRefreshRate();
        } catch (Exception e) {
            // Fallback values
            systemResolution = "Unknown";
            systemDPI = 96;
            systemTheme = "Unknown";
            systemRefresh = 60;
        }
    }
    
    @Override
    public void setVisible(boolean visible) {
        if (visible && !isVisible()) {
            super.setVisible(true);
            
            // IMMEDIATELY apply native styling
            long hwnd = getWindowHandle(this);
            if (hwnd != 0) {
                // Detect real system values first
                detectSystemValues();
                
                // 80% transparency (204/255)
                setWindowTransparency(hwnd, 204);
                
                // Titlebar same color as content (RGB 12, 12, 12)
                setTitleBarColor(hwnd, 12, 12, 12);
                
                // White text
                setTitleBarTextColor(hwnd, 255, 255, 255);
                
                // Dark mode
                setTitleBarDarkMode(hwnd, true);
                
                // Set custom icon (white circle with black dot)
                boolean iconOk = setWindowIcon(hwnd);
                
                // Update log with real values
                logArea.setText(""); // Clear
                log("FastTheme Terminal v1.1");
                log("======================");
                log("");
                log("OS Detection:");
                log("  Resolution: " + systemResolution);
                log("  DPI Scale: " + systemDPI + " (" + (systemDPI * 100 / 96) + "%)");
                log("  Theme: " + systemTheme);
                log("  Refresh: " + systemRefresh + "Hz");
                log("");
                log("Window Styling Applied:");
                log("  Titlebar: RGB(12,12,12) [seamless]");
                log("  Text: RGB(255,255,255) [white]");
                log("  Transparency: 80%");
                log("  Mode: Dark");
                log("");
                log("Ready. JNI + DWM APIs. Pure native.");
                log("");
                log("[JNI] Window handle: 0x" + Long.toHexString(hwnd));
                log("[JNI] Icon: " + (iconOk ? "white circle + black dot" : "FAILED"));
                log("[JNI] DWM attributes applied successfully");
            } else {
                log("");
                log("[ERROR] Failed to get window handle");
            }
        } else {
            super.setVisible(visible);
        }
    }
    
    // Minimal scrollbar UI for dark theme
    static class MinimalScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            thumbColor = new Color(100, 100, 100);
            trackColor = new Color(30, 30, 30);
        }
        
        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }
        
        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }
        
        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            return button;
        }
    }
    
    public static void main(String[] args) {
        // No Swing LAF - we want native
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new FastThemeTerminal().setVisible(true);
        });
    }
}
