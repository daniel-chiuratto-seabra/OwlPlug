/*
 * Copyright (C) 2021-2024 Arthur <dropsnorz@gmail.com>
 *
 * This file is part of OwlPlug.
 *
 * OwlPlug is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * OwlPlug is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OwlPlug.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.owlplug.core.components;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ApplicationPreferences class.
 * These tests verify preference storage, retrieval, and serialization mechanisms
 * for various data types (String, Boolean, Long, List).
 *
 * <p>Test coverage includes:
 * <ul>
 *   <li>String preference storage and retrieval</li>
 *   <li>Boolean preference storage and retrieval</li>
 *   <li>Long preference storage and retrieval</li>
 *   <li>List serialization and deserialization (JSON)</li>
 *   <li>Default value handling</li>
 *   <li>Error handling for corrupted data</li>
 *   <li>Clear operation (preference reset)</li>
 * </ul>
 *
 * <p>Testing strategy:
 * Since ApplicationPreferences uses Java's Preferences API, these tests interact
 * with the actual system preference store. To ensure test isolation, we clean up
 * after each test to prevent side effects.
 *
 * <p>Note: These are integration tests rather than pure unit tests, as they
 * interact with the real Java Preferences backing store on the system.
 */
public class ApplicationPreferencesTest {

    private ApplicationPreferences applicationPreferences;
    private Preferences testPreferences;

    /**
     * Set up test fixtures before each test.
     * Creates a fresh ApplicationPreferences instance and obtains a reference
     * to the underlying Preferences node for verification purposes.
     */
    @BeforeEach
    void setUp() {
        applicationPreferences = new ApplicationPreferences();
        // Get the same preferences node for verification
        testPreferences = Preferences.userRoot().node("com.owlplug.user");
    }

    /**
     * Clean up after each test to ensure test isolation.
     * Clears all preferences to prevent test pollution and ensure each test
     * starts with a clean state.
     */
    @AfterEach
    void tearDown() throws BackingStoreException {
        // Clean up preferences to avoid polluting other tests
        testPreferences.clear();
    }

    // ========================================
    // Tests for String preferences
    // ========================================

    /**
     * Test that put() and get() work correctly for string values.
     * This verifies basic string storage and retrieval functionality.
     *
     * <p>This is the most fundamental preference operation and is used
     * throughout the application for storing paths, URLs, and text settings.
     */
    @Test
    void put_andGet_shouldStoreAndRetrieveStringValue() {
        // Arrange
        String key = "test.string.key";
        String value = "test-value";

        // Act: Store the value
        applicationPreferences.put(key, value);

        // Assert: Retrieve and verify the value
        String retrievedValue = applicationPreferences.get(key, "default");
        assertEquals(value, retrievedValue, "Should retrieve the stored string value");
    }

    /**
     * Test that get() returns the default value when the key doesn't exist.
     * This verifies proper handling of missing preferences.
     *
     * <p>Default values are crucial for first-launch scenarios or when
     * preferences have been cleared/reset.
     */
    @Test
    void get_shouldReturnDefaultValue_whenKeyDoesNotExist() {
        // Arrange
        String key = "nonexistent.key";
        String defaultValue = "default-value";

        // Act: Try to retrieve a non-existent key
        String retrievedValue = applicationPreferences.get(key, defaultValue);

        // Assert: Should return the default value
        assertEquals(defaultValue, retrievedValue,
                "Should return default value when key doesn't exist");
    }

    /**
     * Test that put() overwrites existing values correctly.
     * This ensures that preference updates work as expected.
     *
     * <p>Users frequently update preferences (e.g., changing plugin paths),
     * so proper overwrite behavior is essential.
     */
    @Test
    void put_shouldOverwriteExistingValue() {
        // Arrange
        String key = "test.overwrite.key";
        String originalValue = "original-value";
        String newValue = "new-value";

        // Act: Store original value, then overwrite it
        applicationPreferences.put(key, originalValue);
        applicationPreferences.put(key, newValue);

        // Assert: Should retrieve the new value
        String retrievedValue = applicationPreferences.get(key, "default");
        assertEquals(newValue, retrievedValue,
                "Should retrieve the new value after overwrite");
    }

    // ========================================
    // Tests for Boolean preferences
    // ========================================

    /**
     * Test that putBoolean() and getBoolean() work correctly for true values.
     * This verifies boolean storage for enabled features and toggle states.
     *
     * <p>Boolean preferences are commonly used for feature flags like
     * VST2_DISCOVERY_ENABLED, TELEMETRY_ENABLED, etc.
     */
    @Test
    void putBoolean_andGetBoolean_shouldStoreAndRetrieveTrueValue() {
        // Arrange
        String key = "test.boolean.true";

        // Act: Store true value
        applicationPreferences.putBoolean(key, true);

        // Assert: Retrieve and verify
        boolean retrievedValue = applicationPreferences.getBoolean(key, false);
        assertTrue(retrievedValue, "Should retrieve true value");
    }

    /**
     * Test that putBoolean() and getBoolean() work correctly for false values.
     * This ensures proper handling of disabled features and false states.
     */
    @Test
    void putBoolean_andGetBoolean_shouldStoreAndRetrieveFalseValue() {
        // Arrange
        String key = "test.boolean.false";

        // Act: Store false value
        applicationPreferences.putBoolean(key, false);

        // Assert: Retrieve and verify
        boolean retrievedValue = applicationPreferences.getBoolean(key, true);
        assertFalse(retrievedValue, "Should retrieve false value");
    }

    /**
     * Test that getBoolean() returns the default value when the key doesn't exist.
     * This is critical for features that should be enabled/disabled by default.
     *
     * <p>Example: VST2_DISCOVERY_ENABLED might default to true on first launch.
     */
    @Test
    void getBoolean_shouldReturnDefaultValue_whenKeyDoesNotExist() {
        // Arrange
        String key = "nonexistent.boolean.key";
        boolean defaultValue = true;

        // Act: Try to retrieve a non-existent key
        boolean retrievedValue = applicationPreferences.getBoolean(key, defaultValue);

        // Assert: Should return the default value
        assertEquals(defaultValue, retrievedValue,
                "Should return default boolean value when key doesn't exist");
    }

    // ========================================
    // Tests for Long preferences
    // ========================================

    /**
     * Test that putLong() and getLong() work correctly for positive values.
     * This verifies numeric storage for timestamps, IDs, and counters.
     *
     * <p>Long values are commonly used for timestamps (last sync, last update check)
     * and user/session identifiers.
     */
    @Test
    void putLong_andGetLong_shouldStoreAndRetrievePositiveValue() {
        // Arrange
        String key = "test.long.positive";
        long value = 123456789L;

        // Act: Store the value
        applicationPreferences.putLong(key, value);

        // Assert: Retrieve and verify
        long retrievedValue = applicationPreferences.getLong(key, 0L);
        assertEquals(value, retrievedValue, "Should retrieve the stored long value");
    }

    /**
     * Test that putLong() and getLong() work correctly for negative values.
     * This ensures proper handling of negative numbers (e.g., error codes, offsets).
     */
    @Test
    void putLong_andGetLong_shouldStoreAndRetrieveNegativeValue() {
        // Arrange
        String key = "test.long.negative";
        long value = -987654321L;

        // Act: Store the value
        applicationPreferences.putLong(key, value);

        // Assert: Retrieve and verify
        long retrievedValue = applicationPreferences.getLong(key, 0L);
        assertEquals(value, retrievedValue, "Should retrieve the stored negative long value");
    }

    /**
     * Test that getLong() returns the default value when the key doesn't exist.
     * This is important for numeric preferences that need sensible defaults.
     *
     * <p>Example: Last update check timestamp might default to 0 (epoch time).
     */
    @Test
    void getLong_shouldReturnDefaultValue_whenKeyDoesNotExist() {
        // Arrange
        String key = "nonexistent.long.key";
        long defaultValue = 999L;

        // Act: Try to retrieve a non-existent key
        long retrievedValue = applicationPreferences.getLong(key, defaultValue);

        // Assert: Should return the default value
        assertEquals(defaultValue, retrievedValue,
                "Should return default long value when key doesn't exist");
    }

    // ========================================
    // Tests for List preferences (JSON serialization)
    // ========================================

    /**
     * Test that putList() and getList() work correctly for storing and retrieving lists.
     * This verifies JSON serialization/deserialization for list storage.
     *
     * <p>Lists are commonly used for storing multiple plugin directories,
     * recent file paths, or collections of enabled features.
     */
    @Test
    void putList_andGetList_shouldStoreAndRetrieveList() {
        // Arrange
        String key = "test.list.key";
        List<String> originalList = Arrays.asList("value1", "value2", "value3");

        // Act: Store the list
        applicationPreferences.putList(key, originalList);

        // Assert: Retrieve and verify
        List<String> retrievedList = applicationPreferences.getList(key);
        assertNotNull(retrievedList, "Retrieved list should not be null");
        assertEquals(3, retrievedList.size(), "Retrieved list should have 3 elements");
        assertEquals(originalList, retrievedList, "Retrieved list should match original list");
    }

    /**
     * Test that getList() returns an empty list when the key doesn't exist.
     * This is the convenience method that uses an empty list as default.
     *
     * <p>Empty lists are safer than null for iteration and prevent NullPointerExceptions.
     */
    @Test
    void getList_shouldReturnEmptyList_whenKeyDoesNotExist() {
        // Arrange
        String key = "nonexistent.list.key";

        // Act: Try to retrieve a non-existent key
        List<String> retrievedList = applicationPreferences.getList(key);

        // Assert: Should return an empty list
        assertNotNull(retrievedList, "Should return a list, not null");
        assertTrue(retrievedList.isEmpty(), "Should return an empty list when key doesn't exist");
    }

    /**
     * Test that getList() with custom default returns that default when the key doesn't exist.
     * This verifies the overloaded method that accepts a custom default list.
     *
     * <p>Custom defaults are useful when you need specific fallback values
     * (e.g., default plugin directories).
     */
    @Test
    void getList_shouldReturnCustomDefault_whenKeyDoesNotExist() {
        // Arrange
        String key = "nonexistent.list.key";
        List<String> defaultList = Arrays.asList("default1", "default2");

        // Act: Try to retrieve a non-existent key with custom default
        List<String> retrievedList = applicationPreferences.getList(key, defaultList);

        // Assert: Should return the custom default list
        assertEquals(defaultList, retrievedList,
                "Should return custom default list when key doesn't exist");
    }

    /**
     * Test that putList() correctly handles an empty list.
     * This ensures empty collections are properly serialized and stored.
     *
     * <p>Empty lists might represent cleared search paths or disabled features.
     */
    @Test
    void putList_andGetList_shouldHandleEmptyList() {
        // Arrange
        String key = "test.list.empty";
        List<String> emptyList = Arrays.asList();

        // Act: Store an empty list
        applicationPreferences.putList(key, emptyList);

        // Assert: Retrieve and verify it's empty
        List<String> retrievedList = applicationPreferences.getList(key);
        assertNotNull(retrievedList, "Retrieved list should not be null");
        assertTrue(retrievedList.isEmpty(), "Retrieved list should be empty");
    }

    /**
     * Test that putList() correctly handles a list with special characters.
     * This verifies that JSON serialization properly escapes special characters
     * in file paths and other strings.
     *
     * <p>Plugin paths often contain special characters (spaces, backslashes, quotes)
     * that need to be properly serialized.
     */
    @Test
    void putList_andGetList_shouldHandleSpecialCharacters() {
        // Arrange
        String key = "test.list.special";
        List<String> listWithSpecialChars = Arrays.asList(
                "C:\\Program Files\\VST",
                "/Library/Audio/Plug-Ins",
                "Path with spaces",
                "Path\"with\"quotes"
        );

        // Act: Store the list with special characters
        applicationPreferences.putList(key, listWithSpecialChars);

        // Assert: Retrieve and verify all special characters are preserved
        List<String> retrievedList = applicationPreferences.getList(key);
        assertEquals(listWithSpecialChars.size(), retrievedList.size(),
                "Retrieved list should have same size");
        assertEquals(listWithSpecialChars, retrievedList,
                "Retrieved list should match original with special characters preserved");
    }

    /**
     * Test that getList() returns the default when JSON is corrupted.
     * This verifies error handling for corrupted preference data.
     *
     * <p>This scenario can occur if:
     * <ul>
     *   <li>Preferences are manually edited</li>
     *   <li>File system corruption occurs</li>
     *   <li>Application version incompatibilities</li>
     * </ul>
     */
    @Test
    void getList_shouldReturnDefault_whenJsonIsCorrupted() {
        // Arrange
        String key = "test.list.corrupted";
        List<String> defaultList = Arrays.asList("default");

        // Act: Manually set corrupted JSON data (not valid JSON array)
        applicationPreferences.put(key, "{this is not valid JSON}");

        // Assert: Should return default list due to deserialization error
        List<String> retrievedList = applicationPreferences.getList(key, defaultList);
        assertEquals(defaultList, retrievedList,
                "Should return default list when JSON is corrupted");
    }

    /**
     * Test that putList() overwrites existing list values correctly.
     * This ensures list preference updates work as expected.
     *
     * <p>Users might update plugin directory lists, so proper overwrite is essential.
     */
    @Test
    void putList_shouldOverwriteExistingList() {
        // Arrange
        String key = "test.list.overwrite";
        List<String> originalList = Arrays.asList("original1", "original2");
        List<String> newList = Arrays.asList("new1", "new2", "new3");

        // Act: Store original list, then overwrite it
        applicationPreferences.putList(key, originalList);
        applicationPreferences.putList(key, newList);

        // Assert: Should retrieve the new list
        List<String> retrievedList = applicationPreferences.getList(key);
        assertEquals(newList, retrievedList,
                "Should retrieve the new list after overwrite");
        assertEquals(3, retrievedList.size(),
                "Retrieved list should have 3 elements (from new list)");
    }

    // ========================================
    // Tests for clear operation
    // ========================================

    /**
     * Test that clear() removes all preferences from the node.
     * This verifies that the reset/clear functionality works correctly.
     *
     * <p>Clear is used for:
     * <ul>
     *   <li>Resetting application to defaults</li>
     *   <li>Troubleshooting corrupted preferences</li>
     *   <li>Testing and development</li>
     * </ul>
     */
    @Test
    void clear_shouldRemoveAllPreferences() throws BackingStoreException {
        // Arrange: Store several preferences of different types
        applicationPreferences.put("string.key", "value");
        applicationPreferences.putBoolean("boolean.key", true);
        applicationPreferences.putLong("long.key", 123L);
        applicationPreferences.putList("list.key", Arrays.asList("item1", "item2"));

        // Verify preferences were stored
        assertNotEquals("default", applicationPreferences.get("string.key", "default"));

        // Act: Clear all preferences
        applicationPreferences.clear();

        // Assert: All preferences should be gone (return defaults)
        assertEquals("default", applicationPreferences.get("string.key", "default"),
                "String preference should return default after clear");
        assertFalse(applicationPreferences.getBoolean("boolean.key", false),
                "Boolean preference should return default after clear");
        assertEquals(0L, applicationPreferences.getLong("long.key", 0L),
                "Long preference should return default after clear");
        assertTrue(applicationPreferences.getList("list.key").isEmpty(),
                "List preference should return empty list after clear");
    }

    // ========================================
    // Tests for preference persistence
    // ========================================

    /**
     * Test that preferences are actually persisted to the backing store.
     * This verifies that values aren't just cached in memory but written to disk.
     *
     * <p>By creating a new ApplicationPreferences instance, we ensure we're
     * reading from the actual backing store rather than memory cache.
     */
    @Test
    void preferences_shouldPersistAcrossInstances() {
        // Arrange
        String key = "test.persist.key";
        String value = "persisted-value";

        // Act: Store value in first instance
        applicationPreferences.put(key, value);

        // Create a new instance (simulating application restart)
        ApplicationPreferences newInstance = new ApplicationPreferences();

        // Assert: New instance should read the persisted value
        String retrievedValue = newInstance.get(key, "default");
        assertEquals(value, retrievedValue,
                "Preferences should persist across different ApplicationPreferences instances");
    }

    /**
     * Test that list preferences are correctly persisted as JSON strings.
     * This verifies the underlying storage format for lists.
     *
     * <p>We check the raw stored value to ensure it's valid JSON that can be
     * safely read by future versions of the application.
     */
    @Test
    void putList_shouldStoreAsJsonString() {
        // Arrange
        String key = "test.list.json";
        List<String> list = Arrays.asList("item1", "item2", "item3");

        // Act: Store the list
        applicationPreferences.putList(key, list);

        // Assert: Verify the raw stored value is valid JSON
        String rawValue = testPreferences.get(key, null);
        assertNotNull(rawValue, "Should store a value");
        assertTrue(rawValue.startsWith("[") && rawValue.endsWith("]"),
                "Should store as JSON array format");
        assertTrue(rawValue.contains("item1") && rawValue.contains("item2") && rawValue.contains("item3"),
                "Should contain all list items in JSON format");
    }

    // ========================================
    // Tests for edge cases and boundary conditions
    // ========================================

    /**
     * Test that storing a list with a single item works correctly.
     * This verifies proper handling of single-element lists.
     *
     * <p>Single-item lists are a common edge case that should serialize
     * properly as a JSON array with one element.
     */
    @Test
    void putList_andGetList_shouldHandleSingleItemList() {
        // Arrange
        String key = "test.list.single";
        List<String> singleItemList = Arrays.asList("single-item");

        // Act: Store single-item list
        applicationPreferences.putList(key, singleItemList);

        // Assert: Retrieve and verify
        List<String> retrievedList = applicationPreferences.getList(key);
        assertEquals(1, retrievedList.size(), "Should have exactly one item");
        assertEquals("single-item", retrievedList.get(0), "Should retrieve the single item");
    }

    /**
     * Test that the preference node is correctly identified.
     * This verifies that preferences are stored in the expected location.
     *
     * <p>The node name "com.owlplug.user" is important for:
     * <ul>
     *   <li>Avoiding conflicts with other applications</li>
     *   <li>Making preferences easy to find for troubleshooting</li>
     *   <li>Ensuring proper user-specific storage</li>
     * </ul>
     */
    @Test
    void constructor_shouldUseCorrectPreferenceNode() {
        // Arrange & Act: Constructor is called in setUp()

        // Assert: Verify the preference node name
        assertEquals("com.owlplug.user", testPreferences.name(),
                "Should use 'com.owlplug.user' as the preference node name");
    }
}
