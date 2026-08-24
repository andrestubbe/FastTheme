package fasttheme;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Schema-Free Dynamic Key Registry for FastTheme.
 * Allocates and manages integer slot indices on-the-fly for any arbitrary string key names.
 */
public final class ThemeKeys {

    private static final CopyOnWriteArrayList<String> NAMES = new CopyOnWriteArrayList<>();
    private static final ConcurrentHashMap<String, Integer> NAME_TO_INDEX = new ConcurrentHashMap<>(128);

    private ThemeKeys() {
    }

    /**
     * Registers a key name dynamically and returns its unique allocated slot index.
     * If already registered, returns the existing slot index.
     *
     * @param keyName The string name of the theme key.
     * @return The unique allocated integer slot index.
     */
    public static synchronized int register(String keyName) {
        if (keyName == null || keyName.trim().isEmpty()) {
            throw new IllegalArgumentException("Key name cannot be null or empty");
        }
        String normalized = keyName.trim().toUpperCase();
        Integer existing = NAME_TO_INDEX.get(normalized);
        if (existing != null) {
            return existing;
        }

        int newIndex = NAMES.size();
        NAMES.add(normalized);
        NAME_TO_INDEX.put(normalized, newIndex);
        return newIndex;
    }

    /**
     * Retrieves the slot index for a given key, or dynamically registers it on demand.
     *
     * @param keyName The string key name.
     * @return The unique integer slot index, or -1 if key name is null/empty.
     */
    public static int slot(String keyName) {
        return getOrRegister(keyName);
    }

    /**
     * Returns the slot index for a given key name, or -1 if not registered.
     *
     * @param keyName The string key name.
     * @return The allocated slot index, or -1 if not found.
     */
    public static int indexOf(String keyName) {
        if (keyName == null) return -1;
        Integer idx = NAME_TO_INDEX.get(keyName.trim().toUpperCase());
        return idx != null ? idx : -1;
    }

    /**
     * Returns the string name for a given slot index, or null if out of range.
     *
     * @param index The integer slot index.
     * @return The key name string, or null if index is invalid.
     */
    public static String nameOf(int index) {
        if (index >= 0 && index < NAMES.size()) {
            return NAMES.get(index);
        }
        return null;
    }

    /**
     * Returns the total number of currently registered dynamic slots.
     *
     * @return Total count of registered slots.
     */
    public static int count() {
        return NAMES.size();
    }

    /**
     * Clears all registered keys from the dynamic registry.
     */
    public static synchronized void clear() {
        NAMES.clear();
        NAME_TO_INDEX.clear();
    }

    /**
     * Retrieves the slot index for a given key, or dynamically registers it on demand.
     *
     * @param keyName The string key name.
     * @return The slot index, or -1 if the key name is null/empty.
     */
    public static int getOrRegister(String keyName) {
        if (keyName == null || keyName.trim().isEmpty()) return -1;
        String normalized = keyName.trim().toUpperCase();
        Integer idx = NAME_TO_INDEX.get(normalized);
        if (idx != null) {
            return idx;
        }
        return register(keyName);
    }

    /**
     * Returns an unmodifiable map of all registered key names and their slot indices.
     *
     * @return Map of registered key names to slot indices.
     */
    public static Map<String, Integer> getAllKeys() {
        return Collections.unmodifiableMap(NAME_TO_INDEX);
    }
}
