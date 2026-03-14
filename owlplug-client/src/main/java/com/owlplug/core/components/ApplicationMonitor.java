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

import com.owlplug.core.model.ApplicationState;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * A Spring component that monitors the application lifecycle and tracks shutdown safety.
 * This component helps detect unexpected application termination (crashes) by maintaining
 * an application state flag in preferences. When the application starts, it checks if the
 * previous instance was properly terminated. If not, it indicates a crash or forced termination.
 *
 * <p>The lifecycle works as follows:
 * <ul>
 *   <li>On startup ({@code @PostConstruct}): Checks if previous state is RUNNING (crash detected)
 *       and sets state to RUNNING</li>
 *   <li>On shutdown ({@code @PreDestroy}): Sets state to TERMINATED (clean exit)</li>
 * </ul>
 *
 * <p>This information can be used to:
 * <ul>
 *   <li>Show crash recovery dialogs to the user</li>
 *   <li>Log crash incidents for debugging</li>
 *   <li>Perform recovery operations after crashes</li>
 *   <li>Track application stability metrics</li>
 * </ul>
 */
@Component
public class ApplicationMonitor {

    /**
     * Logger instance for recording application monitoring events.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationMonitor.class);

    /**
     * Flag indicating whether the previous application execution terminated safely.
     * This is set to {@code false} if the previous instance crashed or was forcefully terminated
     * without going through the proper shutdown sequence.
     * Exposed via Lombok's {@code @Getter} for external access.
     */
    @Getter
    private boolean previousExecutionSafelyTerminated = true;

    /**
     * Application preferences service for persisting and retrieving application state.
     * Used to store the current execution state across application restarts.
     */
    protected final ApplicationPreferences applicationPreferences;

    /**
     * Constructs an ApplicationMonitor with the required dependencies.
     * This constructor is invoked by Spring's dependency injection mechanism.
     *
     * @param applicationPreferences the preference service for state persistence
     */
    public ApplicationMonitor(final ApplicationPreferences applicationPreferences) {
        this.applicationPreferences = applicationPreferences;
    }

    /**
     * Initializes the application monitor after the bean is constructed.
     * This method is automatically called by Spring after the dependency injection is complete.
     *
     * <p>Initialization process:
     * <ol>
     *   <li>Retrieves the previous application state from preferences</li>
     *   <li>If state is RUNNING, the previous instance didn't terminate cleanly (crash detected)</li>
     *   <li>Sets the current state to RUNNING to track this execution</li>
     * </ol>
     *
     * <p>The crash detection works because:
     * <ul>
     *   <li>Normal shutdown: State changes from RUNNING → TERMINATED (in {@link #destroy()})</li>
     *   <li>Crash/Force quit: State remains RUNNING (destroy() never called)</li>
     *   <li>Next startup: RUNNING state indicates previous crash</li>
     * </ul>
     */
    @PostConstruct
    private void initialize() {
        LOGGER.info("Application monitor started");

        // Retrieve the last saved application state from preferences
        final var applicationState = getState();

        // If the previous state is RUNNING, it means the application didn't shut down properly
        // (crashed, killed, or force-quit before @PreDestroy could execute)
        if (applicationState.equals(ApplicationState.RUNNING)) {
            LOGGER.info("Previous application instance not terminated safely");
            previousExecutionSafelyTerminated = false;
        }

        // Mark the application as currently running - this will remain until clean shutdown
        applicationPreferences.put(ApplicationDefaults.APPLICATION_STATE_KEY, ApplicationState.RUNNING.getText());
    }

    /**
     * Handles cleanup when the application is shutting down.
     * This method is automatically called by Spring during the shutdown sequence,
     * before the application context is destroyed.
     *
     * <p>This marks the application state as TERMINATED, indicating a clean shutdown.
     * If this method executes successfully, the next application startup will know
     * that this instance terminated properly.
     *
     * <p>Note: This method will NOT be called if:
     * <ul>
     *   <li>The application crashes</li>
     *   <li>The JVM is forcefully killed (kill -9, Task Manager end task, etc.)</li>
     *   <li>The system loses power</li>
     *   <li>An unhandled exception prevents proper shutdown</li>
     * </ul>
     */
    @PreDestroy
    private void destroy() {
        LOGGER.info("Application monitor received shutdown event");

        // Mark the application as cleanly terminated - this indicates a successful shutdown
        applicationPreferences.put(ApplicationDefaults.APPLICATION_STATE_KEY, ApplicationState.TERMINATED.getText());
    }

    /**
     * Retrieves the last saved application state from preferences.
     * This state persists across application restarts and is used to detect crashes.
     *
     * <p>Possible states:
     * <ul>
     *   <li>{@link ApplicationState#RUNNING} - Application is/was running (current or crashed)</li>
     *   <li>{@link ApplicationState#TERMINATED} - Application shut down cleanly</li>
     *   <li>{@link ApplicationState#UNKNOWN} - First launch or state unavailable</li>
     * </ul>
     *
     * @return the last saved {@link ApplicationState}, or UNKNOWN if not found
     */
    public ApplicationState getState() {
        // Retrieve the state string from preferences, defaulting to "UNKNOWN" if not present
        String stateValue = applicationPreferences.get(ApplicationDefaults.APPLICATION_STATE_KEY, "UNKNOWN");

        // Convert the string representation back to an ApplicationState enum
        return ApplicationState.fromString(stateValue);
    }

}
