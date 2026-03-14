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

package com.owlplug.parsers.reaper;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the {@link ReaperPlugin} class.
 */
public class ReaperPluginTest {

    /**
     * Tests the getter and setter for the name property.
     */
    @Test
    public void testGetNameAndSetName() {
        // Create a new ReaperPlugin instance.
        ReaperPlugin plugin = new ReaperPlugin();
        String expectedName = "Test Plugin";

        // Set the name property.
        plugin.setName(expectedName);

        // Verify that the getName method returns the correct value.
        assertEquals(expectedName, plugin.getName());
    }

    /**
     * Tests the getter and setter for the filename property.
     */
    @Test
    public void testGetFilenameAndSetFilename() {
        // Create a new ReaperPlugin instance.
        ReaperPlugin plugin = new ReaperPlugin();
        String expectedFilename = "test.dll";

        // Set the filename property.
        plugin.setFilename(expectedFilename);

        // Verify that the getFilename method returns the correct value.
        assertEquals(expectedFilename, plugin.getFilename());
    }

    /**
     * Tests the getter and setter for the rawId property.
     */
    @Test
    public void testGetRawIdAndSetRawId() {
        // Create a new ReaperPlugin instance.
        ReaperPlugin plugin = new ReaperPlugin();
        String expectedRawId = "12345";

        // Set the rawId property.
        plugin.setRawId(expectedRawId);

        // Verify that the getRawId method returns the correct value.
        assertEquals(expectedRawId, plugin.getRawId());
    }
}
