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

package com.owlplug.core.services;

import com.owlplug.auth.repositories.GoogleCredentialRepository;
import com.owlplug.auth.repositories.UserAccountRepository;
import com.owlplug.core.components.ApplicationDefaults;
import com.owlplug.core.components.ApplicationPreferences;
import com.owlplug.core.components.ImageCache;
import com.owlplug.core.model.OperatingSystem;
import com.owlplug.explore.repositories.RemotePackageRepository;
import com.owlplug.explore.repositories.RemoteSourceRepository;
import com.owlplug.plugin.model.PluginFormat;
import com.owlplug.plugin.repositories.FileStatRepository;
import com.owlplug.plugin.repositories.PluginRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.prefs.BackingStoreException;

@Service
public class OptionsService extends BaseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OptionsService.class);

    private final PluginRepository pluginRepository;
    private final UserAccountRepository userAccountRepository;
    private final GoogleCredentialRepository googleCredentialRepository;
    private final RemoteSourceRepository remoteSourceRepository;
    private final RemotePackageRepository packageRepository;
    private final FileStatRepository fileStatRepository;
    private final ImageCache imageCache;

    public OptionsService(final ApplicationDefaults applicationDefaults, final ApplicationPreferences applicationPreferences,
                          final PluginRepository pluginRepository, final UserAccountRepository userAccountRepository,
                          final GoogleCredentialRepository googleCredentialRepository, final RemoteSourceRepository remoteSourceRepository,
                          final RemotePackageRepository packageRepository, final FileStatRepository fileStatRepository,
                          final ImageCache imageCache) {
        super(applicationDefaults, applicationPreferences);
        this.pluginRepository = pluginRepository;
        this.userAccountRepository = userAccountRepository;
        this.googleCredentialRepository = googleCredentialRepository;
        this.remoteSourceRepository = remoteSourceRepository;
        this.packageRepository = packageRepository;
        this.fileStatRepository = fileStatRepository;
        this.imageCache = imageCache;
    }

    @PostConstruct
    private void initialize() {
        ApplicationPreferences prefs = this.getApplicationPreferences();
        // Init default options
        if (prefs.get(ApplicationDefaults.VST_DIRECTORY_KEY, null) == null) {
            prefs.put(ApplicationDefaults.VST_DIRECTORY_KEY,
                    this.getApplicationDefaults().getDefaultPluginPath(PluginFormat.VST2));
        }
        if (prefs.get(ApplicationDefaults.VST3_DIRECTORY_KEY, null) == null) {
            prefs.put(ApplicationDefaults.VST3_DIRECTORY_KEY,
                    this.getApplicationDefaults().getDefaultPluginPath(PluginFormat.VST3));
        }
        if (prefs.get(ApplicationDefaults.AU_DIRECTORY_KEY, null) == null
                && this.getApplicationDefaults().getRuntimePlatform().getOperatingSystem().equals(OperatingSystem.MAC)) {
            prefs.put(ApplicationDefaults.AU_DIRECTORY_KEY,
                    this.getApplicationDefaults().getDefaultPluginPath(PluginFormat.AU));
        }
        if (prefs.get(ApplicationDefaults.VST2_DISCOVERY_ENABLED_KEY, null) == null) {
            prefs.putBoolean(ApplicationDefaults.VST2_DISCOVERY_ENABLED_KEY, Boolean.TRUE);
        }
        if (prefs.get(ApplicationDefaults.VST3_DISCOVERY_ENABLED_KEY, null) == null) {
            prefs.putBoolean(ApplicationDefaults.VST3_DISCOVERY_ENABLED_KEY, Boolean.TRUE);
        }
        if (prefs.get(ApplicationDefaults.NATIVE_HOST_ENABLED_KEY, null) == null) {
            prefs.putBoolean(ApplicationDefaults.NATIVE_HOST_ENABLED_KEY, Boolean.TRUE);
        }
        if (prefs.get(ApplicationDefaults.SELECTED_ACCOUNT_KEY, null) == null) {
            prefs.putBoolean(ApplicationDefaults.SELECTED_ACCOUNT_KEY, Boolean.FALSE);
        }
        if (prefs.get(ApplicationDefaults.STORE_SUBDIRECTORY_ENABLED, null) == null) {
            prefs.putBoolean(ApplicationDefaults.STORE_SUBDIRECTORY_ENABLED, Boolean.TRUE);
        }
    }

    /**
     * Clear all user data including Options, configured stores and cache.
     */
    public void clearAllUserData() {
        try {
            this.getApplicationPreferences().clear();
            pluginRepository.deleteAll();

            googleCredentialRepository.deleteAll();
            userAccountRepository.deleteAll();
            packageRepository.deleteAll();
            remoteSourceRepository.deleteAll();
            fileStatRepository.deleteAll();

            clearCache();

        } catch (BackingStoreException e) {
            LOGGER.error("Preferences cannot be updated", e);
        }
    }

    /**
     * Clear data from all application caches.
     */
    public void clearCache() {
        imageCache.clear();
    }

}
