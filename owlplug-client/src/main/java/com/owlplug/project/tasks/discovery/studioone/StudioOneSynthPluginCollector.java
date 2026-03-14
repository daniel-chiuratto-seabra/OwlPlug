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

import com.owlplug.core.utils.PluginUtils;
import com.owlplug.project.model.DawPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * Collects instrument (synth) plugins from a Studio One {@code audiosynthfolder.xml} document.
 *
 * <p>The synth folder document lists all virtual instruments loaded in the project.
 * Each instrument is represented as an {@code Attributes} element carrying both {@code name}
 * and {@code speakerFormat} attributes. Results are restricted to entries whose
 * {@code classInfo} carries the {@code "AudioSynth"} category.
 */
public class StudioOneSynthPluginCollector {

    private final Logger LOGGER = LoggerFactory.getLogger(StudioOneSynthPluginCollector.class);

    private final Document document;

    /**
     * Creates a new collector for the given synth folder document.
     *
     * @param document the parsed {@code audiosynthfolder.xml} document
     */
    public StudioOneSynthPluginCollector(Document document) {
        this.document = document;
    }

    /**
     * Collects all instrument plugins found in the synth folder document.
     *
     * @return a list of discovered {@link DawPlugin} instances; never {@code null}
     */
    public List<DawPlugin> collectPlugins() {
        List<DawPlugin> plugins = new ArrayList<>();

        final var xpath = XPathFactory.newInstance().newXPath();
        try {
            final var synthNodes = (NodeList) xpath.compile("//Attributes[@name and @speakerFormat]")
                    .evaluate(document, XPathConstants.NODESET);

            for (int i = 0; i < synthNodes.getLength(); i++) {
                final var node = synthNodes.item(i);
                if (node instanceof Element element) {
                    final var plugin = readSynthElement(element);
                    if (plugin != null) {
                        plugins.add(plugin);
                    }
                }
            }
        } catch (XPathExpressionException e) {
            LOGGER.error("Error extracting plugins from synth folder", e);
        }

        return plugins;
    }

    /**
     * Reads a single synth element and constructs a {@link DawPlugin} from it.
     *
     * @param synthElement the {@code Attributes} element representing an instrument slot
     * @return the constructed plugin, or {@code null} if the name or format could not be
     *         determined, or the entry does not belong to the {@code AudioSynth} category
     */
    private DawPlugin readSynthElement(Element synthElement) {
        final var pluginName = StudioOneDomUtils.extractPluginName(synthElement);
        if (pluginName == null || pluginName.isEmpty()) {
            return null;
        }

        final var format = StudioOneDomUtils.extractSynthPluginFormat(synthElement);
        if (format == null) {
            return null;
        }

        final var plugin = new DawPlugin();
        plugin.setName(PluginUtils.absoluteName(pluginName));
        plugin.setFormat(format);
        return plugin;
    }

}
