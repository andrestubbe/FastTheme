package fasttheme;

/**
 * Listener interface for observing dynamic theme transitions.
 */
@FunctionalInterface
public interface ThemeListener {

    /**
     * Invoked when the global active theme changes.
     *
     * @param newTheme the newly activated ThemeData
     */
    void onThemeChanged(ThemeData newTheme);
}
