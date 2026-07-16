package fasttheme;

import fastanimation.FastAnimation;
import fasttween.Ease;
import fasttween.FastTween;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Demo {

    private enum ThemeState {
        NORMAL(Color.WHITE, new Color(40, 40, 40), 255, false, Color.WHITE, new Color(40, 40, 40)),
        LIGHT(new Color(230, 250, 245), new Color(80, 130, 118), 230, false, new Color(230, 250, 245), new Color(80, 130, 118)),
        DARK(new Color(44, 50, 46), new Color(223, 145, 70), 230, true, new Color(44, 50, 46), new Color(125, 138, 118)),
        MONO(Color.BLACK, Color.WHITE, 230, true, Color.BLACK, Color.WHITE),
        ANTIGRAVITY(new Color(20, 20, 35), new Color(0, 255, 200), 230, true, new Color(30, 30, 50), new Color(255, 100, 255));

        final Color bg, text, titleBarBg, titleBarFg;
        final int transparency;
        final boolean darkMode;

        ThemeState(Color bg, Color text, int transparency, boolean darkMode, Color titleBarBg, Color titleBarFg) {
            this.bg = bg;
            this.text = text;
            this.transparency = transparency;
            this.darkMode = darkMode;
            this.titleBarBg = titleBarBg;
            this.titleBarFg = titleBarFg;
        }
    }

    private static ThemeState currentState = ThemeState.NORMAL;
    private static final int TRANSITION_MS = 1000;
    private static final int AUTO_CYCLE_MS = 10000;

    private static boolean isAnimating = false;
    private static JTextArea console;
    private static float currentCycleProgress = 0;
    private static JPanel smoothProgressBar;

    private static long lastFpsTime = System.nanoTime();
    private static int frameCount = 0;
    private static int currentFps = 0;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createTerminalWindow());
    }

    private static void createTerminalWindow() {
        JFrame frame = new JFrame("FastTheme");
        frame.setSize(1173, 610);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        updateWindowIcon(frame, currentState.text);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(currentState.bg);

        console = new JTextArea();
        console.setOpaque(false);
        console.setEditable(false);
        console.setFont(new Font("Consolas", Font.BOLD, 14));
        console.setForeground(currentState.text);
        console.setBorder(BorderFactory.createEmptyBorder(10, 30, 30, 30));

        smoothProgressBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(console.getForeground());
                int maxBarWidth = getWidth() / 6;
                int currentWidth = (int) (maxBarWidth * currentCycleProgress);
                g.fillRect(0, 0, currentWidth, getHeight());
                updateFps(frame);
            }
        };
        smoothProgressBar.setOpaque(false);
        smoothProgressBar.setPreferredSize(new Dimension(0, 24));

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);

        JPanel barWrapper = new JPanel(new BorderLayout());
        barWrapper.setOpaque(false);
        barWrapper.setBorder(BorderFactory.createEmptyBorder(30, 30, 0, 0));
        barWrapper.add(smoothProgressBar, BorderLayout.CENTER);

        topContainer.add(barWrapper, BorderLayout.NORTH);
        topContainer.add(console, BorderLayout.CENTER);

        updateConsoleText();

        mainPanel.add(topContainer, BorderLayout.NORTH);
        frame.add(mainPanel);

        frame.setVisible(true);

        startAutoCycle(frame, mainPanel);
    }

    private static void updateFps(JFrame frame) {
        frameCount++;
        long now = System.nanoTime();
        if (now - lastFpsTime >= 1_000_000_000L) {
            currentFps = frameCount;
            frameCount = 0;
            lastFpsTime = now;
            frame.setTitle("FastTheme FPS: " + currentFps);
        }
    }

    private static void updateConsoleText() {
        StringBuilder sb = new StringBuilder();
        String modeName = currentState == ThemeState.NORMAL ? "NORMAL (Swing)" : currentState.name();
        sb.append("Mode: ").append(modeName).append("\n");
        sb.append((int) (currentCycleProgress * 100)).append(" %");
        console.setText(sb.toString());
    }

    private static void startAutoCycle(JFrame frame, JPanel panel) {
        FastAnimation.parallel(FastTween.to(0.0f, 1.0f, AUTO_CYCLE_MS).ease(Ease.LINEAR).onUpdate(v -> {
            currentCycleProgress = v;
            smoothProgressBar.repaint();
            updateConsoleText();
        }).onComplete(() -> {
            if (!isAnimating) {
                triggerNextState(frame, panel);
            }
        })).start();
    }

    private static void triggerNextState(JFrame frame, JPanel panel) {
        isAnimating = true;
        ThemeState nextState = getNextState();
        transitionTo(frame, panel, nextState, () -> {
            isAnimating = false;
            currentCycleProgress = 0;
            updateConsoleText();
            startAutoCycle(frame, panel);
        });
    }

    private static void updateWindowIcon(JFrame frame, Color color) {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.fillOval(8, 8, 16, 16);
        g2.dispose();
        frame.setIconImage(img);
    }

    private static ThemeState getNextState() {
        switch (currentState) {
            case NORMAL:
                return ThemeState.LIGHT;
            case LIGHT:
                return ThemeState.DARK;
            case DARK:
                return ThemeState.MONO;
            case MONO:
                return ThemeState.ANTIGRAVITY;
            case ANTIGRAVITY:
                return ThemeState.NORMAL;
            default:
                return ThemeState.NORMAL;
        }
    }

    private static void transitionTo(JFrame frame, JPanel panel, ThemeState target, Runnable onDone) {
        long hwnd = FastTheme.getWindowHandle(frame);
        if (hwnd == 0) return;

        ThemeState start = currentState;
        currentState = target;

        FastTheme.setTitleBarDarkMode(hwnd, target.darkMode);

        FastAnimation.parallel(FastTween.to(0.0f, 1.0f, TRANSITION_MS).ease(Ease.EXPO_OUT).onUpdate(progress -> {
            Color currentBg = lerpColor(start.bg, target.bg, progress);
            Color currentText = lerpColor(start.text, target.text, progress);
            Color currentTB_Bg = lerpColor(start.titleBarBg, target.titleBarBg, progress);
            Color currentTB_Fg = lerpColor(start.titleBarFg, target.titleBarFg, progress);
            int currentTrans = (int) (start.transparency + progress * (target.transparency - start.transparency));

            panel.setBackground(currentBg);
            console.setForeground(currentText);
            updateWindowIcon(frame, currentText);
            updateConsoleText();

            FastTheme.setWindowTransparency(hwnd, currentTrans);
            FastTheme.setTitleBarColor(hwnd, currentTB_Bg.getRed(), currentTB_Bg.getGreen(), currentTB_Bg.getBlue());
            FastTheme.setTitleBarTextColor(hwnd, currentTB_Fg.getRed(), currentTB_Fg.getGreen(), currentTB_Fg.getBlue());

            panel.repaint();
        }).onComplete(() -> {
            if (onDone != null) onDone.run();
        })).start();
    }

    private static Color lerpColor(Color start, Color end, float t) {
        int r = (int) (start.getRed() + t * (end.getRed() - start.getRed()));
        int g = (int) (start.getGreen() + t * (end.getGreen() - start.getGreen()));
        int b = (int) (start.getBlue() + t * (end.getBlue() - start.getBlue()));
        return new Color(r, g, b);
    }
}
