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

package com.owlplug.project.ui;

import com.owlplug.core.components.ApplicationDefaults;
import com.owlplug.project.model.DawProject;
import com.owlplug.project.model.LookupResult;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import lombok.RequiredArgsConstructor;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@RequiredArgsConstructor
public class ProjectTreeCell extends TreeCell<DawProject> {

    private final ApplicationDefaults applicationDefaults;

    @Override
    public void updateItem(final DawProject project, final boolean empty) {
        super.updateItem(project, empty);

        if (empty || project == null) {
            setText(null);
            setGraphic(null);
        } else {
            setText(null);
            final var hBox = new HBox(10);
            final var icon = new ImageView(applicationDefaults.getDAWApplicationIcon(project.getApplication()));
            hBox.getChildren().add(icon);
            Label label = new Label(project.getName());
            hBox.getChildren().add(label);
            final var failedLookups = project.getPluginByLookupResult(LookupResult.MISSING);
            if (isNotEmpty(failedLookups)) {
                final var missingLabel = new Label(failedLookups.size() + " Missing plugin(s)");
                missingLabel.setGraphic(new ImageView(applicationDefaults.errorIconImage));
                missingLabel.getStyleClass().add("label-danger");
                hBox.getChildren().add(missingLabel);
            }

            setGraphic(hBox);
        }

    }

}
