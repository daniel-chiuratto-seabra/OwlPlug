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
import com.owlplug.core.tasks.TaskException;
import com.owlplug.core.tasks.TaskResult;
import com.owlplug.plugin.model.Plugin;
import com.owlplug.plugin.repositories.PluginRepository;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class PluginRemoveTask extends AbstractTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginRemoveTask.class);

    protected Plugin plugin;
    protected PluginRepository pluginRepository;

    public PluginRemoveTask(final Plugin plugin, final PluginRepository pluginRepository) {
        this.plugin = plugin;
        this.pluginRepository = pluginRepository;
        setName("Remove Plugin - " + plugin.getName());
    }

    @Override
    protected TaskResult start() throws Exception {
        updateProgress(0, 1);
        updateMessage("Deleting plugin %s ...".formatted(plugin.getName()));

        File pluginFile = new File(plugin.getPath());
        boolean fileDeleteSuccess = false;
        if (pluginFile.exists() && pluginFile.isDirectory()) {
            try {
                FileUtils.deleteDirectory(pluginFile);
                fileDeleteSuccess = true;
            } catch (IOException e) {
                LOGGER.error("Plugin File can't be removed: {}", pluginFile.getPath(), e);
            }
        } else {
            fileDeleteSuccess = pluginFile.delete();
        }


        if (fileDeleteSuccess) {
            pluginRepository.delete(plugin);
            updateProgress(1, 1);
            updateMessage("Plugin successfully deleted");

            return completed();
        } else {
            updateMessage("Error during plugin removal");
            throw new TaskException("Error during plugin removal");
        }
    }
}
