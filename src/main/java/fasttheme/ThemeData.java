package fasttheme;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Elastic, high-performance in-memory representation of a theme state.
 * Stores 32-bit ARGB packed integer colors in a contiguous primitive array that
 * automatically resizes to accommodate dynamic custom slots.
 */
public final class ThemeData {

    /**
     * Magic header constant for binary .themebin files ("FTHM").
     */
    public static final int MAGIC = 0x4654484D;

    /**
     * Binary format version.
     */
    public static final short FORMAT_VERSION = 1;

    private final String name;
    private int[] values;

    /**
     * Constructs a ThemeData instance with the given theme name.
     *
     * @param name Name of the theme.
     */
    public ThemeData(String name) {
        this(name, new int[ThemeKeys.count()]);
    }

    /**
     * Constructs a ThemeData instance with a specified initial capacity.
     *
     * @param name Name of the theme.
     * @param initialCapacity Minimum slot capacity to preallocate.
     */
    public ThemeData(String name, int initialCapacity) {
        this.name = (name != null && !name.trim().isEmpty()) ? name.trim() : "Unnamed";
        int cap = Math.max(ThemeKeys.count(), initialCapacity);
        this.values = new int[cap];
    }

    /**
     * Constructs a ThemeData instance initialized with an array of ARGB values.
     *
     * @param name Name of the theme.
     * @param values Initial color array.
     */
    public ThemeData(String name, int[] values) {
        this.name = (name != null && !name.trim().isEmpty()) ? name.trim() : "Unnamed";
        int required = ThemeKeys.count();
        if (values == null) {
            this.values = new int[required];
        } else {
            int len = Math.max(values.length, required);
            this.values = new int[len];
            System.arraycopy(values, 0, this.values, 0, values.length);
        }
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity > values.length) {
            int newCap = Math.max(values.length * 2, minCapacity);
            values = Arrays.copyOf(values, newCap);
        }
    }

    /**
     * Returns the current capacity of the internal color array.
     *
     * @return Total allocated slot capacity.
     */
    public int capacity() {
        return values.length;
    }

    /**
     * Serializes this theme into the ultra-compact .themebin binary format.
     *
     * @return Byte array containing serialized binary payload.
     */
    public byte[] toBinary() {
        byte[] nameBytes = name.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        int activeSlots = Math.min(values.length, ThemeKeys.count());
        int totalSize = 4 + 2 + 2 + nameBytes.length + 2 + (activeSlots * 4);
        ByteBuffer buf = ByteBuffer.allocate(totalSize).order(ByteOrder.LITTLE_ENDIAN);

        buf.putInt(MAGIC);
        buf.putShort(FORMAT_VERSION);
        buf.putShort((short) nameBytes.length);
        buf.put(nameBytes);
        buf.putShort((short) activeSlots);

        for (int i = 0; i < activeSlots; i++) {
            buf.putInt(values[i]);
        }

        return buf.array();
    }

    /**
     * Serializes this theme into the human-readable .theme text format.
     *
     * @return Formatted .theme text content.
     */
    public String toText() {
        StringBuilder sb = new StringBuilder(1024);
        sb.append("# FastTheme Definition\r\n");
        sb.append("THEME = ").append(name).append("\r\n\r\n");

        int activeSlots = Math.min(values.length, ThemeKeys.count());
        for (int i = 0; i < activeSlots; i++) {
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

    /**
     * Creates a deep copy of this ThemeData instance.
     *
     * @return Cloned ThemeData instance.
     */
    public ThemeData copy() {
        return new ThemeData(this.name, this.values);
    }

    /**
     * Retrieves the 32-bit ARGB color value for the given slot ID.
     * Zero-allocation direct primitive array read.
     *
     * @param slotIndex Integer slot index.
     * @return Packed 32-bit ARGB color, or 0 if slot is unpopulated/out of range.
     */
    public int get(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < values.length) {
            return values[slotIndex];
        }
        return 0;
    }

    /**
     * Returns the name of this theme.
     *
     * @return Theme name string.
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves the 32-bit ARGB color value by string key name.
     *
     * @param keyName String key name.
     * @return Packed 32-bit ARGB color, or 0 if key is unregistered.
     */
    public int get(String keyName) {
        int idx = ThemeKeys.indexOf(keyName);
        return idx != -1 ? get(idx) : 0;
    }

    /**
     * Returns direct reference to the raw primitive ARGB values array.
     *
     * @return Underlying int[] array.
     */
    public int[] getRawValues() {
        return values;
    }

    /**
     * Sets the 32-bit ARGB color value for the given slot ID, expanding capacity if needed.
     *
     * @param slotIndex Integer slot index.
     * @param argb Packed 32-bit ARGB color value.
     */
    public void set(int slotIndex, int argb) {
        if (slotIndex < 0) return;
        ensureCapacity(slotIndex + 1);
        values[slotIndex] = argb;
    }

    /**
     * Sets the 32-bit ARGB color value by string key name, auto-registering the key if custom.
     *
     * @param keyName String key name.
     * @param argb Packed 32-bit ARGB color value.
     */
    public void set(String keyName, int argb) {
        int idx = ThemeKeys.getOrRegister(keyName);
        if (idx != -1) {
            set(idx, argb);
        }
    }
}
