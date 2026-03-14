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

import com.owlplug.core.model.ApplicationState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the ApplicationMonitor class.
 * These tests verify the crash detection mechanism, lifecycle management,
 * and state persistence without requiring a full Spring application context.
 *
 * <p>Test coverage includes:
 * <ul>
 *   <li>Detection of clean vs. crashed previous executions</li>
 *   <li>Proper initialization behavior (@PostConstruct)</li>
 *   <li>Proper shutdown behavior (@PreDestroy)</li>
 *   <li>State retrieval and persistence</li>
 *   <li>Edge cases (unknown states, first launch)</li>
 * </ul>
 *
 * <p>Testing strategy:
 * Since @PostConstruct and @PreDestroy are lifecycle methods, we test them
 * by invoking them directly via reflection to simulate Spring's lifecycle management.
 */
@ExtendWith(MockitoExtension.class)
public class ApplicationMonitorTest {

    @Mock
    private ApplicationPreferences applicationPreferences;

    private ApplicationMonitor applicationMonitor;

    /**
     * Set up test fixtures before each test.
     * Creates a fresh ApplicationMonitor instance with mocked dependencies
     * to ensure test isolation and prevent side effects between tests.
     */
    @BeforeEach
    void setUp() {
        applicationMonitor = new ApplicationMonitor(applicationPreferences);
    }

    // ========================================
    // Tests for crash detection on startup
    // ========================================

    /**
     * Test that initialize() detects a clean previous shutdown.
     * When the previous application state is TERMINATED, it means the application
     * shut down properly, and previousExecutionSafelyTerminated should remain true.
     *
     * <p>This simulates the normal case where:
     * <ol>
     *   <li>Previous session: Application ran and shut down cleanly</li>
     *   <li>Current session: Application starts and checks previous state</li>
     *   <li>Expected result: No crash detected</li>
     * </ol>
     */
    @Test
    void initialize_shouldDetectCleanShutdown_whenPreviousStateIsTerminated() throws Exception {
        // Arrange: Mock preferences to return TERMINATED (clean shutdown)
        when(applicationPreferences.get(ApplicationDefaults.APPLICATION_STATE_KEY, "UNKNOWN"))
                .thenReturn("TERMINATED");

        // Act: Invoke the @PostConstruct method via reflection to simulate Spring lifecycle
        Method initMethod = ApplicationMonitor.class.getDeclaredMethod("initialize");
        initMethod.setAccessible(true);
        initMethod.invoke(applicationMonitor);

        // Assert: Previous execution should be marked as safely terminated
        assertTrue(applicationMonitor.isPreviousExecutionSafelyTerminated(),
                "Previous execution should be marked as safely terminated when state is TERMINATED");

        // Verify: The monitor should have set the current state to RUNNING
        verify(applicationPreferences).put(ApplicationDefaults.APPLICATION_STATE_KEY, ApplicationState.RUNNING.getText());
    }

    /**
     * Test that initialize() detects a crash when the previous state is RUNNING.
     * When the previous application state is still RUNNING, it means the application
     * didn't go through the proper shutdown sequence (crashed, killed, or force-quit).
     *
     * <p>This simulates the crash scenario where:
     * <ol>
     *   <li>Previous session: Application crashed (state remained RUNNING)</li>
     *   <li>Current session: Application starts and checks previous state</li>
     *   <li>Expected result: Crash detected</li>
     * </ol>
     */
    @Test
    void initialize_shouldDetectCrash_whenPreviousStateIsRunning() throws Exception {
        // Arrange: Mock preferences to return RUNNING (crash scenario)
        when(applicationPreferences.get(ApplicationDefaults.APPLICATION_STATE_KEY, "UNKNOWN"))
                .thenReturn("RUNNING");

        // Act: Invoke the @PostConstruct method via reflection
        Method initMethod = ApplicationMonitor.class.getDeclaredMethod("initialize");
        initMethod.setAccessible(true);
        initMethod.invoke(applicationMonitor);

        // Assert: Previous execution should be marked as NOT safely terminated
        assertFalse(applicationMonitor.isPreviousExecutionSafelyTerminated(),
                "Previous execution should be marked as NOT safely terminated when state is RUNNING");

        // Verify: The monitor should still set the current state to RUNNING for this session
        verify(applicationPreferences).put(ApplicationDefaults.APPLICATION_STATE_KEY, ApplicationState.RUNNING.getText());
    }

    /**
     * Test that initialize() handles UNKNOWN state properly (first launch scenario).
     * When there is no previous state saved, it means this is the first application launch
     * or preferences were cleared. This should be treated as a safe condition.
     *
     * <p>This simulates:
     * <ol>
     *   <li>First application launch (no previous state)</li>
     *   <li>Preferences cleared/reset</li>
     *   <li>Expected result: No crash detected (benefit of the doubt)</li>
     * </ol>
     */
    @Test
    void initialize_shouldHandleUnknownState_whenNoPreviousStateExists() throws Exception {
        // Arrange: Mock preferences to return UNKNOWN (first launch or cleared preferences)
        when(applicationPreferences.get(ApplicationDefaults.APPLICATION_STATE_KEY, "UNKNOWN"))
                .thenReturn("UNKNOWN");

        // Act: Invoke the @PostConstruct method via reflection
        Method initMethod = ApplicationMonitor.class.getDeclaredMethod("initialize");
        initMethod.setAccessible(true);
        initMethod.invoke(applicationMonitor);

        // Assert: Should assume previous execution was safe (benefit of the doubt)
        assertTrue(applicationMonitor.isPreviousExecutionSafelyTerminated(),
                "Previous execution should be marked as safely terminated for UNKNOWN state");

        // Verify: The monitor should set the current state to RUNNING
        verify(applicationPreferences).put(ApplicationDefaults.APPLICATION_STATE_KEY, ApplicationState.RUNNING.getText());
    }

    // ========================================
    // Tests for shutdown behavior
    // ========================================

    /**
     * Test that destroy() properly marks the application as terminated.
     * This method is called during normal shutdown (@PreDestroy) and should
     * persist the TERMINATED state to indicate clean exit.
     *
     * <p>This ensures that:
     * <ol>
     *   <li>Normal shutdown flow: Application calls destroy()</li>
     *   <li>State is persisted as TERMINATED</li>
     *   <li>Next startup: Will detect a clean shutdown (no crash)</li>
     * </ol>
     */
    @Test
    void destroy_shouldMarkApplicationAsTerminated() throws Exception {
        // Act: Invoke the @PreDestroy method via reflection to simulate Spring shutdown
        Method destroyMethod = ApplicationMonitor.class.getDeclaredMethod("destroy");
        destroyMethod.setAccessible(true);
        destroyMethod.invoke(applicationMonitor);

        // Assert: Should have saved TERMINATED state to preferences
        verify(applicationPreferences).put(
                ApplicationDefaults.APPLICATION_STATE_KEY,
                ApplicationState.TERMINATED.getText()
        );
    }

    /**
     * Test the complete lifecycle: initialize → destroy.
     * This test simulates a full application session from startup to clean shutdown,
     * verifying that the state transitions are correct and properly persisted.
     *
     * <p>Lifecycle verification:
     * <ol>
     *   <li>Startup: initialize() sets state to RUNNING</li>
     *   <li>Shutdown: destroy() sets state to TERMINATED</li>
     *   <li>Both state changes are persisted to preferences</li>
     * </ol>
     */
    @Test
    void fullLifecycle_shouldTransitionFromRunningToTerminated() throws Exception {
        // Arrange: Mock clean previous state
        when(applicationPreferences.get(ApplicationDefaults.APPLICATION_STATE_KEY, "UNKNOWN"))
                .thenReturn("TERMINATED");

        // Act: Simulate the full lifecycle
        Method initMethod = ApplicationMonitor.class.getDeclaredMethod("initialize");
        initMethod.setAccessible(true);
        initMethod.invoke(applicationMonitor);

        Method destroyMethod = ApplicationMonitor.class.getDeclaredMethod("destroy");
        destroyMethod.setAccessible(true);
        destroyMethod.invoke(applicationMonitor);

        // Assert: Verify state transitions in order
        ArgumentCaptor<String> stateCaptor = ArgumentCaptor.forClass(String.class);
        verify(applicationPreferences, times(2)).put(
                eq(ApplicationDefaults.APPLICATION_STATE_KEY),
                stateCaptor.capture()
        );

        // The first call should set the state to RUNNING (during initializing)
        assertEquals(ApplicationState.RUNNING.getText(), stateCaptor.getAllValues().get(0),
                "First state transition should be to RUNNING");

        // The second call should set the state to TERMINATED (during destruction)
        assertEquals(ApplicationState.TERMINATED.getText(), stateCaptor.getAllValues().get(1),
                "Second state transition should be to TERMINATED");
    }

    // ========================================
    // Tests for state retrieval
    // ========================================

    /**
     * Test that getState() correctly retrieves RUNNING state from preferences.
     * This verifies that the state retrieval mechanism works correctly for the RUNNING state.
     */
    @Test
    void getState_shouldReturnRunning_whenStateIsRunning() {
        // Arrange: Mock preferences to return RUNNING state
        when(applicationPreferences.get(ApplicationDefaults.APPLICATION_STATE_KEY, "UNKNOWN"))
                .thenReturn("RUNNING");

        // Act: Retrieve the state
        ApplicationState state = applicationMonitor.getState();

        // Assert: Should return RUNNING state
        assertEquals(ApplicationState.RUNNING, state,
                "Should return RUNNING state when preference value is RUNNING");

        // Verify: Preferences were queried with the correct key and default value
        verify(applicationPreferences).get(ApplicationDefaults.APPLICATION_STATE_KEY, "UNKNOWN");
    }

    /**
     * Test that getState() correctly retrieves TERMINATED state from preferences.
     * This verifies that the state retrieval mechanism works correctly for the TERMINATED state.
     */
    @Test
    void getState_shouldReturnTerminated_whenStateIsTerminated() {
        // Arrange: Mock preferences to return TERMINATED state
        when(applicationPreferences.get(ApplicationDefaults.APPLICATION_STATE_KEY, "UNKNOWN"))
                .thenReturn("TERMINATED");

        // Act: Retrieve the state
        ApplicationState state = applicationMonitor.getState();

        // Assert: Should return TERMINATED state
        assertEquals(ApplicationState.TERMINATED, state,
                "Should return TERMINATED state when preference value is TERMINATED");

        // Verify: Preferences were queried
        verify(applicationPreferences).get(ApplicationDefaults.APPLICATION_STATE_KEY, "UNKNOWN");
    }

    /**
     * Test that getState() returns UNKNOWN when no state is saved.
     * This handles the case of first launch or cleared preferences.
     */
    @Test
    void getState_shouldReturnUnknown_whenNoStateExists() {
        // Arrange: Mock preferences to return the default value (UNKNOWN)
        when(applicationPreferences.get(ApplicationDefaults.APPLICATION_STATE_KEY, "UNKNOWN"))
                .thenReturn("UNKNOWN");

        // Act: Retrieve the state
        ApplicationState state = applicationMonitor.getState();

        // Assert: Should return UNKNOWN state
        assertEquals(ApplicationState.UNKNOWN, state,
                "Should return UNKNOWN state when no state is saved");

        // Verify: Preferences were queried with the correct default value
        verify(applicationPreferences).get(ApplicationDefaults.APPLICATION_STATE_KEY, "UNKNOWN");
    }

    /**
     * Test that getState() handles invalid/corrupted state values gracefully.
     * If the preference value is corrupted or invalid, the fromString() method
     * should return null, which we handle appropriately.
     */
    @Test
    void getState_shouldHandleInvalidState_whenStateIsCorrupted() {
        // Arrange: Mock preferences to return an invalid/corrupted state value
        when(applicationPreferences.get(ApplicationDefaults.APPLICATION_STATE_KEY, "UNKNOWN"))
                .thenReturn("INVALID_STATE");

        // Act: Retrieve the state
        ApplicationState state = applicationMonitor.getState();

        // Assert: Should return null for invalid state (ApplicationState.fromString() behavior)
        assertNull(state,
                "Should return null when state value is invalid or corrupted");

        // Verify: Preferences were still queried
        verify(applicationPreferences).get(ApplicationDefaults.APPLICATION_STATE_KEY, "UNKNOWN");
    }

    // ========================================
    // Tests for constructor and initial state
    // ========================================

    /**
     * Test that the constructor properly initializes the ApplicationMonitor.
     * Verifies that dependencies are correctly injected and the initial state is set.
     */
    @Test
    void constructor_shouldInitializeWithDefaultState() {
        // Assert: Fresh instance should have a default state (previous execution safely terminated)
        assertTrue(applicationMonitor.isPreviousExecutionSafelyTerminated(),
                "Initial state should assume previous execution was safe");

        // Assert: Preferences dependency should be properly injected
        assertNotNull(applicationMonitor.applicationPreferences,
                "ApplicationPreferences should be injected");
        assertSame(applicationPreferences, applicationMonitor.applicationPreferences,
                "Injected preferences should be the same instance");
    }

    /**
     * Test that the monitor correctly handles multiple initialize() calls.
     * While Spring typically only calls @PostConstruct once, we test this edge case
     * to ensure the monitor behaves predictably if initialized multiple times.
     *
     * <p>Note: This is an edge case test to ensure robustness, though in practice
     * Spring's lifecycle management ensures @PostConstruct is only called once per bean.
     */
    @Test
    void initialize_shouldHandleMultipleCalls() throws Exception {
        // Arrange: Mock preferences
        when(applicationPreferences.get(ApplicationDefaults.APPLICATION_STATE_KEY, "UNKNOWN"))
                .thenReturn("TERMINATED");

        // Act: Call initialize multiple times
        Method initMethod = ApplicationMonitor.class.getDeclaredMethod("initialize");
        initMethod.setAccessible(true);
        initMethod.invoke(applicationMonitor);
        initMethod.invoke(applicationMonitor);

        // Assert: Should still work correctly and set RUNNING state each time
        verify(applicationPreferences, times(2)).put(
                ApplicationDefaults.APPLICATION_STATE_KEY,
                ApplicationState.RUNNING.getText()
        );

        // Previous execution should still be marked as safe (based on the first check)
        assertTrue(applicationMonitor.isPreviousExecutionSafelyTerminated());
    }

    /**
     * Test that isPreviousExecutionSafelyTerminated getter works correctly.
     * This verifies that the Lombok @Getter annotation is properly generating
     * the accessor method and that it returns the correct value.
     */
    @Test
    void isPreviousExecutionSafelyTerminated_shouldReturnCorrectValue() throws Exception {
        // Arrange: Set up a crash scenario
        when(applicationPreferences.get(ApplicationDefaults.APPLICATION_STATE_KEY, "UNKNOWN"))
                .thenReturn("RUNNING");

        // Act: Initialize and check the flag
        Method initMethod = ApplicationMonitor.class.getDeclaredMethod("initialize");
        initMethod.setAccessible(true);
        initMethod.invoke(applicationMonitor);

        // Assert: Getter should return false after detecting a crash
        assertFalse(applicationMonitor.isPreviousExecutionSafelyTerminated(),
                "Getter should return false when crash is detected");
    }
}
