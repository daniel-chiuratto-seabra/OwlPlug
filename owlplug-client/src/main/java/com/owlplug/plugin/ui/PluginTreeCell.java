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

package com.owlplug.plugin.ui;

import com.owlplug.core.components.ApplicationDefaults;
import com.owlplug.plugin.model.Directory;
import com.owlplug.plugin.model.Plugin;
import com.owlplug.plugin.model.PluginComponent;
import com.owlplug.plugin.model.PluginDirectory;
import com.owlplug.plugin.model.Symlink;
import com.owlplug.plugin.services.PluginService;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import lombok.RequiredArgsConstructor;

import static com.owlplug.plugin.ui.common.StyleClassSetter.setStyleClass;

@RequiredArgsConstructor
public class PluginTreeCell extends TreeCell<Object> {

    private final ApplicationDefaults applicationDefaults;
    private final PluginService pluginService;

    @Override
    public void updateItem(final Object item, final boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            switch (item) {
                case Plugin plugin -> renderPlugin(plugin);
                case PluginComponent pluginComponent -> renderComponent(pluginComponent);
                case Directory directory -> renderDirectory(directory);
                default -> {
                    setText(item.toString());
                    setGraphic(getTreeItem().getGraphic());
                }
            }
        }

        // Force the rendering immediately to avoid blinking nodes on the TreeView
        // Blinking appears since JavaFX14 migration
        applyCss();
    }

    private void renderPlugin(final Plugin plugin) {
        final var hbox = new HBox(4);
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.getChildren().add(new ImageView(applicationDefaults.getPluginFormatIcon(plugin.getFormat())));
        hbox.getChildren().add(new Label(plugin.getName()));

        final var circle = new Circle(0, 0, 2);
        hbox.getChildren().add(circle);

        final var pluginState = pluginService.getPluginState(plugin);
        setStyleClass(pluginState, circle);

        circle.applyCss();

        if (plugin.isDisabled()) {
            final var label = new Label("(disabled)");
            label.getStyleClass().add("label-disabled");
            hbox.getChildren().add(label);
        }

        setGraphic(hbox);
        setText(null);
    }


    private void renderComponent(final PluginComponent pluginComponent) {
        Label label = new Label(pluginComponent.getName());
        label.setGraphic(new ImageView(applicationDefaults.pluginComponentImage));
        setGraphic(label);
        setText(null);
    }


    private void renderDirectory(Directory dir) {
        final var textFlow = new TextFlow();
        Text directoryName;

        if (dir.getDisplayName() != null && !dir.getName().equals(dir.getDisplayName())) {
            String preText = dir.getDisplayName().replaceAll("/" + dir.getName() + "$", "");
            Text pre = new Text(preText);
            pre.getStyleClass().add("text-disabled");
            textFlow.getChildren().add(pre);
            directoryName = new Text("/" + dir.getName());

        } else {
            directoryName = new Text(dir.getName());
        }

        if (dir.isStale()) {
            directoryName.getStyleClass().add("text-danger");
            directoryName.setText(dir.getName() + " (Stale)");
        }

        textFlow.getChildren().add(directoryName);

        Node icon = getIconNode(dir);
        HBox hbox = new HBox(5);
        hbox.getChildren().add(icon);
        hbox.getChildren().add(textFlow);

        setGraphic(hbox);
        setText(null);
    }

    private Node getIconNode(final Directory directory) {
        if (directory instanceof Symlink) {
            return new ImageView(applicationDefaults.symlinkImage);
        } else if (directory instanceof PluginDirectory pluginDirectory) {
            if (pluginDirectory.isRootDirectory()) {
                return new ImageView(applicationDefaults.scanDirectoryImage);
            } else {
                return new ImageView(applicationDefaults.directoryImage);
            }
        }
        return new ImageView(applicationDefaults.directoryImage);
    }

}
