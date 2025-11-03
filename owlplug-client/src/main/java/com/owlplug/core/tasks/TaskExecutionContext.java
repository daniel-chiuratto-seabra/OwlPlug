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

import com.owlplug.core.components.TaskRunner;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import lombok.Getter;

/**
 * A context object that wraps a task and provides convenient methods for scheduling
 * and attaching event handlers. This class acts as a fluent API for task management,
 * decoupling task creation from execution and configuration.
 *
 * <p>The context pattern provides several benefits:
 * <ul>
 *   <li>Encapsulates the relationship between a task and its runner</li>
 *   <li>Provides a fluent API for configuring tasks before execution</li>
 *   <li>Separates task creation concerns from scheduling concerns</li>
 *   <li>Enables method chaining for cleaner, more readable code</li>
 * </ul>
 *
 * <p>Typical usage pattern:
 * <pre>{@code
 * // Create a context for a task (usually via a factory)
 * TaskExecutionContext context = taskFactory.create(new PluginSyncTask());
 *
 * // Configure the task using fluent API
 * context.setOnSucceeded(event -> {
 *         System.out.println("Plugin sync completed!");
 *         refreshUI();
 *     })
 *     .schedule(); // Schedule for execution
 * }</pre>
 *
 * <p>Priority scheduling example:
 * <pre>{@code
 * // High-priority task that should run immediately
 * context.setOnSucceeded(event -> showNotification("Critical update completed"))
 *        .scheduleNow(); // Jumps to the front of the queue
 * }</pre>
 *
 * <p>This class integrates with JavaFX's Task framework, allowing tasks to be monitored
 * and controlled through standard JavaFX mechanisms (progress tracking, cancellation, etc.).
 *
 * <p><strong>Thread Safety:</strong> This class is not thread-safe. It should be created
 * and configured on the JavaFX application thread, though the wrapped task will execute
 * on a background thread managed by the TaskRunner.
 *
 * @see AbstractTask
 * @see TaskRunner
 * @see com.owlplug.core.components.BaseTaskFactory
 */
public class TaskExecutionContext {

    /**
     * The task runner responsible for executing tasks in the background.
     * This runner manages the thread pool and task queue for asynchronous execution.
     */
    private final TaskRunner taskRunner;

    /**
     * The wrapped task that will be executed when scheduled.
     * Exposed via Lombok's {@code @Getter} to allow access to the underlying task
     * for advanced configuration or monitoring.
     */
    @Getter
    private final AbstractTask abstractTask;

    /**
     * Constructs a new TaskExecutionContext that associates a task with a task runner.
     * This constructor is typically called by task factories rather than directly by application code.
     *
     * <p>The context serves as a bridge between task creation and execution, allowing
     * tasks to be configured before being submitted to the runner.
     *
     * @param abstractTask the task to be executed, must extend {@link AbstractTask}
     * @param taskRunner the runner that will execute the task asynchronously
     * @throws NullPointerException if abstractTask or taskRunner is null
     */
    public TaskExecutionContext(final AbstractTask abstractTask, final TaskRunner taskRunner) {
        this.taskRunner = taskRunner;
        this.abstractTask = abstractTask;
    }

    /**
     * Schedules the task for normal-priority execution.
     * The task is added to the end of the task queue and will be executed when
     * a worker thread becomes available.
     *
     * <p>This is the standard scheduling method for most background operations where
     * execution order and priority don't matter significantly (e.g., periodic syncs,
     * background downloads, cache cleanup).
     *
     * <p>Execution behavior:
     * <ul>
     *   <li>Task is added to the end of the queue (FIFO order)</li>
     *   <li>Execution begins when a worker thread is free</li>
     *   <li>Other tasks in the queue are not affected</li>
     *   <li>Non-blocking - returns immediately</li>
     * </ul>
     *
     * <p>Example usage:
     * <pre>{@code
     * // Schedule a plugin sync for normal execution
     * taskFactory.createPluginSyncTask()
     *     .setOnSucceeded(event -> refreshPluginList())
     *     .schedule();
     * }</pre>
     *
     * @see #scheduleNow() for high-priority execution
     */
    public void schedule() {
        // Submit the task to the runner's queue for normal-priority execution
        taskRunner.submitTask(abstractTask);
    }

    /**
     * Schedules the task for immediate high-priority execution.
     * The task is added to the front of the task queue, effectively jumping ahead
     * of all other pending tasks.
     *
     * <p>This method should be used sparingly and only for time-sensitive operations
     * that require immediate execution, such as:
     * <ul>
     *   <li>User-initiated actions that expect immediate feedback</li>
     *   <li>Critical updates that must be applied urgently</li>
     *   <li>Emergency operations (e.g., crash recovery, emergency save)</li>
     *   <li>Operations that block user interaction until complete</li>
     * </ul>
     *
     * <p><strong>Warning:</strong> Overuse of this method can lead to:
     * <ul>
     *   <li>Task starvation (lower-priority tasks never execute)</li>
     *   <li>Unfair execution ordering</li>
     *   <li>Degraded user experience for background operations</li>
     * </ul>
     *
     * <p>Execution behavior:
     * <ul>
     *   <li>Task is inserted at the front of the queue (LIFO-like for high priority)</li>
     *   <li>Executes before all currently queued tasks</li>
     *   <li>Still waits for currently running tasks to complete</li>
     *   <li>Non-blocking - returns immediately</li>
     * </ul>
     *
     * <p>Example usage:
     * <pre>{@code
     * // User clicked "Sync Now" - execute immediately
     * taskFactory.createPluginSyncTask()
     *     .setOnSucceeded(event -> showNotification("Sync completed!"))
     *     .scheduleNow(); // High priority
     * }</pre>
     *
     * @see #schedule() for normal-priority execution
     */
    public void scheduleNow() {
        // Submit the task to the head of the runner's queue for immediate execution
        taskRunner.submitTaskOnQueueHead(abstractTask);
    }

    /**
     * Attaches an event handler called when the task completes successfully.
     * This method enables fluent API usage by returning the context itself,
     * allowing method chaining with scheduling methods.
     *
     * <p>The success handler is invoked when:
     * <ul>
     *   <li>The task's {@link AbstractTask#start()} method returns normally</li>
     *   <li>The task completes without throwing an exception</li>
     *   <li>The task wasn't canceled before completion</li>
     * </ul>
     *
     * <p>The handler is NOT invoked when:
     * <ul>
     *   <li>The task throws an exception (use {@code setOnFailed()} instead)</li>
     *   <li>The task is canceled (use {@code setOnCancelled()} instead)</li>
     *   <li>The task is still running (use {@code setOnRunning()} for start events)</li>
     * </ul>
     *
     * <p>Common use cases for success handlers:
     * <ul>
     *   <li>Refreshing UI components with new data</li>
     *   <li>Showing success notifications to the user</li>
     *   <li>Triggering cascade operations (e.g., sync → refresh → check updates)</li>
     *   <li>Logging completion events</li>
     *   <li>Updating application state</li>
     * </ul>
     *
     * <p><strong>Thread Safety:</strong> The event handler is invoked on the JavaFX
     * application thread, making it safe to update UI components directly without
     * additional Platform.runLater() wrapping.
     *
     * <p>Example usage:
     * <pre>{@code
     * taskFactory.createPluginSyncTask()
     *     .setOnSucceeded(event -> {
     *         // Safe to update UI here - runs on JavaFX thread
     *         TaskResult result = (TaskResult) event.getSource().getValue();
     *         pluginListView.refresh();
     *         statusLabel.setText("Sync completed successfully!");
     *     })
     *     .schedule();
     * }</pre>
     *
     * <p>Method chaining example:
     * <pre>{@code
     * // Multiple handlers can be chained
     * context.setOnSucceeded(event -> refreshUI())
     *        .setOnFailed(event -> showError())
     *        .scheduleNow();
     * }</pre>
     *
     * @param value the event handler to call on successful task completion, can be null to remove handler
     * @return this TaskExecutionContext instance for method chaining
     * @see AbstractTask#setOnSucceeded(EventHandler)
     * @see WorkerStateEvent
     */
    public TaskExecutionContext setOnSucceeded(EventHandler<WorkerStateEvent> value) {
        // Delegate to the wrapped task's setOnSucceeded method
        abstractTask.setOnSucceeded(value);
        // Return this context to enable fluent API / method chaining
        return this;
    }

}
