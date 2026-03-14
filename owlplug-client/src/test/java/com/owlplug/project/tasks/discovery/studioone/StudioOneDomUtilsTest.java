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
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link StudioOneDomUtils}.
 *
 * <p>Tests cover DOM traversal (finding {@code Attributes} elements by {@code x:id}),
 * plugin name extraction with the classInfo-priority rule, and plugin format resolution
 * for both audio effects and instrument (synth) plugins.
 */
class StudioOneDomUtilsTest {

    /**
     * Parses an XML string and returns its root element.
     */
    private static Element parseRoot(String xml) throws Exception {
        final var factory = DocumentBuilderFactory.newInstance();
        final var builder = factory.newDocumentBuilder();
        final Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        return doc.getDocumentElement();
    }

    // ──────────────────────────────────────────
    // findElementByXId
    // ──────────────────────────────────────────

    @Test
    void findElementByXId_returnsMatchingChildElement() throws Exception {
        final var root = parseRoot("""
                <Root>
                    <Attributes x:id="deviceData" name="MyPlugin" />
                </Root>
                """);

        final var found = StudioOneDomUtils.findElementByXId(root, "deviceData");

        assertNotNull(found);
        assertEquals("MyPlugin", found.getAttribute("name"));
    }

    @Test
    void findElementByXId_returnsNull_whenNoChildMatches() throws Exception {
        final var root = parseRoot("""
                <Root>
                    <Attributes x:id="other" name="OtherPlugin" />
                </Root>
                """);

        assertNull(StudioOneDomUtils.findElementByXId(root, "deviceData"));
    }

    @Test
    void findElementByXId_returnsNull_forNullParent() {
        assertNull(StudioOneDomUtils.findElementByXId(null, "deviceData"));
    }

    @Test
    void findElementByXId_returnsNull_forNullXidValue() throws Exception {
        final var root = parseRoot("<Root><Attributes x:id=\"deviceData\" /></Root>");
        assertNull(StudioOneDomUtils.findElementByXId(root, null));
    }

    // ──────────────────────────────────────────
    // findDeviceDataAndGhostData
    // ──────────────────────────────────────────

    @Test
    void findDeviceDataAndGhostData_findsBothElements() throws Exception {
        final var root = parseRoot("""
                <Root>
                    <Attributes x:id="deviceData" name="Plugin" />
                    <Attributes x:id="ghostData">
                        <Attributes x:id="classInfo" name="Plugin" subCategory="VST2" />
                    </Attributes>
                </Root>
                """);

        final var data = StudioOneDomUtils.findDeviceDataAndGhostData(root);

        assertNotNull(data.deviceData());
        assertNotNull(data.ghostData());
        assertEquals("Plugin", data.deviceData().getAttribute("name"));
    }

    @Test
    void findDeviceDataAndGhostData_returnsNullFields_whenChildrenAbsent() throws Exception {
        final var root = parseRoot("<Root></Root>");

        final var data = StudioOneDomUtils.findDeviceDataAndGhostData(root);

        assertNull(data.deviceData());
        assertNull(data.ghostData());
    }

    // ──────────────────────────────────────────
    // extractPluginName
    // ──────────────────────────────────────────

    @Test
    void extractPluginName_prefersClassInfoOverDeviceData() throws Exception {
        final var root = parseRoot("""
                <Root>
                    <Attributes x:id="deviceData" name="Ozone 9 (2)" />
                    <Attributes x:id="ghostData">
                        <Attributes x:id="classInfo" name="iZotope Ozone 9" subCategory="VST2" />
                    </Attributes>
                </Root>
                """);

        assertEquals("iZotope Ozone 9", StudioOneDomUtils.extractPluginName(root));
    }

    @Test
    void extractPluginName_fallsBackToDeviceData_whenClassInfoAbsent() throws Exception {
        final var root = parseRoot("""
                <Root>
                    <Attributes x:id="deviceData" name="FallbackPlugin" />
                    <Attributes x:id="ghostData">
                    </Attributes>
                </Root>
                """);

        assertEquals("FallbackPlugin", StudioOneDomUtils.extractPluginName(root));
    }

    @Test
    void extractPluginName_returnsNull_whenNeitherSourceHasName() throws Exception {
        final var root = parseRoot("<Root></Root>");
        assertNull(StudioOneDomUtils.extractPluginName(root));
    }

    // ──────────────────────────────────────────
    // extractPluginFormat
    // ──────────────────────────────────────────

    @Test
    void extractPluginFormat_returnsVst2_forVst2SubCategory() throws Exception {
        final var root = parseRoot("""
                <Root>
                    <Attributes x:id="ghostData">
                        <Attributes x:id="classInfo" name="Plugin" subCategory="VST2" category="AudioEffect" />
                    </Attributes>
                </Root>
                """);

        assertEquals(PluginFormat.VST2, StudioOneDomUtils.extractPluginFormat(root));
    }

    @Test
    void extractPluginFormat_returnsVst3_forVst3SubCategory() throws Exception {
        final var root = parseRoot("""
                <Root>
                    <Attributes x:id="ghostData">
                        <Attributes x:id="classInfo" name="Plugin" subCategory="VST3" category="AudioEffect" />
                    </Attributes>
                </Root>
                """);

        assertEquals(PluginFormat.VST3, StudioOneDomUtils.extractPluginFormat(root));
    }

    @Test
    void extractPluginFormat_returnsNull_forNativeSubCategory() throws Exception {
        final var root = parseRoot("""
                <Root>
                    <Attributes x:id="ghostData">
                        <Attributes x:id="classInfo" name="NativeDevice" subCategory="(Native)" category="AudioEffect" />
                    </Attributes>
                </Root>
                """);

        assertNull(StudioOneDomUtils.extractPluginFormat(root));
    }

    @Test
    void extractPluginFormat_returnsNull_whenClassInfoMissing() throws Exception {
        final var root = parseRoot("""
                <Root>
                    <Attributes x:id="ghostData">
                    </Attributes>
                </Root>
                """);

        assertNull(StudioOneDomUtils.extractPluginFormat(root));
    }

    // ──────────────────────────────────────────
    // extractSynthPluginFormat
    // ──────────────────────────────────────────

    @Test
    void extractSynthPluginFormat_returnsFormat_forAudioSynthCategory() throws Exception {
        final var root = parseRoot("""
                <Root>
                    <Attributes x:id="ghostData">
                        <Attributes x:id="classInfo" name="Synth" subCategory="VST2" category="AudioSynth" />
                    </Attributes>
                </Root>
                """);

        assertEquals(PluginFormat.VST2, StudioOneDomUtils.extractSynthPluginFormat(root));
    }

    @Test
    void extractSynthPluginFormat_returnsNull_forNonAudioSynthCategory() throws Exception {
        final var root = parseRoot("""
                <Root>
                    <Attributes x:id="ghostData">
                        <Attributes x:id="classInfo" name="Effect" subCategory="VST2" category="AudioEffect" />
                    </Attributes>
                </Root>
                """);

        assertNull(StudioOneDomUtils.extractSynthPluginFormat(root));
    }

    @Test
    void extractSynthPluginFormat_returnsNull_forNativeAudioSynth() throws Exception {
        final var root = parseRoot("""
                <Root>
                    <Attributes x:id="ghostData">
                        <Attributes x:id="classInfo" name="NativeSynth" subCategory="(Native)" category="AudioSynth" />
                    </Attributes>
                </Root>
                """);

        assertNull(StudioOneDomUtils.extractSynthPluginFormat(root));
    }
}
