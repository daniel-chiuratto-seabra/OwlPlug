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

package com.owlplug.plugin.model;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PluginDirectory implements IDirectory {

    protected String name;
    protected String displayName;
    @Column(length = 512)
    protected String path;
    protected boolean rootDirectory;
    protected List<Plugin> pluginList;

    public PluginDirectory() {
    }

    @Override
    public String toString() {
        if (displayName != null) {
            return displayName;
        }
        return name;

    }

    @Override
    public boolean isStale() {
        return false;
    }

}
