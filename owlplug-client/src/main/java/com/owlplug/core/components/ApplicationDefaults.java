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

import com.owlplug.core.model.RuntimePlatform;
import com.owlplug.explore.model.RemotePackage;
import com.owlplug.plugin.model.PluginFormat;
import com.owlplug.project.model.DawApplication;
import javafx.scene.image.Image;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.nio.file.FileSystems.getDefault;
import static java.nio.file.Path.of;

/**
 * A Spring component that provides application-wide default values, configurations,
 * and utility methods related to paths, images, and runtime environment.
 * This class centralizes various constants and helper functions to ensure
 * consistent behavior and easy access to common application resources.
 */
@Component
public class ApplicationDefaults {

    /**
     * The system-dependent path separator character.
     */
    private static final String PATH_SEPARATOR = getDefault().getSeparator();

    /**
     * Standard folder name for VST2 plugins.
     */
    private static final String VST2_FOLDER = "VST";
    /**
     * Standard folder name for VST3 plugins.
     */
    private static final String VST3_FOLDER = "VST3";
    /**
     * Standard folder name for LV2 plugins.
     */
    private static final String LV2_FOLDER = "LV2";

    /**
     * A default placeholder path for audio plugins, used when a specific path cannot be determined.
     */
    private static final String DEFAULT_FOLDER_PATH = of(PATH_SEPARATOR, "path", "to", "audio", "plugins").toString();

    /**
     * The name of the "Program Files" directory on Windows operating systems.
     */
    private static final String WINDOWS_PROGRAM_FILES_FOLDER = "Program Files";
    /**
     * The name of the "Common Files" directory on Windows operating systems.
     */
    private static final String WINDOWS_COMMON_FILES_FOLDER = "Common Files";
    /**
     * The name of the "VSTPlugins" directory on Windows operating systems, typically found within "Program Files".
     */
    private static final String WINDOWS_VST_PLUGINS_FOLDER = "VSTPlugins";
    /**
     * The drive letter for the primary hard drive on Windows operating systems.
     */
    private static final String WINDOWS_C_HARD_DRIVE = "C:";

    /**
     * Generic folder name for plugins on macOS.
     */
    private static final String MACOS_PLUGINS = "Plug-Ins";
    /**
     * The name of the "Library" directory on macOS.
     */
    private static final String MACOS_LIBRARY_FOLDER = "Library";
    /**
     * The name of the "Audio" directory within the macOS Library folder.
     */
    private static final String MACOS_AUDIO_FOLDER = "Audio";
    /**
     * The name of the "Plug-ins" directory within the macOS Audio folder.
     */
    private static final String MACOS_PLUGINS_FOLDER = "Plug-ins";
    /**
     * The name of the "Components" directory on macOS, typically used for Audio Unit plugins.
     */
    private static final String MACOS_COMPONENTS_FOLDER = "Components";

    /**
     * The "usr" folder path on Linux systems, typically the root of user programs.
     */
    private static final String LINUX_USR_FOLDER = "usr";
    /**
     * The "lib" folder path on Linux systems, where libraries and plugins are commonly stored.
     */
    private static final String LINUX_LIB_FOLDER = "lib";

    /**
     * Spring Environment instance for accessing application properties and configuration.
     */
    private final Environment env;
    /**
     * Platform resolver to determine the current operating system and architecture.
     */
    private final RuntimePlatformResolver runtimePlatformResolver;

    /**
     * Constructs an ApplicationDefaults instance with the necessary dependencies.
     * This constructor is invoked by Spring's dependency injection mechanism.
     *
     * @param environment Spring Environment for accessing application properties
     * @param runtimePlatformResolver resolver for determining the current runtime platform
     */
    public ApplicationDefaults(final Environment environment, final RuntimePlatformResolver runtimePlatformResolver) {
        this.env = environment;
        this.runtimePlatformResolver = runtimePlatformResolver;
    }

    /**
     * Cached list of contributors loaded from the CONTRIBUTORS file.
     * Initialized lazily on first access to avoid loading during application startup.
     */
    private static List<String> contributors;

    /**
     * The official name of the application.
     */
    public static final String APPLICATION_NAME = "OwlPlug";

    // CHECKSTYLE:OFF - Image constants are declared together for clarity
    /** Main application logo image. */
    public static final Image owlplugLogo = loadImage("/media/owlplug-logo.png");

    /** Small 16x16 version of the OwlPlug logo. */
    public final Image owlplugLogoSmall = loadImage("/media/owlplug-logo-16.png");
    /** Icon representing a file directory/folder. */
    public final Image directoryImage = loadImage("/icons/folder-grey-16.png");
    /** Icon for VST2 plugin format. */
    public final Image vst2Image = loadImage("/icons/vst2-blue-16.png");
    /** Icon for VST3 plugin format. */
    public final Image vst3Image = loadImage("/icons/vst3-green-16.png");
    /** Icon for Audio Unit (AU) plugin format. */
    public final Image auImage = loadImage("/icons/au-purple-16.png");
    /** Icon for LV2 plugin format. */
    public final Image lv2Image = loadImage("/icons/lv2-orange-16.png");
    /** Icon representing a plugin component. */
    public final Image pluginComponentImage = loadImage("/icons/cube-white-16.png");
    /** Icon indicating a task is pending/queued. */
    public final Image taskPendingImage = loadImage("/icons/loading-grey-16.png");
    /** Icon indicating a task completed successfully. */
    public final Image taskSuccessImage = loadImage("/icons/check-green-16.png");
    /** Icon indicating a task failed. */
    public final Image taskFailImage = loadImage("/icons/cross-red-16.png");
    /** Icon indicating a task is currently running. */
    public final Image taskRunningImage = loadImage("/icons/play-green-16.png");
    /** Rocket icon, typically used for launch or startup actions. */
    public final Image rocketImage = loadImage("/icons/rocket-white-64.png");
    /** Server icon for remote / network-related features. */
    public final Image serverImage = loadImage("/icons/server-white-32.png");
    /** Icon representing instrument plugins (synthesizers, etc.). */
    public final Image instrumentImage = loadImage("/icons/synth-white-16.png");
    /** Icon representing effect plugins (reverb, delay, etc.). */
    public final Image effectImage = loadImage("/icons/effect-white-16.png");
    /** Tag/label icon for categorization. */
    public final Image tagImage = loadImage("/icons/tag-white-16.png");
    /** Icon for symbolic links to folders. */
    public final Image symlinkImage = loadImage("/icons/folderlink-grey-16.png");
    /** User profile/account icon. */
    public final Image userImage = loadImage("/icons/user-white-32.png");
    /** Icon for scanning/searching directories. */
    public final Image scanDirectoryImage = loadImage("/icons/foldersearch-grey-16.png");
    /** Icon indicating a verified or trusted source. */
    public final Image verifiedSourceImage = loadImage("/icons/doublecheck-grey-16.png");
    /** Small Open Audio logo. */
    public final Image openAudioLogoSmall = loadImage("/media/open-audio-16.png");
    /** Ableton Live DAW logo. */
    public final Image abletonLogoImage = loadImage("/icons/ableton-white-16.png");
    /** Reaper DAW logo. */
    public final Image reaperLogoImage = loadImage("/icons/reaper-white-16.png");
    /** Studio One DAW logo. */
    public final Image studioOneLogoImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/studioone-white-16.png")));
    /** Error/warning icon. */
    public final Image errorIconImage = loadImage("/icons/error-red-16.png");
    /** Link/hyperlink icon. */
    public final Image linkIconImage = loadImage("/icons/link-grey-16.png");
    /** Placeholder image for plugins without custom artwork. */
    public final Image pluginPlaceholderImage = loadImage("/media/plugin-placeholder.png");
    // CHECKSTYLE:ON

    /**
     * Loads an image from the classpath resources.
     *
     * @param path the resource path to the image file
     * @return the loaded Image object
     * @throws NullPointerException if the resource is not found
     */
    private static Image loadImage(String path) {
        return new Image(Objects.requireNonNull(ApplicationDefaults.class.getResourceAsStream(path),
                "Resource not found: " + path));
    }

    // Preferences keys for storing user settings and configuration

    /** Preference key for the legacy VST directory path. */
    public static final String VST_DIRECTORY_KEY = "VST_DIRECTORY";
    /** Preference key to enable/disable VST2 plugin discovery. */
    public static final String VST2_DISCOVERY_ENABLED_KEY = "VST2_DISCOVERY_ENABLED";
    /** Preference key for additional VST2 directories to scan. */
    public static final String VST2_EXTRA_DIRECTORY_KEY = "VST2_EXTRA_DIRECTORY_KEY";
    /** Preference key for the VST3 directory path. */
    public static final String VST3_DIRECTORY_KEY = "VST3_DIRECTORY";
    /** Preference key to enable/disable VST3 plugin discovery. */
    public static final String VST3_DISCOVERY_ENABLED_KEY = "VST3_DISCOVERY_ENABLED";
    /** Preference key for additional VST3 directories to scan. */
    public static final String VST3_EXTRA_DIRECTORY_KEY = "VST3_EXTRA_DIRECTORY_KEY";
    /** Preference key for the Audio Unit (AU) directory path. */
    public static final String AU_DIRECTORY_KEY = "AU_DIRECTORY_KEY";
    /** Preference key to enable/disable AU plugin discovery. */
    public static final String AU_DISCOVERY_ENABLED_KEY = "AU_DISCOVERY_ENABLED_KEY";
    /** Preference key for additional AU directories to scan. */
    public static final String AU_EXTRA_DIRECTORY_KEY = "AU_EXTRA_DIRECTORY_KEY";
    /** Preference key for the LV2 directory path. */
    public static final String LV2_DIRECTORY_KEY = "LV2_DIRECTORY_KEY";
    /** Preference key to enable/disable LV2 plugin discovery. */
    public static final String LV2_DISCOVERY_ENABLED_KEY = "LV2_DISCOVERY_ENABLED_KEY";
    /** Preference key for additional LV2 directories to scan. */
    public static final String LV2_EXTRA_DIRECTORY_KEY = "LV2_EXTRA_DIRECTORY_KEY";
    /** Preference key to enable/disable native plugin hosting capabilities. */
    public static final String NATIVE_HOST_ENABLED_KEY = "NATIVE_HOST_ENABLED_KEY";
    /** Preference key for the preferred native plugin loader implementation. */
    public static final String PREFERRED_NATIVE_LOADER = "PREFERRED_NATIVE_LOADER";
    /** Preference key for the currently selected user account. */
    public static final String SELECTED_ACCOUNT_KEY = "SELECTED_ACCOUNT_KEY";
    /** Preference key to enable/disable automatic plugin synchronization on startup. */
    public static final String SYNC_PLUGINS_STARTUP_KEY = "SYNC_PLUGINS_STARTUP_KEY";
    /** Preference key to enable/disable custom store directory. */
    public static final String STORE_DIRECTORY_ENABLED_KEY = "STORE_DIRECTORY_ENABLED_KEY";
    /** Preference key to enable/disable organizing plugins by creator/vendor. */
    public static final String STORE_BY_CREATOR_ENABLED_KEY = "STORE_BY_CREATOR_ENABLED_KEY";
    /** Preference key for the plugin store directory path. */
    public static final String STORE_DIRECTORY_KEY = "STORE_DIRECTORY_KEY";
    /** Preference key to enable/disable subdirectory organization in the store. */
    public static final String STORE_SUBDIRECTORY_ENABLED = "STORE_SUBDIRECTORY_ENABLED";
    /** Preference key indicating if this is the first launch of the application. */
    public static final String FIRST_LAUNCH_KEY = "FIRST_LAUNCH_KEY";
    /** Preference key for storing the application state. */
    public static final String APPLICATION_STATE_KEY = "APPLICATION_STATE_KEY";
    /** Preference key to show/hide the plugin disable confirmation dialog. */
    public static final String SHOW_DIALOG_DISABLE_PLUGIN_KEY = "SHOW_DIALOG_DISABLE_PLUGIN_KEY";
    /** Preference key for the DAW project directory path. */
    public static final String PROJECT_DIRECTORY_KEY = "PROJECT_DIRECTORY_KEY";
    /** Preference key for the preferred plugin display mode (table/tree view). */
    public static final String PLUGIN_PREFERRED_DISPLAY_KEY = "PLUGIN_PREFERRED_DISPLAY_KEY";
    /** Preference key to enable/disable file statistics during synchronization. */
    public static final String SYNC_FILE_STAT_KEY = "SYNC_FILE_STAT_KEY";
    /** Preference key to enable/disable telemetry data collection. */
    public static final String TELEMETRY_ENABLED_KEY = "TELEMETRY_ENABLED_KEY";
    /** Preference key for storing the anonymous telemetry user identifier. */
    public static final String TELEMETRY_USER_ID_KEY = "TELEMETRY_USER_ID_KEY";

    /**
     * Retrieves the current runtime platform (OS and architecture).
     *
     * @return the RuntimePlatform representing the current system environment
     */
    public RuntimePlatform getRuntimePlatform() {
        return runtimePlatformResolver.getCurrentPlatform();
    }

    /**
     * Returns the appropriate icon image for a given plugin format.
     * Used throughout the UI to visually identify plugin types.
     *
     * @param format the plugin format (VST2, VST3, AU, or LV2)
     * @return the Image corresponding to the specified plugin format
     */
    public Image getPluginFormatIcon(PluginFormat format) {
        return switch (format) {
            case VST3 -> vst3Image;
            case AU -> auImage;
            case LV2 -> lv2Image;
            case VST2 -> vst2Image;
        };
    }

    /**
     * Returns the appropriate icon image for a remote package based on its type.
     * Distinguishes between instrument and effect plugins.
     *
     * @param remotePackage the remote package to get an icon for
     * @return the Image corresponding to the package type (instrument or effect)
     */
    public Image getPackageTypeIcon(RemotePackage remotePackage) {
        return switch (remotePackage.getType()) {
            case INSTRUMENT -> instrumentImage;
            case EFFECT -> effectImage;
        };
    }

    /**
     * Returns the logo icon for a specific DAW (Digital Audio Workstation) application.
     * Used in the project explorer and DAW-related features.
     *
     * @param application the DAW application (Ableton or Reaper)
     * @return the Image representing the DAW's logo
     */
    public Image getDAWApplicationIcon(DawApplication application) {
        return switch (application) {
            case ABLETON -> abletonLogoImage;
            case REAPER -> reaperLogoImage;
            case STUDIO_ONE -> studioOneLogoImage;
        };
    }

    /**
     * Determines the default installation path for plugins based on the format and operating system.
     * This method returns platform-specific default paths where audio plugins are typically installed.
     * On Windows, paths reference the C: drive and Program Files directories.
     * On macOS, paths reference the /Library/Audio structure.
     * On Linux, paths reference /usr/lib directories.
     *
     * @param pluginFormat the plugin format to get the default path for
     * @return the platform-specific default installation path as a String
     */
    public String getDefaultPluginPath(final PluginFormat pluginFormat) {
        // Get the current operating system to determine the appropriate path structure
        final var os = runtimePlatformResolver.getCurrentPlatform().getOperatingSystem();
        return switch (os) {
            case WIN -> switch (pluginFormat) {
                case VST2 ->
                        of(WINDOWS_C_HARD_DRIVE, WINDOWS_PROGRAM_FILES_FOLDER, WINDOWS_VST_PLUGINS_FOLDER).toString();
                case VST3 ->
                        of(WINDOWS_C_HARD_DRIVE, WINDOWS_PROGRAM_FILES_FOLDER, WINDOWS_COMMON_FILES_FOLDER, VST3_FOLDER).toString();
                case LV2 ->
                        of(WINDOWS_C_HARD_DRIVE, WINDOWS_PROGRAM_FILES_FOLDER, WINDOWS_COMMON_FILES_FOLDER, LV2_FOLDER).toString();
                default -> DEFAULT_FOLDER_PATH;
            };
            case MAC -> switch (pluginFormat) {
                case VST2 ->
                        of(PATH_SEPARATOR, MACOS_LIBRARY_FOLDER, MACOS_AUDIO_FOLDER, MACOS_PLUGINS_FOLDER, VST2_FOLDER).toString();
                case VST3 ->
                        of(PATH_SEPARATOR, MACOS_LIBRARY_FOLDER, MACOS_AUDIO_FOLDER, MACOS_PLUGINS_FOLDER, VST3_FOLDER).toString();
                case AU ->
                        of(PATH_SEPARATOR, MACOS_LIBRARY_FOLDER, MACOS_AUDIO_FOLDER, MACOS_PLUGINS_FOLDER, MACOS_COMPONENTS_FOLDER).toString();
                case LV2 ->
                        of(PATH_SEPARATOR, MACOS_LIBRARY_FOLDER, MACOS_AUDIO_FOLDER, MACOS_PLUGINS, LV2_FOLDER).toString();
            };
            case LINUX -> switch (pluginFormat) {
                case VST2 ->
                        of(PATH_SEPARATOR, LINUX_USR_FOLDER, LINUX_LIB_FOLDER, VST2_FOLDER.toLowerCase()).toString();
                case VST3 ->
                        of(PATH_SEPARATOR, LINUX_USR_FOLDER, LINUX_LIB_FOLDER, VST3_FOLDER.toLowerCase()).toString();
                case LV2 -> of(PATH_SEPARATOR, LINUX_USR_FOLDER, LINUX_LIB_FOLDER, LV2_FOLDER.toLowerCase()).toString();
                default -> DEFAULT_FOLDER_PATH;
            };
            default -> DEFAULT_FOLDER_PATH;
        };
    }

    /**
     * Retrieves the current application version from the Spring environment properties.
     *
     * @return the application version string, or null if not configured
     */
    public String getVersion() {
        return env.getProperty("owlplug.version");
    }

    /**
     * Retrieves the latest URL configured in the application properties.
     * This URL is typically used for accessing the most up-to-date application resources.
     *
     * @return the latest URL as a String, or null if not configured
     */
    public String getLatestUrl() {
        return env.getProperty("owlplug.github.latest-url");
    }

    /**
     * Retrieves the download URL configured in the application properties.
     * This URL is used for downloading application updates.
     *
     * @return the download URL as a String, or null if not configured
     */
    public String getDownloadUrl() {
        return env.getProperty("owlplug.github.download-url");
    }

    /**
     * Retrieves the OwlPlug Registry URL from configuration.
     * The registry contains plugin packages available for installation.
     *
     * @return the OwlPlug Registry URL, or null if not configured
     */
    public String getOwlPlugRegistryUrl() {
        return env.getProperty("owlplug.registry.url");
    }

    /**
     * Retrieves the Open Audio Registry URL from configuration.
     * This is an alternative plugin registry following the Open Audio standard.
     *
     * @return the Open Audio Registry URL, or null if not configured
     */
    public String getOpenAudioRegistryUrl() {
        return env.getProperty("openaudio.registry.url");
    }

    public String getTelemetryCode() {
        return env.getProperty("owlplug.telemetry.code");
    }

    public String getDonateUrl() {
        return env.getProperty("owlplug.donate-url");
    }

    public String getWorkspaceMinVersion() {
        return env.getProperty("owlplug.workspace.min-version");
    }

    public String getIssuesUrl() {
        return env.getProperty("owlplug.github.issues-url");
    }

    public String getRoadMapUrl() {
        return env.getProperty("owlplug.roadmap-url");
    }

    public String getAboutUrl() {
        return env.getProperty("owlplug.about-url");
    }

    public String getWikiUrl() {
        return env.getProperty("owlplug.github.wiki-url");
    }

    /**
     * Generic method to retrieve any property from the Spring environment configuration.
     * Provides flexible access to both application properties and system properties.
     *
     * @param property the property key to retrieve
     * @return the property value, or null if not found
     */
    public String getEnvProperty(String property) {
        return env.getProperty(property);
    }

    /**
     * Returns the list of OwlPlug contributors loaded from the CONTRIBUTORS resource file.
     * This method is static to allow it to be called in the Preloader before Spring
     * controller initialization. The contributor list is cached after the first load
     * to avoid repeated file I/O operations.
     *
     * @return a new list containing all contributor names (empty lines are filtered out)
     * @throws RuntimeException if the CONTRIBUTORS file cannot be read or is not found
     */
    public static List<String> getContributors() {
        // Return a cached list if already loaded
        if (contributors != null) {
            return new ArrayList<>(contributors);
        }

        // Load contributors from the classpath resource
        String path = "/included/CONTRIBUTORS";
        try (InputStream input = ApplicationDefaults.class.getResourceAsStream(path);
             BufferedReader reader = new BufferedReader(new InputStreamReader(
                     Objects.requireNonNull(input, "Resource not found: " + path)))) {

            // Read all non-empty lines from the file
            contributors = reader.lines()
                    .filter(line -> !line.trim().isEmpty())
                    .collect(Collectors.toList());
            return new ArrayList<>(contributors);
        } catch (Exception e) {
            throw new RuntimeException("Error reading resource file: " + path, e);
        }
    }

    /**
     * Returns the path to the OwlPlug user data directory.
     * This directory is located in the user's home directory and contains
     * application data, preferences, plugin database, and logs.
     * The directory is typically ~/.owlplug on Unix-like systems and
     * C:\Users\[username]\.owlplug on Windows.
     *
     * @return the absolute path to the user data directory as a String
     */
    public static String getUserDataDirectory() {
        return Paths.get(System.getProperty("user.home"), ".owlplug").toString();
    }

    /**
     * Returns the path to the temporary download directory.
     * This directory is used to store files being downloaded before installation.
     * Located within the user data directory as a "temp" subdirectory.
     *
     * @return the absolute path to the temporary download directory as a String
     */
    public static String getTempDownloadDirectory() {
        return Paths.get(getUserDataDirectory(), "temp").toString();
    }

    /**
     * Returns the path to the application log directory.
     * This directory contains application log files for debugging and monitoring.
     * Located within the user data directory as a "logs" subdirectory.
     *
     * @return the absolute path to the log directory as a String
     */
    public static String getLogDirectory() {
        return Paths.get(getUserDataDirectory(), "logs").toString();
    }

}
