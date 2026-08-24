package fasttheme;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.util.concurrent.atomic.AtomicBoolean;

public class FastThemeTest {

    @Test
    public void testKeyMatrixLookup() {
        assertEquals(ThemeKeys.TITLE_BAR_BACKGROUND, ThemeKeys.indexOf("TITLE_BAR_BACKGROUND"));
        assertEquals(ThemeKeys.ACCENT_PRIMARY, ThemeKeys.indexOf("accent_primary"));
        assertEquals("WINDOW_BACKGROUND", ThemeKeys.nameOf(ThemeKeys.WINDOW_BACKGROUND));
        assertEquals(-1, ThemeKeys.indexOf("NON_EXISTENT_KEY"));
    }

    @Test
    public void testThemeDataOperations() {
        ThemeData theme = new ThemeData("Custom");
        theme.set(ThemeKeys.WINDOW_BACKGROUND, ThemeColorUtil.rgb(20, 20, 20));

        assertEquals(ThemeColorUtil.rgb(20, 20, 20), theme.get(ThemeKeys.WINDOW_BACKGROUND));
        assertEquals(ThemeColorUtil.rgb(20, 20, 20), theme.get("WINDOW_BACKGROUND"));

        // Binary serialization roundtrip
        byte[] bin = theme.toBinary();
        assertNotNull(bin);
        assertTrue(bin.length > 0);

        ThemeData parsed = ThemeParser.parseBinary(bin);
        assertEquals("Custom", parsed.getName());
        assertEquals(ThemeColorUtil.rgb(20, 20, 20), parsed.get(ThemeKeys.WINDOW_BACKGROUND));
    }

    @Test
    public void testDynamicCustomKeysAndElasticSlots() {
        int customSlot1 = ThemeKeys.register("MY_CUSTOM_GLOW");
        assertTrue(customSlot1 >= ThemeKeys.STANDARD_COUNT);
        assertEquals(customSlot1, ThemeKeys.indexOf("MY_CUSTOM_GLOW"));
        assertEquals("MY_CUSTOM_GLOW", ThemeKeys.nameOf(customSlot1));

        ThemeData theme = new ThemeData("DynamicTest");
        theme.set(customSlot1, ThemeColorUtil.rgb(255, 128, 0));
        assertEquals(ThemeColorUtil.rgb(255, 128, 0), theme.get(customSlot1));
        assertEquals(ThemeColorUtil.rgb(255, 128, 0), theme.get("MY_CUSTOM_GLOW"));

        // Elastic set by string name
        theme.set("ANOTHER_CUSTOM_KEY", ThemeColorUtil.rgb(0, 255, 100));
        assertEquals(ThemeColorUtil.rgb(0, 255, 100), theme.get("ANOTHER_CUSTOM_KEY"));

        // Text parsing with auto-registered custom keys
        String customThemeText = """
                THEME = ElasticCustom
                BRAND_SPECIAL_COLOR = 120, 200, 255
                BRAND_SUB_COLOR = @BRAND_SPECIAL_COLOR
                """;
        ThemeData parsedCustom = ThemeParser.parseText(customThemeText);
        assertEquals(ThemeColorUtil.rgb(120, 200, 255), parsedCustom.get("BRAND_SPECIAL_COLOR"));
        assertEquals(ThemeColorUtil.rgb(120, 200, 255), parsedCustom.get("BRAND_SUB_COLOR"));
    }

    @Test
    public void testThemeTextParsingWithAliases() {
        String themeText = """
                # Test Theme
                THEME = Test Cyber
                
                ACCENT_PRIMARY = #00E0FF
                WINDOW_BACKGROUND = 19, 20, 31
                BUTTON_NORMAL_BACKGROUND = @WINDOW_BACKGROUND
                BUTTON_HOVER_BORDER = @ACCENT_PRIMARY
                """;

        ThemeData parsed = ThemeParser.parseText(themeText);
        assertEquals("Test Cyber", parsed.getName());

        int expectedAccent = ThemeColorUtil.parseColor("#00E0FF");
        int expectedBg = ThemeColorUtil.rgb(19, 20, 31);

        assertEquals(expectedAccent, parsed.get(ThemeKeys.ACCENT_PRIMARY));
        assertEquals(expectedBg, parsed.get(ThemeKeys.WINDOW_BACKGROUND));
        assertEquals(expectedBg, parsed.get(ThemeKeys.BUTTON_NORMAL_BACKGROUND));
        assertEquals(expectedAccent, parsed.get(ThemeKeys.BUTTON_HOVER_BORDER));
    }

    @Test
    public void testColorMathAndContrast() {
        int darkBg = ThemeColorUtil.rgb(20, 20, 20);
        int lightBg = ThemeColorUtil.rgb(240, 240, 240);

        assertEquals(0xFFFFFFFF, ThemeColorUtil.getContrastForeground(darkBg));
        assertEquals(0xFF111111, ThemeColorUtil.getContrastForeground(lightBg));

        int blue = ThemeColorUtil.rgb(0, 100, 200);
        int lighter = ThemeColorUtil.lighten(blue, 0.2f);
        assertTrue(ThemeColorUtil.red(lighter) >= ThemeColorUtil.red(blue));
        assertTrue(ThemeColorUtil.green(lighter) >= ThemeColorUtil.green(blue));
    }

    @Test
    public void testGlobalFastThemeStateAndListeners() {
        AtomicBoolean notified = new AtomicBoolean(false);
        ThemeListener listener = newTheme -> notified.set(true);

        FastTheme.addListener(listener);
        try {
            ThemeData light = ThemeParser.loadDefaultLight();
            FastTheme.set(light);

            assertTrue(notified.get());
            assertEquals(light.get(ThemeKeys.WINDOW_BACKGROUND), FastTheme.get(ThemeKeys.WINDOW_BACKGROUND));
            assertNotNull(FastTheme.getColor(ThemeKeys.WINDOW_BACKGROUND));
            assertTrue(FastTheme.getAnsiFg(ThemeKeys.ACCENT_PRIMARY).contains("\u001B[38;2;"));

            // Test FastTheme.load(String)
            FastTheme.load("THEME = DirectLoad\nACCENT_PRIMARY = 255,0,128\n");
            assertEquals(ThemeColorUtil.rgb(255, 0, 128), FastTheme.get(ThemeKeys.ACCENT_PRIMARY));
            assertEquals("DirectLoad", FastTheme.current().getName());
        } finally {
            FastTheme.removeListener(listener);
        }
    }
}
