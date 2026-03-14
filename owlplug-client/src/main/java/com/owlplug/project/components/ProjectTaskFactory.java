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


package com.owlplug.project.components;

import com.owlplug.core.components.ApplicationPreferences;
import com.owlplug.core.components.BaseTaskFactory;
import com.owlplug.core.components.TaskRunner;
import com.owlplug.core.tasks.SimpleEventListener;
import com.owlplug.core.tasks.TaskExecutionContext;
import com.owlplug.project.repositories.DawPluginRepository;
import com.owlplug.project.repositories.DawProjectRepository;
import com.owlplug.project.services.PluginLookupService;
import com.owlplug.project.tasks.PluginLookupTask;
import com.owlplug.project.tasks.ProjectSyncTask;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;

import static com.owlplug.core.components.ApplicationDefaults.PROJECT_DIRECTORY_KEY;

@Component
public class ProjectTaskFactory extends BaseTaskFactory {

    private final ApplicationPreferences applicationPreferences;
    private final PluginLookupService pluginLookupService;
    private final DawProjectRepository dawProjectRepository;
    private final DawPluginRepository dawPluginRepository;

    private final Collection<SimpleEventListener> syncProjectsListeners = new ArrayList<>();

    public ProjectTaskFactory(final TaskRunner taskRunner, final ApplicationPreferences applicationPreferences,
                              final PluginLookupService pluginLookupService, final DawProjectRepository dawProjectRepository,
                              final DawPluginRepository dawPluginRepository) {
        super(taskRunner);
        this.applicationPreferences = applicationPreferences;
        this.pluginLookupService = pluginLookupService;
        this.dawProjectRepository = dawProjectRepository;
        this.dawPluginRepository = dawPluginRepository;
    }

    public TaskExecutionContext createSyncTask() {
        final var directories = applicationPreferences.getList(PROJECT_DIRECTORY_KEY);
        final var projectSyncTask = new ProjectSyncTask(dawProjectRepository, directories);
        projectSyncTask.setOnSucceeded(e -> {
            createLookupTask().scheduleNow();
            notifyListeners(syncProjectsListeners);
        });
        return create(projectSyncTask);
    }

    public TaskExecutionContext createLookupTask() {
        final var pluginLookupTask = new PluginLookupTask(dawPluginRepository, pluginLookupService);
        pluginLookupTask.setOnSucceeded(e -> notifyListeners(syncProjectsListeners));
        return create(pluginLookupTask);
    }

    public void addSyncProjectsListener(final SimpleEventListener simpleEventListener) {
        syncProjectsListeners.add(simpleEventListener);
    }

    public void removeSyncProjectsListener(final SimpleEventListener simpleEventListener) {
        syncProjectsListeners.remove(simpleEventListener);
    }
}
