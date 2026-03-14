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

package com.owlplug.host.loaders;

import com.owlplug.host.JuceXMLPlugin;
import com.owlplug.host.NativePlugin;
import com.owlplug.host.io.ClassPathFileExtractor;
import com.owlplug.host.io.ClassPathVersionUtils;
import com.owlplug.host.io.CommandResult;
import com.owlplug.host.io.CommandRunner;
import com.owlplug.host.utils.OSUtils;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.nio.file.FileSystems.getDefault;


public class EmbeddedScannerPluginLoader implements NativePluginLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedScannerPluginLoader.class);

    private static final String PLUGIN_COMPONENT_OUTPUT_DELIMITER_BEGIN = "---BEGIN PLUGIN COMPONENT DELIMITER---";
    private static final String PLUGIN_COMPONENT_OUTPUT_DELIMITER_END = "---END PLUGIN COMPONENT DELIMITER---";

    private static EmbeddedScannerPluginLoader INSTANCE;

    private static final String SEPARATOR = getDefault().getSeparator();
    private static final String DEFAULT_SCANNER_NAME = "owlplug-scanner";
    private static final String DEFAULT_SCANNER_VERSION = ClassPathVersionUtils.getVersionSafe(DEFAULT_SCANNER_NAME);
    private static final String DEFAULT_SCANNER_EXT = getPlatformExecutableExtension();
    private static final String DEFAULT_SCANNER_PLATFORM_TAG = OSUtils.getPlatformTagName();
    private static final String DEFAULT_SCANNER_ID = "%s-%s-%s%s".formatted(DEFAULT_SCANNER_NAME, DEFAULT_SCANNER_VERSION,
            DEFAULT_SCANNER_PLATFORM_TAG, DEFAULT_SCANNER_EXT);

    private boolean available = false;
    private final String scannerDirectory;
    private final String scannerId;

    public static EmbeddedScannerPluginLoader getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new EmbeddedScannerPluginLoader();
        }
        return INSTANCE;
    }

    private EmbeddedScannerPluginLoader() {
        scannerDirectory = System.getProperty("java.io.tmpdir");
        scannerId = DEFAULT_SCANNER_ID;
    }

    @Override
    public void init() {

        LOGGER.debug("Init plugin loader");
        File scannerFile = new File(scannerDirectory, scannerId);
        if (!scannerFile.exists()) {
            try {
                ClassPathFileExtractor.extract(this.getClass(), DEFAULT_SCANNER_ID, scannerFile);
            } catch (IOException e) {
                LOGGER.error("Scanner executable can't be extracted to {}", scannerFile.getAbsolutePath());
            }
        }

        if (scannerFile.exists()) {
            available = true;

            // Apply executable permissions on POSIX filesystem
            if (OSUtils.isPosixFileSystem()) {
                try {
                    Set<PosixFilePermission> executablePermission = PosixFilePermissions.fromString("rwxr-xr--");
                    Files.setPosixFilePermissions(scannerFile.toPath(), executablePermission);
                } catch (IOException e) {
                    LOGGER.error("Permissions can't be applied on file {}", scannerFile.getPath(), e);
                }
            }

        } else {
            LOGGER.error("Can't find owlplug scanner executable at {}", scannerFile.getPath());
        }

    }

    @Override
    public void open() {

    }

    @Override
    public List<NativePlugin> loadPlugin(String path) {

        LOGGER.debug("Load plugin {}", path);

        if (!isAvailable()) {
            throw new IllegalStateException("Plugin loader must be available");
        }

        try {
            CommandRunner commandRunner = new CommandRunner();
            commandRunner.setTimeoutActivated(true);
            commandRunner.setTimeout(30000); // 30-second timeout
            CommandResult result = commandRunner.run(scannerDirectory + SEPARATOR + scannerId, path);
            LOGGER.debug("Response received from scanner");
            LOGGER.debug(result.getOutput());

            if (result.getExitValue() >= 0) {

                LOGGER.debug("Extracting XML from content received by the scanner");
                String output = result.getOutput();

                return createPluginsFromCommandOutput(output);

            } else {
                LOGGER.debug("Invalid return code {} received from plugin scanner", result.getExitValue());
            }

        } catch (IOException e) {
            LOGGER.error("Error executing plugin scanner {}", path, e);
        }

        return null;
    }

    private List<NativePlugin> createPluginsFromCommandOutput(String output) {

        ArrayList<NativePlugin> plugins = new ArrayList<>();
        LOGGER.trace("Looking for PLUGIN COMPONENT DELIMITER in output");

        if (output.contains(PLUGIN_COMPONENT_OUTPUT_DELIMITER_BEGIN)) {

            String[] componentOutputs = output.split(PLUGIN_COMPONENT_OUTPUT_DELIMITER_BEGIN);

            for (int i = 0; i < componentOutputs.length; i++) {

                if (componentOutputs[i].contains("<?xml")) {
                    // Remove content before XML tag in case plugin logged stuff in the stdout.
                    String outputXML = componentOutputs[i].substring(componentOutputs[i].indexOf("<?xml"));

                    // Remove everything after the end delimiter in case the plugin logged stuff in the stdout
                    if (outputXML.contains(PLUGIN_COMPONENT_OUTPUT_DELIMITER_END)) {
                        outputXML = outputXML.substring(0, outputXML.indexOf(PLUGIN_COMPONENT_OUTPUT_DELIMITER_END));
                    }

                    outputXML = outputXML.strip();

                    JuceXMLPlugin plugin = createJUCEPluginFromRawXml(outputXML);
                    if (plugin != null) {
                        plugins.add(plugin.toNativePlugin());
                    }

                } else {
                    LOGGER.trace("No XML tag can be extracted from part {} for plugin", i);
                    LOGGER.trace(componentOutputs[i]);
                }

            }
        } else {
            LOGGER.error("No Plugin delimiter tag can be extracted from scanner output");
            LOGGER.debug(output);
        }

        return plugins;
    }

    private JuceXMLPlugin createJUCEPluginFromRawXml(String xml) {
        LOGGER.debug("Create plugin from raw XML");
        LOGGER.debug(xml);

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(JuceXMLPlugin.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return (JuceXMLPlugin) jaxbUnmarshaller.unmarshal(new StringReader(xml));

        } catch (JAXBException e) {
            LOGGER.error("Error during XML mapping", e);
            LOGGER.error(xml);
            return null;
        }
    }

    @Override
    public void close() {
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public String getName() {
        return "OwlPlug Scanner";
    }

    @Override
    public String getId() {
        return "owlplug-scanner";
    }

    @Override
    public String toString() {
        return this.getName();
    }


    /**
     * Returns platform default executable extension.
     * No extensions are defined for scanners.
     * Returns an empty string for any other hosts
     *
     * @return host default library extension
     */
    private static String getPlatformExecutableExtension() {
        return "";
    }

}
