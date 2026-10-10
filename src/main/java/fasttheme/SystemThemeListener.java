package fasttheme;

/**
 * Listener interface for native Windows OS theme and personalization change events.
 */
@FunctionalInterface
public interface SystemThemeListener {

    /**
     * Invoked when Windows theme settings change (Dark/Light mode or Accent Color).
     *
     * @param isDark      true if AppsUseLightTheme is false (Windows App Dark Mode is active)
     * @param accentColor the current ARGB accent color (0xAARRGGBB)
     */
    void onThemeChanged(boolean isDark, int accentColor);
}
