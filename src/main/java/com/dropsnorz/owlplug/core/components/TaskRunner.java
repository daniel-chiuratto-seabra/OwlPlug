package com.dropsnorz.owlplug.core.components;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.dropsnorz.owlplug.core.controllers.TaskBarController;
import com.dropsnorz.owlplug.core.engine.tasks.AbstractTask;
import com.dropsnorz.owlplug.core.engine.tasks.TaskResult;

import javafx.application.Platform;


/**
 * This class stores and executes submited tasks one by one.
 * Each pending tasks is stored before execution.
 * The runner dispatches execution information to the TaskBarController bean.
 *
 */
@Service
public class TaskRunner {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	TaskBarController taskBarController;
	
	AsyncListenableTaskExecutor  exec;
	private CopyOnWriteArrayList<AbstractTask> pendingTasks;
	private AbstractTask currentTask= null;

	TaskRunner(){
		
		exec = new SimpleAsyncTaskExecutor();
		pendingTasks = new CopyOnWriteArrayList<AbstractTask>();
		
	}

	public void submitTask(AbstractTask task) {

		log.debug("Task submited to queue - {} ", task.getClass().getName());
		pendingTasks.add(task);
		refresh(false);
		
	}

	/**
	 * Refresh the task runner by submitting the next pending task for execution
	 * @param deleteCurrentTask true if the current running task should be deleted
	 */
	private synchronized void refresh(boolean deleteCurrentTask) {

		if(deleteCurrentTask) {
			log.debug("Remove task from queue - {}", currentTask.getClass().getName());
			pendingTasks.remove(currentTask);
			currentTask = null;
			//Unbind progress indicators
			taskBarController.taskProgressBar.progressProperty().unbind();
			taskBarController.taskLabel.textProperty().unbind();
		}

		if(!pendingTasks.isEmpty() && currentTask == null) {
			disableError();
			//Get the next pending task
			this.currentTask = pendingTasks.get(0);
			log.debug("Task submitted to executor - {} ", currentTask.getClass().getName());
			//Bind progress indicators
			taskBarController.taskProgressBar.progressProperty().bind(currentTask.progressProperty());
			taskBarController.taskLabel.textProperty().bind(currentTask.messageProperty());
						
			ListenableFuture<TaskResult> future = (ListenableFuture<TaskResult>) exec.submitListenable(currentTask);

			future.addCallback(new ListenableFutureCallback<TaskResult>() {

				@Override
				public void onSuccess(TaskResult result) {
					Platform.runLater(() -> {
							refresh(true);
					});
				}

				@Override
				public void onFailure(Throwable ex) {
					Platform.runLater(() -> {
							refresh(true);
					});

				}});
		}
	}

	public List<AbstractTask> getPendingTasks() {
		return Collections.unmodifiableList(pendingTasks);
	}
	
	public void triggerOnError() {
		taskBarController.taskProgressBar.getStyleClass().add("progress-bar-error");

	}
	
	public void disableError() {
		taskBarController.taskProgressBar.getStyleClass().remove("progress-bar-error");

	}


}