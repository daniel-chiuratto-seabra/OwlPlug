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
import com.owlplug.plugin.model.Plugin;
import com.owlplug.plugin.model.PluginComponent;
import com.owlplug.plugin.model.PluginDirectory;
import com.owlplug.plugin.model.Symlink;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.ResourceBundle;

@Controller
public class NodeInfoController extends BaseController implements Initializable {

    private final PluginInfoController pluginInfoController;
    private final DirectoryInfoController directoryInfoController;
    private final SymlinkInfoController symlinkInfoController;
    private final ComponentInfoController componentInfoController;

    @FXML private Node pluginInfoView;
    @FXML private Node directoryInfoView;
    @FXML private Node symlinkInfoView;
    @FXML private Node componentInfoView;

    public NodeInfoController(final ApplicationDefaults applicationDefaults, final ApplicationPreferences applicationPreferences,
                              final TelemetryService telemetryService, final DialogManager dialogManager,
                              final PluginInfoController pluginInfoController, final DirectoryInfoController directoryInfoController,
                              final SymlinkInfoController symlinkInfoController, final ComponentInfoController componentInfoController) {
        super(applicationDefaults, applicationPreferences, telemetryService, dialogManager);
        this.pluginInfoController = pluginInfoController;
        this.directoryInfoController = directoryInfoController;
        this.symlinkInfoController = symlinkInfoController;
        this.componentInfoController = componentInfoController;
    }

    /**
     * FXML initialize.
     */
    @Override
    public void initialize(final URL location, final ResourceBundle resourceBundle) {
        pluginInfoView.setVisible(false);
        directoryInfoView.setVisible(false);
        symlinkInfoView.setVisible(false);
        componentInfoView.setVisible(false);
    }

    public void setNode(Object node) {
        pluginInfoView.setVisible(false);
        directoryInfoView.setVisible(false);
        symlinkInfoView.setVisible(false);
        componentInfoView.setVisible(false);

        if (node instanceof Plugin) {
            pluginInfoController.setPlugin((Plugin) node);
            pluginInfoView.setVisible(true);
        }
        if (node instanceof PluginDirectory) {
            directoryInfoController.setPluginDirectory((PluginDirectory) node);
            directoryInfoView.setVisible(true);
        }
        if (node instanceof Symlink) {
            symlinkInfoController.setSymlink((Symlink) node);
            symlinkInfoView.setVisible(true);
        }
        if (node instanceof PluginComponent) {
            componentInfoController.setComponent((PluginComponent) node);
            componentInfoView.setVisible(true);
        }
    }

}
