package fasttheme;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Flat, high-performance in-memory representation of a theme state.
 * Stores 32-bit ARGB packed integer colors in a contiguous primitive array.
 */
public final class ThemeData {

    public static final int MAGIC = 0x4654484D; // "FTHM"
    public static final short FORMAT_VERSION = 1;

    private final String name;
    private final int[] values;

    public ThemeData(String name) {
        this(name, new int[ThemeKeys.COUNT]);
    }

    public ThemeData(String name, int[] values) {
        this.name = (name != null && !name.trim().isEmpty()) ? name.trim() : "Unnamed";
        if (values == null) {
            this.values = new int[ThemeKeys.COUNT];
        } else if (values.length == ThemeKeys.COUNT) {
            this.values = Arrays.copyOf(values, ThemeKeys.COUNT);
        } else {
            this.values = new int[ThemeKeys.COUNT];
            System.arraycopy(values, 0, this.values, 0, Math.min(values.length, ThemeKeys.COUNT));
        }
    }

    public String getName() {
        return name;
    }

    /**
     * Retrieves the 32-bit ARGB color value for the given slot ID.
     * Zero-allocation, direct array access.
     */
    public int get(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < values.length) {
            return values[slotIndex];
        }
        return 0;
    }

    /**
     * Retrieves the 32-bit ARGB color value by string key name.
     */
    public int get(String keyName) {
        int idx = ThemeKeys.indexOf(keyName);
        return idx != -1 ? get(idx) : 0;
    }

    /**
     * Sets the 32-bit ARGB color value for the given slot ID.
     */
    public void set(int slotIndex, int argb) {
        if (slotIndex >= 0 && slotIndex < values.length) {
            values[slotIndex] = argb;
        }
    }

    /**
     * Sets the 32-bit ARGB color value by string key name.
     */
    public void set(String keyName, int argb) {
        int idx = ThemeKeys.indexOf(keyName);
        if (idx != -1) {
            set(idx, argb);
        }
    }

    /**
     * Direct reference to the raw primitive array (read-only usage recommended).
     */
    public int[] getRawValues() {
        return values;
    }

    /**
     * Serializes this theme into the ultra-compact .themebin binary format.
     */
    public byte[] toBinary() {
        byte[] nameBytes = name.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        int totalSize = 4 + 2 + 2 + nameBytes.length + 2 + (values.length * 4);
        ByteBuffer buf = ByteBuffer.allocate(totalSize).order(ByteOrder.LITTLE_ENDIAN);

        buf.putInt(MAGIC);
        buf.putShort(FORMAT_VERSION);
        buf.putShort((short) nameBytes.length);
        buf.put(nameBytes);
        buf.putShort((short) values.length);

        for (int v : values) {
            buf.putInt(v);
        }

        return buf.array();
    }

    /**
     * Serializes this theme into the human-readable .theme text format.
     */
    public String toText() {
        StringBuilder sb = new StringBuilder(1024);
        sb.append("# FastTheme Definition\r\n");
        sb.append("THEME = ").append(name).append("\r\n\r\n");

        for (int i = 0; i < values.length; i++) {
            String keyName = ThemeKeys.nameOf(i);
            if (keyName != null) {
                int c = values[i];
                int a = (c >>> 24) & 0xFF;
                int r = (c >>> 16) & 0xFF;
                int g = (c >>> 8) & 0xFF;
                int b = c & 0xFF;

                if (a == 255) {
                    sb.append(String.format("%-32s = %d,%d,%d\r\n", keyName, r, g, b));
                } else {
                    sb.append(String.format("%-32s = %d,%d,%d,%d\r\n", keyName, r, g, b, a));
                }
            }
        }
        return sb.toString();
    }

    public ThemeData copy() {
        return new ThemeData(this.name, this.values);
    }
}
