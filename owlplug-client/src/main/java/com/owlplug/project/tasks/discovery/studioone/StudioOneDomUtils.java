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

import com.owlplug.plugin.model.PluginFormat;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Utility class providing DOM traversal and data extraction helpers
 * for Studio One project XML documents.
 *
 * <p>Studio One project files are ZIP archives containing XML documents that use a
 * non-standard {@code x:id} attribute (the colon is part of the literal attribute name,
 * not an XML namespace separator) to link related {@code Attributes} elements together.
 */
public class StudioOneDomUtils {

    /**
     * Finds a child {@code Attributes} element with the given {@code x:id} attribute value.
     *
     * <p>Note: {@code x:id} is a literal attribute name — the colon is not a namespace separator.
     *
     * @param parent   the parent element to search within
     * @param xidValue the expected value of the {@code x:id} attribute
     * @return the matching element, or {@code null} if not found
     */
    public static Element findElementByXId(Element parent, String xidValue) {
        return findAttributeByXId(parent, xidValue);
    }

    /**
     * Finds the {@code deviceData} and {@code ghostData} child elements within the given element.
     *
     * @param element the parent element to search within
     * @return a record containing the {@code deviceData} and {@code ghostData} elements;
     *         either field may be {@code null} if the corresponding child was not found
     */
    public static DeviceDataAndGhostData findDeviceDataAndGhostData(Element element) {
        return new DeviceDataAndGhostData(
                findAttributeByXId(element, "deviceData"),
                findAttributeByXId(element, "ghostData")
        );
    }

    /**
     * Finds the {@code classInfo} child element within the given {@code ghostData} element.
     *
     * @param ghostData the {@code ghostData} element to search within
     * @return the {@code classInfo} element, or {@code null} if not found or {@code ghostData} is {@code null}
     */
    public static Element findClassInfo(Element ghostData) {
        return findAttributeByXId(ghostData, "classInfo");
    }

    /**
     * Extracts the plugin name from the given element.
     *
     * <p>{@code classInfo} is preferred over {@code deviceData} because it holds the canonical
     * plugin name rather than the instance-specific label (e.g., {@code "Ozone 9"} instead of
     * {@code "Ozone 9 (2)"}). {@code deviceData} is used as a fallback only when {@code classInfo}
     * is absent or carries no name.
     *
     * @param element the element containing plugin information
     * @return the plugin name, or {@code null} if it could not be determined
     */
    public static String extractPluginName(Element element) {
        final var data = findDeviceDataAndGhostData(element);

        final var classInfo = findClassInfo(data.ghostData());
        if (classInfo != null) {
            // Element.getAttribute() always returns a non-null String per the W3C DOM spec
            // (empty string when the attribute is absent), so no null check is needed.
            final var name = classInfo.getAttribute("name");
            if (!name.isEmpty()) {
                return name;
            }
        }

        if (data.deviceData() != null) {
            final var name = data.deviceData().getAttribute("name");
            if (!name.isEmpty()) {
                return name;
            }
        }

        return null;
    }

    /**
     * Extracts the plugin format from the given element for audio effect plugins.
     *
     * <p>Returns {@code null} for native Studio One devices.
     *
     * @param element the element containing plugin information
     * @return the {@link PluginFormat}, or {@code null} if the format is native or could not be determined
     */
    public static PluginFormat extractPluginFormat(Element element) {
        final var data = findDeviceDataAndGhostData(element);
        final var classInfo = findClassInfo(data.ghostData());
        if (classInfo == null) {
            return null;
        }
        return subCategoryToFormat(classInfo.getAttribute("subCategory"));
    }

    /**
     * Extracts the plugin format from the given element for instrument (synth) plugins.
     *
     * <p>Unlike {@link #extractPluginFormat(Element)}, this method additionally requires
     * the element's {@code category} attribute to equal {@code "AudioSynth"}, filtering out
     * non-instrument entries. Returns {@code null} for native Studio One devices.
     *
     * @param element the element containing plugin information
     * @return the {@link PluginFormat}, or {@code null} if the format is native, the category
     *         is not {@code "AudioSynth"}, or the format could not be determined
     */
    public static PluginFormat extractSynthPluginFormat(Element element) {
        final var data = findDeviceDataAndGhostData(element);
        final var classInfo = findClassInfo(data.ghostData());
        if (classInfo == null || !"AudioSynth".equals(classInfo.getAttribute("category"))) {
            return null;
        }
        return subCategoryToFormat(classInfo.getAttribute("subCategory"));
    }

    /**
     * Maps a Studio One {@code subCategory} attribute value to a {@link PluginFormat}.
     *
     * @param subCategory the {@code subCategory} attribute value
     * @return the matching {@link PluginFormat}, or {@code null} if the value is blank,
     *         {@code "(Native)"}, or unrecognized
     */
    private static PluginFormat subCategoryToFormat(String subCategory) {
        if (subCategory.isEmpty() || "(Native)".equals(subCategory)) {
            return null;
        }
        if (subCategory.contains("VST2")) {
            return PluginFormat.VST2;
        } else if (subCategory.contains("VST3")) {
            return PluginFormat.VST3;
        }
        return null;
    }

    /**
     * Finds an {@code Attributes} child element with the given {@code x:id} attribute value.
     *
     * @param parent   the parent element to search within; may be {@code null}
     * @param xidValue the expected {@code x:id} value; may be {@code null}
     * @return the matching element, or {@code null} if not found
     */
    private static Element findAttributeByXId(Element parent, String xidValue) {
        if (parent == null || xidValue == null) {
            return null;
        }
        final var children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            final var child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                final var elem = (Element) child;
                if ("Attributes".equals(elem.getTagName()) && xidValue.equals(elem.getAttribute("x:id"))) {
                    return elem;
                }
            }
        }
        return null;
    }

    /**
     * Holds the {@code deviceData} and {@code ghostData} child elements found within
     * a Studio One plugin element. Either field may be {@code null} if the corresponding
     * child element is absent.
     *
     * @param deviceData the {@code deviceData} element, or {@code null} if absent
     * @param ghostData  the {@code ghostData} element, or {@code null} if absent
     */
    public record DeviceDataAndGhostData(Element deviceData, Element ghostData) {
    }

}
