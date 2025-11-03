/*
 * Copyright (C) 2021-2024 Arthur <dropsnorz@gmail.com>
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the TaskExecutionContext class.
 * These tests verify the context's ability to wrap tasks, manage scheduling,
 * and provide a fluent API for task configuration.
 *
 * <p>Test coverage includes:
 * <ul>
 *   <li>Context construction and task wrapping</li>
 *   <li>Normal-priority scheduling (schedule)</li>
 *   <li>High-priority scheduling (scheduleNow)</li>
 *   <li>Event handler attachment (setOnSucceeded)</li>
 *   <li>Fluent API method chaining</li>
 *   <li>Task runner integration</li>
 * </ul>
 *
 * <p>Testing strategy:
 * These are unit tests that use mocking to isolate the context's behavior
 * from actual task execution. We verify that the context correctly delegates
 * to the task runner and task without executing real background work.
 */
@ExtendWith(MockitoExtension.class)
public class TaskExecutionContextTest {

    @Mock
    private TaskRunner taskRunner;

    @Mock
    private AbstractTask abstractTask;

    private TaskExecutionContext context;

    /**
     * Concrete test task implementation for testing purposes.
     * This task just returns success without doing any work.
     */
    private static class TestTask extends AbstractTask {
        public TestTask() {
            super("Test Task");
        }

        @Override
        protected TaskResult start() {
            return success();
        }
    }

    /**
     * Set up test fixtures before each test.
     * Creates a fresh TaskExecutionContext with mocked dependencies
     * to ensure test isolation.
     */
    @BeforeEach
    void setUp() {
        context = new TaskExecutionContext(abstractTask, taskRunner);
    }

    // ========================================
    // Tests for constructor and initialization
    // ========================================

    /**
     * Test that the constructor properly initializes the context with a task and runner.
     * This verifies the basic construction and dependency injection.
     *
     * <p>The context must maintain references to both the task and runner
     * to enable scheduling and configuration operations.
     */
    @Test
    void constructor_shouldInitializeWithTaskAndRunner() {
        // Arrange: Constructor is called in setUp()

        // Assert: Context should be properly initialized
        assertNotNull(context, "Context should not be null");
        assertSame(abstractTask, context.getAbstractTask(),
                "Context should contain the provided task");
    }

    /**
     * Test that the context can be constructed with a real task instance.
     * This verifies that the context works with actual AbstractTask subclasses,
     * not just mocks.
     *
     * <p>This is important because the context will be used with real task
     * implementations in production code.
     */
    @Test
    void constructor_shouldWorkWithRealTask() {
        // Arrange: Create a real task instance
        TestTask realTask = new TestTask();

        // Act: Create context with a real task
        TaskExecutionContext realContext = new TaskExecutionContext(realTask, taskRunner);

        // Assert: Context should wrap the real task
        assertNotNull(realContext, "Context should not be null");
        assertSame(realTask, realContext.getAbstractTask(),
                "Context should contain the real task instance");
        assertEquals("Test Task", realContext.getAbstractTask().getName(),
                "Should preserve task properties");
    }

    // ========================================
    // Tests for normal-priority scheduling
    // ========================================

    /**
     * Test that schedule() delegates to the task runner's submitTask method.
     * This verifies normal-priority scheduling behavior.
     *
     * <p>Normal scheduling adds the task to the end of the queue,
     * ensuring FIFO execution order for regular background operations.
     */
    @Test
    void schedule_shouldSubmitTaskToRunner() {
        // Act: Schedule the task normally
        context.schedule();

        // Assert: TaskRunner's submitTask should have been called with the task
        verify(taskRunner).submitTask(abstractTask);
    }

    /**
     * Test that schedule() can be called multiple times.
     * This verifies that the context can be reused to schedule the same task
     * multiple times (though this is not a typical usage).
     *
     * <p>Note: In practice, tasks should generally only be scheduled once,
     * but the context should handle repeated calls gracefully.
     */
    @Test
    void schedule_shouldAllowMultipleCalls() {
        // Act: Schedule the task multiple times
        context.schedule();
        context.schedule();
        context.schedule();

        // Assert: TaskRunner should be called each time
        verify(taskRunner, times(3)).submitTask(abstractTask);
    }

    /**
     * Test that schedule() is non-blocking and returns immediately.
     * This verifies that scheduling doesn't wait for task execution.
     *
     * <p>The schedule method must return immediately to keep the UI responsive,
     * with actual execution happening asynchronously in the background.
     */
    @Test
    void schedule_shouldReturnImmediately() {
        // Act: Schedule and measure that it returns quickly
        long startTime = System.currentTimeMillis();
        context.schedule();
        long elapsedTime = System.currentTimeMillis() - startTime;

        // Assert: Should return in less than 100 ms (essentially immediate)
        assertTrue(elapsedTime < 100,
                "schedule() should return immediately, took " + elapsedTime + "ms");
        verify(taskRunner).submitTask(abstractTask);
    }

    // ========================================
    // Tests for high-priority scheduling
    // ========================================

    /**
     * Test that scheduleNow() delegates to the task runner's submitTaskOnQueueHead method.
     * This verifies high-priority scheduling behavior.
     *
     * <p>High-priority scheduling adds the task to the front of the queue,
     * ensuring it executes before other pending tasks (LIFO-like for priorities).
     */
    @Test
    void scheduleNow_shouldSubmitTaskToQueueHead() {
        // Act: Schedule the task with high priority
        context.scheduleNow();

        // Assert: TaskRunner's submitTaskOnQueueHead should have been called
        verify(taskRunner).submitTaskOnQueueHead(abstractTask);
    }

    /**
     * Test that scheduleNow() is different from schedule().
     * This verifies that the two scheduling methods use different runner methods.
     *
     * <p>This distinction is important for ensuring proper priority handling
     * and queue management in the task runner.
     */
    @Test
    void scheduleNow_shouldUseDifferentMethodThanSchedule() {
        // Act: Call both scheduling methods
        TaskExecutionContext context1 = new TaskExecutionContext(abstractTask, taskRunner);
        TaskExecutionContext context2 = new TaskExecutionContext(abstractTask, taskRunner);

        context1.schedule();
        context2.scheduleNow();

        // Assert: Different runner methods should be called
        verify(taskRunner).submitTask(abstractTask);
        verify(taskRunner).submitTaskOnQueueHead(abstractTask);
    }

    /**
     * Test that scheduleNow() is also non-blocking.
     * This verifies that high-priority scheduling doesn't wait for execution either.
     *
     * <p>Even high-priority tasks must not block the UI thread during scheduling.
     */
    @Test
    void scheduleNow_shouldReturnImmediately() {
        // Act: Schedule with priority and measure response time
        long startTime = System.currentTimeMillis();
        context.scheduleNow();
        long elapsedTime = System.currentTimeMillis() - startTime;

        // Assert: Should return in less than 100 ms (essentially immediate)
        assertTrue(elapsedTime < 100,
                "scheduleNow() should return immediately, took " + elapsedTime + "ms");
        verify(taskRunner).submitTaskOnQueueHead(abstractTask);
    }

    // ========================================
    // Tests for event handler attachment
    // ========================================

    /**
     * Test that setOnSucceeded() attaches the handler to the task.
     * This verifies that event handlers are properly delegated to the wrapped task.
     *
     * <p>Success handlers are crucial for reacting to task completion and
     * updating the UI with results.
     */
    @Test
    void setOnSucceeded_shouldAttachHandlerToTask() {
        // Arrange: Create a mock event handler
        @SuppressWarnings("unchecked")
        EventHandler<WorkerStateEvent> handler = mock(EventHandler.class);

        // Act: Attach the handler
        context.setOnSucceeded(handler);

        // Assert: Task's setOnSucceeded should have been called
        verify(abstractTask).setOnSucceeded(handler);
    }

    /**
     * Test that setOnSucceeded() returns the context for method chaining.
     * This verifies the fluent API behavior.
     *
     * <p>Method chaining is a key feature of the context pattern,
     * enabling clean, readable configuration code.
     */
    @Test
    void setOnSucceeded_shouldReturnContextForChaining() {
        // Arrange: Create a handler
        @SuppressWarnings("unchecked")
        EventHandler<WorkerStateEvent> handler = mock(EventHandler.class);

        // Act: Call setOnSucceeded
        TaskExecutionContext returnedContext = context.setOnSucceeded(handler);

        // Assert: Should return the same context instance
        assertSame(context, returnedContext,
                "setOnSucceeded should return the same context instance for chaining");
    }

    /**
     * Test that setOnSucceeded() can be chained with schedule().
     * This verifies the complete fluent API workflow.
     *
     * <p>This is the most common usage pattern - attach a handler and immediately schedule.
     */
    @Test
    void setOnSucceeded_shouldChainWithSchedule() {
        // Arrange: Create a handler
        @SuppressWarnings("unchecked")
        EventHandler<WorkerStateEvent> handler = mock(EventHandler.class);

        // Act: Chain setOnSucceeded with schedule
        context.setOnSucceeded(handler).schedule();

        // Assert: Both operations should have been performed
        verify(abstractTask).setOnSucceeded(handler);
        verify(taskRunner).submitTask(abstractTask);
    }

    /**
     * Test that setOnSucceeded() can be chained with scheduleNow().
     * This verifies a fluent API with high-priority scheduling.
     *
     * <p>High-priority tasks often need immediate user feedback,
     * making this chaining pattern particularly useful.
     */
    @Test
    void setOnSucceeded_shouldChainWithScheduleNow() {
        // Arrange: Create a handler
        @SuppressWarnings("unchecked")
        EventHandler<WorkerStateEvent> handler = mock(EventHandler.class);

        // Act: Chain setOnSucceeded with scheduleNow
        context.setOnSucceeded(handler).scheduleNow();

        // Assert: Both operations should have been performed
        verify(abstractTask).setOnSucceeded(handler);
        verify(taskRunner).submitTaskOnQueueHead(abstractTask);
    }

    /**
     * Test that null handlers can be set (to remove handlers).
     * This verifies that the context handles null values gracefully.
     *
     * <p>Setting a null handler is a valid way to remove a previously
     * attached handler from the task.
     */
    @Test
    void setOnSucceeded_shouldAcceptNullHandler() {
        // Act: Set a null handler
        TaskExecutionContext returnedContext = context.setOnSucceeded(null);

        // Assert: Should not throw exception and should still return context
        assertNotNull(returnedContext, "Should return context even with null handler");
        verify(abstractTask).setOnSucceeded(null);
    }

    // ========================================
    // Tests for getAbstractTask accessor
    // ========================================

    /**
     * Test that getAbstractTask() returns the wrapped task.
     * This verifies the Lombok-generated getter works correctly.
     *
     * <p>Access to the underlying task is sometimes needed for advanced
     * configuration or monitoring that the context doesn't expose directly.
     */
    @Test
    void getAbstractTask_shouldReturnWrappedTask() {
        // Act: Get the task
        AbstractTask retrievedTask = context.getAbstractTask();

        // Assert: Should return the same task instance
        assertSame(abstractTask, retrievedTask,
                "getAbstractTask should return the wrapped task instance");
    }

    /**
     * Test that the task reference is immutable (final field).
     * This verifies that the context maintains a consistent task reference.
     *
     * <p>The task should not change after context creation, ensuring
     * predictable behavior throughout the context's lifetime.
     */
    @Test
    void getAbstractTask_shouldReturnSameInstanceOnMultipleCalls() {
        // Act: Get the task multiple times
        AbstractTask task1 = context.getAbstractTask();
        AbstractTask task2 = context.getAbstractTask();
        AbstractTask task3 = context.getAbstractTask();

        // Assert: All calls should return the same instance
        assertSame(task1, task2, "Should return same instance on multiple calls");
        assertSame(task2, task3, "Should return same instance on multiple calls");
    }

    // ========================================
    // Tests for real-world usage patterns
    // ========================================

    /**
     * Test a complete usage scenario with a real task.
     * This demonstrates the typical workflow: create context, attach handler, schedule.
     *
     * <p>This integration-style test verifies that all components work together
     * correctly in a realistic usage scenario.
     */
    @Test
    void completeUsageScenario_shouldWorkCorrectly() {
        // Arrange: Create a real task and handler
        TestTask realTask = new TestTask();
        @SuppressWarnings("WriteOnlyObject")
        AtomicBoolean handlerCalled = new AtomicBoolean(false);
        EventHandler<WorkerStateEvent> handler = event -> handlerCalled.set(true);

        // Act: Create context and use fluent API
        TaskExecutionContext realContext = new TaskExecutionContext(realTask, taskRunner);
        realContext.setOnSucceeded(handler).schedule();

        // Assert: Handler should be attached and a task should be scheduled
        assertNotNull(realTask.getOnSucceeded(),
                "Handler should be attached to task");
        verify(taskRunner).submitTask(realTask);
    }

    /**
     * Test that the context pattern enables clean separation of concerns.
     * This verifies that the context successfully decouples task creation
     * from scheduling and configuration.
     *
     * <p>The context should allow tasks to be created and configured
     * independently, with scheduling as a separate final step.
     */
    @Test
    void contextPattern_shouldEnableSeparationOfConcerns() {
        // Arrange: Create handler separately from scheduling
        @SuppressWarnings("unchecked")
        EventHandler<WorkerStateEvent> successHandler = mock(EventHandler.class);

        // Act: Configure context in multiple steps (simulating different code locations)
        TaskExecutionContext configuredContext = context.setOnSucceeded(successHandler);
        // ... other code could execute here ...
        configuredContext.schedule();

        // Assert: Configuration and scheduling should work independently
        verify(abstractTask).setOnSucceeded(successHandler);
        verify(taskRunner).submitTask(abstractTask);
    }

    /**
     * Test that multiple event handlers can be configured before scheduling.
     * This verifies that the context supports complex configuration scenarios.
     *
     * <p>While this test only shows setOnSucceeded, in practice the context
     * could support other handlers (onFailed, onCancelled, etc.) in the future.
     */
    @Test
    void multipleHandlers_canBeConfiguredBeforeScheduling() {
        // Arrange: Create multiple handlers
        @SuppressWarnings("unchecked")
        EventHandler<WorkerStateEvent> successHandler = mock(EventHandler.class);

        // Act: Configure and then schedule
        context.setOnSucceeded(successHandler);
        // Could add more handler types here in future
        context.schedule();

        // Assert: All configurations should be applied before scheduling
        verify(abstractTask).setOnSucceeded(successHandler);
        verify(taskRunner).submitTask(abstractTask);
    }

    /**
     * Test that the context works correctly when handlers are not set.
     * This verifies that event handlers are optional and the context
     * works fine without them.
     *
     * <p>Not all tasks need completion handlers - some just need to run
     * in the background without UI feedback.
     */
    @Test
    void schedule_shouldWorkWithoutHandlers() {
        // Act: Schedule without setting any handlers
        context.schedule();

        // Assert: Should schedule successfully without handlers
        verify(taskRunner).submitTask(abstractTask);
        // No handler attachment should have occurred
        verify(abstractTask, never()).setOnSucceeded(any());
    }
}
