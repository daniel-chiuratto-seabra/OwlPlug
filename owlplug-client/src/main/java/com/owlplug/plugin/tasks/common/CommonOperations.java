package com.owlplug.plugin.tasks.common;

import com.owlplug.plugin.model.Symlink;
import com.owlplug.plugin.tasks.discovery.PluginFileCollector;
import com.owlplug.plugin.tasks.discovery.PluginScanTaskParameters;
import com.owlplug.plugin.tasks.discovery.SymlinkCollector;
import com.owlplug.plugin.tasks.discovery.fileformats.PluginFile;

import java.util.Collection;
import java.util.Set;

import static com.owlplug.plugin.model.PluginFormat.AU;
import static com.owlplug.plugin.model.PluginFormat.LV2;
import static com.owlplug.plugin.model.PluginFormat.VST2;
import static com.owlplug.plugin.model.PluginFormat.VST3;

public class CommonOperations {

    public static void collectPlugins(final PluginScanTaskParameters pluginScanTaskParameters,
                                      final Set<PluginFile> collectedPluginFiles, final PluginFileCollector pluginCollector,
                                      final Collection<Symlink> collectedSymlinks, final SymlinkCollector symlinkCollector) {
        if (pluginScanTaskParameters.getDirectoryScope() != null) {
            // Plugins are retrieved from a scoped directory
            if (pluginScanTaskParameters.isFindLv2()) {
                collectedPluginFiles.addAll(pluginCollector.collect(pluginScanTaskParameters.getDirectoryScope(), LV2));
            }
            if (pluginScanTaskParameters.isFindVst3()) {
                collectedPluginFiles.addAll(pluginCollector.collect(pluginScanTaskParameters.getDirectoryScope(), VST3));
            }
            if (pluginScanTaskParameters.isFindVst2()) {
                collectedPluginFiles.addAll(pluginCollector.collect(pluginScanTaskParameters.getDirectoryScope(), VST2));
            }
            if (pluginScanTaskParameters.isFindAu()) {
                collectedPluginFiles.addAll(pluginCollector.collect(pluginScanTaskParameters.getDirectoryScope(), AU));
            }

            collectedSymlinks.addAll(symlinkCollector.collect(pluginScanTaskParameters.getDirectoryScope()));

        } else {
            // Plugins are retrieved from regulars directories
            final var vst2Directory = pluginScanTaskParameters.getVst2Directory();
            final var vst3Directory = pluginScanTaskParameters.getVst3Directory();
            final var auDirectory = pluginScanTaskParameters.getAuDirectory();
            final var lv2Directory = pluginScanTaskParameters.getLv2Directory();

            if (pluginScanTaskParameters.isFindLv2()) {
                collectedPluginFiles.addAll(pluginCollector.collect(lv2Directory, LV2));
                collectedSymlinks.addAll(symlinkCollector.collect(lv2Directory));
                pluginScanTaskParameters.getLv2ExtraDirectories().forEach(directory -> {
                    collectedPluginFiles.addAll(pluginCollector.collect(directory, LV2));
                    collectedSymlinks.addAll(symlinkCollector.collect(directory));
                });
            }

            if (pluginScanTaskParameters.isFindVst3()) {
                collectedPluginFiles.addAll(pluginCollector.collect(vst3Directory, VST3));
                collectedSymlinks.addAll(symlinkCollector.collect(vst3Directory));
                pluginScanTaskParameters.getVst3ExtraDirectories().forEach(directory -> {
                    collectedPluginFiles.addAll(pluginCollector.collect(directory, VST3));
                    collectedSymlinks.addAll(symlinkCollector.collect(directory));
                });
            }

            if (pluginScanTaskParameters.isFindVst2()) {
                collectedPluginFiles.addAll(pluginCollector.collect(vst2Directory, VST2));
                collectedSymlinks.addAll(symlinkCollector.collect(vst2Directory));
                pluginScanTaskParameters.getVst2ExtraDirectories().forEach(directory -> {
                    collectedPluginFiles.addAll(pluginCollector.collect(directory, VST2));
                    collectedSymlinks.addAll(symlinkCollector.collect(directory));
                });
            }

            if (pluginScanTaskParameters.isFindAu()) {
                collectedPluginFiles.addAll(pluginCollector.collect(auDirectory, AU));
                collectedSymlinks.addAll(symlinkCollector.collect(auDirectory));
                pluginScanTaskParameters.getAuExtraDirectories().forEach(directory -> {
                    collectedPluginFiles.addAll(pluginCollector.collect(directory, AU));
                    collectedSymlinks.addAll(symlinkCollector.collect(directory));
                });
            }
        }
    }
}
