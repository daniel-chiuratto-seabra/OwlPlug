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

package com.owlplug.project.tasks.discovery.ableton;

import com.owlplug.core.utils.FileUtils;
import com.owlplug.project.model.DawApplication;
import com.owlplug.project.model.DawProject;
import com.owlplug.project.tasks.discovery.ProjectExplorer;
import com.owlplug.project.tasks.discovery.ProjectExplorerException;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

public class AbletonProjectExplorer implements ProjectExplorer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbletonProjectExplorer.class);

    public boolean canExploreFile(File file) {
        return file.isFile() && file.getAbsolutePath().endsWith(".als");
    }

    public DawProject explore(final File file) throws ProjectExplorerException {

        if (!canExploreFile(file)) {
            return null;
        }

        LOGGER.debug("Starting exploring file {}", file.getAbsoluteFile());

        try {
            final var xmlDocument = createDocument(file);
            final var xpath = XPathFactory.newInstance().newXPath();

            final var dawProject = new DawProject();
            dawProject.setApplication(DawApplication.ABLETON);
            dawProject.setPath(FileUtils.convertPath(file.getAbsolutePath()));
            dawProject.setName(FilenameUtils.removeExtension(file.getName()));

            final var abletonNode = (NodeList) xpath.compile("/Ableton").evaluate(xmlDocument, XPathConstants.NODESET);
            dawProject.setAppFullName(abletonNode.item(0).getAttributes().getNamedItem("Creator").getNodeValue());
            dawProject.setFormatVersion(abletonNode.item(0).getAttributes().getNamedItem("MajorVersion").getNodeValue());
            dawProject.setLastModifiedAt(new Date(file.lastModified()));

            final var basicFileAttributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);

            final var fileTime = basicFileAttributes.creationTime();
            dawProject.setCreatedAt(Date.from(fileTime.toInstant()));

            final var abletonSchema5PluginCollector = new AbletonSchema5PluginCollector(xmlDocument);
            final var dawPluginList = abletonSchema5PluginCollector.collectPlugins();

            dawPluginList.forEach(dawPlugin -> {
                dawPlugin.setProject(dawProject);
                dawProject.getPlugins().add(dawPlugin);
            });

            return dawProject;

        } catch (XPathExpressionException e) {
            throw new ProjectExplorerException("Error while parsing project file " + file.getAbsolutePath(), e);
        } catch (IOException e) {
            throw new ProjectExplorerException("Error while reading file " + file.getAbsolutePath(), e);
        }

    }

    private Document createDocument(final File file) throws ProjectExplorerException {

        try (final var inputStream = new FileInputStream(file);
             final var bufferedInputStream = new BufferedInputStream(inputStream);
             final var compressorInputStream = new CompressorStreamFactory().createCompressorInputStream(bufferedInputStream);
             final var bgzi = new BufferedInputStream(compressorInputStream)) {

            final var documentBuilderFactory = DocumentBuilderFactory.newInstance();
            final var documentBuilder = documentBuilderFactory.newDocumentBuilder();
            return documentBuilder.parse(bgzi);

        } catch (FileNotFoundException e) {
            throw new ProjectExplorerException("Project file not found: " + file.getAbsolutePath(), e);
        } catch (CompressorException e) {
            throw new ProjectExplorerException("Error while uncompressing project file: " + file.getAbsolutePath(), e);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new ProjectExplorerException("Unexpected error while reading project file: {}", e);
        }
    }

}
