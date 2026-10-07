package fasttheme;

/**
 * TitleBarTheme - Configuration palette for native Fluent custom title bar & buttons.
 */
public class TitleBarTheme {

    // Backgrounds
    public int inactiveBackgroundColor = 0x202020;
    public int activeBackgroundColor   = 0x1b2223;

    // Glyphs
    public int glyphColorActive        = 0xffffff;
    public int glyphColorInactive      = 0x797979;
    public int glyphColorInactiveHover = 0xfcfcfc;

    // Min / Max buttons
    public int inactiveButtonHoverBg   = 0x2d2d2d;
    public int inactiveButtonPressedBg = 0x252525;
    public int activeButtonHoverBg     = 0x282f30;
    public int activeButtonPressedBg   = 0x252c2c;

    // Close button
    public int activeCloseHoverBg      = 0xc42b1c;
    public int activeClosePressedBg    = 0xb22a1c;
    public int closeHoverGlyphColor    = 0xffffff;

    public TitleBarTheme() {
    }

    /**
     * Default Fluent Dark palette matching Windows 11 Snipping Tool / Terminal.
     */
    public static TitleBarTheme fluentDark() {
        return new TitleBarTheme();
    }
}
