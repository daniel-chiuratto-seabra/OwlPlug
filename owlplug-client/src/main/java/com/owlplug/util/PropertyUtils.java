package com.owlplug.util;

import com.owlplug.Bootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

import static org.apache.commons.collections4.MapUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/* OwlPlug
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

/**
 * Utility class for managing application properties and configuration loading.
 * <p>
 * This class provides utility methods to retrieve application configuration values
 * from the application.yaml file, particularly for JavaFX-related settings such as
 * the preloader class name.
 * </p>
 * <p>
 * The class handles configuration loading with proper error handling and fallback
 * mechanisms to ensure the application can start even if configuration files are
 * missing or malformed.
 * </p>
 */
public class PropertyUtils {
    /**
     * The fully qualified class name of the default JavaFX preloader to be used
     * if no specific preloader is configured in the application.yaml file.
     */
    public static final String DEFAULT_PRELOADER = "com.owlplug.OwlPlugPreloader";

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyUtils.class);

    /**
     * Retrieves the JavaFX preloader class name from the application configuration.
     * <p>
     * This method performs the following operations in sequence:
     * </p>
     * <ol>
     *   <li>Creates a YAML parser instance to read configuration files</li>
     *   <li>Attempts to load the {@code /application.yaml} file from the classpath</li>
     *   <li>If the file is not found, logs a warning and returns the default preloader</li>
     *   <li>Parses the YAML content into a Map structure</li>
     *   <li>Navigates to the {@code javafx.pre-loader} property path in the configuration</li>
     *   <li>Validates that the property exists, is a non-empty String, and is not blank</li>
     *   <li>Returns the configured preloader class name if valid, otherwise returns the default</li>
     * </ol>
     * <p>
     * The expected YAML structure is:
     * </p>
     * <pre>
     * javafx:
     *   pre-loader: "com.example.CustomPreloader"
     * </pre>
     * <p>
     * Error Handling:
     * </p>
     * <ul>
     *   <li>If the configuration file is missing, a warning is logged and the default is returned</li>
     *   <li>If any exception occurs during parsing, an error is logged and the default is returned</li>
     *   <li>If the property is missing or invalid, the default preloader is returned silently</li>
     * </ul>
     *
     * @return the fully qualified class name of the JavaFX preloader to use. Returns
     *         {@link #DEFAULT_PRELOADER} if the configuration cannot be loaded, is invalid,
     *         or does not contain a valid preloader value.
     */
    public static String getPreLoader() {
        try {
            // Create a new YAML parser instance.
            final var yaml = new Yaml();
            // Attempt to load the application.yaml file from the classpath.
            try (final var is = Bootstrap.class.getResourceAsStream("/application.yaml")) {
                // If the application.yaml file is not found, log a warning and return the default preloader.
                if (is == null) {
                    LOGGER.warn("application.yaml not found in classpath, using default preloader.");
                    return DEFAULT_PRELOADER;
                }

                // Load the YAML content into a Map.
                final Map<String, Object> yamlProps = yaml.load(is);
                // Check if the YAML properties are not empty and contain a 'javafx' section.
                if (isNotEmpty(yamlProps) && yamlProps.get("javafx") instanceof Map<?, ?> javaFxProps) {
                    // Check if the 'javafx' section is not empty, contains 'pre-loader', and its value is a String.
                    if (isNotEmpty(javaFxProps) && javaFxProps.containsKey("pre-loader") &&
                            javaFxProps.get("pre-loader") instanceof String preLoaderValue) {
                        // If the 'pre-loader' value is a non-blank string, return it.
                        if (isNotBlank(preLoaderValue)) {
                            return preLoaderValue;
                        }
                    }
                }
                // If the 'pre-loader' property is not found or is invalid, return the default preloader.
                return DEFAULT_PRELOADER;
            }
        } catch (final Exception exception) {
            // Catch any exceptions that occur during YAML loading or parsing.
            // Log the error and return the default preloader.
            // A simple logger is used here as the full Spring context might not be available yet.
            LOGGER.error("Could not load application.yaml to set preloader.", exception);
            return DEFAULT_PRELOADER;
        }
    }
}
