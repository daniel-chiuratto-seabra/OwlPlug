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

package com.owlplug.project.tasks.discovery;

import com.owlplug.plugin.model.PluginFormat;
import com.owlplug.project.model.DawApplication;
import com.owlplug.project.model.DawProject;
import com.owlplug.project.tasks.discovery.studioone.StudioOneProjectExplorer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StudioOneProjectExplorerTest {

    private StudioOneProjectExplorer explorer;

    @BeforeEach
    void setUp() {
        explorer = new StudioOneProjectExplorer();
    }

    @Test
    public void studioOne7schema8VstValidProject() throws ProjectExplorerException {
        File file = new File(requireNonNull(getClass().getClassLoader()
                .getResource("projects/studioone/studioone7schema8.song")).getFile());

        DawProject project = explorer.explore(file);
        assertEquals("OwlPlug", project.getName());
        assertEquals(DawApplication.STUDIO_ONE, project.getApplication());
        assertEquals("Studio One/7.2.3.108761", project.getAppFullName());
        assertEquals("8", project.getFormatVersion());
        // The project has Wobbleizer inserted on two separate channels (hence 2 entries),
        // iZotope Ozone 5 on the main bus, and Sylenth1 as a synth instrument.
        assertEquals(4, project.getPlugins().size());

        assertThat(project.getPlugins(), containsInAnyOrder(
                allOf(
                        hasProperty("name", is("Wobbleizer")),
                        hasProperty("format", is(PluginFormat.VST2))
                ),
                allOf(
                        hasProperty("name", is("Wobbleizer")),
                        hasProperty("format", is(PluginFormat.VST2))
                ),
                allOf(
                        hasProperty("name", is("iZotope Ozone 5")),
                        hasProperty("format", is(PluginFormat.VST2))
                ),
                allOf(
                        hasProperty("name", is("Sylenth1")),
                        hasProperty("format", is(PluginFormat.VST2))
                )
        ));
    }

    @Test
    void canExploreFile_returnsTrue_forValidSongFile() {
        File file = new File(requireNonNull(getClass().getClassLoader()
                .getResource("projects/studioone/studioone7schema8.song")).getFile());
        assertTrue(explorer.canExploreFile(file));
    }

    @Test
    void canExploreFile_returnsFalse_forNonSongFile() {
        File file = mock(File.class);
        when(file.isFile()).thenReturn(true);
        when(file.getAbsolutePath()).thenReturn("/Songs/MyProject.als");
        assertFalse(explorer.canExploreFile(file));
    }

    @Test
    void canExploreFile_returnsFalse_forDirectory() {
        File file = mock(File.class);
        when(file.isFile()).thenReturn(false);
        assertFalse(explorer.canExploreFile(file));
    }

    @Test
    void canExploreFile_returnsFalse_forAutosaveFile() {
        File file = mock(File.class);
        when(file.isFile()).thenReturn(true);
        when(file.getAbsolutePath()).thenReturn("/Songs/MySong (Autosaved).song");
        when(file.getName()).thenReturn("MySong (Autosaved).song");
        assertFalse(explorer.canExploreFile(file));
    }

    @Test
    void canExploreFile_returnsFalse_forFileInHistoryDirectory() {
        File file = mock(File.class);
        when(file.isFile()).thenReturn(true);
        when(file.getAbsolutePath()).thenReturn("/Songs/History/OldVersion.song");
        when(file.getName()).thenReturn("OldVersion.song");
        assertFalse(explorer.canExploreFile(file));
    }

    @Test
    void explore_returnsNull_forNonSongFile() throws ProjectExplorerException {
        File file = mock(File.class);
        when(file.isFile()).thenReturn(true);
        when(file.getAbsolutePath()).thenReturn("/Songs/MyProject.als");
        assertNull(explorer.explore(file));
    }
}
