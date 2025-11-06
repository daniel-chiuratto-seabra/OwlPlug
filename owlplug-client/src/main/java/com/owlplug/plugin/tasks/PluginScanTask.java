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
import com.owlplug.host.NativePlugin;
import com.owlplug.plugin.model.Plugin;
import com.owlplug.plugin.model.PluginComponent;
import com.owlplug.plugin.model.PluginFootprint;
import com.owlplug.plugin.model.PluginType;
import com.owlplug.plugin.model.Symlink;
import com.owlplug.plugin.repositories.PluginFootprintRepository;
import com.owlplug.plugin.repositories.PluginRepository;
import com.owlplug.plugin.repositories.SymlinkRepository;
import com.owlplug.plugin.services.NativeHostService;
import com.owlplug.plugin.tasks.discovery.DifferentialScanEntityCollector;
import com.owlplug.plugin.tasks.discovery.PluginScanTaskParameters;
import com.owlplug.plugin.tasks.discovery.ScopedScanEntityCollector;
import com.owlplug.plugin.tasks.discovery.fileformats.PluginFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * OwlPlug task to collect plugin metadata from directories
 * By default, the task collects and sync all plugins from user folders. A directory scope
 * can be defined to reduce the number of scanned files.
 *
 */
public class PluginScanTask extends AbstractTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginScanTask.class);

    private final PluginRepository pluginRepository;
    private final SymlinkRepository symlinkRepository;
    private final PluginFootprintRepository pluginFootprintRepository;
    private final NativeHostService nativeHostService;
    private final PluginScanTaskParameters pluginScanTaskParameters;

    /**
     * Creates a new PluginSyncTask.
     *
     * @param pluginScanTaskParameters  Task Parameters
     * @param pluginRepository          pluginRepository
     * @param pluginFootprintRepository pluginFootprintRepository
     * @param symlinkRepository         symlinkRepository
     * @param nativeHostService         nativeHostService
     */
    public PluginScanTask(final PluginScanTaskParameters pluginScanTaskParameters,
                          final PluginRepository pluginRepository,
                          final PluginFootprintRepository pluginFootprintRepository,
                          final SymlinkRepository symlinkRepository,
                          final NativeHostService nativeHostService) {
        this.pluginScanTaskParameters = pluginScanTaskParameters;
        this.pluginRepository = pluginRepository;
        this.pluginFootprintRepository = pluginFootprintRepository;
        this.symlinkRepository = symlinkRepository;
        this.nativeHostService = nativeHostService;

        setName("Sync Plugins");
        setMaxProgress(100);
    }

    @Override
    protected TaskResult start() throws Exception {
        try {
            collect();
            return success();
        } catch (final Exception exception) {
            updateMessage("Plugins scan failed: %s".formatted(exception.getMessage()));
            LOGGER.error("Plugins scan failed", exception);
            throw new TaskException("Plugins scan failed", exception);
        }
    }

    protected void collect() throws Exception {

        LOGGER.info("Plugin Scan task started");
        updateMessage("Collecting plugins...");
        commitProgress(20);

        // Clear data from the previous scan if not incremental
        if (!pluginScanTaskParameters.isDifferential()) {
            if (pluginScanTaskParameters.getDirectoryScope() != null) {
                // Delete previous plugins scanned in the directory scope

                // Ensure a trailing slash to make sure only scoped paths are deleted
                // Prevents deleting /r/u/vst3 when /r/u/vst is scoped
                String scopedPath = pluginScanTaskParameters.getDirectoryScope();
                if (!scopedPath.endsWith("/")) {
                    scopedPath += "/";
                }
                pluginRepository.deleteByPathContainingIgnoreCase(scopedPath);
                symlinkRepository.deleteByPathContainingIgnoreCase(scopedPath);
            } else {
                // Delete all previous plugins by default (in case of a complete Scan task)
                pluginRepository.deleteAll();
                symlinkRepository.deleteAll();
            }
            // Flushing context to the database as next queries will recreate entities
            pluginRepository.flush();
            symlinkRepository.flush();
        }

        List<PluginFile> pluginFiles = new ArrayList<>();
        List<Symlink> symlinks = new ArrayList<>();

        if (pluginScanTaskParameters.isDifferential()) {
            LOGGER.info("Running differential plugin and symlink collection");
            List<Plugin> p = pluginRepository.findAll();
            List<Symlink> s = symlinkRepository.findAll();
            DifferentialScanEntityCollector collector = new DifferentialScanEntityCollector(pluginScanTaskParameters);
            collector.collect().differentialPlugins(p)
                    .differentialSymlinks(s);

            for (String deleted : collector.getPluginDifferential().getRemoved()) {
                pluginRepository.deleteByPathContainingIgnoreCase(deleted);
            }
            for (String deleted : collector.getSymlinkDifferential().getRemoved()) {
                symlinkRepository.deleteByPathContainingIgnoreCase(deleted);
            }

            pluginFiles.addAll(collector.getPluginDifferential().getAdded());
            symlinks.addAll(collector.getSymlinkDifferential().getAdded());
        } else {
            ScopedScanEntityCollector collector = new ScopedScanEntityCollector(pluginScanTaskParameters);
            collector.collect();
            pluginFiles.addAll(collector.getPluginFiles());
            symlinks.addAll(collector.getSymlinks());
        }

        LOGGER.info("{} plugins collected for analysis", pluginFiles.size());

        //Save all discovered symlinks
        symlinkRepository.saveAll(symlinks);

        for (PluginFile pluginFile : pluginFiles) {
            Plugin plugin = pluginFile.toPlugin();
            PluginFootprint pluginFootprint = pluginFootprintRepository.findByPath(plugin.getPath());

            if (pluginFootprint == null) {
                pluginFootprint = new PluginFootprint(plugin.getPath());
                pluginFootprintRepository.saveAndFlush(pluginFootprint);
            }
            plugin.setFootprint(pluginFootprint);
            pluginRepository.save(plugin);

            if (nativeHostService.isNativeHostEnabled() && nativeHostService.getCurrentPluginLoader().isAvailable()
                    && pluginFootprint.isNativeDiscoveryEnabled() && !plugin.isDisabled()) {

                LOGGER.debug("Load plugin using native discovery: {}", plugin.getPath());
                updateMessage("Exploring plugin " + plugin.getName());
                List<NativePlugin> nativePlugins = nativeHostService.loadPlugin(plugin.getPath());

                if (nativePlugins != null && !nativePlugins.isEmpty()) {
                    LOGGER.debug("Found {} components (nativePlugin) for plugin {}", nativePlugins.size(), plugin.getName());

                    plugin.setNativeCompatible(true);

                    for (NativePlugin nativePlugin : nativePlugins) {
                        PluginComponent component = createComponentFromNative(nativePlugin);
                        component.setPlugin(plugin);
                        plugin.getComponents().add(component);
                        LOGGER.debug("Created component {} for plugin {}", component.getName(), plugin.getName());
                    }

                    // Hardcode plugin properties from the first component (nativePlugin) retrieved.
                    mapPluginPropertiesFromNative(plugin, nativePlugins.getFirst());
                }
            }

            plugin.setScanComplete(true);
            pluginRepository.save(plugin);

            commitProgress(80.0 / pluginFiles.size());
        }

        updateProgress(1, 1);
        updateMessage("Plugins scanned");
        LOGGER.info("Plugin Scan task complete");
    }

    private PluginComponent createComponentFromNative(final NativePlugin nativePlugin) {
        final var pluginComponent = new PluginComponent();
        pluginComponent.setName(nativePlugin.getName());
        pluginComponent.setDescriptiveName(nativePlugin.getDescriptiveName());
        pluginComponent.setVersion(nativePlugin.getVersion());
        pluginComponent.setCategory(nativePlugin.getCategory());
        pluginComponent.setManufacturerName(nativePlugin.getManufacturerName());
        pluginComponent.setIdentifier(nativePlugin.getFileOrIdentifier());
        pluginComponent.setUid(String.valueOf(nativePlugin.getUid()));

        if (nativePlugin.isInstrument()) {
            pluginComponent.setType(PluginType.INSTRUMENT);
        } else {
            pluginComponent.setType(PluginType.EFFECT);
        }

        return pluginComponent;
    }

    private void mapPluginPropertiesFromNative(final Plugin plugin, final NativePlugin nativePlugin) {
        plugin.setDescriptiveName(nativePlugin.getDescriptiveName());
        plugin.setVersion(nativePlugin.getVersion());
        plugin.setCategory(nativePlugin.getCategory());
        plugin.setManufacturerName(nativePlugin.getManufacturerName());
        plugin.setIdentifier(nativePlugin.getFileOrIdentifier());
        plugin.setUid(String.valueOf(nativePlugin.getUid()));

        if (nativePlugin.isInstrument()) {
            plugin.setType(PluginType.INSTRUMENT);
        } else {
            plugin.setType(PluginType.EFFECT);
        }
    }
}
