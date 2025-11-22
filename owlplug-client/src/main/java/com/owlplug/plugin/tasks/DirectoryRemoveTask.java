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

package com.owlplug.plugin.tasks;

import com.owlplug.core.tasks.AbstractTask;
import com.owlplug.core.tasks.TaskResult;
import com.owlplug.plugin.model.PluginDirectory;

import java.io.File;

import static org.apache.commons.io.FileUtils.deleteDirectory;

public class DirectoryRemoveTask extends AbstractTask {

    protected PluginDirectory pluginDirectory;

    public DirectoryRemoveTask(final PluginDirectory pluginDirectory) {
        this.pluginDirectory = pluginDirectory;
        setName("Remove directory");
    }

    @Override
    protected TaskResult start() throws Exception {
        updateProgress(0, 1);
        updateMessage("Deleting directory %s ...".formatted(pluginDirectory.getName()));

        final var directoryFile = new File(pluginDirectory.getPath());

        deleteDirectory(directoryFile);

        updateProgress(1, 1);
        updateMessage("Directory successfully deleted");

        return completed();
    }
}
