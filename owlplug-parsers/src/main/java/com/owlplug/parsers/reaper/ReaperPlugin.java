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

/**
 * Represents a Reaper plugin, containing information about the plugin's name, filename, and raw ID.
 */
public class ReaperPlugin {

  private String name;
  private String filename;
  private String rawId;

  /**
   * Gets the name of the Reaper plugin.
   *
   * @return The name of the plugin.
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name of the Reaper plugin.
   *
   * @param name The new name of the plugin.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets the filename of the Reaper plugin.
   *
   * @return The filename of the plugin.
   */
  public String getFilename() {
    return filename;
  }

  /**
   * Sets the filename of the Reaper plugin.
   *
   * @param filename The new filename of the plugin.
   */
  public void setFilename(String filename) {
    this.filename = filename;
  }

  /**
   * Gets the raw ID of the Reaper plugin.
   *
   * @return The raw ID of the plugin.
   */
  public String getRawId() {
    return rawId;
  }

  /**
   * Sets the raw ID of the Reaper plugin.
   *
   * @param rawId The new raw ID of the plugin.
   */
  public void setRawId(String rawId) {
    this.rawId = rawId;
  }
}
