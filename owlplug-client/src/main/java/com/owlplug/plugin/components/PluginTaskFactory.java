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
import com.owlplug.plugin.tasks.PluginSyncTask;
import com.owlplug.plugin.tasks.discovery.PluginSyncTaskParameters;
import com.owlplug.project.components.ProjectTaskFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

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

    private final Collection<SimpleEventListener> syncPluginsListeners = new ArrayList<>();

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
     * Creates a {@link PluginSyncTask} and binds listeners to the success callback.
     *
     * @return taskExecutionContext
     */
    public TaskExecutionContext createPluginSyncTask() {

        return createPluginSyncTask(null);
    }

    /**
     * Creates a {@link PluginSyncTask} and binds listeners to the success callback.
     * The task synchronizes plugins in the given directory scope.
     *
     * @param directoryScope directory scope path
     * @return taskExecutionContext
     */
    public TaskExecutionContext createPluginSyncTask(String directoryScope) {

        PluginSyncTaskParameters parameters = new PluginSyncTaskParameters();
        parameters.setPlatform(applicationDefaults.getRuntimePlatform());
        parameters.setVst2Directory(prefs.get(VST_DIRECTORY_KEY, ""));
        parameters.setVst3Directory(prefs.get(VST3_DIRECTORY_KEY, ""));
        parameters.setAuDirectory(prefs.get(AU_DIRECTORY_KEY, ""));
        parameters.setLv2Directory(prefs.get(LV2_DIRECTORY_KEY, ""));
        parameters.setFindVst2(prefs.getBoolean(VST2_DISCOVERY_ENABLED_KEY, false));
        parameters.setFindVst3(prefs.getBoolean(VST3_DISCOVERY_ENABLED_KEY, false));
        parameters.setFindAu(prefs.getBoolean(AU_DISCOVERY_ENABLED_KEY, false));
        parameters.setFindLv2(prefs.getBoolean(LV2_DISCOVERY_ENABLED_KEY, false));
        parameters.setVst2ExtraDirectories(prefs.getList(VST2_EXTRA_DIRECTORY_KEY));
        parameters.setVst3ExtraDirectories(prefs.getList(VST3_EXTRA_DIRECTORY_KEY));
        parameters.setAuExtraDirectories(prefs.getList(AU_EXTRA_DIRECTORY_KEY));
        parameters.setLv2ExtraDirectories(prefs.getList(LV2_EXTRA_DIRECTORY_KEY));

        if (directoryScope != null) {
            parameters.setDirectoryScope(FileUtils.convertPath(directoryScope));
        }

        final var syncTask = getPluginSyncTask(directoryScope, parameters);
        return create(syncTask);
    }

    public TaskExecutionContext createFileStatSyncTask() {

        Set<String> directorySet = pluginService.getDirectoriesExplorationSet();
        FileSyncTask task = new FileSyncTask(fileStatRepository, directorySet.stream().toList());

        return create(task);
    }

    public TaskExecutionContext createFileStatSyncTask(String directoryScope) {
        FileSyncTask task = new FileSyncTask(fileStatRepository, directoryScope);
        return create(task);
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

    public void addSyncPluginsListener(final SimpleEventListener simpleEventListener) {
        syncPluginsListeners.add(simpleEventListener);
    }

    public void removeSyncPluginsListener(final SimpleEventListener simpleEventListener) {
        syncPluginsListeners.remove(simpleEventListener);
    }

    private PluginSyncTask getPluginSyncTask(final String directoryScope, final PluginSyncTaskParameters parameters) {
        PluginSyncTask syncTask = new PluginSyncTask(parameters, pluginRepository, pluginFootprintRepository,
                symlinkRepository, nativeHostService);

        syncTask.setOnSucceeded(syncEvent -> {
            notifyListeners(syncPluginsListeners);
            TaskExecutionContext lookupTask = projectTaskFactory.createLookupTask();

            if (prefs.getBoolean(ApplicationDefaults.SYNC_FILE_STAT_KEY, true)) {
                lookupTask.getAbstractTask().setOnScheduled(lookupEvent -> {
                    if (directoryScope != null) {
                        createFileStatSyncTask(directoryScope).scheduleNow();
                    } else {
                        createFileStatSyncTask().scheduleNow();
                    }
                });
            }
            lookupTask.scheduleNow();
        });
        return syncTask;
    }

}
