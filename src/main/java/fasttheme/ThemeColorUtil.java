package fasttheme;

import java.awt.Color;

/**
 * High-performance color mathematics, state generator, WCAG accessibility metrics,
 * and ANSI terminal escape sequence utilities.
 */
public final class ThemeColorUtil {

    public static final String ANSI_ESCAPE = "\u001B[";
    public static final String ANSI_RESET = "\u001B[0m";

    public static int rgb(int r, int g, int b) {
        return (0xFF << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    public static int argb(int a, int r, int g, int b) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    public static int alpha(int argb) { return (argb >>> 24) & 0xFF; }
    public static int red(int argb)   { return (argb >>> 16) & 0xFF; }
    public static int green(int argb) { return (argb >>> 8) & 0xFF; }
    public static int blue(int argb)  { return argb & 0xFF; }

    public static int setAlpha(int argb, int newAlpha) {
        return ((newAlpha & 0xFF) << 24) | (argb & 0x00FFFFFF);
    }

    public static Color toAwtColor(int argb) {
        return new Color(red(argb), green(argb), blue(argb), alpha(argb));
    }

    public static int fromAwtColor(Color c) {
        if (c == null) return 0;
        return c.getRGB();
    }

    /**
     * Calculates WCAG 2.1 relative luminance for the given ARGB color.
     */
    public static double luminance(int argb) {
        double r = linearize(red(argb) / 255.0);
        double g = linearize(green(argb) / 255.0);
        double b = linearize(blue(argb) / 255.0);
        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }

    private static double linearize(double val) {
        return (val <= 0.03928) ? (val / 12.92) : Math.pow((val + 0.055) / 1.055, 2.4);
    }

    /**
     * Calculates WCAG contrast ratio between two colors (range 1.0 to 21.0).
     */
    public static double contrastRatio(int c1, int c2) {
        double l1 = luminance(c1);
        double l2 = luminance(c2);
        double brighter = Math.max(l1, l2);
        double darker = Math.min(l1, l2);
        return (brighter + 0.05) / (darker + 0.05);
    }

    /**
     * Returns either pure white (0xFFFFFFFF) or dark (0xFF111111) text color
     * to guarantee maximum readability against the given background.
     */
    public static int getContrastForeground(int bgArgb) {
        double lum = luminance(bgArgb);
        return (lum > 0.45) ? 0xFF111111 : 0xFFFFFFFF;
    }

    /**
     * Lightens the color by the specified percentage (0.0 to 1.0).
     */
    public static int lighten(int argb, float amount) {
        int a = alpha(argb);
        int r = Math.min(255, (int) (red(argb) + (255 - red(argb)) * amount));
        int g = Math.min(255, (int) (green(argb) + (255 - green(argb)) * amount));
        int b = Math.min(255, (int) (blue(argb) + (255 - blue(argb)) * amount));
        return argb(a, r, g, b);
    }

    /**
     * Darkens the color by the specified percentage (0.0 to 1.0).
     */
    public static int darken(int argb, float amount) {
        int a = alpha(argb);
        int r = Math.max(0, (int) (red(argb) * (1.0f - amount)));
        int g = Math.max(0, (int) (green(argb) * (1.0f - amount)));
        int b = Math.max(0, (int) (blue(argb) * (1.0f - amount)));
        return argb(a, r, g, b);
    }

    /**
     * Linear interpolation/blending between two colors.
     */
    public static int blend(int c1, int c2, float t) {
        t = Math.max(0.0f, Math.min(1.0f, t));
        float inv = 1.0f - t;
        int a = (int) (alpha(c1) * inv + alpha(c2) * t);
        int r = (int) (red(c1) * inv + red(c2) * t);
        int g = (int) (green(c1) * inv + green(c2) * t);
        int b = (int) (blue(c1) * inv + blue(c2) * t);
        return argb(a, r, g, b);
    }

    /**
     * Formats an ANSI 24-bit Truecolor foreground escape sequence.
     */
    public static String toAnsiForeground(int argb) {
        return ANSI_ESCAPE + "38;2;" + red(argb) + ";" + green(argb) + ";" + blue(argb) + "m";
    }

    /**
     * Formats an ANSI 24-bit Truecolor background escape sequence.
     */
    public static String toAnsiBackground(int argb) {
        return ANSI_ESCAPE + "48;2;" + red(argb) + ";" + green(argb) + ";" + blue(argb) + "m";
    }

    /**
     * Parses a color string in various formats:
     * - "#RRGGBB" / "#AARRGGBB"
     * - "0xRRGGBB" / "0xAARRGGBB"
     * - "R,G,B" / "R,G,B,A"
     */
    public static int parseColor(String str) {
        if (str == null) return 0;
        String s = str.trim();
        if (s.isEmpty()) return 0;

        if (s.equalsIgnoreCase("TRANSPARENT")) {
            return 0;
        }

        // Hex formats
        if (s.startsWith("#") || s.startsWith("0x") || s.startsWith("0X")) {
            String hex = s.startsWith("#") ? s.substring(1) : s.substring(2);
            if (hex.length() == 6) {
                int rgb = (int) Long.parseLong(hex, 16);
                return (0xFF << 24) | (rgb & 0xFFFFFF);
            } else if (hex.length() == 8) {
                return (int) Long.parseLong(hex, 16);
            }
        }

        // Comma separated format
        if (s.contains(",")) {
            String[] parts = s.split(",");
            if (parts.length >= 3) {
                int r = Integer.parseInt(parts[0].trim());
                int g = Integer.parseInt(parts[1].trim());
                int b = Integer.parseInt(parts[2].trim());
                int a = (parts.length >= 4) ? Integer.parseInt(parts[3].trim()) : 255;
                return argb(a, r, g, b);
            }
        }

        // Raw integer string
        try {
            return (int) Long.parseLong(s);
        } catch (NumberFormatException ignored) {}

        return 0;
    }

    private ThemeColorUtil() {}
}
