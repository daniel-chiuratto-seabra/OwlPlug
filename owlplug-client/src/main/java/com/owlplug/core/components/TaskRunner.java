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

package com.owlplug.core.components;

import com.owlplug.core.controllers.TaskBarController;
import com.owlplug.core.tasks.AbstractTask;
import com.owlplug.core.tasks.TaskResult;
import com.owlplug.core.utils.StringUtils;
import javafx.concurrent.Worker.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Supplier;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static javafx.application.Platform.runLater;

/**
 * This class stores and executes submitted tasks one by one. Each pending task
 * is stored before execution. The runner dispatches execution information to
 * the TaskBarController bean.
 */
@Component
public class TaskRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskRunner.class);

    private final TaskBarController taskBarController;
    private final SimpleAsyncTaskExecutor exec;
    private final LinkedBlockingDeque<AbstractTask> taskQueue;
    private final List<AbstractTask> taskHistory;

    private AbstractTask currentTask = null;

    public TaskRunner(final TaskBarController taskBarController) {
        this.taskBarController = taskBarController;

        exec = new SimpleAsyncTaskExecutor();
        taskQueue = new LinkedBlockingDeque<>();
        taskHistory = new ArrayList<>();
    }

    /**
     * Submit a task at the end of the executor queue.
     *
     * @param task - the task to submit
     */
    public void submitTask(AbstractTask task) {
        LOGGER.debug("Task submitted to queue - {} ", task.getClass().getName());
        taskQueue.addLast(task);
        scheduleNext();
    }

    /**
     * Submit a task in front of the executor queue.
     *
     * @param task - the task to submit
     */
    public void submitTaskOnQueueHead(AbstractTask task) {
        LOGGER.debug("Task submitted on queue head - {} ", task.getClass().getName());
        taskQueue.addFirst(task);
        scheduleNext();
    }

    /**
     * Refresh the task runner by submitting the next pending task for execution.
     */
    private synchronized void scheduleNext() {
        if (!taskQueue.isEmpty() && currentTask == null) {
            disableError();
            taskBarController.resetErrorLog();
            // Get the next pending task
            AbstractTask polledTask = taskQueue.pollFirst();
            setCurrentTask(polledTask);
            addInTaskHistory(currentTask);
            LOGGER.debug("Task submitted to executor - {} ", currentTask.getClass().getName());
            supplyAsync(currentTaskCall(), exec).whenComplete((result, ex) -> {
                if (ex != null) {
                    Throwable cause = ex.getCause();
                    LOGGER.error("Error while running task", cause);
                    runLater(() -> {
                        taskBarController.setErrorLog(currentTask, cause.getMessage(), cause.toString());
                        removeCurrentTask();
                        scheduleNext();
                    });
                } else {
                    runLater(() -> {
                        if (currentTask.getState() == State.FAILED) {
                            if (currentTask.getException() != null) {
                                LOGGER.error("Error while running task", currentTask.getException());
                                taskBarController.setErrorLog(currentTask, currentTask.getException().getMessage(),
                                        StringUtils.getStackTraceAsString(currentTask.getException()));
                            }
                            triggerOnError();
                        }
                        removeCurrentTask();
                        scheduleNext();
                    });
                }
            });
        }
    }

    private Supplier<TaskResult> currentTaskCall() {
        return () -> {
            try {
                return currentTask.call();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        };
    }

    private void setCurrentTask(AbstractTask task) {
        currentTask = task;
        // Bind progress indicators
        taskBarController.taskProgressBar.progressProperty().bind(currentTask.progressProperty());
        taskBarController.taskLabel.textProperty().bind(currentTask.messageProperty());
    }

    private void removeCurrentTask() {
        // Unbind progress indicators
        taskBarController.taskProgressBar.progressProperty().unbind();
        taskBarController.taskLabel.textProperty().unbind();

        currentTask = null;
    }

    private void addInTaskHistory(AbstractTask task) {
        if (taskHistory.size() >= 10) {
            taskHistory.removeFirst();
        }
        taskHistory.add(task);
    }

    public void close() {
        taskQueue.clear();
        AbstractTask pendingTask = currentTask;
        removeCurrentTask();
        if (pendingTask != null) {
            pendingTask.cancel();
        }
    }

    public List<AbstractTask> getPendingTasks() {
        return new ArrayList<>(taskQueue);
    }

    public List<AbstractTask> getTaskHistory() {
        return new ArrayList<>(taskHistory);
    }

    private void triggerOnError() {
        taskBarController.taskProgressBar.getStyleClass().add("progress-bar-error");
    }

    public void disableError() {
        taskBarController.taskProgressBar.getStyleClass().remove("progress-bar-error");
    }

}
