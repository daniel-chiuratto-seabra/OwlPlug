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

package com.owlplug.plugin.tasks.discovery;


import com.owlplug.plugin.model.Symlink;
import com.owlplug.plugin.tasks.discovery.fileformats.PluginFile;
import lombok.Getter;

import java.util.LinkedHashSet;
import java.util.Set;

import static com.owlplug.plugin.tasks.common.CommonOperations.collectPlugins;

/**
 * Collect plugins and symlinks based on task scan properties.
 */
public class ScopedScanEntityCollector {

    private final PluginScanTaskParameters parameters;

    @Getter
    private Set<PluginFile> pluginFiles;

    @Getter
    private Set<Symlink> symlinks;

    public ScopedScanEntityCollector(PluginScanTaskParameters parameters) {
        this.parameters = parameters;
    }

    public ScopedScanEntityCollector collect() {

        final var collectedPluginFiles = new LinkedHashSet<PluginFile>();
        final var pluginCollector = new PluginFileCollector(parameters.getPlatform());

        final var collectedSymlinks = new LinkedHashSet<Symlink>();
        final var symlinkCollector = new SymlinkCollector(true);

        collectPlugins(parameters, collectedPluginFiles, pluginCollector, collectedSymlinks, symlinkCollector);

        pluginFiles = collectedPluginFiles;
        symlinks = collectedSymlinks;

        return this;
    }

}
