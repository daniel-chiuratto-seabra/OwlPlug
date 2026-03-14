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

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link StudioOneSynthPluginCollector}.
 *
 * <p>Tests cover instrument plugin collection including AudioSynth category filtering,
 * format detection for both VST2 and VST3, and exclusion of native Studio One devices.
 */
public class StudioOneSynthPluginCollectorTest {

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
        File testFile = new File(requireNonNull(getClass().getClassLoader()
                .getResource("projects/studioone/files/audiosynthfolder.xml")).getFile());
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(testFile);

        StudioOneSynthPluginCollector collector = new StudioOneSynthPluginCollector(document);
        List<DawPlugin> plugins = collector.collectPlugins();

        assertNotNull(plugins);
        assertEquals(2, plugins.size());

        DawPlugin firstPlugin = plugins.get(0);
        assertEquals("Sylenth1", firstPlugin.getName());
        assertEquals("VST2", firstPlugin.getFormat().name());
    }

    @Test
    void testCollectPlugins_nonAudioSynthCategoryIsSkipped() throws Exception {
        final var xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <SynthFolder>
                    <Attributes name="SomeEffect" speakerFormat="Stereo">
                        <Attributes x:id="deviceData" name="SomeEffect" />
                        <Attributes x:id="ghostData">
                            <Attributes x:id="classInfo" name="SomeEffect" subCategory="VST2" category="AudioEffect" />
                        </Attributes>
                    </Attributes>
                </SynthFolder>
                """;

        final var collector = new StudioOneSynthPluginCollector(parseXml(xml));
        final var plugins = collector.collectPlugins();

        assertTrue(plugins.isEmpty(), "Non-AudioSynth category entries should be excluded");
    }

    @Test
    void testCollectPlugins_nativeSynthIsSkipped() throws Exception {
        final var xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <SynthFolder>
                    <Attributes name="NativeSynth" speakerFormat="Stereo">
                        <Attributes x:id="deviceData" name="NativeSynth" />
                        <Attributes x:id="ghostData">
                            <Attributes x:id="classInfo" name="NativeSynth" subCategory="(Native)" category="AudioSynth" />
                        </Attributes>
                    </Attributes>
                </SynthFolder>
                """;

        final var collector = new StudioOneSynthPluginCollector(parseXml(xml));
        final var plugins = collector.collectPlugins();

        assertTrue(plugins.isEmpty(), "Native Studio One instrument should be skipped");
    }

    @Test
    void testCollectPlugins_vst3SynthIsCollected() throws Exception {
        final var xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <SynthFolder>
                    <Attributes name="Serum" speakerFormat="Stereo">
                        <Attributes x:id="deviceData" name="Serum" />
                        <Attributes x:id="ghostData">
                            <Attributes x:id="classInfo" name="Serum" subCategory="VST3" category="AudioSynth" />
                        </Attributes>
                    </Attributes>
                </SynthFolder>
                """;

        final var collector = new StudioOneSynthPluginCollector(parseXml(xml));
        final var plugins = collector.collectPlugins();

        assertEquals(1, plugins.size());
        assertEquals("Serum", plugins.get(0).getName());
        assertEquals(PluginFormat.VST3, plugins.get(0).getFormat());
    }

    @Test
    void testCollectPlugins_noSynthElements_returnsEmptyList() throws Exception {
        final var xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <SynthFolder>
                </SynthFolder>
                """;

        final var collector = new StudioOneSynthPluginCollector(parseXml(xml));
        final var plugins = collector.collectPlugins();

        assertTrue(plugins.isEmpty());
    }
}
