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

import com.owlplug.core.model.OperatingSystem;
import com.owlplug.core.model.RuntimePlatform;
import com.owlplug.explore.model.RemotePackage;
import com.owlplug.plugin.model.PluginFormat;
import com.owlplug.plugin.model.PluginType;
import com.owlplug.project.model.DawApplication;
import javafx.scene.image.Image;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the ApplicationDefaults class.
 * These tests verify the behavior of default values, path generation,
 * icon retrieval, and configuration property access without requiring
 * a full Spring application context.
 * <p>
 * Test coverage includes:
 * - Platform-specific default plugin paths for Windows, macOS, and Linux
 * - Plugin format icon retrieval
 * - Package type icon retrieval
 * - DAW application icon retrieval
 * - Environment property access methods
 * - Static utility methods for directory paths
 * - Contributors list loading
 */
@ExtendWith(MockitoExtension.class)
public class ApplicationDefaultsTest {

    @Mock
    private Environment environment;

    @Mock
    private RuntimePlatformResolver runtimePlatformResolver;

    private ApplicationDefaults applicationDefaults;

    /**
     * Set up test fixtures before each test.
     * Creates a fresh ApplicationDefaults instance with mocked dependencies
     * to ensure test isolation and prevent side effects between tests.
     */
    @BeforeEach
    void setUp() {
        applicationDefaults = new ApplicationDefaults(environment, runtimePlatformResolver);
    }

    // ========================================
    // Tests for Windows default plugin paths
    // ========================================

    /**
     * Test that getDefaultPluginPath returns the correct Windows VST2 path.
     * On Windows, VST2 plugins are typically installed in C:\Program Files\VSTPlugins.
     * This path is the standard location expected by most DAWs on Windows systems.
     */
    @Test
    void testGetDefaultPluginPath_Win_VST2() {
        when(runtimePlatformResolver.getCurrentPlatform()).thenReturn(new RuntimePlatform(null, OperatingSystem.WIN, null));
        String path = applicationDefaults.getDefaultPluginPath(PluginFormat.VST2);
        assertEquals("C:/Program Files/VSTPlugins", path);
    }

    /**
     * Test that getDefaultPluginPath returns the correct Windows VST3 path.
     * On Windows, VST3 plugins are installed in C:\Program Files\Common Files\VST3.
     * This is the standardized location defined by the VST3 specification.
     */
    @Test
    void testGetDefaultPluginPath_Win_VST3() {
        when(runtimePlatformResolver.getCurrentPlatform()).thenReturn(new RuntimePlatform(null, OperatingSystem.WIN, null));
        String path = applicationDefaults.getDefaultPluginPath(PluginFormat.VST3);
        assertEquals("C:/Program Files/Common Files/VST3", path);
    }

    /**
     * Test that getDefaultPluginPath returns the correct Windows LV2 path.
     * On Windows, LV2 plugins are installed in C:\Program Files\Common Files\LV2.
     * This follows the LV2 plugin specification for Windows systems.
     */
    @Test
    void testGetDefaultPluginPath_Win_LV2() {
        when(runtimePlatformResolver.getCurrentPlatform()).thenReturn(new RuntimePlatform(null, OperatingSystem.WIN, null));
        String path = applicationDefaults.getDefaultPluginPath(PluginFormat.LV2);
        assertEquals("C:/Program Files/Common Files/LV2", path);
    }

    /**
     * Test that getDefaultPluginPath returns a fallback path for unsupported formats on Windows.
     * When requesting Audio Unit (AU) format on Windows (which is macOS-only),
     * the method should return a generic fallback path to prevent errors.
     */
    @Test
    void testGetDefaultPluginPath_Win_Default() {
        when(runtimePlatformResolver.getCurrentPlatform()).thenReturn(new RuntimePlatform(null, OperatingSystem.WIN, null));
        String path = applicationDefaults.getDefaultPluginPath(PluginFormat.AU);
        assertEquals("/path/to/audio/plugins", path);
    }

    // ========================================
    // Tests for macOS default plugin paths
    // ========================================

    /**
     * Test that getDefaultPluginPath returns the correct macOS VST2 path.
     * On macOS, VST2 plugins are installed in /Library/Audio/Plug-ins/VST.
     * This is the system-wide location that all DAWs scan on macOS.
     */
    @Test
    void testGetDefaultPluginPath_Mac_VST2() {
        when(runtimePlatformResolver.getCurrentPlatform()).thenReturn(new RuntimePlatform(null, OperatingSystem.MAC, null));
        String path = applicationDefaults.getDefaultPluginPath(PluginFormat.VST2);
        assertEquals("/Library/Audio/Plug-ins/VST", path);
    }

    /**
     * Test that getDefaultPluginPath returns the correct macOS VST3 path.
     * On macOS, VST3 plugins are installed in /Library/Audio/Plug-ins/VST3.
     * This follows the VST3 specification for macOS systems.
     */
    @Test
    void testGetDefaultPluginPath_Mac_VST3() {
        when(runtimePlatformResolver.getCurrentPlatform()).thenReturn(new RuntimePlatform(null, OperatingSystem.MAC, null));
        String path = applicationDefaults.getDefaultPluginPath(PluginFormat.VST3);
        assertEquals("/Library/Audio/Plug-ins/VST3", path);
    }

    /**
     * Test that getDefaultPluginPath returns the correct macOS Audio Unit path.
     * On macOS, Audio Unit plugins are installed in /Library/Audio/Plug-ins/Components.
     * AU is a macOS-only format, and this is the standard system location.
     */
    @Test
    void testGetDefaultPluginPath_Mac_AU() {
        when(runtimePlatformResolver.getCurrentPlatform()).thenReturn(new RuntimePlatform(null, OperatingSystem.MAC, null));
        String path = applicationDefaults.getDefaultPluginPath(PluginFormat.AU);
        assertEquals("/Library/Audio/Plug-ins/Components", path);
    }

    /**
     * Test that getDefaultPluginPath returns the correct macOS LV2 path.
     * On macOS, LV2 plugins are installed in /Library/Audio/Plug-Ins/LV2.
     * This follows the LV2 specification for macOS systems.
     */
    @Test
    void testGetDefaultPluginPath_Mac_LV2() {
        when(runtimePlatformResolver.getCurrentPlatform()).thenReturn(new RuntimePlatform(null, OperatingSystem.MAC, null));
        String path = applicationDefaults.getDefaultPluginPath(PluginFormat.LV2);
        assertEquals("/Library/Audio/Plug-Ins/LV2", path);
    }

    // ========================================
    // Tests for Linux default plugin paths
    // ========================================

    /**
     * Test that getDefaultPluginPath returns the correct Linux VST2 path.
     * On Linux, VST2 plugins are typically installed in /usr/lib/vst.
     * This is the standard system-wide location for VST2 plugins on Linux distributions.
     */
    @Test
    void testGetDefaultPluginPath_Linux_VST2() {
        when(runtimePlatformResolver.getCurrentPlatform()).thenReturn(new RuntimePlatform(null, OperatingSystem.LINUX, null));
        String path = applicationDefaults.getDefaultPluginPath(PluginFormat.VST2);
        assertEquals("/usr/lib/vst", path);
    }

    /**
     * Test that getDefaultPluginPath returns the correct Linux VST3 path.
     * On Linux, VST3 plugins are installed in /usr/lib/vst3.
     * This follows the VST3 specification for Linux systems.
     */
    @Test
    void testGetDefaultPluginPath_Linux_VST3() {
        when(runtimePlatformResolver.getCurrentPlatform()).thenReturn(new RuntimePlatform(null, OperatingSystem.LINUX, null));
        String path = applicationDefaults.getDefaultPluginPath(PluginFormat.VST3);
        assertEquals("/usr/lib/vst3", path);
    }

    /**
     * Test that getDefaultPluginPath returns the correct Linux LV2 path.
     * On Linux, LV2 plugins are installed in /usr/lib/lv2.
     * LV2 is particularly popular on Linux, and this is the standard system location.
     */
    @Test
    void testGetDefaultPluginPath_Linux_LV2() {
        when(runtimePlatformResolver.getCurrentPlatform()).thenReturn(new RuntimePlatform(null, OperatingSystem.LINUX, null));
        String path = applicationDefaults.getDefaultPluginPath(PluginFormat.LV2);
        assertEquals("/usr/lib/lv2", path);
    }

    /**
     * Test that getDefaultPluginPath returns a fallback path for unsupported formats on Linux.
     * When requesting Audio Unit (AU) format on Linux (which is macOS-only),
     * the method should return a generic fallback path to prevent errors.
     */
    @Test
    void testGetDefaultPluginPath_Linux_Default() {
        when(runtimePlatformResolver.getCurrentPlatform()).thenReturn(new RuntimePlatform(null, OperatingSystem.LINUX, null));
        String path = applicationDefaults.getDefaultPluginPath(PluginFormat.AU);
        assertEquals("/path/to/audio/plugins", path);
    }

    // ========================================
    // Tests for unknown/unsupported platforms
    // ========================================

    /**
     * Test that getDefaultPluginPath returns a fallback path for unknown operating systems.
     * This ensures the application doesn't crash when running on an unrecognized platform,
     * instead providing a generic fallback path that can be manually configured by the user.
     */
    @Test
    void testGetDefaultPluginPath_Default() {
        when(runtimePlatformResolver.getCurrentPlatform()).thenReturn(new RuntimePlatform(null, OperatingSystem.UNKNOWN, null));
        String path = applicationDefaults.getDefaultPluginPath(PluginFormat.VST2);
        assertEquals("/path/to/audio/plugins", path);
    }

    // ========================================
    // Tests for plugin format icon retrieval
    // ========================================

    /**
     * Test that getPluginFormatIcon returns a non-null icon for VST2 format.
     * This icon is used throughout the UI to visually identify VST2 plugins.
     */
    @Test
    void testGetPluginFormatIcon_VST2() {
        Image icon = applicationDefaults.getPluginFormatIcon(PluginFormat.VST2);
        assertNotNull(icon, "VST2 icon should not be null");
        assertSame(applicationDefaults.vst2Image, icon);
    }

    /**
     * Test that getPluginFormatIcon returns a non-null icon for VST3 format.
     * This icon is used throughout the UI to visually identify VST3 plugins.
     */
    @Test
    void testGetPluginFormatIcon_VST3() {
        Image icon = applicationDefaults.getPluginFormatIcon(PluginFormat.VST3);
        assertNotNull(icon, "VST3 icon should not be null");
        assertSame(applicationDefaults.vst3Image, icon);
    }

    /**
     * Test that getPluginFormatIcon returns a non-null icon for AU format.
     * This icon is used throughout the UI to visually identify Audio Unit plugins.
     */
    @Test
    void testGetPluginFormatIcon_AU() {
        Image icon = applicationDefaults.getPluginFormatIcon(PluginFormat.AU);
        assertNotNull(icon, "AU icon should not be null");
        assertSame(applicationDefaults.auImage, icon);
    }

    /**
     * Test that getPluginFormatIcon returns a non-null icon for LV2 format.
     * This icon is used throughout the UI to visually identify LV2 plugins.
     */
    @Test
    void testGetPluginFormatIcon_LV2() {
        Image icon = applicationDefaults.getPluginFormatIcon(PluginFormat.LV2);
        assertNotNull(icon, "LV2 icon should not be null");
        assertSame(applicationDefaults.lv2Image, icon);
    }

    // ========================================
    // Tests for package type icon retrieval
    // ========================================

    /**
     * Test that getPackageTypeIcon returns the instrument icon for instrument packages.
     * This helps users visually distinguish between instrument plugins (synthesizers, samplers)
     * and effect plugins in the store and plugin browser.
     */
    @Test
    void testGetPackageTypeIcon_Instrument() {
        RemotePackage remotePackage = mock(RemotePackage.class);
        when(remotePackage.getType()).thenReturn(PluginType.INSTRUMENT);

        Image icon = applicationDefaults.getPackageTypeIcon(remotePackage);
        assertNotNull(icon, "Instrument icon should not be null");
        assertSame(applicationDefaults.instrumentImage, icon);
    }

    /**
     * Test that getPackageTypeIcon returns the effect icon for effect packages.
     * This helps users visually distinguish between effect plugins (reverb, delay, EQ)
     * and instrument plugins in the store and plugin browser.
     */
    @Test
    void testGetPackageTypeIcon_Effect() {
        RemotePackage remotePackage = mock(RemotePackage.class);
        when(remotePackage.getType()).thenReturn(PluginType.EFFECT);

        Image icon = applicationDefaults.getPackageTypeIcon(remotePackage);
        assertNotNull(icon, "Effect icon should not be null");
        assertSame(applicationDefaults.effectImage, icon);
    }

    // ========================================
    // Tests for DAW application icon retrieval
    // ========================================

    /**
     * Test that getDAWApplicationIcon returns the Ableton logo for Ableton Live.
     * This icon is used in the project explorer to identify DAW project files.
     */
    @Test
    void testGetDAWApplicationIcon_Ableton() {
        Image icon = applicationDefaults.getDAWApplicationIcon(DawApplication.ABLETON);
        assertNotNull(icon, "Ableton icon should not be null");
        assertSame(applicationDefaults.abletonLogoImage, icon);
    }

    /**
     * Test that getDAWApplicationIcon returns the Reaper logo for Reaper.
     * This icon is used in the project explorer to identify DAW project files.
     */
    @Test
    void testGetDAWApplicationIcon_Reaper() {
        Image icon = applicationDefaults.getDAWApplicationIcon(DawApplication.REAPER);
        assertNotNull(icon, "Reaper icon should not be null");
        assertSame(applicationDefaults.reaperLogoImage, icon);
    }

    /**
     * Test that getDAWApplicationIcon returns the Studio One logo for Studio One.
     * This icon is used in the project explorer to identify Studio One project files.
     * Studio One was added in this update and requires its own icon entry.
     */
    @Test
    void testGetDAWApplicationIcon_StudioOne() {
        Image icon = applicationDefaults.getDAWApplicationIcon(DawApplication.STUDIO_ONE);
        assertNotNull(icon, "Studio One icon should not be null");
        assertSame(applicationDefaults.studioOneLogoImage, icon);
    }

    // ========================================
    // Tests for environment property access
    // ========================================

    /**
     * Test that getVersion retrieves the application version from environment properties.
     * The version is displayed in the UI and used for update checks.
     */
    @Test
    void testGetVersion() {
        when(environment.getProperty("owlplug.version")).thenReturn("1.2.3");
        assertEquals("1.2.3", applicationDefaults.getVersion());
        verify(environment).getProperty("owlplug.version");
    }

    /**
     * Test that getLatestUrl retrieves the GitHub latest release URL from environment properties.
     * This URL points to the GitHub API endpoint used to check for the latest application release.
     */
    @Test
    void testGetLatestUrl() {
        when(environment.getProperty("owlplug.github.latest-url")).thenReturn("https://api.github.com/repos/DropSnorz/OwlPlug/releases/latest");
        assertEquals("https://api.github.com/repos/DropSnorz/OwlPlug/releases/latest", applicationDefaults.getLatestUrl());
        verify(environment).getProperty("owlplug.github.latest-url");
    }

    /**
     * Test that getDownloadUrl retrieves the GitHub release download URL from environment properties.
     * This URL is used by the update service to direct users to the download page.
     */
    @Test
    void testGetDownloadUrl() {
        when(environment.getProperty("owlplug.github.download-url")).thenReturn("https://github.com/DropSnorz/OwlPlug/releases");
        assertEquals("https://github.com/DropSnorz/OwlPlug/releases", applicationDefaults.getDownloadUrl());
        verify(environment).getProperty("owlplug.github.download-url");
    }

    /**
     * Test that getOwlPlugRegistryUrl retrieves the OwlPlug registry URL from environment properties.
     * The registry contains available plugin packages for installation through the store.
     */
    @Test
    void testGetOwlPlugRegistryUrl() {
        when(environment.getProperty("owlplug.registry.url")).thenReturn("https://registry.owlplug.com");
        assertEquals("https://registry.owlplug.com", applicationDefaults.getOwlPlugRegistryUrl());
        verify(environment).getProperty("owlplug.registry.url");
    }

    /**
     * Test that getOpenAudioRegistryUrl retrieves the Open Audio registry URL from environment properties.
     * This provides access to plugins following the Open Audio standard specification.
     */
    @Test
    void testGetOpenAudioRegistryUrl() {
        when(environment.getProperty("openaudio.registry.url")).thenReturn("https://openaudio.org");
        assertEquals("https://openaudio.org", applicationDefaults.getOpenAudioRegistryUrl());
        verify(environment).getProperty("openaudio.registry.url");
    }

    /**
     * Test that getEnvProperty can retrieve arbitrary properties from the environment.
     * This generic accessor provides flexibility for custom configuration values.
     */
    @Test
    void testGetEnvProperty() {
        when(environment.getProperty("custom.property")).thenReturn("custom-value");
        assertEquals("custom-value", applicationDefaults.getEnvProperty("custom.property"));
        verify(environment).getProperty("custom.property");
    }

    /**
     * Test that getRuntimePlatform delegate to the RuntimePlatformResolver.
     * This ensures proper platform detection throughout the application.
     */
    @Test
    void testGetRuntimePlatform() {
        RuntimePlatform mockPlatform = new RuntimePlatform(null, OperatingSystem.MAC, null);
        when(runtimePlatformResolver.getCurrentPlatform()).thenReturn(mockPlatform);

        RuntimePlatform result = applicationDefaults.getRuntimePlatform();
        assertSame(mockPlatform, result);
        verify(runtimePlatformResolver).getCurrentPlatform();
    }

    // ========================================
    // Tests for static directory path methods
    // ========================================

    /**
     * Test that getUserDataDirectory returns a path in the user's home directory.
     * The user data directory (.owlplug) contains all application data, preferences, and databases.
     */
    @Test
    void testGetUserDataDirectory() {
        String userDataDir = ApplicationDefaults.getUserDataDirectory();
        assertNotNull(userDataDir);
        assertTrue(userDataDir.contains(".owlplug"));

        String expectedPath = Paths.get(System.getProperty("user.home"), ".owlplug").toString();
        assertEquals(expectedPath, userDataDir);
    }

    /**
     * Test that getTempDownloadDirectory returns the temp subdirectory within user data.
     * This directory is used for temporary storage during plugin downloads and installations.
     */
    @Test
    void testGetTempDownloadDirectory() {
        String tempDir = ApplicationDefaults.getTempDownloadDirectory();
        assertNotNull(tempDir);
        assertTrue(tempDir.contains(".owlplug"));
        assertTrue(tempDir.contains("temp"));
    }

    /**
     * Test that getLogDirectory returns the logs subdirectory within user data.
     * This directory contains application log files for debugging and troubleshooting.
     */
    @Test
    void testGetLogDirectory() {
        String logDir = ApplicationDefaults.getLogDirectory();
        assertNotNull(logDir);
        assertTrue(logDir.contains(".owlplug"));
        assertTrue(logDir.contains("logs"));
    }

    // ========================================
    // Tests for contributors list
    // ========================================

    /**
     * Test that getContributors returns a non-null list.
     * The contributor list is loaded from a resource file and displayed in the about dialog.
     */
    @Test
    void testGetContributors() {
        List<String> contributors = ApplicationDefaults.getContributors();
        assertNotNull(contributors, "Contributors list should not be null");
    }

    /**
     * Test that getContributors returns consistent data on multiple calls (caching behavior).
     * This verifies that the contributor list is cached after the first load for efficiency.
     */
    @Test
    void testGetContributors_Caching() {
        List<String> contributors1 = ApplicationDefaults.getContributors();
        List<String> contributors2 = ApplicationDefaults.getContributors();
        assertEquals(contributors1, contributors2);
    }

    // ========================================
    // Tests for constants
    // ========================================

    /**
     * Test that the APPLICATION_NAME constant is correctly defined.
     * This constant is used throughout the application for branding and display.
     */
    @Test
    void testApplicationNameConstant() {
        assertEquals("OwlPlug", ApplicationDefaults.APPLICATION_NAME);
    }
}
