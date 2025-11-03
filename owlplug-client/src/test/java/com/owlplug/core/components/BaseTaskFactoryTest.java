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

package com.owlplug.core.components;

import com.owlplug.core.tasks.AbstractTask;
import com.owlplug.core.tasks.SimpleEventListener;
import com.owlplug.core.tasks.TaskExecutionContext;
import com.owlplug.core.tasks.TaskResult;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the BaseTaskFactory class.
 * These tests verify the task factory's ability to create execution contexts,
 * manage task lifecycle, and coordinate event notifications.
 *
 * <p>Test coverage includes:
 * <ul>
 *   <li>Task execution context creation</li>
 *   <li>Context building behavior</li>
 *   <li>Event listener notification mechanism</li>
 *   <li>Integration with TaskRunner</li>
 *   <li>Subclass extensibility patterns</li>
 * </ul>
 *
 * <p>Testing strategy:
 * These are unit tests that use mocking to isolate the factory's behavior
 * from actual task execution. We verify that the factory correctly creates
 * contexts and coordinates with the task runner without executing real tasks.
 */
@ExtendWith(MockitoExtension.class)
public class BaseTaskFactoryTest {

    @Mock
    private TaskRunner taskRunner;

    private BaseTaskFactory taskFactory;

    /**
     * Concrete implementation of AbstractTask for testing purposes.
     * This task just returns a success result without doing any work.
     */
    private static class TestTask extends AbstractTask {
        public TestTask() {
            super("Test Task");
        }

        public TestTask(String name) {
            super(name);
        }

        @Override
        protected TaskResult start() {
            return success();
        }
    }

    /**
     * Extended factory for testing subclass behavior.
     * This demonstrates how subclasses can override buildContext to add custom behavior
     * and how they can use the protected notifyListeners method.
     */
    @Getter
    private static class ExtendedTaskFactory extends BaseTaskFactory {
        private boolean buildContextCalled = false;

        public ExtendedTaskFactory(TaskRunner taskRunner) {
            super(taskRunner);
        }

        @Override
        protected TaskExecutionContext buildContext(AbstractTask task) {
            buildContextCalled = true;
            return super.buildContext(task);
        }

        // Public method that exposes the protected notifyListeners for testing
        public void publicNotifyListeners(java.util.Collection<SimpleEventListener> listeners) {
            notifyListeners(listeners);
        }
    }

    /**
     * Set up test fixtures before each test.
     * Creates a fresh BaseTaskFactory instance with a mocked TaskRunner
     * to ensure test isolation.
     */
    @BeforeEach
    void setUp() {
        taskFactory = new BaseTaskFactory(taskRunner);
    }

    // ========================================
    // Tests for task execution context creation
    // ========================================

    /**
     * Test that create() returns a valid TaskExecutionContext.
     * This verifies the basic factory functionality - creating contexts for tasks.
     *
     * <p>The context is the bridge between task creation and execution,
     * so this is fundamental to the factory's purpose.
     */
    @Test
    void create_shouldReturnTaskExecutionContext() {
        // Arrange: Create a test task
        TestTask task = new TestTask();

        // Act: Create a context using the factory
        TaskExecutionContext context = taskFactory.create(task);

        // Assert: Should return a valid context
        assertNotNull(context, "Context should not be null");
        assertSame(task, context.getAbstractTask(),
                "Context should contain the same task instance");
    }

    /**
     * Test that create() delegates to buildContext().
     * This verifies that the public method properly delegates to the protected
     * method, which allows subclasses to override context creation.
     *
     * <p>This delegation pattern enables extensibility while maintaining
     * a clean public API.
     */
    @Test
    void create_shouldDelegateToBuildContext() {
        // Arrange: Create an extended factory that tracks buildContext calls
        ExtendedTaskFactory extendedFactory = new ExtendedTaskFactory(taskRunner);
        TestTask task = new TestTask();

        // Act: Create a context
        TaskExecutionContext context = extendedFactory.create(task);

        // Assert: buildContext should have been called
        assertTrue(extendedFactory.isBuildContextCalled(),
                "create() should delegate to buildContext()");
        assertNotNull(context, "Should still return a valid context");
    }

    /**
     * Test that create() works with different task types.
     * This ensures the factory is generic and works with any AbstractTask subclass.
     *
     * <p>The factory should be agnostic to the specific task implementation,
     * working with plugin tasks, sync tasks, download tasks, etc.
     */
    @Test
    void create_shouldWorkWithDifferentTaskTypes() {
        // Arrange: Create tasks with different names (simulating different types)
        TestTask task1 = new TestTask("Plugin Sync Task");
        TestTask task2 = new TestTask("File Download Task");

        // Act: Create contexts for both tasks
        TaskExecutionContext context1 = taskFactory.create(task1);
        TaskExecutionContext context2 = taskFactory.create(task2);

        // Assert: Both should return valid contexts with correct tasks
        assertNotNull(context1, "First context should not be null");
        assertNotNull(context2, "Second context should not be null");
        assertEquals("Plugin Sync Task", context1.getAbstractTask().getName(),
                "First context should contain the plugin sync task");
        assertEquals("File Download Task", context2.getAbstractTask().getName(),
                "Second context should contain the file download task");
    }

    /**
     * Test that multiple calls to create() produce independent contexts.
     * This ensures each task gets its own context without cross-contamination.
     *
     * <p>Context independence is crucial for concurrent task execution
     * and proper task isolation.
     */
    @Test
    void create_shouldProduceIndependentContexts() {
        // Arrange: Create two different tasks
        TestTask task1 = new TestTask("Task 1");
        TestTask task2 = new TestTask("Task 2");

        // Act: Create contexts for both tasks
        TaskExecutionContext context1 = taskFactory.create(task1);
        TaskExecutionContext context2 = taskFactory.create(task2);

        // Assert: Contexts should be different instances
        assertNotSame(context1, context2,
                "Each create() call should produce a new context instance");
        assertNotSame(context1.getAbstractTask(), context2.getAbstractTask(),
                "Contexts should contain different task instances");
    }

    // ========================================
    // Tests for buildContext behavior
    // ========================================

    /**
     * Test that buildContext() creates a context with the correct task.
     * This verifies the core context building logic.
     *
     * <p>While buildContext is protected, we test it indirectly through create()
     * to ensure proper behavior without violating encapsulation.
     */
    @Test
    void buildContext_shouldCreateContextWithCorrectTask() {
        // Arrange
        TestTask task = new TestTask("Build Context Test");

        // Act: Create context (which calls buildContext internally)
        TaskExecutionContext context = taskFactory.create(task);

        // Assert: Context should contain the correct task
        assertNotNull(context, "Context should not be null");
        assertSame(task, context.getAbstractTask(),
                "Context should be associated with the provided task");
    }

    /**
     * Test that buildContext() associates the task with the task runner.
     * This ensures the created context has access to the runner for scheduling.
     *
     * <p>The task runner is essential for actually executing tasks in the background,
     * so this association is critical for the factory's functionality.
     */
    @Test
    void buildContext_shouldAssociateTaskWithRunner() {
        // Arrange
        TestTask task = new TestTask();

        // Act: Create context
        TaskExecutionContext context = taskFactory.create(task);

        // Verify the context can schedule the task (which requires the runner)
        context.schedule();

        // Assert: TaskRunner should have been invoked
        verify(taskRunner).submitTask(task);
    }

    // ========================================
    // Tests for listener notification
    // ========================================

    /**
     * Test that notifyListeners() invokes all listeners in the collection.
     * This verifies the event notification mechanism that allows UI updates
     * and cascade operations after task completion.
     *
     * <p>Listener notification is crucial for keeping the UI in sync with
     * background task results.
     */
    @Test
    void notifyListeners_shouldInvokeAllListeners() {
        // Arrange: Create a collection of listeners that track invocations
        AtomicInteger callCount = new AtomicInteger(0);
        List<SimpleEventListener> listeners = Arrays.asList(
                callCount::incrementAndGet,
                callCount::incrementAndGet,
                callCount::incrementAndGet
        );

        // Act: Notify all listeners
        taskFactory.notifyListeners(listeners);

        // Assert: All three listeners should have been called
        assertEquals(3, callCount.get(),
                "All listeners should have been invoked exactly once");
    }

    /**
     * Test that notifyListeners() works with an empty collection.
     * This ensures the method handles edge cases gracefully without errors.
     *
     * <p>Empty listener collections might occur when no UI components
     * are registered for updates yet.
     */
    @Test
    void notifyListeners_shouldHandleEmptyCollection() {
        // Arrange: Create an empty listener collection
        List<SimpleEventListener> emptyListeners = new ArrayList<>();

        // Act & Assert: Should not throw any exceptions
        assertDoesNotThrow(() -> taskFactory.notifyListeners(emptyListeners),
                "notifyListeners should handle empty collections without errors");
    }

    /**
     * Test that notifyListeners() invokes listeners in order.
     * This verifies that listeners are called sequentially in the order they
     * were added to the collection.
     *
     * <p>Order matters when listeners have dependencies or when certain
     * operations must complete before others (e.g., refresh data before UI).
     */
    @Test
    void notifyListeners_shouldInvokeListenersInOrder() {
        // Arrange: Create listeners that record the order they were called
        List<Integer> callOrder = new ArrayList<>();
        List<SimpleEventListener> listeners = Arrays.asList(
                () -> callOrder.add(1),
                () -> callOrder.add(2),
                () -> callOrder.add(3)
        );

        // Act: Notify all listeners
        taskFactory.notifyListeners(listeners);

        // Assert: Listeners should have been called in order
        assertEquals(Arrays.asList(1, 2, 3), callOrder,
                "Listeners should be invoked in the order they appear in the collection");
    }

    /**
     * Test that notifyListeners() propagates exceptions from listeners.
     * This verifies that listener errors are not silently swallowed.
     *
     * <p>While we could catch and log exceptions, propagating them ensures
     * that serious errors in event handlers are not ignored. Clients can
     * wrap the notifyListeners call in try-catch if needed.
     */
    @Test
    void notifyListeners_shouldPropagateExceptions() {
        // Arrange: Create a listener that throws an exception
        RuntimeException expectedException = new RuntimeException("Listener error");
        List<SimpleEventListener> listeners = List.of(
                () -> {
                    throw expectedException;
                }
        );

        // Act & Assert: Exception should be propagated
        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> taskFactory.notifyListeners(listeners),
                "Exception from listener should be propagated");

        assertEquals(expectedException, thrown,
                "Should propagate the exact exception thrown by the listener");
    }

    // ========================================
    // Tests for factory extensibility
    // ========================================

    /**
     * Test that subclasses can override buildContext() to add custom behavior.
     * This verifies the extensibility pattern that allows specialized factories
     * to customize context creation.
     *
     * <p>This is important for domain-specific factories that need to:
     * <ul>
     *   <li>Add default event handlers</li>
     *   <li>Configure task properties</li>
     *   <li>Add monitoring or logging</li>
     * </ul>
     */
    @Test
    void subclass_canOverrideBuildContext() {
        // Arrange: Create a custom factory with overridden buildContext
        BaseTaskFactory customFactory = new BaseTaskFactory(taskRunner) {
            @Override
            protected TaskExecutionContext buildContext(AbstractTask task) {
                // Custom logic before creating context
                task.setName("Modified: " + task.getName());
                return super.buildContext(task);
            }
        };

        TestTask task = new TestTask("Original Name");

        // Act: Create context using a custom factory
        TaskExecutionContext context = customFactory.create(task);

        // Assert: Custom logic should have been applied
        assertEquals("Modified: Original Name", context.getAbstractTask().getName(),
                "Subclass should be able to customize task before context creation");
    }

    /**
     * Test that subclasses can use notifyListeners() to coordinate event notifications.
     * This demonstrates how the protected notifyListeners method can be used by
     * subclasses to trigger UI updates or cascade operations.
     *
     * <p>This pattern is used extensively in PluginTaskFactory, ExploreTaskFactory, etc.
     * to refresh UI components after background operations complete.
     */
    @Test
    void subclass_canUseNotifyListeners() {
        // Arrange: Track if listeners were called
        AtomicInteger listener1Called = new AtomicInteger(0);
        AtomicInteger listener2Called = new AtomicInteger(0);

        List<SimpleEventListener> listeners = Arrays.asList(
                listener1Called::incrementAndGet,
                listener2Called::incrementAndGet
        );

        // Create an extended factory instance that can expose the protected method
        ExtendedTaskFactory extendedFactory = new ExtendedTaskFactory(taskRunner);

        // Act: Use the public method that internally calls notifyListeners
        extendedFactory.publicNotifyListeners(listeners);

        // Assert: Both listeners should have been called
        assertEquals(1, listener1Called.get(),
                "First listener should be notified");
        assertEquals(1, listener2Called.get(),
                "Second listener should be notified");
    }

    // ========================================
    // Tests for constructor and initialization
    // ========================================

    /**
     * Test that the factory is properly initialized with a task runner.
     * This verifies that Lombok's @RequiredArgsConstructor correctly
     * generates the constructor and assigns the field.
     */
    @Test
    void constructor_shouldInitializeWithTaskRunner() {
        // Arrange & Act: Constructor is called in setUp()

        // Assert: Factory should be initialized and functional
        assertNotNull(taskFactory, "Factory should be initialized");

        // Verify it can create contexts (which requires the task runner)
        TestTask task = new TestTask();
        TaskExecutionContext context = taskFactory.create(task);
        assertNotNull(context, "Factory should be able to create contexts");
    }

    /**
     * Test that the factory maintains a reference to the task runner.
     * This ensures the runner is available for context creation throughout
     * the factory's lifetime.
     */
    @Test
    void factory_shouldMaintainTaskRunnerReference() {
        // Arrange
        TestTask task1 = new TestTask("Task 1");
        TestTask task2 = new TestTask("Task 2");

        // Act: Create multiple contexts
        TaskExecutionContext context1 = taskFactory.create(task1);
        TaskExecutionContext context2 = taskFactory.create(task2);

        // Both contexts should be able to schedule tasks (requires task runner)
        context1.schedule();
        context2.schedule();

        // Assert: Task runner should be called for both tasks
        verify(taskRunner).submitTask(task1);
        verify(taskRunner).submitTask(task2);
    }
}
