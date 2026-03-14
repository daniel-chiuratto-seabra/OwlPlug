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
import com.owlplug.project.model.DawPlugin;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link StudioOneAudioMixerPluginCollector}.
 *
 * <p>Tests cover the full XML parsing pipeline including FX slot filtering,
 * format detection, native device exclusion, and name extraction priority rules.
 */
public class StudioOneAudioMixerPluginCollectorTest {

    /**
     * Parses an XML string into a {@link Document} for use in tests.
     */
    private static Document parseXml(String xml) throws Exception {
        final var factory = DocumentBuilderFactory.newInstance();
        final var builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void testCollectPlugins() throws Exception {
        File testFile = new File(this.getClass().getClassLoader()
                .getResource("projects/studioone/files/audiomixer.xml").getFile());
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(testFile);

        StudioOneAudioMixerPluginCollector collector = new StudioOneAudioMixerPluginCollector(document);
        List<DawPlugin> plugins = collector.collectPlugins();

        assertNotNull(plugins);
        assertEquals(3, plugins.size());

        DawPlugin firstPlugin = plugins.get(0);
        assertEquals("Reverb", firstPlugin.getName());
        assertEquals("VST2", firstPlugin.getFormat().name());
    }

    @Test
    void testCollectPlugins_emptyFxSlotsAreSkipped() throws Exception {
        final var xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <AudioMixer>
                    <Attributes name="FX EmptySlot" speakerFormat="Stereo">
                    </Attributes>
                    <Attributes name="FX Reverb" speakerFormat="Stereo">
                        <Attributes x:id="deviceData" name="Reverb" />
                        <Attributes x:id="ghostData">
                            <Attributes x:id="classInfo" name="Reverb" subCategory="VST2" category="AudioEffect" />
                        </Attributes>
                    </Attributes>
                </AudioMixer>
                """;

        final var collector = new StudioOneAudioMixerPluginCollector(parseXml(xml));
        final var plugins = collector.collectPlugins();

        assertEquals(1, plugins.size(), "Empty FX slot should be skipped");
        assertEquals("Reverb", plugins.get(0).getName());
    }

    @Test
    void testCollectPlugins_vst3PluginIsCollected() throws Exception {
        final var xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <AudioMixer>
                    <Attributes name="FX Pro-Q 3" speakerFormat="Stereo">
                        <Attributes x:id="deviceData" name="Pro-Q 3" />
                        <Attributes x:id="ghostData">
                            <Attributes x:id="classInfo" name="Pro-Q 3" subCategory="VST3" category="AudioEffect" />
                        </Attributes>
                    </Attributes>
                </AudioMixer>
                """;

        final var collector = new StudioOneAudioMixerPluginCollector(parseXml(xml));
        final var plugins = collector.collectPlugins();

        assertEquals(1, plugins.size());
        assertEquals("Pro-Q 3", plugins.get(0).getName());
        assertEquals(PluginFormat.VST3, plugins.get(0).getFormat());
    }

    @Test
    void testCollectPlugins_nativePluginIsSkipped() throws Exception {
        final var xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <AudioMixer>
                    <Attributes name="FX NativeReverb" speakerFormat="Stereo">
                        <Attributes x:id="deviceData" name="NativeReverb" />
                        <Attributes x:id="ghostData">
                            <Attributes x:id="classInfo" name="NativeReverb" subCategory="(Native)" category="AudioEffect" />
                        </Attributes>
                    </Attributes>
                </AudioMixer>
                """;

        final var collector = new StudioOneAudioMixerPluginCollector(parseXml(xml));
        final var plugins = collector.collectPlugins();

        assertTrue(plugins.isEmpty(), "Native Studio One device should be skipped");
    }

    @Test
    void testCollectPlugins_classInfoNameTakesPriorityOverDeviceDataName() throws Exception {
        final var xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <AudioMixer>
                    <Attributes name="FX Ozone" speakerFormat="Stereo">
                        <Attributes x:id="deviceData" name="Ozone 9 (2)" />
                        <Attributes x:id="ghostData">
                            <Attributes x:id="classInfo" name="iZotope Ozone 9" subCategory="VST2" category="AudioEffect" />
                        </Attributes>
                    </Attributes>
                </AudioMixer>
                """;

        final var collector = new StudioOneAudioMixerPluginCollector(parseXml(xml));
        final var plugins = collector.collectPlugins();

        assertEquals(1, plugins.size());
        assertEquals("iZotope Ozone 9", plugins.get(0).getName(),
                "classInfo name should take priority over the instance-specific deviceData name");
    }

    @Test
    void testCollectPlugins_noFxElements_returnsEmptyList() throws Exception {
        final var xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <AudioMixer>
                </AudioMixer>
                """;

        final var collector = new StudioOneAudioMixerPluginCollector(parseXml(xml));
        final var plugins = collector.collectPlugins();

        assertTrue(plugins.isEmpty());
    }
}
