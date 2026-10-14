package fasttheme;

/**
 * WindowFrameTheme - Configuration palette for native Fluent custom window frame, title bar and caption buttons.
 */
public class WindowFrameTheme {

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

    public WindowFrameTheme() {
    }

    /**
     * Default Fluent Dark palette matching Windows 11 Snipping Tool / Terminal.
     */
    public static WindowFrameTheme fluentDark() {
        return new WindowFrameTheme();
    }

    /**
     * Default Fluent Light palette matching Windows 11 Light Mode apps.
     */
    public static WindowFrameTheme fluentLight() {
        WindowFrameTheme t = new WindowFrameTheme();
        t.inactiveBackgroundColor = 0xf3f3f3;
        t.activeBackgroundColor   = 0xf9f9f9;
        t.glyphColorActive        = 0x1a1a1a;
        t.glyphColorInactive      = 0x8a8a8a;
        t.glyphColorInactiveHover = 0x1a1a1a;
        t.inactiveButtonHoverBg   = 0xe5e5e5;
        t.inactiveButtonPressedBg = 0xcccccc;
        t.activeButtonHoverBg     = 0xe9e9e9;
        t.activeButtonPressedBg   = 0xdedede;
        t.activeCloseHoverBg      = 0xc42b1c;
        t.activeClosePressedBg    = 0xb22a1c;
        t.closeHoverGlyphColor    = 0xffffff;
        return t;
    }

    /**
     * Constructs a WindowFrameTheme dynamically derived from a ThemeData instance.
     * Extracts colors for window frame, buttons, and glyphs from dynamic theme keys.
     */
    public static WindowFrameTheme fromTheme(ThemeData data) {
        WindowFrameTheme t = new WindowFrameTheme();
        if (data != null) {
            t.updateFromTheme(data);
        }
        return t;
    }

    /**
     * Updates this WindowFrameTheme palette from a given ThemeData instance.
     */
    public void updateFromTheme(ThemeData data) {
        if (data == null) return;

        int inactBg = data.get("TITLE_BAR_INACTIVE_BACKGROUND");
        if (inactBg == 0) inactBg = data.get("WINDOW_INACTIVE_BACKGROUND");
        if (inactBg != 0) this.inactiveBackgroundColor = inactBg & 0xFFFFFF;

        int actBg = data.get("TITLE_BAR_BACKGROUND");
        if (actBg != 0) this.activeBackgroundColor = actBg & 0xFFFFFF;

        int glyphAct = data.get("TITLE_BAR_TEXT");
        if (glyphAct != 0) this.glyphColorActive = glyphAct & 0xFFFFFF;

        int glyphInact = data.get("TITLE_BAR_INACTIVE_TEXT");
        if (glyphInact != 0) this.glyphColorInactive = glyphInact & 0xFFFFFF;

        int btnHover = data.get("BUTTON_HOVER");
        if (btnHover != 0) this.activeButtonHoverBg = btnHover & 0xFFFFFF;

        int btnPressed = data.get("BUTTON_PRESSED");
        if (btnPressed != 0) this.activeButtonPressedBg = btnPressed & 0xFFFFFF;
    }
}
