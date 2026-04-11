package fasttheme;

public class demo {
    public static void main(String[] args) throws Exception {
        System.out.println("FastTheme Demo - Resolution/PPI Change Monitor");
        System.out.println("=============================================");
        
        FastTheme theme = new FastTheme();
        theme.setListener(new FastTheme.ThemeListener() {
            @Override
            public void onResolutionChanged(int width, int height, int dpi, int refreshRate) {
                int scalePercent = (dpi * 100) / 96;
                String orientation = (width > height) ? "LANDSCAPE" : "PORTRAIT";
                System.out.println("EVENT: Resolution changed to " + width + "x" + height + 
                                 " | Scale: " + scalePercent + "% (DPI: " + dpi + ")" +
                                 " | " + refreshRate + "Hz" +
                                 " | " + orientation);
            }
            
            @Override
            public void onOrientationChanged(FastTheme.Orientation orientation) {
                // Orientation is now shown in resolution event
            }
            
            @Override
            public void onInitialState(int width, int height, int dpi, int refreshRate, FastTheme.Orientation orientation, boolean isDarkTheme) {
                int scalePercent = (dpi * 100) / 96;
                System.out.println("INIT: Resolution " + width + "x" + height + 
                                 " | Scale: " + scalePercent + "% (DPI: " + dpi + ")" +
                                 " | " + refreshRate + "Hz" +
                                 " | " + orientation +
                                 " | Theme: " + (isDarkTheme ? "DARK" : "LIGHT"));
            }
            
            @Override
            public void onThemeChanged(boolean isDarkTheme) {
                System.out.println("EVENT: Theme changed to " + (isDarkTheme ? "DARK" : "LIGHT") + " mode");
            }
        });

        if (theme.startMonitoring()) {
            System.out.println("Monitoring started. Change display resolution or scale to test.");
            System.out.println("Press Enter to stop monitoring and exit.");
            System.in.read();
            theme.stopMonitoring();
            System.out.println("Monitoring stopped.");
        } else {
            System.err.println("Failed to start monitoring!");
        }
    }
}
