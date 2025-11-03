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

package com.owlplug.core.controllers;

import com.owlplug.controls.Popup;
import com.owlplug.core.components.ApplicationDefaults;
import com.owlplug.core.components.ApplicationPreferences;
import com.owlplug.core.components.DialogManager;
import com.owlplug.core.components.TaskRunner;
import com.owlplug.core.services.TelemetryService;
import com.owlplug.core.tasks.AbstractTask;
import javafx.concurrent.Worker.State;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;

@Controller
public class TaskBarController extends BaseController {

    private final TaskRunner taskRunner;

    @FXML
    public Label taskLabel;
    @FXML
    public ProgressBar taskProgressBar;
    @FXML
    private Button taskHistoryButton;
    @FXML
    private Button logsButton;

    public TaskBarController(final ApplicationDefaults applicationDefaults, final ApplicationPreferences applicationPreferences,
                             final TelemetryService telemetryService, final DialogManager dialogManager, @Lazy final TaskRunner taskRunner) {
        super(applicationDefaults, applicationPreferences, telemetryService, dialogManager);
        this.taskRunner = taskRunner;
    }

    /**
     * FXML initialize.
     */
    public void initialize() {
        taskHistoryButton.setOnAction(e -> openTaskHistory());
        resetErrorLog();
    }

    public void setErrorLog(AbstractTask task, String title, String content) {

        getTelemetryService().event("/Error/TaskExecution", p -> {
            p.put("taskName", task.getName());
            p.put("error", title);
            p.put("content", content);
        });
        logsButton.setVisible(true);
        logsButton.setManaged(true);
        logsButton.setOnAction(e -> {
            showErrorDialog(title, content);
        });
    }

    public void resetErrorLog() {
        logsButton.setManaged(false);
        logsButton.setVisible(false);
    }

    private void openTaskHistory() {
        if (!taskRunner.getTaskHistory().isEmpty()) {
            ListView<AbstractTask> list = new ListView<>();
            list.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

            ArrayList<AbstractTask> tasks = new ArrayList<>(taskRunner.getTaskHistory());
            tasks.addAll(taskRunner.getPendingTasks());

            list.getItems().addAll(tasks);
            list.setCellFactory((ListView<AbstractTask> param) -> getCellFactory());

            Popup popup = new Popup(list);
            popup.show(taskHistoryButton, Popup.PopupVPosition.BOTTOM, Popup.PopupHPosition.RIGHT);
        }

    }

    private ListCell<AbstractTask> getCellFactory() {
        return new ListCell<>() {
            @Override
            public void updateItem(final AbstractTask item, final boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    Image icon = getApplicationDefaults().taskPendingImage;
                    if (item.isRunning()) {
                        icon = getApplicationDefaults().taskRunningImage;
                    } else if (item.isDone()) {
                        icon = getApplicationDefaults().taskSuccessImage;
                    }
                    if (item.getState().equals(State.FAILED)) {
                        icon = getApplicationDefaults().taskFailImage;
                        setOnMouseClicked(e -> {
                            showErrorDialog(item.getException().getMessage(), item.getException().toString());
                        });
                    }
                    ImageView imageView = new ImageView(icon);
                    setGraphic(imageView);
                    setText(item.getName());
                }
            }
        };
    }

    private void showErrorDialog(String title, String content) {
        getDialogManager().newSimpleInfoDialog(new Label(title), new TextArea(content)).show();
    }
}
