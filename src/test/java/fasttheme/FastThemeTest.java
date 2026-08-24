package fasttheme;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicBoolean;

public class FastThemeTest {

    @BeforeEach
    public void setup() {
        ThemeKeys.clear();
    }

    @Test
    public void testSchemaFreeDynamicKeyRegistry() {
        int slot1 = ThemeKeys.slot("custom.accent");
        int slot2 = ThemeKeys.slot("custom.accent");
        assertEquals(slot1, slot2);

        int slot3 = ThemeKeys.slot("sidebar.bg");
        assertNotEquals(slot1, slot3);

        assertEquals("CUSTOM.ACCENT", ThemeKeys.nameOf(slot1));
        assertEquals(slot1, ThemeKeys.indexOf("custom.accent"));
        assertEquals(2, ThemeKeys.count());
    }

    @Test
    public void testCustomThemeDataOperations() {
        ThemeData theme = new ThemeData("MyCustomAppTheme");
        theme.set("window.bg", ThemeColorUtil.rgb(20, 20, 20));
        theme.set("window.fg", ThemeColorUtil.rgb(240, 240, 240));

        assertEquals(ThemeColorUtil.rgb(20, 20, 20), theme.get("window.bg"));
        assertEquals(ThemeColorUtil.rgb(240, 240, 240), theme.get("window.fg"));

        int bgSlot = ThemeKeys.indexOf("window.bg");
        assertTrue(bgSlot >= 0);
        assertEquals(ThemeColorUtil.rgb(20, 20, 20), theme.get(bgSlot));

        // Binary serialization roundtrip
        byte[] bin = theme.toBinary();
        assertNotNull(bin);
        assertTrue(bin.length > 0);

        ThemeData parsed = ThemeParser.parseBinary(bin);
        assertEquals("MyCustomAppTheme", parsed.getName());
        assertEquals(ThemeColorUtil.rgb(20, 20, 20), parsed.get(bgSlot));
    }

    @Test
    public void testTextParsingWithCompletelyCustomKeysAndAliases() {
        String themeText = """
                # Pure Custom Theme Definition
                THEME = Neon Cyberpunk
                
                neon.pink = #FF007F
                neon.cyan = #00F0FF
                surface.dark = 10, 12, 18
                
                editor.background = @surface.dark
                editor.cursor = @neon.pink
                button.border = @neon.cyan
                """;

        ThemeData parsed = ThemeParser.parseText(themeText);
        assertEquals("Neon Cyberpunk", parsed.getName());

        int expectedPink = ThemeColorUtil.parseColor("#FF007F");
        int expectedCyan = ThemeColorUtil.parseColor("#00F0FF");
        int expectedSurface = ThemeColorUtil.rgb(10, 12, 18);

        assertEquals(expectedPink, parsed.get("neon.pink"));
        assertEquals(expectedCyan, parsed.get("neon.cyan"));
        assertEquals(expectedSurface, parsed.get("surface.dark"));
        assertEquals(expectedSurface, parsed.get("editor.background"));
        assertEquals(expectedPink, parsed.get("editor.cursor"));
        assertEquals(expectedCyan, parsed.get("button.border"));
    }

    @Test
    public void testColorMathAndWCAGContrast() {
        int darkBg = ThemeColorUtil.rgb(15, 15, 15);
        int lightBg = ThemeColorUtil.rgb(245, 245, 245);

        assertEquals(0xFFFFFFFF, ThemeColorUtil.getContrastForeground(darkBg));
        assertEquals(0xFF111111, ThemeColorUtil.getContrastForeground(lightBg));

        int blue = ThemeColorUtil.rgb(0, 100, 200);
        int lighter = ThemeColorUtil.lighten(blue, 0.2f);
        assertTrue(ThemeColorUtil.red(lighter) >= ThemeColorUtil.red(blue));
        assertTrue(ThemeColorUtil.green(lighter) >= ThemeColorUtil.green(blue));

        int blended = ThemeColorUtil.blend(ThemeColorUtil.rgb(0, 0, 0), ThemeColorUtil.rgb(100, 100, 100), 0.5f);
        assertEquals(50, ThemeColorUtil.red(blended));
    }

    @Test
    public void testGlobalFastThemeStateAndObservers() {
        AtomicBoolean notified = new AtomicBoolean(false);
        ThemeListener listener = newTheme -> notified.set(true);

        FastTheme.addListener(listener);
        try {
            ThemeData custom = new ThemeData("LiveTheme");
            custom.set("app.bg", ThemeColorUtil.rgb(32, 32, 32));
            FastTheme.set(custom);

            assertTrue(notified.get());
            assertEquals(ThemeColorUtil.rgb(32, 32, 32), FastTheme.get("app.bg"));
            assertNotNull(FastTheme.getColor("app.bg"));

            // Test FastTheme.load(String)
            FastTheme.load("THEME = LoadedLive\ncustom.brand = 255,0,128\n");
            assertEquals(ThemeColorUtil.rgb(255, 0, 128), FastTheme.get("custom.brand"));
            assertEquals("LoadedLive", FastTheme.current().getName());
        } finally {
            FastTheme.removeListener(listener);
        }
    }
}
