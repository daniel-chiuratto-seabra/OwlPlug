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
import com.owlplug.plugin.model.Plugin;
import com.owlplug.plugin.services.PluginService;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

public class RecoveredPluginView extends HBox {


    /**
     * Creates a RecoveredPluginView used in crash recovery reports.
     *
     * @param plugin              - recovered plugin instance
     * @param pluginService       - PluginService
     * @param applicationDefaults - ApplicationDefaults
     */
    public RecoveredPluginView(final Plugin plugin, final PluginService pluginService, final ApplicationDefaults applicationDefaults) {
        super();

        setAlignment(Pos.BASELINE_LEFT);
        getStyleClass().add("recovered-plugin-view");

        final var label = new Label(plugin.getName());
        ImageView imageView = new ImageView();
        imageView.setImage(applicationDefaults.getPluginFormatIcon(plugin.getFormat()));

        label.setGraphic(imageView);
        getChildren().add(label);

        final var transparentPane = new Pane();
        HBox.setHgrow(transparentPane, Priority.ALWAYS);
        getChildren().add(transparentPane);

        final var toggleButton = new ToggleButton();
        toggleButton.setText("Native Discovery");
        toggleButton.setSelected(plugin.getFootprint().isNativeDiscoveryEnabled());

        toggleButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            plugin.getFootprint().setNativeDiscoveryEnabled(newValue);
            pluginService.save(plugin.getFootprint());
        });

        getChildren().add(toggleButton);
    }

}
