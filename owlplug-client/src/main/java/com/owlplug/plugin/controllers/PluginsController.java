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
import com.owlplug.plugin.components.PluginTaskFactory;
import com.owlplug.plugin.controllers.dialogs.ExportDialogController;
import com.owlplug.plugin.controllers.dialogs.NewLinkController;
import com.owlplug.plugin.model.Plugin;
import com.owlplug.plugin.repositories.PluginRepository;
import com.owlplug.plugin.services.PluginService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.ResourceBundle;

import static com.owlplug.core.components.ApplicationDefaults.PLUGIN_PREFERRED_DISPLAY_KEY;
import static com.owlplug.plugin.controllers.PluginTreeViewController.Display.DirectoryTree;
import static com.owlplug.plugin.controllers.PluginTreeViewController.Display.FlatTree;
import static jfxtras.styles.jmetro.JMetroStyleClass.UNDERLINE_TAB_PANE;

@Controller
public class PluginsController extends BaseController implements Initializable {

    private final PluginService pluginService;
    private final PluginRepository pluginRepository;
    private final NodeInfoController nodeInfoController;
    private final NewLinkController newLinkController;
    private final ExportDialogController exportDialogController;

    protected final PluginTaskFactory pluginTaskFactory;
    protected final PluginTreeViewController pluginTreeViewController;
    protected final PluginTableController tableController;

    @FXML private Button syncButton;
    @FXML private Button exportButton;
    @FXML private TabPane displaySwitchTabPane;
    @FXML private Tab displayListTab;
    @FXML private Tab displayDirectoriesTab;
    @FXML private Tab displayTableTab;

    @FXML private TextField searchTextField;
    @FXML private Button newLinkButton;
    @FXML private VBox pluginInfoPane;
    @FXML private VBox pluginsContainer;

    public PluginsController(final ApplicationDefaults applicationDefaults, final ApplicationPreferences applicationPreferences,
                             final TelemetryService telemetryService, final DialogManager dialogManager, final PluginService pluginService,
                             final PluginRepository pluginRepository, final NodeInfoController nodeInfoController,
                             final NewLinkController newLinkController, final ExportDialogController exportDialogController,
                             final PluginTaskFactory pluginTaskFactory, final PluginTreeViewController pluginTreeViewController,
                             final PluginTableController tableController) {
        super(applicationDefaults, applicationPreferences, telemetryService, dialogManager);
        this.pluginService = pluginService;
        this.pluginRepository = pluginRepository;
        this.nodeInfoController = nodeInfoController;
        this.newLinkController = newLinkController;
        this.exportDialogController = exportDialogController;
        this.pluginTaskFactory = pluginTaskFactory;
        this.pluginTreeViewController = pluginTreeViewController;
        this.tableController = tableController;
    }

    /**
     * FXML initialize method.
     */
    @FXML
    @Override
    public void initialize(final URL location, final ResourceBundle resources) {

        newLinkButton.setOnAction(e -> newLinkController.show());

        // Add Plugin Table and TreeView to the scene graph
        pluginsContainer.getChildren().add(pluginTreeViewController.getTreeView());
        pluginsContainer.getChildren().add(tableController.getTableView());

        /* ===================
         * Plugins TreeView properties init and bindings
         * ===================
         */

        // Dispatches treeView selection event to the nodeInfoController
        pluginTreeViewController.getTreeView().getSelectionModel()
                .selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        nodeInfoController.setNode(newValue.getValue());
                        setInfoPaneDisplay(true);
                    }
                });
        pluginTreeViewController.getTreeView().setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                toggleInfoPaneDisplay();
            }
        });
        pluginTreeViewController.searchProperty().bind(searchTextField.textProperty());

        /* ===================
         * Plugins Table properties init and bindings
         * ===================
         */

        tableController.getTableView().setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                toggleInfoPaneDisplay();
            }
        });
        tableController.getTableView().getSelectionModel()
                .selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        nodeInfoController.setNode(newValue);
                    }
                });

        tableController.searchProperty().bind(searchTextField.textProperty());


        /* ===================
         * Controller and node graph initialization
         * ===================
         */
        displaySwitchTabPane.getStyleClass().add(UNDERLINE_TAB_PANE);

        // Set the default display (flat plugin tree)
        pluginTreeViewController.setDisplayMode(FlatTree);
        pluginTreeViewController.getTreeView().setVisible(true);
        pluginTreeViewController.getTreeView().setManaged(true);
        tableController.getTableView().setManaged(false);
        tableController.getTableView().setVisible(false);
        setInfoPaneDisplay(true);

        // Handles tabPane selection event and toggles displayed treeView
        displaySwitchTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab.equals(displayListTab)) {
                pluginTreeViewController.setDisplayMode(FlatTree);
                pluginTreeViewController.setNodeManaged(true);
                tableController.setNodeManaged(false);
                setInfoPaneDisplay(true);
                getApplicationPreferences().put(PLUGIN_PREFERRED_DISPLAY_KEY, "LIST");
            } else if (newTab.equals(displayDirectoriesTab)) {
                pluginTreeViewController.setDisplayMode(DirectoryTree);
                pluginTreeViewController.setNodeManaged(true);
                tableController.setNodeManaged(false);
                setInfoPaneDisplay(true);
                getApplicationPreferences().put(PLUGIN_PREFERRED_DISPLAY_KEY, "DIRECTORIES");
            } else {
                pluginTreeViewController.setNodeManaged(false);
                tableController.setNodeManaged(true);
                setInfoPaneDisplay(false);
                getApplicationPreferences().put(PLUGIN_PREFERRED_DISPLAY_KEY, "TABLE");
            }
        });

        if (getApplicationPreferences().get(PLUGIN_PREFERRED_DISPLAY_KEY, "").equals("TABLE")) {
            displaySwitchTabPane.getSelectionModel().select(displayTableTab);
        } else if (getApplicationPreferences().get(PLUGIN_PREFERRED_DISPLAY_KEY, "").equals("DIRECTORIES")) {
            displaySwitchTabPane.getSelectionModel().select(displayDirectoriesTab);
        } else {
            displaySwitchTabPane.getSelectionModel().select(displayListTab);
        }

        syncButton.setOnAction(e -> {
            getTelemetryService().event("/Plugins/Scan");
            pluginService.syncPlugins();
        });

        pluginTaskFactory.addSyncPluginsListener(this::displayPlugins);

        exportButton.setOnAction(e -> {
            getTelemetryService().event("/Plugins/Export");
            exportDialogController.show();
        });

        displayPlugins();
    }

    public void displayPlugins() {
        final var plugins = pluginRepository.findAll();
        pluginTreeViewController.setPlugins(plugins);
        tableController.setPlugins(plugins);
    }

    public void selectPluginById(long id) {
        if (displaySwitchTabPane.getSelectionModel().getSelectedItem().equals(displayTableTab)) {
            tableController.selectPluginById(id);
        } else {
            pluginTreeViewController.selectPluginById(id);
        }
    }

    public void refresh() {
        pluginTreeViewController.refresh();
        tableController.refresh();
    }

    public void setInfoPaneDisplay(boolean display) {
        pluginInfoPane.setManaged(display);
        pluginInfoPane.setVisible(display);
    }

    public void toggleInfoPaneDisplay() {
        pluginInfoPane.setManaged(!pluginInfoPane.isManaged());
        pluginInfoPane.setVisible(!pluginInfoPane.isVisible());
    }

}
