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

import com.owlplug.core.components.ApplicationDefaults;
import com.owlplug.core.components.ApplicationPreferences;
import com.owlplug.core.services.BaseService;
import com.owlplug.host.NativePlugin;
import com.owlplug.host.loaders.DummyPluginLoader;
import com.owlplug.host.loaders.EmbeddedScannerPluginLoader;
import com.owlplug.host.loaders.NativePluginLoader;
import com.owlplug.host.loaders.jni.JNINativePluginLoader;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class NativeHostService extends BaseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeHostService.class);

    @Getter private final Collection<NativePluginLoader> pluginLoaders = new ArrayList<>();
    @Getter @Setter private NativePluginLoader currentPluginLoader = null;

    private NativePluginLoader fallbackLoader = null;

    public NativeHostService(final ApplicationDefaults applicationDefaults, final ApplicationPreferences applicationPreferences) {
        super(applicationDefaults, applicationPreferences);
    }

    @PostConstruct
    private void init() {
        pluginLoaders.add(EmbeddedScannerPluginLoader.getInstance());
        pluginLoaders.add(JNINativePluginLoader.getInstance());
        fallbackLoader = DummyPluginLoader.getInstance();
        pluginLoaders.add(fallbackLoader);

        for (NativePluginLoader loader : pluginLoaders) {
            loader.init();
        }
        configureCurrentPluginLoader();
    }

    private void configureCurrentPluginLoader() {

        String prefLoaderId = this.getApplicationPreferences().get(ApplicationDefaults.PREFERRED_NATIVE_LOADER, null);

        // If a preferred loader as been set
        if (prefLoaderId != null) {
            // If it's not the fallback loader
            if (!prefLoaderId.equals(fallbackLoader.getId())) {
                Optional<NativePluginLoader> optLoader = getLoaderById(prefLoaderId);
                // If the loader is known
                if (optLoader.isPresent()) {
                    NativePluginLoader loader = optLoader.get();
                    // If the loader is available
                    if (loader.isAvailable()) {
                        this.currentPluginLoader = loader;
                    } else {
                        LOGGER.error("Preferred loader {} is not available", loader.getId());
                    }
                } else {
                    LOGGER.error("Preferred loader {} is not known", prefLoaderId);
                }
            } else {
                LOGGER.info("Preferred loader as been previously set to the fallback loader, OwlPlug will look for other available loader.");
            }
        } else {
            LOGGER.info("No preferred native loader configured");
        }

        if (currentPluginLoader == null) {
            Optional<NativePluginLoader> availableLoader = getAvailablePluginLoaders().stream().findFirst();
            if (availableLoader.isPresent()) {
                currentPluginLoader = availableLoader.get();
            } else {
                LOGGER.warn("No available native plugin loader found. Using fallback loader.");
                currentPluginLoader = fallbackLoader;
            }
        }

        LOGGER.info("Native plugin loader set to {}", currentPluginLoader.getId());
    }

    public List<NativePluginLoader> getAvailablePluginLoaders() {
        return pluginLoaders.stream().filter(NativePluginLoader::isAvailable).toList();
    }

    public Optional<NativePluginLoader> getLoaderById(String id) {
        return pluginLoaders.stream().filter(l -> l.getId().equals(id))
                .findFirst();
    }

    public List<NativePlugin> loadPlugin(String path) {
        if (currentPluginLoader != null) {
            return currentPluginLoader.loadPlugin(path);
        } else {
            LOGGER.error("Native plugin loader not set");
            throw new IllegalStateException("Native plugin loader not set");
        }
    }

    public boolean isNativeHostEnabled() {
        return this.getApplicationPreferences().getBoolean(ApplicationDefaults.NATIVE_HOST_ENABLED_KEY, false);
    }

    public boolean isNativeHostUnavailable() {
        for (NativePluginLoader loader : pluginLoaders) {
            if (loader.isAvailable()) {
                return false;
            }
        }
        return true;
    }

}
