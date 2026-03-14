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
 * Collects audio effect plugins from a Studio One {@code audiomixer.xml} document.
 *
 * <p>The audio mixer document contains all FX inserts across channels and bus groups.
 * Each insert is represented as an {@code Attributes} element whose {@code name} attribute
 * starts with {@code "FX"}. Empty FX slots — those lacking both {@code deviceData} and
 * {@code ghostData} children — are skipped.
 */
public class StudioOneAudioMixerPluginCollector {

    private final Logger LOGGER = LoggerFactory.getLogger(StudioOneAudioMixerPluginCollector.class);

    private final Document document;

    /**
     * Creates a new collector for the given audio mixer document.
     *
     * @param document the parsed {@code audiomixer.xml} document
     */
    public StudioOneAudioMixerPluginCollector(Document document) {
        this.document = document;
    }

    /**
     * Collects all audio effect plugins found in the audio mixer document.
     *
     * @return a list of discovered {@link DawPlugin} instances; never {@code null}
     */
    public List<DawPlugin> collectPlugins() {
        List<DawPlugin> plugins = new ArrayList<>();

        final var xpath = XPathFactory.newInstance().newXPath();
        try {
            final var fxNodes = (NodeList) xpath
                    .compile("//Attributes[starts-with(@name, 'FX')]")
                    .evaluate(document, XPathConstants.NODESET);

            for (int i = 0; i < fxNodes.getLength(); i++) {
                final var node = fxNodes.item(i);
                if (node instanceof Element element) {
                    final var data = StudioOneDomUtils.findDeviceDataAndGhostData(element);
                    if (data.deviceData() == null && data.ghostData() == null) {
                        continue;
                    }
                    final var plugin = readPluginElement(element);
                    if (plugin != null) {
                        plugins.add(plugin);
                    }
                }
            }
        } catch (XPathExpressionException e) {
            LOGGER.error("Error extracting plugins from audio mixer", e);
        }

        return plugins;
    }

    /**
     * Reads a single FX element and constructs a {@link DawPlugin} from it.
     *
     * @param pluginElement the FX {@code Attributes} element to read
     * @return the constructed plugin, or {@code null} if the name or format could not be determined
     */
    private DawPlugin readPluginElement(Element pluginElement) {
        final var pluginName = StudioOneDomUtils.extractPluginName(pluginElement);
        if (pluginName == null || pluginName.isEmpty()) {
            return null;
        }

        final var format = StudioOneDomUtils.extractPluginFormat(pluginElement);
        if (format == null) {
            return null;
        }

        final var plugin = new DawPlugin();
        plugin.setName(PluginUtils.absoluteName(pluginName));
        plugin.setFormat(format);
        return plugin;
    }

}
