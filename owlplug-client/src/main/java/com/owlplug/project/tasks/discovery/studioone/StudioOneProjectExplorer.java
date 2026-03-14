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

package com.owlplug.project.tasks.discovery.studioone;

import com.owlplug.core.utils.ArchiveUtils;
import com.owlplug.core.utils.FileUtils;
import com.owlplug.project.model.DawApplication;
import com.owlplug.project.model.DawPlugin;
import com.owlplug.project.model.DawProject;
import com.owlplug.project.tasks.discovery.ProjectExplorer;
import com.owlplug.project.tasks.discovery.ProjectExplorerException;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

/**
 * Explores Studio One project files ({@code .song}) to extract project metadata and plugin usage.
 *
 * <p>Studio One project files are ZIP archives. This explorer selectively extracts only the
 * necessary XML files ({@code metainfo.xml}, {@code audiomixer.xml}, {@code audiosynthfolder.xml})
 * to a temporary directory, parses them, and cleans up afterwards.
 *
 * <p>Autosave files (those whose names contain {@code "(Autosaved)"}) and files located inside
 * {@code History} directories are automatically skipped.
 */
public class StudioOneProjectExplorer implements ProjectExplorer {

    private final Logger LOGGER = LoggerFactory.getLogger(StudioOneProjectExplorer.class);

    /**
     * Returns {@code true} if this explorer is capable of processing the given file.
     *
     * <p>A file is considered explorable if it:
     * <ul>
     *   <li>has a {@code .song} extension,</li>
     *   <li>does not contain {@code "(Autosaved)"} in its filename, and</li>
     *   <li>is not located inside a {@code History} directory.</li>
     * </ul>
     *
     * @param file the file to test
     * @return {@code true} if the file should be explored; {@code false} otherwise
     */
    public boolean canExploreFile(File file) {
        if (!file.isFile() || !file.getAbsolutePath().endsWith(".song")) {
            return false;
        }
        final var fileName = file.getName();
        final var filePath = file.getAbsolutePath();
        if (fileName.contains("(Autosaved)") || filePath.contains("\\History\\") || filePath.contains("/History/")) {
            LOGGER.debug("Skipping autosave file: {}", file.getAbsolutePath());
            return false;
        }
        return true;
    }

    /**
     * Explores a Studio One project file and extracts its metadata and plugin list.
     *
     * @param file the {@code .song} file to explore
     * @return the populated {@link DawProject}, or {@code null} if the file cannot be explored
     *         or a non-critical extraction error occurred
     * @throws ProjectExplorerException if a fatal I/O or XML parsing error occurs
     */
    public DawProject explore(File file) throws ProjectExplorerException {
        if (!canExploreFile(file)) {
            return null;
        }

        LOGGER.debug("Starting exploring Studio One file: {}", file.getAbsoluteFile());

        Path tempDir = null;
        try {
            // Extract only the files we need from the ZIP archive to a temporary directory.
            // This avoids creating directories with reserved names (e.g., the Windows "Strings" directory).
            tempDir = Files.createTempDirectory("owlplug-studioone-");
            LOGGER.debug("Extracting Studio One project to: {}", tempDir);

            final var targetFiles = List.of(
                    "metainfo.xml",
                    "Devices/audiomixer.xml",
                    "Devices/audiosynthfolder.xml"
            );

            try {
                ArchiveUtils.extract(file, tempDir.toFile(), targetFiles);
            } catch (IOException e) {
                LOGGER.warn("Failed to extract Studio One project file: {} - {}",
                        file.getAbsolutePath(), e.getMessage());
                return null;
            }

            final var metainfoFile = new File(tempDir.toFile(), "metainfo.xml");
            if (!metainfoFile.exists()) {
                throw new ProjectExplorerException(
                        "metainfo.xml not found in Studio One project: " + file.getAbsolutePath(), null);
            }

            final var metainfoDoc = createDocument(metainfoFile);
            final var xpath = XPathFactory.newInstance().newXPath();

            final var project = new DawProject();
            project.setApplication(DawApplication.STUDIO_ONE);
            project.setPath(FileUtils.convertPath(file.getAbsolutePath()));
            project.setName(FilenameUtils.removeExtension(file.getName()));

            extractMetadata(metainfoDoc, project, xpath);

            project.setLastModifiedAt(new Date(file.lastModified()));
            final var attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            project.setCreatedAt(Date.from(attr.creationTime().toInstant()));

            collectPlugins(tempDir, project);

            return project;

        } catch (IOException e) {
            throw new ProjectExplorerException(
                    "Error while extracting Studio One project file: " + file.getAbsolutePath(), e);
        } catch (ParserConfigurationException | SAXException e) {
            throw new ProjectExplorerException(
                    "Error while parsing XML in Studio One project: " + file.getAbsolutePath(), e);
        } finally {
            if (tempDir != null) {
                try {
                    org.apache.commons.io.FileUtils.deleteDirectory(tempDir.toFile());
                    LOGGER.debug("Cleaned up temporary directory: {}", tempDir);
                } catch (IOException e) {
                    LOGGER.error("Failed to clean up temporary directory: {}", tempDir, e);
                }
            }
        }
    }

    /**
     * Extracts project metadata from the {@code metainfo.xml} document and populates
     * the given {@link DawProject}.
     *
     * <p>Each metadata field is extracted independently; a failure in one does not abort
     * the others. Missing or unrecognized values fall back to safe defaults.
     *
     * @param metainfoDoc the parsed {@code metainfo.xml} document
     * @param project     the project to populate
     * @param xpath       the XPath instance to use for queries
     */
    private void extractMetadata(Document metainfoDoc, DawProject project, XPath xpath) {
        try {
            final var titleNodes = (NodeList) xpath.compile("//Attribute[@id='Document:Title']/@value")
                    .evaluate(metainfoDoc, XPathConstants.NODESET);
            if (titleNodes.getLength() > 0) {
                final var title = titleNodes.item(0).getNodeValue();
                if (title != null && !title.isEmpty()) {
                    project.setName(title);
                }
            }
        } catch (XPathExpressionException e) {
            LOGGER.debug("Error extracting Document:Title from metainfo.xml, using filename as default", e);
        }

        try {
            final var generatorNodes = (NodeList) xpath.compile("//Attribute[@id='Document:Generator']/@value")
                    .evaluate(metainfoDoc, XPathConstants.NODESET);
            if (generatorNodes.getLength() > 0) {
                final var generator = generatorNodes.item(0).getNodeValue();
                project.setAppFullName(
                        generator != null && generator.startsWith("Studio One/") ? generator : "Studio One");
            } else {
                project.setAppFullName("Studio One");
            }
        } catch (XPathExpressionException e) {
            LOGGER.debug("Error extracting Document:Generator from metainfo.xml, using default", e);
            project.setAppFullName("Studio One");
        }

        try {
            final var versionNodes = (NodeList) xpath.compile("//Attribute[@id='Document:FormatVersion']/@value")
                    .evaluate(metainfoDoc, XPathConstants.NODESET);
            if (versionNodes.getLength() > 0) {
                project.setFormatVersion(versionNodes.item(0).getNodeValue());
            }
        } catch (XPathExpressionException e) {
            LOGGER.debug("Error extracting Document:FormatVersion from metainfo.xml, leaving unset", e);
        }
    }

    /**
     * Parses the XML device files in the temporary directory and adds all discovered plugins
     * to the given project.
     *
     * @param tempDir the directory containing the extracted XML device files
     * @param project the project to populate with plugins
     */
    private void collectPlugins(Path tempDir, DawProject project) {
        parseAndCollectPlugins(
                new File(tempDir.toFile(), "Devices/audiomixer.xml"),
                doc -> new StudioOneAudioMixerPluginCollector(doc).collectPlugins(),
                project,
                "Error parsing audiomixer.xml"
        );
        parseAndCollectPlugins(
                new File(tempDir.toFile(), "Devices/audiosynthfolder.xml"),
                doc -> new StudioOneSynthPluginCollector(doc).collectPlugins(),
                project,
                "Error parsing audiosynthfolder.xml"
        );
    }

    /**
     * Parses the given XML file, applies the provided plugin extractor, and adds the resulting
     * plugins to the project. Does nothing if the file does not exist.
     *
     * @param xmlFile         the XML file to parse
     * @param pluginExtractor a function that extracts plugins from the parsed document
     * @param project         the project to populate
     * @param errorMessage    the message to log if parsing or extraction fails
     */
    private void parseAndCollectPlugins(File xmlFile, Function<Document, List<DawPlugin>> pluginExtractor,
            DawProject project, String errorMessage) {
        if (!xmlFile.exists()) {
            return;
        }
        try {
            final var plugins = pluginExtractor.apply(createDocument(xmlFile));
            plugins.forEach(plugin -> {
                plugin.setProject(project);
                project.getPlugins().add(plugin);
            });
        } catch (Exception e) {
            LOGGER.error(errorMessage, e);
        }
    }

    /**
     * Parses an XML file into a {@link Document} with XXE protections enabled.
     *
     * @param file the file to parse
     * @return the parsed document
     * @throws ProjectExplorerException     if the file is not found
     * @throws ParserConfigurationException if the parser cannot be configured
     * @throws SAXException                 if a parsing error occurs
     * @throws IOException                  if an I/O error occurs
     */
    private Document createDocument(File file)
            throws ProjectExplorerException, ParserConfigurationException, SAXException, IOException {
        try (final var fis = new FileInputStream(file)) {
            final var builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            builderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            builderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            final var builder = builderFactory.newDocumentBuilder();
            return builder.parse(fis);
        } catch (FileNotFoundException e) {
            throw new ProjectExplorerException("File not found: " + file.getAbsolutePath(), e);
        }
    }

}
