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

package com.owlplug.project.controllers;

import com.owlplug.core.components.ApplicationDefaults;
import com.owlplug.core.components.ApplicationPreferences;
import com.owlplug.core.components.DialogManager;
import com.owlplug.core.controllers.BaseController;
import com.owlplug.core.controllers.MainController;
import com.owlplug.core.services.TelemetryService;
import com.owlplug.core.utils.PlatformUtils;
import com.owlplug.core.utils.TimeUtils;
import com.owlplug.plugin.controllers.PluginsController;
import com.owlplug.plugin.model.Plugin;
import com.owlplug.plugin.model.PluginFormat;
import com.owlplug.project.model.DawPlugin;
import com.owlplug.project.model.DawProject;
import com.owlplug.project.model.LookupResult;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Controller;

import java.io.File;

import static com.owlplug.project.controllers.common.TableColumnUtils.cellFactory;

/**
 * A JavaFX and Spring {@link Controller} for displaying detailed information about a selected {@link DawProject}.
 * <p>
 * This controller manages the "Project Info" pane, which becomes visible when a user selects a project
 * from the projects list. It is responsible for populating the UI with the project's metadata and the
 * plugins it uses.
 *
 * <h3>Responsibilities:</h3>
 * <ul>
 *     <li><b>Displaying Project Metadata:</b> Shows information such as the project's name, the Digital Audio Workstation (DAW)
 *     it was created with, creation and modification dates, and its file path.</li>
 *     <li><b>Listing Used Plugins:</b> Populates a {@link TableView} with the plugins ({@link DawPlugin}) found in the project file.
 *     For each plugin, it displays its name, format, and lookup status (e.g., "Found", "Missing").</li>
 *     <li><b>User Actions:</b>
 *         <ul>
 *             <li>Provides a button to open the project file in its default associated application.</li>
 *             <li>Provides a button to open the directory containing the project file.</li>
 *             <li>Includes a hyperlink for each "Found" plugin, allowing the user to navigate directly to that
 *             plugin's details in the main plugins view.</li>
 *         </ul>
 *     </li>
 *     <li><b>Data Population:</b> The {@link #setProject(DawProject)} method is the primary entry point for updating the
 *     view with the data of a new {@link DawProject}.</li>
 * </ul>
 */
@Controller
public class ProjectInfoController extends BaseController {

    @FXML private VBox projectInfoPane;
    @FXML private Label projectNameLabel;
    @FXML private ImageView projectAppImageView;
    @FXML private Label projectAppLabel;
    @FXML private Button projectOpenButton;
    @FXML private Label appFullNameLabel;
    @FXML private Label projectFormatVersionLabel;
    @FXML private Label projectCreatedLabel;
    @FXML private Label projectLastModifiedLabel;
    @FXML private Label projectPluginsFoundLabel;
    @FXML private Label projectPathLabel;
    @FXML private Button openDirectoryButton;
    @FXML private TableView<DawPlugin> pluginTable;
    @FXML private TableColumn<DawPlugin, PluginFormat> pluginTableFormatColumn;
    @FXML private TableColumn<DawPlugin, String> pluginTableNameColumn;
    @FXML private TableColumn<DawPlugin, String> pluginTableStatusColumn;
    @FXML private TableColumn<DawPlugin, Plugin> pluginTableLinkColumn;

    private DawProject currentProject = null;

    private final PluginsController pluginsController;
    private final MainController mainController;

    /**
     * Constructs the controller and injects its dependencies via Spring.
     *
     * @param applicationDefaults    Provides application-wide default values and resources, such as icons.
     * @param applicationPreferences Manages user-specific settings.
     * @param telemetryService       Handles the collection of anonymous usage data.
     * @param dialogManager          Manages the display of dialogs and alerts.
     * @param pluginsController      The controller for the main plugins view, used to select a plugin.
     * @param mainController         The main application controller, used to switch between primary tabs.
     */
    public ProjectInfoController(final ApplicationDefaults applicationDefaults, final ApplicationPreferences applicationPreferences,
                                 final TelemetryService telemetryService, final DialogManager dialogManager,
                                 final PluginsController pluginsController, final MainController mainController) {
        super(applicationDefaults, applicationPreferences, telemetryService, dialogManager);
        this.pluginsController = pluginsController;
        this.mainController = mainController;
    }

    /**
     * Initializes the controller after its root element has been completely processed.
     * This method is automatically called by the FXMLLoader.
     * <p>
     * It sets up event handlers for buttons, configures the table columns with custom cell factories
     * for data binding and rendering, and sets the initial visibility of the info pane to false.
     */
    @FXML
    public void initialize() {
        openDirectoryButton.setOnAction(e -> {
            File projectFile = new File(projectPathLabel.getText());
            PlatformUtils.openFromDesktop(projectFile.getParentFile());
        });

        projectOpenButton.setOnAction(e -> {
            if (currentProject != null) {
                PlatformUtils.openFromDesktop(currentProject.getPath());
                // Disable to prevent opening the project several times.
                projectOpenButton.setDisable(true);
            }
        });

        // Set invisible by default if no project is selected.
        projectInfoPane.setVisible(false);

        pluginTableNameColumn.setCellValueFactory(cellData -> {
            return new SimpleStringProperty(cellData.getValue().getName());
        });
        pluginTableStatusColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getLookup() != null
                    && cellData.getValue().getLookup().getResult() != null) {
                return new SimpleStringProperty(cellData.getValue().getLookup().getResult().getValue());
            }
            return new SimpleStringProperty("Unknown");
        });
        pluginTableFormatColumn.setCellValueFactory(cellData -> {
            return new SimpleObjectProperty<>(cellData.getValue().getFormat());
        });

        pluginTableStatusColumn.setCellFactory(e -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                this.getStyleClass().remove("cell-unknown-link");
                this.getStyleClass().remove("cell-missing-link");
                this.getStyleClass().remove("cell-found-link");
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(item);
                    if (item.equals(LookupResult.MISSING.getValue())) {
                        this.getStyleClass().add("cell-missing-link");
                    } else if (item.equals(LookupResult.FOUND.getValue())) {
                        this.getStyleClass().add("cell-found-link");
                    } else {
                        this.getStyleClass().add("cell-unknown-link");
                    }
                }
            }
        });

        pluginTableLinkColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getLookup() != null) {
                return new SimpleObjectProperty<>(cellData.getValue().getLookup().getPlugin());
            }
            return null;
        });

        pluginTableLinkColumn.setCellFactory(e -> new TableCell<>() {
            @Override
            public void updateItem(Plugin item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    final var hyperlink = new Hyperlink();
                    hyperlink.setGraphic(new ImageView(getApplicationDefaults().linkIconImage));
                    hyperlink.setOnAction(e -> {
                        pluginsController.selectPluginById(item.getId());
                        mainController.selectMainTab(MainController.PLUGINS_TAB_INDEX);
                    });
                    setGraphic(hyperlink);
                }
            }
        });

        pluginTableFormatColumn.setCellFactory(tableColumn -> cellFactory(tableColumn, getApplicationDefaults()));
    }

    /**
     * Populates the project information pane with the details of the specified {@link DawProject}.
     * <p>
     * This method updates all UI elements, including labels, images, and the plugin table,
     * to reflect the data of the given project. It also makes the info pane visible.
     *
     * @param project The {@link DawProject} to display. Must not be null.
     */
    public void setProject(final DawProject project) {
        currentProject = project;
        projectInfoPane.setVisible(true);
        projectNameLabel.setText(project.getName());
        projectAppLabel.setText(project.getApplication().getName());
        projectAppImageView.setImage(this.getApplicationDefaults().getDAWApplicationIcon(project.getApplication()));
        projectOpenButton.setDisable(false);
        appFullNameLabel.setText(project.getAppFullName());
        projectCreatedLabel.setText(TimeUtils.getHumanReadableDurationFrom(project.getCreatedAt()));
        projectLastModifiedLabel.setText(TimeUtils.getHumanReadableDurationFrom(project.getLastModifiedAt()));
        projectPluginsFoundLabel.setText(String.valueOf(project.getPlugins().size()));
        projectFormatVersionLabel.setText("v" + project.getFormatVersion());
        projectPathLabel.setText(project.getPath());

        pluginTable.setItems(FXCollections.observableList(project.getPlugins().stream().toList()));
    }

}
