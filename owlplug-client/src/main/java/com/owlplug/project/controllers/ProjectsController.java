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
import com.owlplug.core.services.TelemetryService;
import com.owlplug.core.ui.FilterableTreeItem;
import com.owlplug.plugin.controllers.dialogs.ListDirectoryDialogController;
import com.owlplug.project.components.ProjectTaskFactory;
import com.owlplug.project.model.DawProject;
import com.owlplug.project.services.ProjectService;
import com.owlplug.project.ui.ProjectTreeCell;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeView;
import jfxtras.styles.jmetro.JMetroStyleClass;
import org.springframework.stereotype.Controller;

import java.util.concurrent.Callable;
import java.util.function.Predicate;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Controller
public class ProjectsController extends BaseController {

    private final ProjectService projectService;
    private final ProjectTaskFactory projectTaskFactory;
    private final ListDirectoryDialogController listDirectoryDialogController;
    private final ProjectInfoController projectInfoController;

    @FXML private Button syncProjectButton;
    @FXML private Button projectDirectoryButton;
    @FXML private TextField searchTextField;
    @FXML private TabPane projectTreeViewTabPane;
    @FXML private TreeView<DawProject> projectTreeView;

    private FilterableTreeItem<DawProject> projectTreeNode;

    public ProjectsController(final ApplicationDefaults applicationDefaults, final ApplicationPreferences applicationPreferences,
                              final TelemetryService telemetryService, final DialogManager dialogManager, final ProjectService projectService,
                              final ProjectTaskFactory projectTaskFactory, final ListDirectoryDialogController listDirectoryDialogController,
                              final ProjectInfoController projectInfoController) {
        super(applicationDefaults, applicationPreferences, telemetryService, dialogManager);
        this.projectService = projectService;
        this.projectTaskFactory = projectTaskFactory;
        this.listDirectoryDialogController = listDirectoryDialogController;
        this.projectInfoController = projectInfoController;
    }

    @FXML
    public void initialize() {
        syncProjectButton.setOnAction(e -> {
            getTelemetryService().event("/Projects/Scan");
            projectService.syncProjects();
        });

        projectTaskFactory.addSyncProjectsListener(this::refresh);

        projectTreeViewTabPane.getStyleClass().add(JMetroStyleClass.UNDERLINE_TAB_PANE);

        projectTreeView.setCellFactory(p -> new ProjectTreeCell(getApplicationDefaults()));

        projectTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                projectInfoController.setProject(newValue.getValue());
            }
        });

        projectDirectoryButton.setOnAction(actionEvent -> {
            listDirectoryDialogController.configure(ApplicationDefaults.PROJECT_DIRECTORY_KEY);
            listDirectoryDialogController.show();
        });

        projectTreeNode = new FilterableTreeItem<>(null); // Root node value is not displayed
        projectTreeView.setRoot(projectTreeNode);
        projectTreeView.setShowRoot(false); // Hide the root to show a list of projects

        // Binds search property to plugin tree filter
        projectTreeNode.predicateProperty().bind(Bindings.createObjectBinding(searchFunction(), searchTextField.textProperty()));

        refresh();
    }

    public void refresh() {
        projectTreeNode.getInternalChildren().clear();
        projectService.getAllProjects().forEach(project -> projectTreeNode.getInternalChildren().add(new FilterableTreeItem<>(project)));
        projectTreeNode.setExpanded(true);
    }

    private Callable<Predicate<DawProject>> searchFunction() {
        return () -> {
            final String searchText = searchTextField.getText();
            if (isBlank(searchText)) {
                return null;
            }
            return (project) -> project.getName().toLowerCase().contains(searchText.toLowerCase());
        };
    }
}
