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

package com.owlplug.plugin.controllers;

import com.owlplug.core.components.ApplicationDefaults;
import com.owlplug.core.components.ApplicationPreferences;
import com.owlplug.core.components.DialogManager;
import com.owlplug.core.controllers.BaseController;
import com.owlplug.core.services.TelemetryService;
import com.owlplug.core.ui.FilterableTreeItem;
import com.owlplug.plugin.model.IDirectory;
import com.owlplug.plugin.model.Plugin;
import com.owlplug.plugin.model.PluginDirectory;
import com.owlplug.plugin.model.Symlink;
import com.owlplug.plugin.repositories.SymlinkRepository;
import com.owlplug.plugin.services.PluginService;
import com.owlplug.plugin.ui.PluginTreeCell;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.owlplug.plugin.controllers.PluginTreeViewController.Display.DirectoryTree;
import static com.owlplug.plugin.controllers.PluginTreeViewController.Display.FlatTree;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Controller
public class PluginTreeViewController extends BaseController {

    private final PluginService pluginService;
    private final SymlinkRepository symlinkRepository;

    private final SimpleStringProperty search = new SimpleStringProperty();
    private final TreeView<Object> pluginTreeView;
    private final FilterableTreeItem<Object> treePluginNode;
    private final FilterableTreeItem<Object> treeFileRootNode;

    private PluginTreeViewController.FileTree pluginTree;

    public PluginTreeViewController(final ApplicationDefaults applicationDefaults, final ApplicationPreferences applicationPreferences,
                                    final TelemetryService telemetryService, final DialogManager dialogManager, PluginService pluginService, SymlinkRepository symlinkRepository) {
        super(applicationDefaults, applicationPreferences, telemetryService, dialogManager);
        this.pluginService = pluginService;
        this.symlinkRepository = symlinkRepository;

        pluginTreeView = new TreeView<>();
        VBox.setVgrow(pluginTreeView, Priority.ALWAYS);
        treePluginNode = new FilterableTreeItem<>("(all)");
        treeFileRootNode = new FilterableTreeItem<>("(all)");

        pluginTreeView.setCellFactory(p -> new PluginTreeCell(getApplicationDefaults(), pluginService));

        pluginTreeView.setRoot(treePluginNode);

        // Binds search property to plugin tree filter
        treePluginNode.predicateProperty().bind(Bindings.createObjectBinding(() -> {
            if (search.getValue() == null || search.getValue().isEmpty()) {
                return null;
            }
            return (item) -> {
                if (item instanceof Plugin plugin) {
                    return plugin.getName().toLowerCase().contains(search.getValue().toLowerCase())
                            || (plugin.getCategory() != null && plugin.getCategory().toLowerCase().contains(
                            search.getValue().toLowerCase()));
                } else {
                    return item.toString().toLowerCase().contains(search.getValue().toLowerCase());
                }
            };
        }, search));

        // Binds search property to file tree filter
        treeFileRootNode.predicateProperty().bind(Bindings.createObjectBinding(() -> {
            if (search.getValue() == null || search.getValue().isEmpty()) {
                return null;
            }
            return (item) -> {
                if (item instanceof Plugin plugin) {
                    return plugin.getName().toLowerCase().contains(search.getValue().toLowerCase())
                            || (plugin.getCategory() != null && plugin.getCategory().toLowerCase().contains(search.getValue().toLowerCase()));
                } else {
                    return item.toString().toLowerCase().contains(search.getValue().toLowerCase());
                }
            };
        }, search));

    }

    public SimpleStringProperty searchProperty() {
        return this.search;
    }

    public TreeView<Object> getTreeView() {
        return pluginTreeView;
    }

    public void refresh() {
        pluginTreeView.refresh();
    }

    public void setDisplayMode(final Display display) {
        if (DirectoryTree.equals(display)) {
            pluginTreeView.setRoot(treeFileRootNode);
        } else if (FlatTree.equals(display)) {
            pluginTreeView.setRoot(treePluginNode);
        }
    }

    public void setNodeManaged(final boolean isManaged) {
        this.pluginTreeView.setManaged(isManaged);
        this.pluginTreeView.setVisible(isManaged);
    }

    public void setPlugins(final Iterable<Plugin> plugins) {
        clearAndFillPluginTree(plugins);
    }

    public void selectPluginById(long id) {
        List<TreeItem<Object>> items = getNestedChildren(pluginTreeView.getRoot());

        for (TreeItem<Object> item : items) {
            if (item.getValue() instanceof Plugin plugin && plugin.getId().equals(id)) {
                final var row = pluginTreeView.getRow(item);
                pluginTreeView.getSelectionModel().select(row);
            }
        }

    }

    /**
     * Processes a plugin and adds it to the flat plugin tree view.
     * If the plugin contains multiple components, they are added as subitems.
     *
     * @param node   {@link FilterableTreeItem} instance
     * @param plugin The plugin to process.
     */
    private void processPlugin(final FilterableTreeItem<Object> node, final Plugin plugin) {
        final var item = new FilterableTreeItem<Object>(plugin);
        node.getInternalChildren().add(item);

        // Display subcomponents in the plugin tree
        if (plugin.getComponents().size() > 1) {
            plugin.getComponents().forEach(component -> item.getInternalChildren()
                    .add(new FilterableTreeItem<>(component)));
        }
    }

    /**
     * Refreshes displayed pluginIterable in tree views.
     */
    private void clearAndFillPluginTree(final Iterable<Plugin> pluginIterable) {
        treePluginNode.getInternalChildren().clear();

        pluginIterable.forEach(plugin -> processPlugin(treePluginNode, plugin));

        treePluginNode.setExpanded(true);
        treeFileRootNode.getInternalChildren().clear();

        generatePluginTree(pluginIterable);

        pluginService.getDirectoriesExplorationSet()
                .forEach(directory -> {
                    FilterableTreeItem<Object> item = initDirectoryRoot(pluginTree, directory);
                    // Ensure the tree item is not empty before adding the root directory
                    // This way; no entry is added if no plugins have been detected
                    if (item.getValue() != null) {
                        treeFileRootNode.getInternalChildren().add(item);
                    }
                });

        treeFileRootNode.setExpanded(true);
    }

    /**
     * Generates a PluginTree representation.
     * <pre>
     * [rootDir ->
     *   [ subDir1 -> [ plugin1 -> [] ],
     *     subDir2 -> [ plugin2 -> [] , plugin3 -> [] ]
     *   ]
     * ]
     * </pre>
     *
     */
    private void generatePluginTree(final Iterable<Plugin> pluginIterable) {

        pluginTree = new PluginTreeViewController.FileTree();

        pluginIterable.forEach(plug -> {
            PluginTreeViewController.FileTree node = pluginTree;
            String[] subDirs = plug.getPath().split("/");
            String currentPath = "";
            for (int i = 0; i < subDirs.length; i++) {
                currentPath = currentPath + subDirs[i] + "/";
                String segment = subDirs[i];
                PluginTreeViewController.FileTree ft = new PluginTreeViewController.FileTree();

                if (node.get(segment) == null) {
                    // Node is a plugin (End of branch)
                    if (i == subDirs.length - 1) {
                        ft.setNodeValue(plug);

                        // Node is a directory
                    } else {
                        // TODO Should be optimized for large plugin set
                        List<Plugin> localPluginList = new ArrayList<>();
                        for (Plugin p : pluginIterable) {
                            if (p.getPath().startsWith(currentPath)) {
                                localPluginList.add(p);
                            }
                        }

                        // Retrieve Symlink if exist
                        // TODO: This can be refactored to prevent trailing slash removal
                        Symlink symlink = symlinkRepository.findByPath(currentPath.substring(0, currentPath.length() - 1));
                        if (symlink != null) {
                            symlink.setPluginList(localPluginList);
                            ft.setNodeValue(symlink);
                        } else {
                            PluginDirectory directory = new PluginDirectory();
                            directory.setName(segment);
                            directory.setPath(currentPath);
                            directory.setPluginList(localPluginList);
                            ft.setNodeValue(directory);
                        }

                    }
                    node.put(segment, ft);
                }
                node = node.get(segment);
            }
        });
    }

    private FilterableTreeItem<Object> initDirectoryRoot(final FileTree pluginTree, final String directoryPath) {
        final var item = new FilterableTreeItem<>(null);
        item.setExpanded(true);

        var treeHead = pluginTree;
        String[] directories = directoryPath.split("/");

        for (String dir : directories) {
            if (treeHead != null) {
                treeHead = treeHead.get(dir);
            }
        }

        if (treeHead != null && treeHead.getNodeValue() instanceof PluginDirectory directory) {
            directory.setRootDirectory(true);
            item.setValue(directory);
            buildDirectoryTree(treeHead, item, EMPTY);
        }

        return item;
    }

    /**
     * Builds the directory tree view using file tree representation. If some
     * directories contain only one subdirectory and nothing else, they are merged
     * in one node.
     *
     * @param pluginTree   File tree representation
     * @param node         root tree node
     * @param mergedParent Name of a merged parent tree
     */
    private void buildDirectoryTree(FileTree pluginTree, FilterableTreeItem<Object> node, String mergedParent) {

        String mergedParentName = mergedParent;
        node.setExpanded(true);

        if (mergedParentName == null) {
            mergedParentName = "";
        }

        // For each subdirectory (aka child nodes)
        for (String dir : pluginTree.keySet()) {
            FileTree child = pluginTree.get(dir);
            // If child is empty then we have reached a plugin, and we can't go deeper
            if (child.isEmpty()) {
                processPlugin(node, (Plugin) child.getNodeValue());

                // If not, we are exploring a directory
            } else {
                IDirectory directory;
                // If the child node contains only one directory, we can merge it with the child node
                if (child.size() == 1 && ((FileTree) child.values().toArray()[0]).getNodeValue() instanceof PluginDirectory
                        && !(node.getValue() instanceof Symlink)
                        && !(child.getNodeValue() instanceof Symlink)) {

                    directory = (IDirectory) child.getNodeValue();
                    mergedParentName = mergedParentName + directory.getName() + "/";

                    buildDirectoryTree(child, node, mergedParentName);
                    // We don't want to merge next directories in the current iteration
                    mergedParentName = "";

                    // In case our child cannot be merged (contains not only one subdirectory)
                } else {
                    directory = (IDirectory) child.getNodeValue();
                    directory.setDisplayName(mergedParentName + directory.getName());

                    // We don't want to merge next directories in the current iteration
                    mergedParentName = "";
                    FilterableTreeItem<Object> item = new FilterableTreeItem<>(directory);
                    node.getInternalChildren().add(item);
                    buildDirectoryTree(child, item, mergedParentName);
                }
            }
        }
    }

    private List<TreeItem<Object>> getNestedChildren(final TreeItem<Object> item) {
        final var items = new ArrayList<TreeItem<Object>>();
        items.add(item);

        final var children = new ArrayList<>(item.getChildren());
        children.forEach(child -> items.addAll(getNestedChildren(child)));
        return items;
    }

    @Getter
    @Setter
    static class FileTree extends HashMap<String, PluginTreeViewController.FileTree> {
        private Object nodeValue;
    }

    public enum Display {
        DirectoryTree, FlatTree
    }

}
