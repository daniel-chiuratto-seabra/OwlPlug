package com.owlplug.core.utils;

import com.owlplug.core.components.ApplicationDefaults;
import com.owlplug.core.components.ApplicationPreferences;
import com.owlplug.core.components.ImageCache;
import com.owlplug.explore.model.PackageTag;
import com.owlplug.explore.model.RemotePackage;
import com.owlplug.host.loaders.NativePluginLoader;
import com.owlplug.plugin.model.Plugin;
import com.owlplug.plugin.services.NativeHostService;
import com.owlplug.plugin.services.PluginService;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;

import java.util.Collection;
import java.util.Set;

import static com.owlplug.core.components.ApplicationDefaults.NATIVE_HOST_ENABLED_KEY;
import static com.owlplug.core.components.ApplicationDefaults.PREFERRED_NATIVE_LOADER;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;
import static javafx.collections.FXCollections.observableArrayList;

/**
 * A utility class providing helper methods for various application operations,
 * particularly focused on UI updates, image handling, and configuration management.
 * This class centralizes common operational logic to promote code reusability
 * and maintainability across different parts of the OwlPlug application.
 */
public class OperationUtils {
    /**
     * Updates a set of common UI labels with provided plugin information.
     * This method is designed to centralize the logic for displaying plugin details
     * such as title, name, version, manufacturer, identifier, and category
     * across different parts of the application's user interface.
     * It handles cases where certain information might be {@code null} by
     * providing default "Unknown" values or falling back to the plugin's primary name.
     *
     * @param pluginTitleLabel The {@link Label} to display the plugin's primary title/name.
     * @param name The primary name of the plugin.
     * @param pluginNameLabel The {@link Label} to display the plugin's descriptive name, or primary name if descriptive is null.
     * @param descriptiveName The descriptive name of the plugin, can be {@code null}.
     * @param pluginVersionLabel The {@link Label} to display the plugin's version.
     * @param version The version string of the plugin, can be {@code null}.
     * @param pluginManufacturerLabel The {@link Label} to display the plugin's manufacturer name.
     * @param manufacturerName The manufacturer's name, can be {@code null}.
     * @param pluginIdentifierLabel The {@link Label} to display the plugin's unique identifier (UID).
     * @param uid The unique identifier string of the plugin, can be {@code null}.
     * @param pluginCategoryLabel The {@link Label} to display the plugin's category.
     * @param category The category string of the plugin, can be {@code null}.
     */
    public static void updateCommonLabel(final Label pluginTitleLabel, final String name, final Label pluginNameLabel,
                                         final String descriptiveName, final Label pluginVersionLabel, final String version,
                                         final Label pluginManufacturerLabel, final String manufacturerName,
                                         final Label pluginIdentifierLabel, final String uid, final Label pluginCategoryLabel,
                                         final String category) {
        // Set the primary title label with the plugin's main name.
        pluginTitleLabel.setText(name);
        // Set the plugin name label, falling back to the primary name if the descriptive name is null.
        pluginNameLabel.setText(ofNullable(descriptiveName).orElse(name));
        // Set the plugin version label, defaulting to "Unknown" if the version is null.
        pluginVersionLabel.setText(ofNullable(version).orElse("Unknown"));
        // Set the plugin manufacturer label, defaulting to "Unknown" if the manufacturer name is null.
        pluginManufacturerLabel.setText(ofNullable(manufacturerName).orElse("Unknown"));
        // Set the plugin identifier label, defaulting to "Unknown" if the UID is null.
        pluginIdentifierLabel.setText(ofNullable(uid).orElse("Unknown"));
        // Set the plugin category label, defaulting to "Unknown" if the category is null.
        pluginCategoryLabel.setText(ofNullable(category).orElse("Unknown"));
    }

    /**
     * Sets the background image of a given {@link Pane} to display a plugin's screenshot.
     * This method attempts to load the screenshot from the plugin's URL. If the URL is
     * missing or the image is not found in the cache, it falls back to a placeholder image.
     * It also handles resolving and saving the image URL if it's missing from the plugin's footprint.
     *
     * @param currentPlugin The {@link Plugin} object containing screenshot URL information.
     * @param pluginService The {@link PluginService} used to resolve and save image URLs.
     * @param knownPluginImages A {@link Collection} of known plugin image URLs to track loaded images.
     * @param imageCache The {@link ImageCache} used to retrieve and store images.
     * @param applicationDefaults The {@link ApplicationDefaults} providing default images like placeholders.
     * @param pluginScreenshotPane The {@link Pane} whose background will be set with the plugin image.
     */
    public static void setPluginImage(final Plugin currentPlugin, final PluginService pluginService, final Collection<String> knownPluginImages,
                                      final ImageCache imageCache, final ApplicationDefaults applicationDefaults,
                                      final Pane pluginScreenshotPane) {
        // Get the screenshot URL from the current plugin.
        String url = currentPlugin.getScreenshotUrl();
        // If the plugin's direct screenshot URL is null or empty, try to get it from its footprint.
        if (url == null || url.isEmpty()) {
            // Fallback to footprint screenshot URL.
            String footprintUrl = currentPlugin.getFootprint().getScreenshotUrl();
            // If the footprint URL is also missing, attempt to resolve and save the image URL.
            if (footprintUrl == null || footprintUrl.isEmpty()) {
                pluginService.tryResolveAndSaveImageUrl(currentPlugin);
            }
            // Update the URL with the (potentially newly resolved) footprint screenshot URL.
            url = currentPlugin.getFootprint().getScreenshotUrl();
        }

        // If the URL is still null, or if the image is known but not in the cache, use a placeholder.
        if (url == null || (knownPluginImages.contains(url) && !imageCache.contains(url))) {
            // Create a BackgroundImage using the default plugin placeholder image.
            BackgroundImage bgImg = new BackgroundImage(applicationDefaults.pluginPlaceholderImage,
                    BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                    new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true));
            // Set the background of the pane to the placeholder image.
            pluginScreenshotPane.setBackground(new Background(bgImg));
        } else {
            // Add the URL to known plugin images.
            knownPluginImages.add(url);
            // Retrieve the screenshot image from the cache.
            Image screenshot = imageCache.get(url);
            // If the screenshot is successfully retrieved from the cache.
            if (screenshot != null) {
                // Create a BackgroundImage using the retrieved screenshot.
                BackgroundImage bgImg = new BackgroundImage(screenshot, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER, new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO,
                        false, false, true, true));
                // Set the background of the pane to the screenshot image.
                pluginScreenshotPane.setBackground(new Background(bgImg));
            }
        }
    }

    /**
     * Initializes the native host settings UI components.
     *
     * @param applicationPreferences The application preferences.
     * @param checkBox               The checkbox to enable/disable the native host.
     * @param comboBox               The combobox to select the preferred native plugin loader.
     * @param nativeHostService      The native host service.
     */
    public static void initializeNativeHostSettings(final ApplicationPreferences applicationPreferences, final CheckBox checkBox,
                                                    final ComboBox<NativePluginLoader> comboBox, final NativeHostService nativeHostService) {
        // Add a listener to the checkbox to handle changes in the native host-enabled state.
        // This listener will update application preferences and enable/disable the plugin loader combobox.
        checkBox.selectedProperty().addListener(nativeHostEnabledKeyListener(applicationPreferences, comboBox));

        // Populate the combobox with available native plugin loaders from the native host service.
        final var pluginLoaders = observableArrayList(nativeHostService.getAvailablePluginLoaders());
        comboBox.setItems(pluginLoaders);
        // Add a listener to the combobox to handle changes in the selected native plugin loader.
        // This listener will update application preferences and set the current plugin loader in the native host service.
        comboBox.getSelectionModel().selectedItemProperty().addListener(preferredNativeLoaderListener(applicationPreferences, nativeHostService));
    }

    /**
     * Creates a change listener for the preferred native plugin loader ComboBox.
     * This listener updates the application preferences and sets the current native plugin loader
     * when a new loader is selected.
     *
     * @param applicationPreferences The application preferences to be updated.
     * @param nativeHostService      The native host service to set the current plugin loader on.
     * @return A {@link ChangeListener} for the {@link NativePluginLoader}.
     */
    private static ChangeListener<NativePluginLoader> preferredNativeLoaderListener(final ApplicationPreferences applicationPreferences,
                                                                                    final NativeHostService nativeHostService) {
        return (observable, oldValue, newValue) -> {
            // Check if a new value (plugin loader) has been selected.
            if (newValue != null) {
                // Store the ID of the newly selected plugin loader in application preferences.
                applicationPreferences.put(PREFERRED_NATIVE_LOADER, newValue.getId());
                // Update the native host service with the newly selected plugin loader.
                nativeHostService.setCurrentPluginLoader(newValue);
            }
        };
    }

    /**
     * Creates a change listener for the native host enabled CheckBox.
     * This listener updates the application preferences and enables/disables the native plugin loader ComboBox.
     *
     * @param applicationPreferences The application preferences to be updated.
     * @param comboBox               The ComboBox to be enabled or disabled.
     * @return A {@link ChangeListener} for the Boolean property of the CheckBox.
     */
    private static ChangeListener<Boolean> nativeHostEnabledKeyListener(final ApplicationPreferences applicationPreferences,
                                                                        final ComboBox<NativePluginLoader> comboBox) {
        return (observable, oldValue, newValue) -> {
            // Update the application preferences with the new state of the native host enabled checkbox.
            applicationPreferences.putBoolean(NATIVE_HOST_ENABLED_KEY, newValue);
            // Enable or disable the plugin loader combobox based on the checkbox's new value.
            // If a native host is enabled (newValue is true), the combobox is enabled.
            // If the native host is disabled (newValue is false), the combobox is disabled.
            comboBox.setDisable(!newValue);
        };
    }

    /**
     * Converts a collection of tag strings into a Set of {@link PackageTag} objects.
     *
     * @param tagCollection The collection of strings to be converted.
     * @param remotePackage The remote package with which the tags will be associated.
     * @return A Set of {@link PackageTag} objects.
     */
    public static Set<PackageTag> getPackageTagList(final Collection<String> tagCollection, final RemotePackage remotePackage) {
        // Stream the collection of tag strings.
        return tagCollection.stream()
                // Map each tag string to a new PackageTag object, associating it with the given remotePackage.
                .map(tag -> new PackageTag(tag, remotePackage))
                // Collect the resulting PackageTag objects into a Set to ensure uniqueness.
                .collect(toSet());
    }
}
