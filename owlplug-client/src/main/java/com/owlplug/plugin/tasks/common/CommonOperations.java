package com.owlplug.plugin.tasks.common;

import com.owlplug.plugin.model.PluginFormat;
import com.owlplug.plugin.model.Symlink;
import com.owlplug.plugin.tasks.discovery.PluginFileCollector;
import com.owlplug.plugin.tasks.discovery.PluginScanTaskParameters;
import com.owlplug.plugin.tasks.discovery.SymlinkCollector;
import com.owlplug.plugin.tasks.discovery.fileformats.PluginFile;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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
            final var scope = pluginScanTaskParameters.getDirectoryScope();
            if (pluginScanTaskParameters.isFindLv2()) {
                collectedPluginFiles.addAll(pluginCollector.collect(scope, LV2));
            }
            if (pluginScanTaskParameters.isFindVst3()) {
                collectedPluginFiles.addAll(pluginCollector.collect(scope, VST3));
            }
            if (pluginScanTaskParameters.isFindVst2()) {
                collectedPluginFiles.addAll(pluginCollector.collect(scope, VST2));
            }
            if (pluginScanTaskParameters.isFindAu()) {
                collectedPluginFiles.addAll(pluginCollector.collect(scope, AU));
            }

            collectedSymlinks.addAll(symlinkCollector.collect(scope));

            // Try to restore the original scan directory when a directory scope is used
            // https://github.com/DropSnorz/OwlPlug/issues/424
            final var scanPath = getScanPathFromDirectoryScope(pluginScanTaskParameters);
            if (scanPath != null) {
                final var scanFile = new File(scanPath);
                collectedPluginFiles.forEach(f -> f.setScanDirectory(scanFile));
            }

        } else {
            // Plugins are retrieved from regular directories
            if (pluginScanTaskParameters.isFindLv2()) {
                collectByDirectory(pluginScanTaskParameters.getLv2Directory(), LV2,
                        pluginScanTaskParameters.getLv2ExtraDirectories(),
                        collectedPluginFiles, pluginCollector, collectedSymlinks, symlinkCollector);
            }
            if (pluginScanTaskParameters.isFindVst3()) {
                collectByDirectory(pluginScanTaskParameters.getVst3Directory(), VST3,
                        pluginScanTaskParameters.getVst3ExtraDirectories(),
                        collectedPluginFiles, pluginCollector, collectedSymlinks, symlinkCollector);
            }
            if (pluginScanTaskParameters.isFindVst2()) {
                collectByDirectory(pluginScanTaskParameters.getVst2Directory(), VST2,
                        pluginScanTaskParameters.getVst2ExtraDirectories(),
                        collectedPluginFiles, pluginCollector, collectedSymlinks, symlinkCollector);
            }
            if (pluginScanTaskParameters.isFindAu()) {
                collectByDirectory(pluginScanTaskParameters.getAuDirectory(), AU,
                        pluginScanTaskParameters.getAuExtraDirectories(),
                        collectedPluginFiles, pluginCollector, collectedSymlinks, symlinkCollector);
            }
        }
    }

    private static void collectByDirectory(final String directory, final PluginFormat format, final List<String> extraDirectories,
                                           final Set<PluginFile> collectedPluginFiles, final PluginFileCollector pluginCollector,
                                           final Collection<Symlink> collectedSymlinks, final SymlinkCollector symlinkCollector) {
        collectedPluginFiles.addAll(pluginCollector.collect(directory, format));
        collectedSymlinks.addAll(symlinkCollector.collect(directory));
        extraDirectories.forEach(dir -> {
            collectedPluginFiles.addAll(pluginCollector.collect(dir, format));
            collectedSymlinks.addAll(symlinkCollector.collect(dir));
        });
    }

    /**
     * Compute the original scan directory based on the provided directory scope.
     * See related <a href="https://github.com/DropSnorz/OwlPlug/issues/424">issue</a>
     *
     * @param pluginScanTaskParameters the directory scope
     * @return path to the potential scan directory
     */
    private static String getScanPathFromDirectoryScope(final PluginScanTaskParameters pluginScanTaskParameters) {
        return getAllPluginScanPath(pluginScanTaskParameters)
                .stream()
                .sorted()
                .filter(pluginScanTaskParameters.getDirectoryScope()::startsWith)
                .findFirst().orElse(null);
    }

    private static Set<String> getAllPluginScanPath(final PluginScanTaskParameters pluginScanTaskParameters) {
        Set<String> paths = new HashSet<>();

        paths.add(pluginScanTaskParameters.getVst2Directory());
        paths.add(pluginScanTaskParameters.getVst3Directory());
        paths.add(pluginScanTaskParameters.getAuDirectory());
        paths.add(pluginScanTaskParameters.getLv2Directory());

        paths.addAll(pluginScanTaskParameters.getVst2ExtraDirectories());
        paths.addAll(pluginScanTaskParameters.getVst3ExtraDirectories());
        paths.addAll(pluginScanTaskParameters.getAuExtraDirectories());
        paths.addAll(pluginScanTaskParameters.getLv2ExtraDirectories());

        // Clean null and blank paths if some are provided
        paths.removeIf(p -> p == null || p.isBlank());

        return paths;
    }

}
