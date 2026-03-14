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
import com.owlplug.plugin.model.Symlink;

import java.io.File;

public class SymlinkRemoveTask extends AbstractTask {

    protected Symlink symlink;

    public SymlinkRemoveTask(final Symlink symlink) {
        this.symlink = symlink;
        setName("Remove symlink");
    }

    @Override
    protected TaskResult start() throws Exception {

        updateProgress(-1, 1);
        updateMessage("Deleting directory %s ...".formatted(symlink.getName()));

        final var symlinkFile = new File(symlink.getPath());

        if (!symlinkFile.delete()) {
            updateProgress(1, 1);
            updateMessage("Error deleting symlink: %s".formatted(symlink.getName()));
            throw new TaskException("Error deleting symlink: %s".formatted(symlink.getName()));
        }

        updateProgress(1, 1);
        updateMessage("Symlink successfully deleted");

        return completed();
    }
}
