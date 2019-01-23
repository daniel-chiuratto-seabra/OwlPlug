package com.owlplug.core.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListCell;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXPopup;
import com.jfoenix.controls.JFXPopup.PopupHPosition;
import com.jfoenix.controls.JFXPopup.PopupVPosition;
import com.owlplug.core.components.ApplicationDefaults;
import com.owlplug.core.components.TaskRunner;
import com.owlplug.core.tasks.AbstractTask;
import java.util.ArrayList;
import javafx.concurrent.Worker.State;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.util.Callback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class TaskBarController {

  @Autowired
  private TaskRunner taskRunner;
  @Autowired
  private ApplicationDefaults applicationDefaults;

  @FXML
  public Label taskLabel;
  @FXML
  public ProgressBar taskProgressBar;
  @FXML
  private JFXButton taskHistoryButton;

  /**
   * FXML initialize.
   */
  public void initialize() {

    taskHistoryButton.setOnAction(e -> openTaskHistory());
  }

  private void openTaskHistory() {

    if (!taskRunner.getTaskHistory().isEmpty()) {
      JFXListView<AbstractTask> list = new JFXListView<>();
      list.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

      ArrayList<AbstractTask> tasks = new ArrayList<>(taskRunner.getTaskHistory());
      tasks.addAll(taskRunner.getPendingTasks());
      list.getItems().addAll(tasks);

      list.setCellFactory(new Callback<ListView<AbstractTask>, ListCell<AbstractTask>>() {
        @Override
        public ListCell<AbstractTask> call(ListView<AbstractTask> param) {
          return new JFXListCell<AbstractTask>() {
            @Override
            public void updateItem(AbstractTask item, boolean empty) {
              super.updateItem(item, empty);
              if (item != null && !empty) {
                Image icon = applicationDefaults.taskPendingImage;
                if (item.isRunning()) {
                  icon = applicationDefaults.taskRunningImage;
                } else if (item.isDone()) {
                  icon = applicationDefaults.taskSuccessImage;
                }
                if (item.getState().equals(State.FAILED)) {
                  icon = applicationDefaults.taskFailImage;
                }
                ImageView imageView = new ImageView(icon);
                setGraphic(imageView);
                setText(item.getName());
              }
            }
          };
        }
      });

      JFXPopup popup = new JFXPopup(list);
      popup.show(taskHistoryButton, PopupVPosition.BOTTOM, PopupHPosition.RIGHT);
    }

  }

}