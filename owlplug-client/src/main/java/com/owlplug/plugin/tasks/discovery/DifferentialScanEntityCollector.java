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

package com.owlplug.plugin.tasks.discovery;

import com.owlplug.plugin.model.Plugin;
import com.owlplug.plugin.model.Symlink;
import com.owlplug.plugin.tasks.discovery.fileformats.PluginFile;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends {@link ScopedScanEntityCollector} with differential collection capabilities.
 *
 * <p>After a {@link #collect()} pass, call {@link #differentialPlugins(List)} and/or
 * {@link #differentialSymlinks(List)} to compute which plugins/symlinks were added or
 * removed relative to the previously persisted state. Results are exposed via
 * {@link #getPluginDifferential()} and {@link #getSymlinkDifferential()}.
 */
public class DifferentialScanEntityCollector extends ScopedScanEntityCollector {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private PluginFileDifferential pluginDifferential = new PluginFileDifferential();
    private SymlinkDifferential symlinkDifferential = new SymlinkDifferential();

    /**
     * Creates a new {@code DifferentialScanEntityCollector} for the given scan parameters.
     *
     * @param parameters the scan scope and configuration
     */
    public DifferentialScanEntityCollector(PluginScanTaskParameters parameters) {
        super(parameters);
    }

    /**
     * Computes the plugin differential between the current scan result and the previously
     * persisted plugin list.
     *
     * <p>Plugins present in the scan result but absent from {@code original} are classified
     * as <em>added</em>; plugins present in {@code original} but absent from the scan result
     * are classified as <em>removed</em>. The result is stored internally and available via
     * {@link #getPluginDifferential()}.
     *
     * @param original the list of plugins currently persisted in the database
     * @return {@code this}, for method chaining
     */
    public DifferentialScanEntityCollector differentialPlugins(List<Plugin> original) {

        List<String> collected = this.getPluginFiles().stream()
                .map(PluginFile::getPath)
                .collect(Collectors.toList());

        List<String> persisted = original.stream()
                .map(Plugin::getPath)
                .collect(Collectors.toList());

        LOGGER.debug("Plugin differential, collected {} plugins", collected.size());
        LOGGER.debug("Plugin differential, persisted {} plugins", persisted.size());

        PathDifferential diff = differential(collected, persisted);

        pluginDifferential = new PluginFileDifferential();
        for (PluginFile file : getPluginFiles()) {
            if (diff.getAdded().contains(file.getPath())) {
                pluginDifferential.getAdded().add(file);
            }
        }
        pluginDifferential.setRemoved(diff.getRemoved());

        LOGGER.info("Plugin differential, added {} plugins", pluginDifferential.getAdded().size());
        LOGGER.info("Plugin differential, removed {} plugins", pluginDifferential.getRemoved().size());

        return this;

    }

    /**
     * Computes the symlink differential between the current scan result and the previously
     * persisted symlink list.
     *
     * <p>Symlinks present in the scan result but absent from {@code original} are classified
     * as <em>added</em>; symlinks present in {@code original} but absent from the scan result
     * are classified as <em>removed</em>. The result is stored internally and available via
     * {@link #getSymlinkDifferential()}.
     *
     * @param original the list of symlinks currently persisted in the database
     * @return {@code this}, for method chaining
     */
    public DifferentialScanEntityCollector differentialSymlinks(List<Symlink> original) {

        List<String> collected = this.getSymlinks().stream()
                .map(Symlink::getPath)
                .collect(Collectors.toList());

        List<String> persisted = original.stream()
                .map(Symlink::getPath)
                .collect(Collectors.toList());

        LOGGER.debug("Symlink differential, collected {} symlinks", collected.size());
        LOGGER.debug("Symlink differential, persisted {} symlinks", persisted.size());

        PathDifferential diff = differential(collected, persisted);

        symlinkDifferential = new SymlinkDifferential();
        for (Symlink symlink : getSymlinks()) {
            if (diff.getAdded().contains(symlink.getPath())) {
                symlinkDifferential.getAdded().add(symlink);
            }
        }
        symlinkDifferential.setRemoved(diff.getRemoved());

        LOGGER.info("Symlink differential, added {} symlinks", symlinkDifferential.getAdded().size());
        LOGGER.info("Symlink differential, removed {} symlinks", symlinkDifferential.getRemoved().size());

        return this;
    }

    /**
     * Runs the scoped collection pass and returns {@code this} for method chaining.
     *
     * @return {@code this}
     */
    @Override
    public DifferentialScanEntityCollector collect() {
        super.collect();
        return this;
    }

    /**
     * Returns the plugin differential computed by the last {@link #differentialPlugins} call.
     *
     * @return the current {@link PluginFileDifferential}; never {@code null}
     */
    public PluginFileDifferential getPluginDifferential() {
        return pluginDifferential;
    }

    /**
     * Returns the symlink differential computed by the last {@link #differentialSymlinks} call.
     *
     * @return the current {@link SymlinkDifferential}; never {@code null}
     */
    public SymlinkDifferential getSymlinkDifferential() {
        return symlinkDifferential;
    }

    /**
     * Computes the set difference between two path lists.
     *
     * @param newList paths from the current scan
     * @param oldList paths from the previously persisted state
     * @return a {@link PathDifferential} describing added and removed paths
     */
    private PathDifferential differential(List<String> newList, List<String> oldList) {
        PathDifferential diff = new PathDifferential();
        List<String> added = new ArrayList<>(newList);
        added.removeAll(oldList);
        diff.setAdded(added);

        List<String> removed = new ArrayList<>(oldList);
        removed.removeAll(newList);
        diff.setRemoved(removed);

        return diff;
    }

    /**
     * Holds the result of a path-level set difference: paths that were added and
     * paths that were removed between two scans.
     */
    public static final class PathDifferential {
        private List<String> added = new ArrayList<>();
        private List<String> removed = new ArrayList<>();

        /** Returns the list of paths present in the new scan but absent from the old one. */
        public List<String> getAdded() {
            return added;
        }

        /** Sets the list of added paths. */
        public void setAdded(List<String> added) {
            this.added = added;
        }

        /** Returns the list of paths present in the old scan but absent from the new one. */
        public List<String> getRemoved() {
            return removed;
        }

        /** Sets the list of removed paths. */
        public void setRemoved(List<String> removed) {
            this.removed = removed;
        }
    }

    /**
     * Holds the plugin-level differential: {@link PluginFile} objects that were newly
     * discovered and file paths that are no longer present.
     */
    public static final class PluginFileDifferential {
        private List<PluginFile> added = new ArrayList<>();
        private List<String> removed = new ArrayList<>();

        /** Returns the list of newly discovered plugin files. */
        public List<PluginFile> getAdded() {
            return added;
        }

        /** Sets the list of newly discovered plugin files. */
        public void setAdded(List<PluginFile> added) {
            this.added = added;
        }

        /** Returns the paths of plugins that are no longer present on disk. */
        public List<String> getRemoved() {
            return removed;
        }

        /** Sets the paths of removed plugins. */
        public void setRemoved(List<String> removed) {
            this.removed = removed;
        }
    }

    /**
     * Holds the symlink-level differential: {@link Symlink} objects that were newly
     * discovered and paths of symlinks that are no longer present.
     */
    public static final class SymlinkDifferential {
        private List<Symlink> added = new ArrayList<>();
        private List<String> removed = new ArrayList<>();

        /** Returns the list of newly discovered symlinks. */
        public List<Symlink> getAdded() {
            return added;
        }

        /** Sets the list of newly discovered symlinks. */
        public void setAdded(List<Symlink> added) {
            this.added = added;
        }

        /** Returns the paths of symlinks that are no longer present on disk. */
        public List<String> getRemoved() {
            return removed;
        }

        /** Sets the paths of removed symlinks. */
        public void setRemoved(List<String> removed) {
            this.removed = removed;
        }
    }


}
