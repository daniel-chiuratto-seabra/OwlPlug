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

import com.owlplug.controls.Dialog;
import com.owlplug.controls.DialogLayout;
import com.owlplug.core.components.ApplicationDefaults;
import com.owlplug.core.components.ApplicationPreferences;
import com.owlplug.core.components.DialogManager;
import com.owlplug.core.controllers.BaseController;
import com.owlplug.core.services.TelemetryService;
import com.owlplug.plugin.components.PluginTaskFactory;
import com.owlplug.plugin.model.Plugin;
import com.owlplug.plugin.model.Symlink;
import com.owlplug.plugin.tasks.SymlinkRemoveTask;
import com.owlplug.plugin.ui.PluginListCellFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import org.springframework.stereotype.Controller;

import java.util.Optional;

import static com.owlplug.core.utils.PlatformUtils.openFromDesktop;

@Controller
public class SymlinkInfoController extends BaseController {

    private final PluginTaskFactory pluginTaskFactory;

    @FXML private Label directoryPathLabel;
    @FXML private ListView<Plugin> pluginDirectoryListView;
    @FXML private Button openLinkButton;
    @FXML private Button openTargetButton;
    @FXML private Button deleteLinkButton;
    @FXML private Label targetPathLabel;

    private Symlink symlink;

    public SymlinkInfoController(final PluginTaskFactory pluginTaskFactory, final ApplicationDefaults applicationDefaults,
                                 final ApplicationPreferences applicationPreferences, final TelemetryService telemetryService,
                                 final DialogManager dialogManager) {
        super(applicationDefaults, applicationPreferences, telemetryService, dialogManager);
        this.pluginTaskFactory = pluginTaskFactory;
    }

    /**
     * FXML Initialize.
     */
    public void initialize() {
        openLinkButton.setGraphic(new ImageView(this.getApplicationDefaults().symlinkImage));
        openLinkButton.setOnAction(e -> openFromDesktop(symlink.getPath()));

        openTargetButton.setGraphic(new ImageView(this.getApplicationDefaults().directoryImage));
        openTargetButton.setOnAction(e -> openFromDesktop(symlink.getTargetPath()));

        pluginDirectoryListView.setCellFactory(new PluginListCellFactory(this.getApplicationDefaults()));

        deleteLinkButton.setOnAction(e -> {
            Dialog dialog = this.getDialogManager().newDialog();
            DialogLayout layout = new DialogLayout();

            layout.setHeading(new Label("Remove directory"));
            layout.setBody(new Label("Do you really want to delete link " + symlink.getName()
                    + " ? Content will NOT be removed from the target folder."));

            Button cancelButton = new Button("Cancel");

            cancelButton.setOnAction(cancelEvent -> dialog.close());

            Button removeButton = new Button("Delete");
            removeButton.setOnAction(removeEvent -> {
                dialog.close();
                pluginTaskFactory.create(new SymlinkRemoveTask(symlink))
                        .setOnSucceeded(x -> pluginTaskFactory.createPluginSyncTask().scheduleNow()).schedule();
            });
            removeButton.getStyleClass().add("button-danger");

            layout.setActions(removeButton, cancelButton);
            dialog.setContent(layout);
            dialog.show();
        });
    }

    public void setSymlink(Symlink symlink) {
        this.symlink = symlink;
        directoryPathLabel.setText(symlink.getPath());
        pluginDirectoryListView.getItems().setAll(symlink.getPluginList());
        targetPathLabel.setText(Optional.ofNullable(symlink.getTargetPath()).orElse("Unknown"));

        openTargetButton.setVisible(symlink.getTargetPath() != null);
    }

}
