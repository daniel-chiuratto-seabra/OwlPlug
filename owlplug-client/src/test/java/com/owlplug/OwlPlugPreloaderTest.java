/* OwlPlug
 * Copyright (C) 2021-2024 Arthur <dropsnorz@gmail.com>
 *
 * This file is part of OwlPlug.
 *
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

package com.owlplug;

import com.owlplug.core.components.ApplicationDefaults;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.application.Preloader.StateChangeNotification.Type;
import javafx.scene.Scene;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the {@link OwlPlugPreloader} class.
 * <p>
 * This class verifies the behavior of the application's splash screen preloader.
 * It uses the TestFX framework to interact with and assert the state of JavaFX components
 * in a headless environment.
 *
 * <h3>Test Scenarios:</h3>
 * <ul>
 *     <li><b>Stage Initialization:</b> Ensures the preloader stage is correctly created, styled, and displayed
 *     by the {@link OwlPlugPreloader#start(Stage)} method.</li>
 *     <li><b>State Change Handling:</b> Verifies that the preloader stage is hidden upon receiving a
 *     {@link javafx.application.Preloader.StateChangeNotification.Type#BEFORE_START} notification.</li>
 *     <li><b>Application Notification Handling:</b>
 *         <ul>
 *             <li>Confirms that an error {@link javafx.scene.control.Alert} is shown when an error message is received.</li>
 *             <li>Ensures that other notification types are handled gracefully without causing errors.</li>
 *         </ul>
 *     </li>
 * </ul>
 * <p>
 * All UI interactions are carefully managed to run on the JavaFX Application Thread to prevent common threading issues.
 */
@ExtendWith(ApplicationExtension.class)
class OwlPlugPreloaderTest {

    private OwlPlugPreloader preloader;

    @Start
    private void start(final Stage stage) {
        this.preloader = new OwlPlugPreloader();
    }

    @BeforeEach
    void setUp() {
        // Ensure ApplicationDefaults icon is loaded for tests
        // This is a simplified way to ensure the static resource is available.
        // In a real scenario, you might need to initialize it properly if it's not a simple resource load.
        assertNotNull(ApplicationDefaults.owlplugLogo, "Application logo should be loaded");
    }

    @AfterEach
    void tearDown(FxRobot robot) {
        // Close any alert dialogs that might be open
        robot.lookup(".dialog-pane").queryAllAs(DialogPane.class).forEach(dialogPane -> {
            Stage stage = (Stage) dialogPane.getScene().getWindow();
            stage.close();
        });
    }

    @Test
    void testStart(FxRobot robot) {
        // WHEN
        final Stage[] stageContainer = new Stage[1];
        robotInteract(robot, stageContainer);

        // THEN
        final Stage preloaderStage = stageContainer[0];
        assertTrue(preloaderStage.isShowing());
        assertEquals(ApplicationDefaults.APPLICATION_NAME, preloaderStage.getTitle());
        assertEquals(StageStyle.UNDECORATED, preloaderStage.getStyle());
        assertEquals(600, preloaderStage.getWidth());
        assertEquals(300, preloaderStage.getHeight());
        assertNotNull(preloaderStage.getScene());
        assertFalse(preloaderStage.getIcons().isEmpty());
        assertEquals(ApplicationDefaults.owlplugLogo, preloaderStage.getIcons().getFirst());

        Scene scene = preloaderStage.getScene();
        assertNotNull(scene.getRoot());
        assertTrue(scene.getStylesheets().stream().anyMatch(s -> s.endsWith("/owlplug.css")));
    }

    @Test
    void testHandleStateChangeNotification_BeforeStart(FxRobot robot) {
        // GIVEN
        final Stage[] stageContainer = new Stage[1];
        robotInteract(robot, stageContainer);
        final Stage preloaderStage = stageContainer[0];
        assertTrue(preloaderStage.isShowing(), "Preloader stage should be showing after start");
        var notification = new Preloader.StateChangeNotification(Type.BEFORE_START);

        // WHEN
        robot.interact(() -> preloader.handleStateChangeNotification(notification));

        // THEN
        assertFalse(preloaderStage.isShowing());
    }

    @Test
    void testHandleStateChangeNotification_OtherType(FxRobot robot) {
        // GIVEN
        final Stage[] stageContainer = new Stage[1];
        robotInteract(robot, stageContainer);
        final Stage preloaderStage = stageContainer[0];
        assertTrue(preloaderStage.isShowing(), "Preloader stage should be showing after start");
        var notification = new Preloader.StateChangeNotification(Type.BEFORE_LOAD);

        // WHEN
        robot.interact(() -> preloader.handleStateChangeNotification(notification));

        // THEN
        assertTrue(preloaderStage.isShowing());
    }

    @Test
    void testHandleApplicationNotification_Error(FxRobot robot) {
        // GIVEN
        String errorMessage = "Something went wrong!";
        var notification = new PreloaderProgressMessage("error", errorMessage);

        // WHEN
        // Use Platform.runLater inside interact to show the dialog without blocking the test thread.
        // This avoids a deadlock where showAndWait() would block interact() from completing.
        robot.interact(() -> Platform.runLater(() -> preloader.handleApplicationNotification(notification)));

        // THEN
        // The .alert style class is on the DialogPane
        DialogPane dialogPane = robot.lookup(".alert").queryAs(DialogPane.class);
        assertNotNull(dialogPane);
        assertEquals("Error", dialogPane.getHeaderText());
        assertEquals(errorMessage, dialogPane.getContentText());

        Stage alertStage = (Stage) dialogPane.getScene().getWindow();
        assertEquals("Error", alertStage.getTitle());

        // Close the alert to allow the test to finish
        robot.clickOn("OK");
    }

    @Test
    void testHandleApplicationNotification_NonErrorProgressMessage(FxRobot robot) {
        // GIVEN
        var notification = new PreloaderProgressMessage("progress", "50%");

        // WHEN & THEN
        // No exception should be thrown, and no dialog should appear.
        assertDoesNotThrow(() -> robot.interact(() -> preloader.handleApplicationNotification(notification)));
    }

    @Test
    void testHandleApplicationNotification_OtherNotification(FxRobot robot) {
        // GIVEN
        var notification = new Preloader.PreloaderNotification() {};

        // WHEN & THEN
        // No exception should be thrown, and no dialog should appear.
        assertDoesNotThrow(() -> robot.interact(() -> preloader.handleApplicationNotification(notification)));
    }

    /**
     * Helper method to initialize the preloader stage on the JavaFX Application Thread.
     * <p>
     * This is necessary because creating a {@link Stage} and calling {@link Preloader#start(Stage)}
     * are UI operations that must not be performed on the main test thread.
     * It uses a container array to pass the newly created stage back to the calling test method
     * for subsequent assertions.
     *
     * @param robot          The {@link FxRobot} instance to perform the interaction.
     * @param stageContainer A one-element array to hold the created {@link Stage}.
     */
    private void robotInteract(@NonNull final FxRobot robot, @NonNull final Stage[] stageContainer) {
        robot.interact(() -> {
            try {
                stageContainer[0] = new Stage();
                preloader.start(stageContainer[0]);
            } catch (Exception e) {
                Assertions.fail("Preloader start failed", e);
            }
        });
    }
    
}
