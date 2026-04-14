package fasttheme;

/**
 * Basic FastTheme Demo - Monitor theme and display changes
 */
public class ThemeDemo {
    public static void main(String[] args) {
        System.out.println("=== FastTheme Demo ===\n");
        
        FastTheme theme = new FastTheme();
        
        theme.setListener(new FastTheme.ThemeListener() {
            @Override
            public void onInitialState(int width, int height, int dpi, int refreshRate, 
                                       FastTheme.Orientation orientation, boolean isDarkTheme) {
                System.out.println("Initial State:");
                System.out.println("  Display: " + width + "x" + height + " @ " + dpi + "DPI");
                System.out.println("  Refresh: " + refreshRate + "Hz");
                System.out.println("  Theme: " + (isDarkTheme ? "DARK" : "LIGHT"));
                System.out.println();
            }
            
            @Override
            public void onResolutionChanged(int width, int height, int dpi, int refreshRate) {
                System.out.println("Resolution changed: " + width + "x" + height + " @ " + dpi + "DPI");
            }
            
            @Override
            public void onOrientationChanged(FastTheme.Orientation orientation) {
                System.out.println("Orientation changed: " + orientation);
            }
            
            @Override
            public void onThemeChanged(boolean isDarkTheme) {
                System.out.println("Theme changed: " + (isDarkTheme ? "DARK MODE" : "LIGHT MODE"));
            }
        });
        
        System.out.println("Starting monitoring... (Press Ctrl+C to stop)");
        theme.startMonitoring();
        
        // Keep alive
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            theme.stopMonitoring();
        }
    }
}
