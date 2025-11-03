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

import com.owlplug.core.utils.DomUtils;
import com.owlplug.plugin.model.PluginFormat;
import com.owlplug.project.model.DawPlugin;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class AbletonSchema5PluginCollector {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbletonSchema5PluginCollector.class);

    private final Document document;

    public List<DawPlugin> collectPlugins() {

        ArrayList<DawPlugin> plugins = new ArrayList<>();

        XPath xpath = XPathFactory.newInstance().newXPath();
        try {
            final var vstPlugins = (NodeList) xpath.compile("//PluginDevice/PluginDesc/VstPluginInfo")
                    .evaluate(document, XPathConstants.NODESET);

            for (int i = 0; i < vstPlugins.getLength(); i++) {
                final var node = vstPlugins.item(i);
                if (node instanceof Element element) {
                    plugins.add(readVstPluginElement(element));
                }
            }

            final var vst3Plugins = (NodeList) xpath.compile("//PluginDevice/PluginDesc/Vst3PluginInfo")
                    .evaluate(document, XPathConstants.NODESET);
            for (int i = 0; i < vst3Plugins.getLength(); i++) {
                final var node = vst3Plugins.item(i);
                if (node instanceof Element element) {
                    plugins.add(readVst3PluginElement(element));
                }
            }

            final var auPlugins = (NodeList) xpath.compile("//AuPluginDevice/PluginDesc/AuPluginInfo")
                    .evaluate(document, XPathConstants.NODESET);
            for (int i = 0; i < auPlugins.getLength(); i++) {
                final var node = auPlugins.item(i);
                if (node instanceof Element element) {
                    plugins.add(readAuPluginElement(element));
                }
            }

        } catch (final XPathExpressionException xPathExpressionException) {
            LOGGER.error("Error extracting plugin", xPathExpressionException);
        }

        return plugins;
    }

    private DawPlugin readVstPluginElement(final Element pluginElement) {
        final var dawPlugin = new DawPlugin();
        dawPlugin.setFormat(PluginFormat.VST2);

        final var fileNameNodes = DomUtils.getDirectDescendantElementsByTagName(pluginElement, "FileName");
        if (fileNameNodes.getLength() >= 1) {
            dawPlugin.setFileName(fileNameNodes.item(0).getAttributes().getNamedItem("Value").getNodeValue());
        }

        final var nameNodes = DomUtils.getDirectDescendantElementsByTagName(pluginElement, "PlugName");
        if (nameNodes.getLength() >= 1) {
            dawPlugin.setName(nameNodes.item(0).getAttributes().getNamedItem("Value").getNodeValue());
        }

        final var uniqueIdNode = DomUtils.getDirectDescendantElementsByTagName(pluginElement, "UniqueId");
        if (uniqueIdNode.getLength() >= 1) {
            dawPlugin.setUid(uniqueIdNode.item(0).getAttributes().getNamedItem("Value").getNodeValue());
        }

        return dawPlugin;
    }

    private DawPlugin readVst3PluginElement(final Element pluginElement) {
        final var dawPlugin = new DawPlugin();
        dawPlugin.setFormat(PluginFormat.VST3);
        NodeList nameNodes = DomUtils.getDirectDescendantElementsByTagName(pluginElement, "Name");
        if (nameNodes.getLength() >= 1) {
            dawPlugin.setName(nameNodes.item(0).getAttributes().getNamedItem("Value").getNodeValue());

        }
        return dawPlugin;
    }

    private DawPlugin readAuPluginElement(final Element pluginElement) {
        final var dawPlugin = new DawPlugin();
        dawPlugin.setFormat(PluginFormat.AU);

        final var nameNodes = DomUtils.getDirectDescendantElementsByTagName(pluginElement, "Name");
        if (nameNodes.getLength() >= 1) {
            dawPlugin.setName(nameNodes.item(0).getAttributes().getNamedItem("Value").getNodeValue());

        }
        return dawPlugin;
    }

}
