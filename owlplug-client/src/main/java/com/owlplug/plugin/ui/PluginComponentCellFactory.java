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
import com.owlplug.plugin.model.PluginComponent;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PluginComponentCellFactory implements Callback<ListView<PluginComponent>, ListCell<PluginComponent>> {

    private final ApplicationDefaults applicationDefaults;

    @Override
    public ListCell<PluginComponent> call(final ListView<PluginComponent> pluginComponentListView) {
        return new ListCell<>() {
            @Override
            public void updateItem(PluginComponent plugin, boolean empty) {
                super.updateItem(plugin, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    final var imageView = new ImageView();
                    imageView.setImage(applicationDefaults.pluginComponentImage);
                    setText(plugin.getName());
                    setGraphic(imageView);
                }
            }
        };
    }
}
