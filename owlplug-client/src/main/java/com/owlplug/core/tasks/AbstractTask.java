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

package com.owlplug.core.tasks;

import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;

@NoArgsConstructor
public abstract class AbstractTask extends Task<TaskResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTask.class);

    @Getter
    @Setter
    private String name = "OwlPlug task";

    @Getter
    private Instant taskStarted;

    @Getter
    private Instant taskCompleted;

    private double maxProgress = 1;
    private double committedProgress = 0;

    public AbstractTask(final String name) {
        this.name = name;
    }

    @Override
    public TaskResult call() throws Exception {
        taskStarted = Instant.now();
        TaskResult result = start();
        taskCompleted = Instant.now();

        Duration elapsed = Duration.between(taskStarted, taskCompleted);
        LOGGER.info("Task {} completed in {}m{}s.", name, elapsed.toMinutes(), elapsed.toSecondsPart());
        return result;
    }

    protected abstract TaskResult start() throws Exception;

    protected void commitProgress(double progress) {
        committedProgress += progress;
        updateProgress(committedProgress, getMaxProgress());
    }

    protected double getCommittedProgress() {
        return committedProgress;
    }

    protected double getMaxProgress() {
        return maxProgress;
    }

    protected void setMaxProgress(double maxProgress) {
        this.maxProgress = maxProgress;
    }

    protected void computeTotalProgress(double progress) {
        updateProgress(committedProgress + progress, maxProgress);
    }

    protected TaskResult success() {
        return new TaskResult();
    }

    @Override
    protected void updateMessage(String message) {
        LOGGER.trace("Task {} status update [{}]", name, message);
        super.updateMessage(message);
    }

    @Override
    public String toString() {
        String prefix = "W";
        if (isRunning()) {
            prefix = "R";
        }
        if (isDone()) {
            prefix = "D";
        }
        if (getState().equals(Worker.State.FAILED)) {
            prefix = "F";
        }
        return "%s - %s".formatted(prefix, getName());
    }
}
