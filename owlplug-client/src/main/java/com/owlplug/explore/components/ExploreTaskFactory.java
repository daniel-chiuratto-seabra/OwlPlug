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

package com.owlplug.explore.components;

import com.owlplug.core.components.ApplicationDefaults;
import com.owlplug.core.components.BaseTaskFactory;
import com.owlplug.core.components.TaskRunner;
import com.owlplug.core.tasks.SimpleEventListener;
import com.owlplug.core.tasks.TaskExecutionContext;
import com.owlplug.core.utils.FileUtils;
import com.owlplug.explore.model.PackageBundle;
import com.owlplug.explore.repositories.RemotePackageRepository;
import com.owlplug.explore.repositories.RemoteSourceRepository;
import com.owlplug.explore.tasks.BundleInstallTask;
import com.owlplug.explore.tasks.SourceSyncTask;
import com.owlplug.plugin.components.PluginTaskFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

@Component
public class ExploreTaskFactory extends BaseTaskFactory {

    private final ApplicationDefaults applicationDefaults;
    private final PluginTaskFactory pluginTaskFactory;
    private final RemoteSourceRepository remoteSourceRepository;
    private final RemotePackageRepository remotePackageRepository;

    private final Collection<SimpleEventListener> syncSourcesListeners = new ArrayList<>();

    public ExploreTaskFactory(final TaskRunner taskRunner, final ApplicationDefaults applicationDefaults,
                              final PluginTaskFactory pluginTaskFactory, final RemoteSourceRepository remoteSourceRepository,
                              final RemotePackageRepository remotePackageRepository) {
        super(taskRunner);
        this.applicationDefaults = applicationDefaults;
        this.pluginTaskFactory = pluginTaskFactory;
        this.remoteSourceRepository = remoteSourceRepository;
        this.remotePackageRepository = remotePackageRepository;
    }

    /**
     * Creates a {@link SourceSyncTask} and binds listeners to the success callback.
     *
     * @return {@link TaskExecutionContext} instance
     */
    public TaskExecutionContext createSourceSyncTask() {
        final var task = new SourceSyncTask(remoteSourceRepository, remotePackageRepository);
        task.setOnSucceeded(e -> notifyListeners(syncSourcesListeners));
        return create(task);
    }

    /**
     * Creates a task to download and installs a package in a directory.
     *
     * @param bundle          - package bundle to retrieve
     * @param targetDirectory - target install directory
     * @return task execution context
     */
    public TaskExecutionContext createBundleInstallTask(PackageBundle bundle, File targetDirectory) {
        final var path = FileUtils.convertPath(targetDirectory.getAbsolutePath());
        return create(new BundleInstallTask(bundle, targetDirectory, applicationDefaults))
                .setOnSucceeded(e -> pluginTaskFactory.createPluginSyncTask(path).scheduleNow());
    }

    public void addSyncSourcesListener(SimpleEventListener eventListener) {
        syncSourcesListeners.add(eventListener);
    }

}
