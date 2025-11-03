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

package com.owlplug;

import com.owlplug.core.components.ApplicationDefaults;
import javafx.application.Preloader;
import javafx.application.Preloader.StateChangeNotification.Type;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import lombok.NonNull;

import static java.util.Objects.requireNonNull;

/**
 * A JavaFX {@link Preloader} that displays a splash screen during application startup.
 * <p>
 * This class is responsible for showing an initial UI to the user while the main
 * application ({@link OwlPlug}) initializes the Spring context and other resources in the background.
 *
 * <h3>Responsibilities:</h3>
 * <ul>
 *     <li><b>Displaying a Splash Screen ({@link #start(Stage)}):</b> Loads and displays an FXML-based
 *     splash screen, configured with custom styling and branding.</li>
 *     <li><b>Handling Application State ({@link #handleStateChangeNotification(StateChangeNotification)}):</b>
 *     Listens for the {@link Type#BEFORE_START} notification from the main application to hide the splash
 *     screen just before the main application window is shown.</li>
 *     <li><b>Displaying Startup Errors ({@link #handleApplicationNotification(PreloaderNotification)}):</b>
 *     Receives notifications from the main application. If an error occurs during initialization,
 *     it displays an error alert to the user.</li>
 * </ul>
 */
public class OwlPlugPreloader extends Preloader {

    private Stage preloaderStage;

    /**
     * Initializes and displays the preloader stage (splash screen).
     * This method is called by the JavaFX platform when the preloader is launched.
     * It loads the preloader FXML, sets up the scene with JMetro styling,
     * applies custom CSS, sets the stage properties (icon, title, undecorated style, size),
     * and then displays the preloader window.
     *
     * @param primaryStage The primary stage for this preloader, onto which the preloader scene can be set.
     * @throws Exception if an error occurs during FXML loading or stage setup.
     */
    @Override
    public void start(@NonNull final Stage primaryStage) throws Exception {
        // Store the primary stage for later use, specifically for hiding it.
        this.preloaderStage = primaryStage;

        // Load the preloader UI from the FXML file.
        final var fXMLLoader = new FXMLLoader(getClass().getResource("/fxml/Preloader.fxml"));
        final Parent root = fXMLLoader.load();

        // Create a new scene with the loaded root node.
        final var scene = new Scene(root);
        // Initialize JMetro for modern UI styling with a dark theme for the preloader.
        JMetro jMetro = new JMetro(Style.DARK);
        // Apply the JMetro theme to the preloader scene.
        jMetro.setScene(scene);

        // Load the application's custom CSS stylesheet and add it to the scene's stylesheets.
        final var owlPlugCssResource = requireNonNull(OwlPlugPreloader.class.getResource("/owlplug.css"), "OwlPlug CSS File Not Found").toExternalForm();
        scene.getStylesheets().add(owlPlugCssResource);

        // Set the application icon for the preloader stage.
        primaryStage.getIcons().add(ApplicationDefaults.owlplugLogo);
        // Set the title of the preloader stage.
        primaryStage.setTitle(ApplicationDefaults.APPLICATION_NAME);

        // Configure the preloader stage to be undecorated (no native window borders/title bar).
        primaryStage.initStyle(StageStyle.UNDECORATED);
        // Set the fixed dimensions for the preloader window.
        primaryStage.setWidth(600);
        primaryStage.setHeight(300);
        // Set the scene to the primary stage.
        primaryStage.setScene(scene);
        // Display the preloader stage.
        primaryStage.show();
    }

    /**
     * Handles state change notifications from the main application.
     * This method is primarily used to hide the preloader stage when the main
     * application is about to start, ensuring a smooth transition from the splash screen
     * to the main application window.
     *
     * @param stateChangeNotification The notification indicating a change in the application's state.
     */
    @Override
    public void handleStateChangeNotification(@NonNull final StateChangeNotification stateChangeNotification) {
        // Check if the notification type indicates that the main application is about to start.
        if (stateChangeNotification.getType() == Type.BEFORE_START) {
            // If the main application is starting, hide the preloader stage.
            preloaderStage.hide();
        }
    }

    /**
     * Handles application-specific notifications sent from the main application.
     * This method is used to process custom notifications, such as displaying
     * error messages during the application's initialization phase.
     *
     * @param pn The {@link PreloaderNotification} received from the application.
     */
    @Override
    public void handleApplicationNotification(@NonNull final PreloaderNotification pn) {
        // Check if the received notification is an instance of PreloaderProgressMessage.
        if (pn instanceof PreloaderProgressMessage ppm) {
            // If it's a PreloaderProgressMessage, check if its type indicates an error.
            if ("error".equals(ppm.getType())) {
                // Create a new error alert dialog.
                final var alert = new Alert(AlertType.ERROR);
                // Set the title, header, and content text for the alert using information from the PreloaderProgressMessage.
                alert.setTitle("Error");
                alert.setHeaderText("Error");
                alert.setContentText(ppm.getMessage());
                // Display the alert dialog and wait for the user to close it.
                alert.showAndWait();
            }
        }
    }
}
