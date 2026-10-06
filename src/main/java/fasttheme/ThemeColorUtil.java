package fasttheme;

import java.awt.Color;

/**
 * High-performance color mathematics and accessibility utilities for FastTheme.
 * Provides WCAG 2.1 contrast metrics, tint/shade state generation, and color packing/parsing.
 */
public final class ThemeColorUtil {

    private ThemeColorUtil() {
    }


    /**
     * Calculates the WCAG 2.1 relative luminance of a color.
     *
     * @param argb Packed 32-bit ARGB color.
     * @return Relative luminance score between 0.0 (pure black) and 1.0 (pure white).
     */
    public static double luminance(int argb) {
        double r = red(argb) / 255.0;
        double g = green(argb) / 255.0;
        double b = blue(argb) / 255.0;

        r = (r <= 0.03928) ? r / 12.92 : Math.pow((r + 0.055) / 1.055, 2.4);
        g = (g <= 0.03928) ? g / 12.92 : Math.pow((g + 0.055) / 1.055, 2.4);
        b = (b <= 0.03928) ? b / 12.92 : Math.pow((b + 0.055) / 1.055, 2.4);

        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }

    /**
     * Calculates the WCAG 2.1 contrast ratio between two colors.
     *
     * @param color1 First packed ARGB color.
     * @param color2 Second packed ARGB color.
     * @return Contrast ratio from 1.0 (no contrast) to 21.0 (black on white).
     */
    public static double contrastRatio(int color1, int color2) {
        double l1 = luminance(color1);
        double l2 = luminance(color2);
        double lighter = Math.max(l1, l2);
        double darker = Math.min(l1, l2);
        return (lighter + 0.05) / (darker + 0.05);
    }

    /**
     * Lightens a color by a linear fraction.
     *
     * @param argb   Packed 32-bit ARGB color.
     * @param amount Fraction to lighten (0.0 to 1.0).
     * @return Lightened packed ARGB color.
     */
    public static int lighten(int argb, float amount) {
        int a = alpha(argb);
        int r = Math.min(255, (int) (red(argb) + (255 - red(argb)) * amount));
        int g = Math.min(255, (int) (green(argb) + (255 - green(argb)) * amount));
        int b = Math.min(255, (int) (blue(argb) + (255 - blue(argb)) * amount));
        return argb(a, r, g, b);
    }

    /**
     * Darkens a color by a linear fraction.
     *
     * @param argb   Packed 32-bit ARGB color.
     * @param amount Fraction to darken (0.0 to 1.0).
     * @return Darkened packed ARGB color.
     */
    public static int darken(int argb, float amount) {
        int a = alpha(argb);
        int r = Math.max(0, (int) (red(argb) * (1.0f - amount)));
        int g = Math.max(0, (int) (green(argb) * (1.0f - amount)));
        int b = Math.max(0, (int) (blue(argb) * (1.0f - amount)));
        return argb(a, r, g, b);
    }

    /**
     * Linearly interpolates (blends) between two packed ARGB colors.
     *
     * @param c1 Start color.
     * @param c2 End color.
     * @param t  Interpolation factor (0.0 = c1, 1.0 = c2).
     * @return Interpolated packed ARGB color.
     */
    public static int blend(int c1, int c2, float t) {
        t = Math.max(0.0f, Math.min(1.0f, t));
        int a = (int) (alpha(c1) + (alpha(c2) - alpha(c1)) * t);
        int r = (int) (red(c1) + (red(c2) - red(c1)) * t);
        int g = (int) (green(c1) + (green(c2) - green(c1)) * t);
        int b = (int) (blue(c1) + (blue(c2) - blue(c1)) * t);
        return argb(a, r, g, b);
    }

    /**
     * Parses a string representation of a color into a packed 32-bit ARGB integer.
     * Supports formats:
     * - Hex: #RGB, #RRGGBB, #AARRGGBB, 0xRRGGBB
     * - RGB: rgb(r, g, b)
     * - RGBA: rgba(r, g, b, a)
     * - Comma-separated: r, g, b or r, g, b, a
     *
     * @param str Color string representation.
     * @return Packed 32-bit ARGB color integer.
     * @throws IllegalArgumentException If color string cannot be parsed.
     */
    public static int parseColor(String str) {
        if (str == null || str.trim().isEmpty()) {
            throw new IllegalArgumentException("Color string cannot be null or empty");
        }
        String s = str.trim();

        // 1. Hex format (#... or 0x...)
        if (s.startsWith("#") || s.startsWith("0x") || s.startsWith("0X")) {
            String hex = s.startsWith("#") ? s.substring(1) : s.substring(2);
            if (hex.length() == 3) {
                int r = Integer.parseInt(hex.substring(0, 1), 16) * 17;
                int g = Integer.parseInt(hex.substring(1, 2), 16) * 17;
                int b = Integer.parseInt(hex.substring(2, 3), 16) * 17;
                return rgb(r, g, b);
            } else if (hex.length() == 6) {
                int rgb = Integer.parseInt(hex, 16);
                return 0xFF000000 | rgb;
            } else if (hex.length() == 8) {
                long argb = Long.parseLong(hex, 16);
                return (int) argb;
            }
            throw new IllegalArgumentException("Invalid hex color format: " + str);
        }

        // 2. Functional format: rgb(...) or rgba(...)
        if (s.toLowerCase().startsWith("rgb(") && s.endsWith(")")) {
            String[] parts = s.substring(4, s.length() - 1).split(",");
            if (parts.length == 3) {
                return rgb(Integer.parseInt(parts[0].trim()),
                        Integer.parseInt(parts[1].trim()),
                        Integer.parseInt(parts[2].trim()));
            }
        }
        if (s.toLowerCase().startsWith("rgba(") && s.endsWith(")")) {
            String[] parts = s.substring(5, s.length() - 1).split(",");
            if (parts.length == 4) {
                float aVal = Float.parseFloat(parts[3].trim());
                int alpha = (aVal <= 1.0f) ? (int) (aVal * 255) : (int) aVal;
                return argb(alpha,
                        Integer.parseInt(parts[0].trim()),
                        Integer.parseInt(parts[1].trim()),
                        Integer.parseInt(parts[2].trim()));
            }
        }

        // 3. Comma-separated: r, g, b[, a]
        if (s.contains(",")) {
            String[] parts = s.split(",");
            if (parts.length == 3) {
                return rgb(Integer.parseInt(parts[0].trim()),
                        Integer.parseInt(parts[1].trim()),
                        Integer.parseInt(parts[2].trim()));
            } else if (parts.length == 4) {
                float aVal = Float.parseFloat(parts[3].trim());
                int alpha = (aVal <= 1.0f) ? (int) (aVal * 255) : (int) aVal;
                return argb(alpha,
                        Integer.parseInt(parts[0].trim()),
                        Integer.parseInt(parts[1].trim()),
                        Integer.parseInt(parts[2].trim()));
            }
        }

        throw new IllegalArgumentException("Unrecognized color format: " + str);
    }

    /**
     * Converts a 32-bit packed ARGB integer into an AWT Color object.
     *
     * @param argb Packed 32-bit ARGB color.
     * @return Corresponding {@link java.awt.Color}.
     */
    public static Color toAwtColor(int argb) {
        return new Color(argb, true);
    }

    /**
     * Packs 8-bit RGB color channels into a 32-bit integer with full alpha (0xFF).
     *
     * @param r Red component (0..255).
     * @param g Green component (0..255).
     * @param b Blue component (0..255).
     * @return 32-bit packed ARGB integer.
     */
    public static int rgb(int r, int g, int b) {
        return (0xFF << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    /**
     * Packs 8-bit ARGB color channels into a 32-bit integer.
     *
     * @param a Alpha component (0..255).
     * @param r Red component (0..255).
     * @param g Green component (0..255).
     * @param b Blue component (0..255).
     * @return 32-bit packed ARGB integer.
     */
    public static int argb(int a, int r, int g, int b) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }


    /**
     * Extracts the alpha component (0..255) from a packed 32-bit ARGB integer.
     *
     * @param argb Packed 32-bit ARGB color.
     * @return Alpha channel value (0..255).
     */
    public static int alpha(int argb) {
        return (argb >>> 24) & 0xFF;
    }

    /**
     * Extracts the red component (0..255) from a packed 32-bit ARGB integer.
     *
     * @param argb Packed 32-bit ARGB color.
     * @return Red channel value (0..255).
     */
    public static int red(int argb) {
        return (argb >>> 16) & 0xFF;
    }

    /**
     * Extracts the green component (0..255) from a packed 32-bit ARGB integer.
     *
     * @param argb Packed 32-bit ARGB color.
     * @return Green channel value (0..255).
     */
    public static int green(int argb) {
        return (argb >>> 8) & 0xFF;
    }

    /**
     * Extracts the blue component (0..255) from a packed 32-bit ARGB integer.
     *
     * @param argb Packed 32-bit ARGB color.
     * @return Blue channel value (0..255).
     */
    public static int blue(int argb) {
        return argb & 0xFF;
    }

    /**
     * Returns an optimal readable text color (pure white or dark charcoal)
     * based on the WCAG contrast of the background.
     *
     * @param bgArgb Background color in packed 32-bit ARGB format.
     * @return Optimal foreground color in packed 32-bit ARGB format.
     */
    public static int getContrastForeground(int bgArgb) {
        double whiteContrast = contrastRatio(bgArgb, 0xFFFFFFFF);
        double darkContrast = contrastRatio(bgArgb, 0xFF111111);
        return whiteContrast >= darkContrast ? 0xFFFFFFFF : 0xFF111111;
    }
}
