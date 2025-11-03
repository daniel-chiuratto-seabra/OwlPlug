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

import com.owlplug.core.tasks.AbstractTask;
import com.owlplug.core.tasks.SimpleEventListener;
import com.owlplug.core.tasks.TaskExecutionContext;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

/**
 * A base factory class for creating and managing task execution contexts.
 * This class serves as the foundation for specialized task factories that create
 * domain-specific tasks (plugin sync, file operations, remote operations, etc.).
 *
 * <p>The factory pattern is used here to:
 * <ul>
 *   <li>Encapsulate task creation and configuration logic</li>
 *   <li>Provide a consistent interface for scheduling tasks</li>
 *   <li>Separate task construction from task execution</li>
 *   <li>Enable specialized factories to add domain-specific behavior</li>
 * </ul>
 *
 * <p>This class is designed to be extended by specialized factories such as:
 * <ul>
 *   <li>{@code PluginTaskFactory} - Creates plugin-related tasks (sync, remove, etc.)</li>
 *   <li>{@code ExploreTaskFactory} - Creates store/registry tasks (sync sources, install packages)</li>
 *   <li>{@code ProjectTaskFactory} - Creates project-related tasks (DAW project sync)</li>
 * </ul>
 *
 * <p>Typical usage pattern:
 * <pre>{@code
 * // In a specialized factory (e.g., PluginTaskFactory)
 * public TaskExecutionContext createPluginSyncTask() {
 *     PluginSyncTask task = new PluginSyncTask(dependencies...);
 *     TaskExecutionContext context = create(task);
 *     context.setOnSucceeded(event -> notifyListeners(syncListeners));
 *     return context;
 * }
 *
 * // In a controller or service
 * TaskExecutionContext context = taskFactory.createPluginSyncTask();
 * context.schedule(); // or scheduleNow() for priority execution
 * }</pre>
 *
 * <p>The {@link TaskExecutionContext} returned by this factory provides methods to:
 * <ul>
 *   <li>Schedule the task normally ({@code schedule()})</li>
 *   <li>Schedule with priority ({@code scheduleNow()})</li>
 *   <li>Attach event handlers ({@code setOnSucceeded()}, {@code setOnFailed()}, etc.)</li>
 * </ul>
 *
 * @see TaskExecutionContext
 * @see AbstractTask
 * @see TaskRunner
 */
@RequiredArgsConstructor
public class BaseTaskFactory {

    /**
     * The task runner responsible for executing tasks asynchronously.
     * This runner manages the task queue and thread pool for background execution.
     * Injected via Lombok's {@code @RequiredArgsConstructor}.
     */
    private final TaskRunner taskRunner;

    /**
     * Creates a task execution context for the given task.
     * This is the primary public method for creating tasks, delegating to
     * {@link #buildContext(AbstractTask)} which can be overridden by subclasses.
     *
     * <p>The returned context wraps the task and provides methods for scheduling
     * and attaching event handlers without directly exposing the task runner.
     *
     * <p>Example usage:
     * <pre>{@code
     * AbstractTask myTask = new MyTask();
     * TaskExecutionContext context = taskFactory.create(myTask);
     * context.setOnSucceeded(event -> System.out.println("Task completed!"));
     * context.schedule();
     * }</pre>
     *
     * @param task the task to be executed, must extend {@link AbstractTask}
     * @return a {@link TaskExecutionContext} that can be used to schedule and manage the task
     * @throws NullPointerException if a task is null
     */
    public TaskExecutionContext create(final AbstractTask task) {
        return buildContext(task);
    }

    /**
     * Builds a task execution context for the given task.
     * This method creates the context that associates the task with the task runner,
     * enabling task scheduling and execution management.
     *
     * <p>This method is protected to allow subclasses to override the context creation
     * process if they need to add additional configuration or wrap the task in a
     * specialized context.
     *
     * <p>Subclasses can override this to:
     * <ul>
     *   <li>Add default event handlers</li>
     *   <li>Configure task properties before wrapping</li>
     *   <li>Return specialized context subclasses</li>
     *   <li>Add logging or monitoring hooks</li>
     * </ul>
     *
     * @param task the task to wrap in an execution context
     * @return a new {@link TaskExecutionContext} for the given task
     * @throws NullPointerException if a task is null
     */
    protected TaskExecutionContext buildContext(final AbstractTask task) {
        // Create a new context that associates the task with the task runner
        return new TaskExecutionContext(task, taskRunner);
    }

    /**
     * Notifies all listeners in the collection by invoking their {@code onAction()} method.
     * This utility method is provided for subclasses to trigger event notifications
     * after task completion or other significant events.
     *
     * <p>This is particularly useful for:
     * <ul>
     *   <li>Notifying UI components to refresh after task completion</li>
     *   <li>Triggering cascade operations (e.g., sync plugins → refresh UI → check for updates)</li>
     *   <li>Implementing observer pattern for task events</li>
     *   <li>Coordinating multiple components that react to task results</li>
     * </ul>
     *
     * <p>Example usage in a subclass:
     * <pre>{@code
     * // After a plugin sync task completes
     * public TaskExecutionContext createPluginSyncTask() {
     *     PluginSyncTask task = new PluginSyncTask(...);
     *     TaskExecutionContext context = create(task);
     *     context.setOnSucceeded(event -> {
     *         notifyListeners(syncListeners); // Refresh plugin views
     *     });
     *     return context;
     * }
     * }</pre>
     *
     * <p>All listeners are notified sequentially in the order they appear in the collection.
     * If a listener throws an exception, it is propagated and remaining listeners may not be notified.
     *
     * @param listeners the collection of listeners to notify must not be null
     * @throws NullPointerException if listeners are null
     * @see SimpleEventListener
     */
    protected void notifyListeners(final Collection<SimpleEventListener> listeners) {
        // Iterate through all listeners and trigger their onAction callback
        listeners.forEach(SimpleEventListener::onAction);
    }

}
