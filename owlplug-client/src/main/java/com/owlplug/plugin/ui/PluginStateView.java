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

import com.owlplug.plugin.model.PluginState;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.shape.Circle;

import static com.owlplug.plugin.ui.common.StyleClassSetter.setStyleClass;

public class PluginStateView extends Label {

    public PluginStateView() {
        super("Unknown");
    }

    public PluginStateView(final PluginState pluginState) {
        setPluginState(pluginState);
    }

    public void setPluginState(PluginState state) {
        final var circle = new Circle(0, 0, 2);
        setStyleClass(state, circle);

        setGraphic(circle);
        setContentDisplay(ContentDisplay.RIGHT);
        setText(state.getText());
        setTooltip(new Tooltip(state.getDescription()));
    }

}
