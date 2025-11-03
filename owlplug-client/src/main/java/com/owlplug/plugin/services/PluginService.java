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

package com.owlplug.plugin.services;

import com.google.common.collect.Iterables;
import com.owlplug.core.components.ApplicationDefaults;
import com.owlplug.core.components.ApplicationPreferences;
import com.owlplug.core.services.BaseService;
import com.owlplug.core.utils.FileUtils;
import com.owlplug.core.utils.PluginUtils;
import com.owlplug.explore.model.RemotePackage;
import com.owlplug.explore.services.ExploreService;
import com.owlplug.plugin.components.PluginTaskFactory;
import com.owlplug.plugin.model.Plugin;
import com.owlplug.plugin.model.PluginFootprint;
import com.owlplug.plugin.model.PluginFormat;
import com.owlplug.plugin.model.PluginState;
import com.owlplug.plugin.repositories.PluginFootprintRepository;
import com.owlplug.plugin.repositories.PluginRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PluginService extends BaseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginService.class);

    protected final ExploreService exploreService;
    protected final PluginRepository pluginRepository;
    protected final PluginFootprintRepository pluginFootprintRepository;
    protected final PluginTaskFactory pluginTaskFactory;

    public PluginService(final ApplicationDefaults applicationDefaults, final ApplicationPreferences applicationPreferences,
                         @Lazy final ExploreService exploreService, final PluginRepository pluginRepository, final PluginFootprintRepository pluginFootprintRepository,
                         @Lazy final PluginTaskFactory pluginTaskFactory) {
        super(applicationDefaults, applicationPreferences);
        this.exploreService = exploreService;
        this.pluginRepository = pluginRepository;
        this.pluginFootprintRepository = pluginFootprintRepository;
        this.pluginTaskFactory = pluginTaskFactory;
    }

    public void syncPlugins() {
        pluginTaskFactory.createPluginSyncTask().schedule();
    }

    public void syncFiles() {
        pluginTaskFactory.createFileStatSyncTask().schedule();
    }

    public Iterable<Plugin> getAllPlugins() {
        return pluginRepository.findAll();
    }

    /**
     * Returns url to retrieve plugin screenshots. Url can be retrieved from
     * registered packages in remote sources.
     *
     * @param plugin the plugin
     * @return screenshot url
     */
    public String resolveImageUrl(final Plugin plugin) {

        String absoluteName = PluginUtils.absoluteName(plugin.getName());
        Iterable<RemotePackage> packages = exploreService.getPackagesByName(absoluteName);

        if (!Iterables.isEmpty(packages)) {
            return Iterables.get(packages, 0).getScreenshotUrl();
        }

        if (plugin.getDescriptiveName() != null) {
            packages = exploreService.getPackagesByName(plugin.getDescriptiveName());
            if (!Iterables.isEmpty(packages)) {
                return Iterables.get(packages, 0).getScreenshotUrl();
            }
        }

        return null;
    }

    /**
     * Resolves the screenshot URL for a plugin and saves it in the plugin's footprint.
     *
     * @param plugin the plugin to resolve and save the image URL for
     * @throws IllegalArgumentException if the plugin or its footprint is null
     */
    public void tryResolveAndSaveImageUrl(Plugin plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        if (plugin.getFootprint() == null) {
            throw new IllegalArgumentException("Plugin footprint cannot be null");
        }
        String url = this.resolveImageUrl(plugin);
        if (url != null) {
            plugin.getFootprint().setScreenshotUrl(url);
            pluginFootprintRepository.save(plugin.getFootprint());
        }
    }

    public void disablePlugin(Plugin plugin) {

        File originFile = new File(plugin.getPath());
        File destFile = new File(plugin.getPath() + ".disabled");

        if (originFile.renameTo(destFile)) {
            plugin.setDisabled(true);
            plugin.setPath(plugin.getPath() + ".disabled");
            pluginRepository.save(plugin);

        } else {
            LOGGER.error("Plugin can't be disabled: failed to rename file {}", plugin.getPath());
        }

    }

    public void enablePlugin(Plugin plugin) {

        File originFile = new File(plugin.getPath());

        String newPath = plugin.getPath();
        if (plugin.getPath().endsWith(".disabled")) {
            newPath = plugin.getPath().substring(0, plugin.getPath().length() - ".disabled".length());
        }
        File destFile = new File(newPath);

        if (originFile.renameTo(destFile)) {
            plugin.setDisabled(false);
            plugin.setPath(newPath);
            pluginRepository.save(plugin);
        } else {
            LOGGER.error("Plugin can't be enabled: failed to rename file {}", plugin.getPath());
        }

    }

    public PluginState getPluginState(Plugin plugin) {

        if (!plugin.isSyncComplete()) {
            return PluginState.UNSTABLE;
        }
        if (plugin.isDisabled()) {
            return PluginState.DISABLED;
        }
        if (plugin.isNativeCompatible()) {
            return PluginState.ACTIVE;
        }
        return PluginState.INSTALLED;
    }

    public Iterable<Plugin> find(String name, PluginFormat pluginFormat) {
        Specification<Plugin> spec = PluginRepository.nameContains(name)
                .and(PluginRepository.hasFormat(pluginFormat));


        return pluginRepository.findAll(spec);
    }

    public Iterable<Plugin> findByComponentName(String name, PluginFormat pluginFormat) {
        Specification<Plugin> spec = PluginRepository.hasComponentName(name)
                .and(PluginRepository.hasFormat(pluginFormat));

        return pluginRepository.findAll(spec);

    }

    /**
     * Get the primary plugin path defined in preferences based on the plugin format.
     *
     * @param format plugin format
     * @return the directory path
     */
    public String getPrimaryPluginPathByFormat(PluginFormat format) {

        if (PluginFormat.VST2.equals(format)) {
            return this.getApplicationPreferences().get(ApplicationDefaults.VST_DIRECTORY_KEY, "");
        } else if (PluginFormat.VST3.equals(format)) {
            return this.getApplicationPreferences().get(ApplicationDefaults.VST3_DIRECTORY_KEY, "");
        } else if (PluginFormat.AU.equals(format)) {
            return this.getApplicationPreferences().get(ApplicationDefaults.AU_DIRECTORY_KEY, "");
        } else if (PluginFormat.LV2.equals(format)) {
            return this.getApplicationPreferences().get(ApplicationDefaults.LV2_DIRECTORY_KEY, "");
        }

        return this.getApplicationPreferences().get(ApplicationDefaults.VST_DIRECTORY_KEY, "");
    }

    /**
     * Returns a full list of explored directories during sync
     * based on user preferences.
     *
     * @return the set of explored directories
     */
    public Set<String> getDirectoriesExplorationSet() {
        Set<String> directorySet = new HashSet<>();

        ApplicationPreferences prefs = this.getApplicationPreferences();
        if (prefs.getBoolean(ApplicationDefaults.VST2_DISCOVERY_ENABLED_KEY, false)
                && !prefs.get(ApplicationDefaults.VST_DIRECTORY_KEY, "").isBlank()) {
            String path = prefs.get(ApplicationDefaults.VST_DIRECTORY_KEY, "");
            directorySet.add(FileUtils.convertPath(path));
            directorySet.addAll(prefs.getList(ApplicationDefaults.VST2_EXTRA_DIRECTORY_KEY));
        }

        if (prefs.getBoolean(ApplicationDefaults.VST3_DISCOVERY_ENABLED_KEY, false)
                && !prefs.get(ApplicationDefaults.VST3_DIRECTORY_KEY, "").isBlank()) {
            String path = prefs.get(ApplicationDefaults.VST3_DIRECTORY_KEY, "");
            directorySet.add(FileUtils.convertPath(path));
            directorySet.addAll(prefs.getList(ApplicationDefaults.VST3_EXTRA_DIRECTORY_KEY));
        }

        if (prefs.getBoolean(ApplicationDefaults.AU_DISCOVERY_ENABLED_KEY, false)
                && !prefs.get(ApplicationDefaults.AU_DIRECTORY_KEY, "").isBlank()) {
            String path = prefs.get(ApplicationDefaults.AU_DIRECTORY_KEY, "");
            directorySet.add(FileUtils.convertPath(path));
            directorySet.addAll(prefs.getList(ApplicationDefaults.AU_EXTRA_DIRECTORY_KEY));
        }

        if (prefs.getBoolean(ApplicationDefaults.LV2_DISCOVERY_ENABLED_KEY, false)
                && !prefs.get(ApplicationDefaults.LV2_DIRECTORY_KEY, "").isBlank()) {
            String path = prefs.get(ApplicationDefaults.LV2_DIRECTORY_KEY, "");
            directorySet.add(FileUtils.convertPath(path));
            directorySet.addAll(prefs.getList(ApplicationDefaults.LV2_EXTRA_DIRECTORY_KEY));
        }
        return directorySet;
    }

    /**
     * Check if format discovery is enabled.
     *
     * @param format pluginFormat
     * @return true if discovery is enabled.
     */
    public boolean isFormatEnabled(PluginFormat format) {

        if (PluginFormat.VST2.equals(format)) {
            return this.getApplicationPreferences().getBoolean(ApplicationDefaults.VST2_DISCOVERY_ENABLED_KEY, false);
        } else if (PluginFormat.VST3.equals(format)) {
            return this.getApplicationPreferences().getBoolean(ApplicationDefaults.VST3_DISCOVERY_ENABLED_KEY, false);
        } else if (PluginFormat.AU.equals(format)) {
            return this.getApplicationPreferences().getBoolean(ApplicationDefaults.AU_DISCOVERY_ENABLED_KEY, false);
        } else if (PluginFormat.LV2.equals(format)) {
            return this.getApplicationPreferences().getBoolean(ApplicationDefaults.LV2_DISCOVERY_ENABLED_KEY, false);
        }

        return false;
    }

    /**
     * Removes a plugin reference from database.
     *
     * @param plugin - plugin to remove
     */
    public void delete(Plugin plugin) {
        pluginRepository.delete(plugin);
    }


    public PluginFootprint save(PluginFootprint pluginFootprint) {
        return pluginFootprintRepository.save(pluginFootprint);
    }

    public List<Plugin> getSyncIncompletePlugins() {
        return pluginRepository.findBySyncComplete(false);
    }
}
