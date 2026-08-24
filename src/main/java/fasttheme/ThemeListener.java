package fasttheme;

/**
 * Functional observer interface for subscribing to global theme changes in FastTheme.
 */
@FunctionalInterface
public interface ThemeListener {

    /**
     * Invoked when the globally active theme changes.
     *
     * @param newTheme The newly activated {@link ThemeData} instance.
     */
    void onThemeChanged(ThemeData newTheme);
}
