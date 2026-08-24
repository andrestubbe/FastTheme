package fasttheme.benchmark;

import fasttheme.FastTheme;
import fasttheme.ThemeColorUtil;
import fasttheme.ThemeData;
import fasttheme.ThemeKeys;
import fasttheme.ThemeParser;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class FastThemeBenchmark {

    private int slot;
    private String key;
    private byte[] binaryPayload;
    private String themeText;
    private int sampleBgColor;

    @Setup
    public void setup() {
        themeText = """
                THEME = Benchmark Theme
                window.background = #13141F
                window.text = #C3CDF7
                titlebar.background = #0C0E10
                accent.cyan = #00E0FF
                button.hover = @accent.cyan
                """;

        ThemeData theme = ThemeParser.parseText(themeText);
        FastTheme.set(theme);

        key = "window.background";
        slot = ThemeKeys.slot(key);
        binaryPayload = theme.toBinary();
        sampleBgColor = ThemeColorUtil.rgb(19, 20, 31);
    }

    @Benchmark
    public int benchmarkCachedSlotAccess() {
        return FastTheme.get(slot);
    }

    @Benchmark
    public int benchmarkStringKeyLookup() {
        return FastTheme.get(key);
    }

    @Benchmark
    public ThemeData benchmarkTextParsing() {
        return ThemeParser.parseText(themeText);
    }

    @Benchmark
    public ThemeData benchmarkBinaryDeserialization() {
        return ThemeParser.parseBinary(binaryPayload);
    }

    @Benchmark
    public int benchmarkWcagContrastForeground() {
        return ThemeColorUtil.getContrastForeground(sampleBgColor);
    }
}
