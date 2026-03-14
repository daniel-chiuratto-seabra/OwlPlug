/* OwlPlug
 * Copyright (C) 2021 Arthur <dropsnorz@gmail.com>
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static java.util.Arrays.asList;

/**
 * A Spring component that provides a wrapper around Java's {@link Preferences} API
 * with enhanced functionality for persisting application settings and user preferences.
 *
 * <p>This class extends the standard Java Preferences API with additional features:
 * <ul>
 *   <li>Support for storing and retrieving lists of strings (serialized as JSON)</li>
 *   <li>Type-safe methods for common data types (String, Boolean, Long)</li>
 *   <li>Error handling and logging for serialization/deserialization failures</li>
 *   <li>Centralized preference management for the entire application</li>
 * </ul>
 *
 * <p>All preferences are stored under the user root node with the identifier
 * "com.owlplug.user", ensuring preferences persist across application restarts
 * and are specific to each user's operating system account.
 *
 * <p>Storage locations by platform:
 * <ul>
 *   <li>Windows: Registry (HKEY_CURRENT_USER\Software\JavaSoft\Prefs)</li>
 *   <li>macOS: ~/Library/Preferences/com.apple.java.util.prefs.plist</li>
 *   <li>Linux: ~/.java/.userPrefs</li>
 * </ul>
 *
 * <p>Common use cases:
 * <ul>
 *   <li>Plugin discovery settings (VST/AU paths, enabled formats)</li>
 *   <li>Application state tracking (first launch, crash detection)</li>
 *   <li>User interface preferences (display modes, window positions)</li>
 *   <li>Plugin directories and search paths (stored as lists)</li>
 *   <li>Telemetry and analytics settings</li>
 * </ul>
 *
 * @see Preferences
 * @see ApplicationDefaults for preference key constants
 */
@Component
public class ApplicationPreferences {

    /**
     * Logger instance for recording preference operations and errors.
     * Particularly useful for debugging serialization/deserialization issues.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationPreferences.class);

    /**
     * The underlying Java Preferences instance that handles persistent storage.
     * All operations are delegated to this instance, with additional functionality
     * layered on top (e.g., list serialization).
     */
    private final Preferences basePreferences;

    /**
     * Constructs an ApplicationPreferences instance and initializes the preferences' node.
     * The preferences are stored under the user root with the node "com.owlplug.user",
     * ensuring user-specific and application-specific storage.
     *
     * <p>This constructor is invoked by Spring's dependency injection mechanism
     * as a singleton bean, ensuring a single preferences instance throughout the application.
     */
    public ApplicationPreferences() {
        // Initialize preferences under the user root with a unique node identifier
        // This ensures preferences are user-specific and won't conflict with other applications
        this.basePreferences = Preferences.userRoot().node("com.owlplug.user");
    }

    /**
     * Retrieves a string preference value for the specified key.
     * If the key doesn't exist, returns the provided default value.
     *
     * <p>This is a direct delegation to the underlying {@link Preferences#get(String, String)} method.
     *
     * @param key the preference key to retrieve (e.g., "VST_DIRECTORY")
     * @param def the default value to return if the key is not found
     * @return the stored preference value, or the default value if not found
     */
    public String get(final String key, final String def) {
        return basePreferences.get(key, def);
    }

    /**
     * Stores a string preference value for the specified key.
     * If the key already exists, its value is overwritten.
     * The value is persisted immediately to the backing store.
     *
     * <p>This is a direct delegation to the underlying {@link Preferences#put(String, String)} method.
     *
     * @param key   the preference key to store (e.g., "VST_DIRECTORY")
     * @param value the value to store (cannot be null)
     * @throws NullPointerException if key or value is null
     */
    public void put(final String key, final String value) {
        basePreferences.put(key, value);
    }

    /**
     * Retrieves a boolean preference value for the specified key.
     * If the key doesn't exist, returns the provided default value.
     *
     * <p>This is useful for feature flags and toggle settings, such as:
     * <ul>
     *   <li>VST2_DISCOVERY_ENABLED</li>
     *   <li>TELEMETRY_ENABLED</li>
     *   <li>SYNC_PLUGINS_STARTUP</li>
     * </ul>
     *
     * @param key the preference key to retrieve
     * @param def the default value to return if the key is not found
     * @return the stored boolean value, or the default value if not found
     */
    public boolean getBoolean(final String key, final boolean def) {
        return basePreferences.getBoolean(key, def);
    }

    /**
     * Stores a boolean preference value for the specified key.
     * The value is persisted immediately to the backing store.
     *
     * <p>Commonly used for storing feature toggle states and user preferences.
     *
     * @param key   the preference key to store
     * @param value the boolean value to store
     * @throws NullPointerException if the key is null
     */
    public void putBoolean(final String key, final boolean value) {
        basePreferences.putBoolean(key, value);
    }

    /**
     * Retrieves a long (64-bit integer) preference value for the specified key.
     * If the key doesn't exist, returns the provided default value.
     *
     * <p>This is useful for storing numeric values such as:
     * <ul>
     *   <li>Timestamps (last sync time, last update check)</li>
     *   <li>User IDs or session identifiers</li>
     *   <li>Large numeric counters or statistics</li>
     * </ul>
     *
     * @param key the preference key to retrieve
     * @param def the default value to return if the key is not found
     * @return the stored long value, or the default value if not found
     */
    public long getLong(final String key, final long def) {
        return basePreferences.getLong(key, def);
    }

    /**
     * Stores a long (64-bit integer) preference value for the specified key.
     * The value is persisted immediately to the backing store.
     *
     * @param key   the preference key to store
     * @param value the long value to store
     * @throws NullPointerException if the key is null
     */
    public void putLong(final String key, final long value) {
        basePreferences.putLong(key, value);
    }

    /**
     * Retrieves a list of strings for the specified key, returning an empty list if not found.
     * This is a convenience method that delegates to {@link #getList(String, List)} with an empty list as default.
     *
     * <p>The list is stored internally as a JSON array string and is deserialized on retrieval.
     *
     * @param key the preference key to retrieve
     * @return the stored list of strings, or an empty list if not found or deserialization fails
     */
    public List<String> getList(final String key) {
        return getList(key, new ArrayList<>());
    }

    /**
     * Retrieves a list of strings for the specified key with a custom default value.
     * Lists are stored as JSON array strings using Jackson ObjectMapper.
     *
     * <p>This is particularly useful for storing collections of paths or settings, such as:
     * <ul>
     *   <li>Extra plugin directories (VST2_EXTRA_DIRECTORY, VST3_EXTRA_DIRECTORY)</li>
     *   <li>Recent files or project paths</li>
     *   <li>User-defined search paths</li>
     *   <li>Lists of enabled/disabled features</li>
     * </ul>
     *
     * <p>Deserialization process:
     * <ol>
     *   <li>Retrieves the JSON string from preferences</li>
     *   <li>If null or not found, returns the default list</li>
     *   <li>Deserializes JSON array to String[]</li>
     *   <li>Converts an array to List and returns</li>
     *   <li>On error, logs the issue and returns the default list</li>
     * </ol>
     *
     * @param key the preference key to retrieve
     * @param def the default list to return if the key is not found or deserialization fails
     * @return the stored list of strings, or the default list if unavailable
     */
    public List<String> getList(final String key, final List<String> def) {
        // Retrieve the JSON string representation of the list
        final var jsonValue = get(key, null);
        if (jsonValue == null) {
            return def;
        }

        // Deserialize a JSON array string to a list of strings
        final var objectMapper = new ObjectMapper();
        try {
            final var values = objectMapper.readValue(jsonValue, String[].class);
            return asList(values);
        } catch (final JsonProcessingException jsonProcessingException) {
            // Log deserialization error and return default - this could happen if:
            // - The stored JSON is corrupted
            // - The format has changed between application versions
            // - Manual preference file editing introduced errors
            LOGGER.error("List value can't be deserialized from Preferences using key {}: {}", key, jsonValue, jsonProcessingException);
            return def;
        }
    }

    /**
     * Stores a list of strings for the specified key.
     * The list is serialized to a JSON array string using Jackson ObjectMapper before storage.
     *
     * <p>This enables storage of multiple values under a single preference key,
     * which is useful for collections like plugin directories or recent file paths.
     *
     * <p>Serialization process:
     * <ol>
     *   <li>Serializes the list to a JSON array string</li>
     *   <li>Stores the JSON string in preferences</li>
     *   <li>On error, logs the issue (preference is not saved)</li>
     * </ol>
     *
     * <p>Example storage format: {@code ["C:/VST", "D:/Plugins", "E:/Audio/VST"]}
     *
     * @param key   the preference key to store
     * @param value the list of strings to store (cannot be null)
     * @throws NullPointerException if key or value is null
     */
    public void putList(final String key, final List<String> value) {
        // Serialize the list to a JSON array string
        final var objectMapper = new ObjectMapper();
        try {
            final var jsonValue = objectMapper.writeValueAsString(value);
            // Store the JSON string in preferences
            put(key, jsonValue);
        } catch (final JsonProcessingException jsonProcessingException) {
            // Log serialization error - this could happen if:
            // - The list contains values that can't be serialized (unlikely with strings)
            // - ObjectMapper configuration issues
            LOGGER.error("List value can't be serialized to Preferences using key {}", key, jsonProcessingException);
        }
    }

    /**
     * Removes all preferences from the current preference node.
     * This operation is permanent and cannot be undone - all stored preferences are lost.
     *
     * <p>This method is typically used for:
     * <ul>
     *   <li>Resetting application to default settings</li>
     *   <li>Clearing user data during uninstallation</li>
     *   <li>Testing and development (resetting state between tests)</li>
     *   <li>Troubleshooting corrupted preferences</li>
     * </ul>
     *
     * <p><strong>Warning:</strong> This will delete all preferences including:
     * <ul>
     *   <li>Plugin paths and discovery settings</li>
     *   <li>User interface preferences</li>
     *   <li>Application state and telemetry settings</li>
     *   <li>All custom user configurations</li>
     * </ul>
     *
     * @throws BackingStoreException if the backing store is unavailable or the operation fails
     */
    public void clear() throws BackingStoreException {
        basePreferences.clear();
    }

}
