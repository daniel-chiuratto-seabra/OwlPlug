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

package com.owlplug.plugin.components;

import com.owlplug.core.components.ApplicationDefaults;
import com.owlplug.core.components.ApplicationPreferences;
import com.owlplug.core.components.BaseTaskFactory;
import com.owlplug.core.components.TaskRunner;
import com.owlplug.core.tasks.SimpleEventListener;
import com.owlplug.core.tasks.TaskExecutionContext;
import com.owlplug.core.utils.FileUtils;
import com.owlplug.plugin.model.Plugin;
import com.owlplug.plugin.repositories.FileStatRepository;
import com.owlplug.plugin.repositories.PluginFootprintRepository;
import com.owlplug.plugin.repositories.PluginRepository;
import com.owlplug.plugin.repositories.SymlinkRepository;
import com.owlplug.plugin.services.NativeHostService;
import com.owlplug.plugin.services.PluginService;
import com.owlplug.plugin.tasks.FileSyncTask;
import com.owlplug.plugin.tasks.PluginRemoveTask;
import com.owlplug.plugin.tasks.PluginScanTask;
import com.owlplug.plugin.tasks.discovery.PluginScanTaskParameters;
import com.owlplug.project.components.ProjectTaskFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

import static com.owlplug.core.components.ApplicationDefaults.AU_DIRECTORY_KEY;
import static com.owlplug.core.components.ApplicationDefaults.AU_DISCOVERY_ENABLED_KEY;
import static com.owlplug.core.components.ApplicationDefaults.AU_EXTRA_DIRECTORY_KEY;
import static com.owlplug.core.components.ApplicationDefaults.LV2_DIRECTORY_KEY;
import static com.owlplug.core.components.ApplicationDefaults.LV2_DISCOVERY_ENABLED_KEY;
import static com.owlplug.core.components.ApplicationDefaults.LV2_EXTRA_DIRECTORY_KEY;
import static com.owlplug.core.components.ApplicationDefaults.VST2_DISCOVERY_ENABLED_KEY;
import static com.owlplug.core.components.ApplicationDefaults.VST2_EXTRA_DIRECTORY_KEY;
import static com.owlplug.core.components.ApplicationDefaults.VST3_DIRECTORY_KEY;
import static com.owlplug.core.components.ApplicationDefaults.VST3_DISCOVERY_ENABLED_KEY;
import static com.owlplug.core.components.ApplicationDefaults.VST3_EXTRA_DIRECTORY_KEY;
import static com.owlplug.core.components.ApplicationDefaults.VST_DIRECTORY_KEY;

@Service
public class PluginTaskFactory extends BaseTaskFactory {

    private final ApplicationDefaults applicationDefaults;
    private final ApplicationPreferences prefs;
    private final PluginRepository pluginRepository;
    private final PluginService pluginService;
    private final PluginFootprintRepository pluginFootprintRepository;
    private final SymlinkRepository symlinkRepository;
    private final NativeHostService nativeHostService;
    private final ProjectTaskFactory projectTaskFactory;
    private final FileStatRepository fileStatRepository;

    private final Collection<SimpleEventListener> scanPluginsListeners = new ArrayList<>();

    public PluginTaskFactory(final TaskRunner taskRunner, final ApplicationDefaults applicationDefaults, final ApplicationPreferences prefs,
                             final PluginRepository pluginRepository, final PluginService pluginService, final PluginFootprintRepository pluginFootprintRepository,
                             final SymlinkRepository symlinkRepository, final NativeHostService nativeHostService, final ProjectTaskFactory projectTaskFactory,
                             final FileStatRepository fileStatRepository) {
        super(taskRunner);
        this.applicationDefaults = applicationDefaults;
        this.prefs = prefs;
        this.pluginRepository = pluginRepository;
        this.pluginService = pluginService;
        this.pluginFootprintRepository = pluginFootprintRepository;
        this.symlinkRepository = symlinkRepository;
        this.nativeHostService = nativeHostService;
        this.projectTaskFactory = projectTaskFactory;
        this.fileStatRepository = fileStatRepository;
    }

    /**
     * Creates a {@link PluginScanTask} and binds listeners to the success callback.
     *
     * @return taskExecutionContext
     */
    public TaskExecutionContext createPluginScanTask() {
        return createPluginScanTask(null);
    }

    public TaskExecutionContext createPluginScanTask(final boolean differential) {
        return createPluginScanTask(null, differential);
    }

    public TaskExecutionContext createPluginScanTask(final String directoryScope) {
        return createPluginScanTask(directoryScope, false);
    }

    /**
     * Creates a {@link PluginScanTask} and binds listeners to the success callback.
     * The task synchronizes plugins in the given directory scope.
     *
     * @param directoryScope directory scope path
     * @return taskExecutionContext
     */
    public TaskExecutionContext createPluginScanTask(final String directoryScope, final boolean differential) {
        final var pluginSyncTaskParameters = new PluginScanTaskParameters();
        pluginSyncTaskParameters.setPlatform(applicationDefaults.getRuntimePlatform());
        pluginSyncTaskParameters.setVst2Directory(prefs.get(VST_DIRECTORY_KEY, ""));
        pluginSyncTaskParameters.setVst3Directory(prefs.get(VST3_DIRECTORY_KEY, ""));
        pluginSyncTaskParameters.setAuDirectory(prefs.get(AU_DIRECTORY_KEY, ""));
        pluginSyncTaskParameters.setLv2Directory(prefs.get(LV2_DIRECTORY_KEY, ""));
        pluginSyncTaskParameters.setFindVst2(prefs.getBoolean(VST2_DISCOVERY_ENABLED_KEY, false));
        pluginSyncTaskParameters.setFindVst3(prefs.getBoolean(VST3_DISCOVERY_ENABLED_KEY, false));
        pluginSyncTaskParameters.setFindAu(prefs.getBoolean(AU_DISCOVERY_ENABLED_KEY, false));
        pluginSyncTaskParameters.setFindLv2(prefs.getBoolean(LV2_DISCOVERY_ENABLED_KEY, false));
        pluginSyncTaskParameters.setVst2ExtraDirectories(prefs.getList(VST2_EXTRA_DIRECTORY_KEY));
        pluginSyncTaskParameters.setVst3ExtraDirectories(prefs.getList(VST3_EXTRA_DIRECTORY_KEY));
        pluginSyncTaskParameters.setAuExtraDirectories(prefs.getList(AU_EXTRA_DIRECTORY_KEY));
        pluginSyncTaskParameters.setLv2ExtraDirectories(prefs.getList(LV2_EXTRA_DIRECTORY_KEY));
        pluginSyncTaskParameters.setDifferential(differential);

        if (directoryScope != null) {
            pluginSyncTaskParameters.setDirectoryScope(FileUtils.convertPath(directoryScope));
        }

        return create(getPluginSyncTask(directoryScope, pluginSyncTaskParameters));
    }

    public TaskExecutionContext createFileStatSyncTask() {
        final var directorySet = pluginService.getDirectoriesExplorationSet();
        final var fileSyncTask = new FileSyncTask(fileStatRepository, directorySet.stream().toList());
        return create(fileSyncTask);
    }

    public TaskExecutionContext createFileStatSyncTask(String directoryScope) {
        return create(new FileSyncTask(fileStatRepository, directoryScope));
    }

    /**
     * Creates a {@link PluginRemoveTask}.
     *
     * @param plugin - plugin to remove
     * @return task execution context
     */
    public TaskExecutionContext createPluginRemoveTask(Plugin plugin) {
        PluginRemoveTask task = new PluginRemoveTask(plugin, pluginRepository);

        return create(task);
    }

    public void addScanPluginsListener(final SimpleEventListener simpleEventListener) {
        scanPluginsListeners.add(simpleEventListener);
    }

    public void removeSyncPluginsListener(final SimpleEventListener simpleEventListener) {
        scanPluginsListeners.remove(simpleEventListener);
    }

    private PluginScanTask getPluginSyncTask(final String directoryScope, final PluginScanTaskParameters parameters) {
        final var pluginScanTask = new PluginScanTask(parameters, pluginRepository, pluginFootprintRepository,
                symlinkRepository, nativeHostService);

        pluginScanTask.setOnSucceeded(syncEvent -> {
            notifyListeners(scanPluginsListeners);
            final var taskExecutionContext = projectTaskFactory.createLookupTask();

            if (prefs.getBoolean(ApplicationDefaults.SYNC_FILE_STAT_KEY, true) && !parameters.isDifferential()) {
                taskExecutionContext.getAbstractTask().setOnScheduled(lookupEvent -> {
                    if (directoryScope != null) {
                        createFileStatSyncTask(directoryScope).scheduleNow();
                    } else {
                        createFileStatSyncTask().scheduleNow();
                    }
                });
            }
            taskExecutionContext.scheduleNow();
        });
        return pluginScanTask;
    }
}
