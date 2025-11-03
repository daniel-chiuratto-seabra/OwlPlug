/* OwlPlug
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

package com.owlplug;

import com.owlplug.core.components.ApplicationDefaults;
import com.owlplug.core.controllers.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import lombok.NonNull;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/**
 * The main entry point for the OwlPlug JavaFX application.
 * <p>
 * This class extends {@link Application} and is responsible for orchestrating the application's lifecycle.
 * It integrates the Spring Boot framework with the JavaFX UI layer.
 *
 * <h3>Responsibilities:</h3>
 * <ul>
 *     <li><b>Initialization ({@link #init()}):</b> Bootstraps the Spring {@link ConfigurableApplicationContext},
 *     loads the main FXML view, and sets up the controller factory to enable dependency injection in JavaFX controllers.
 *     It also handles startup errors and communicates them to the {@link OwlPlugPreloader}.</li>
 *     <li><b>UI Setup ({@link #start(Stage)}):</b> Configures and displays the primary application window ({@link Stage}),
 *     applying themes (JMetro) and custom stylesheets.</li>
 *     <li><b>Shutdown ({@link #stop()}):</b> Ensures a graceful shutdown by closing the Spring application context
 *     when the JavaFX application terminates.</li>
 * </ul>
 */
@Component
public class OwlPlug extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(OwlPlug.class);

    private ConfigurableApplicationContext context;
    private Parent rootNode;

    /**
     * Initializes the JavaFX application. This method is called by the JavaFX
     * platform prior to the {@code start()} method. It bootstraps the Spring Boot
     * application context, autowires this {@code OwlPlug} instance, loads the
     * main FXML view, and dispatches post-initialization tasks. It also includes
     * error handling for issues during context initialization, such as a running
     * instance of OwlPlug or other startup failures.
     *
     * @throws Exception if an error occurs during the initialization process,
     *                   preventing the application from starting correctly.
     */
    @Override
    public void init() throws Exception {
        try {
            // Start the Spring Boot application context using the Bootstrap class.
            // This initializes all Spring-managed beans and configurations.
            context = SpringApplication.run(Bootstrap.class, getParameters().getRaw().toArray(new String[0]));

            // Autowire this OwlPlug instance with dependencies from the Spring context.
            // This allows Spring to inject any required beans into this class.
            context.getAutowireCapableBeanFactory().autowireBean(this);

            // Load the main application view from the FXML file.
            final var loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
            // Set the controller factory to allow Spring to manage the lifecycle
            // and dependencies of the FXML controllers.
            loader.setControllerFactory(context::getBean);
            rootNode = loader.load();

            // Dispatch any post-initialization logic required by the MainController.
            context.getBean(MainController.class).dispatchPostInitialize();
        } catch (BeanCreationException e) {
            // Handle specific BeanCreationException, often indicating issues with Spring context.
            if (e.getRootCause() instanceof HibernateException) {
                // Log and notify preloader if a HibernateException suggests OwlPlug is already running.
                LOGGER.error("OwlPlug is maybe already running", e);
                notifyPreloader(new PreloaderProgressMessage("error", "OwlPlug is maybe already running"));
            } else {
                // Log and notify preloader for other BeanCreationException errors.
                LOGGER.error("Error during application context initialization", e);
                notifyPreloader(new PreloaderProgressMessage("error", "Error during application context initialization"));
            }
            throw e; // Re-throw the exception to halt application startup.
        } catch (Exception e) {
            // Catch any other general exceptions during initialization.
            LOGGER.error("OwlPlug could not be started", e);
            notifyPreloader(new PreloaderProgressMessage("error", "OwlPlug could not be started"));
            throw e; // Re-throw the exception.
        }
    }

    /**
     * The main entry point for the JavaFX application. This method is called after
     * the {@link #init()} method has returned and the application is ready to be displayed.
     * It sets up the primary stage, configures the scene with the loaded root node,
     * applies styling, sets the application icon and title, and displays the main window.
     *
     * @param primaryStage The primary stage for this application, onto which the application scene can be set.
     *                     The first stage is constructed by the platform.
     * @throws Exception if an error occurs during the setup or display of the primary stage.
     */
    @Override
    public void start(@NonNull final Stage primaryStage) throws Exception {
        // Define the initial width and height for the application window.
        final var width = 1050d;
        final var height = 800d;

        // Create a new Scene with the pre-loaded rootNode and specified dimensions.
        final var scene = new Scene(rootNode, width, height);
        // Initialize JMetro for modern UI styling with a dark theme.
        final var metroTheme = new JMetro(Style.DARK);

        // Apply the JMetro theme to the scene.
        metroTheme.setScene(scene);

        // Load the application's custom CSS stylesheet and add it to the JMetro overriding stylesheets.
        final var owlPlugCssResource = requireNonNull(OwlPlug.class.getResource("/owlplug.css"), "OwlPlug CSS file not found").toExternalForm();
        metroTheme.getOverridingStylesheets().add(owlPlugCssResource);

        // Set the application icon for the primary stage.
        primaryStage.getIcons().add(ApplicationDefaults.owlplugLogo);
        // Set the title of the primary stage using the application's defined name.
        primaryStage.setTitle(ApplicationDefaults.APPLICATION_NAME);

        // Set the configured scene to the primary stage.
        primaryStage.setScene(scene);
        // Set the initial and minimum dimensions for the primary stage.
        primaryStage.setHeight(height);
        primaryStage.setWidth(width);
        primaryStage.setMinHeight(height);
        primaryStage.setMinWidth(width);
        // Center the primary stage on the screen.
        primaryStage.centerOnScreen();

        // Display the primary stage, making the application window visible to the user.
        primaryStage.show();
    }

    /**
     * This method is called by the JavaFX platform when the application is
     * requested to close. It performs necessary cleanup operations,
     * such as closing the Spring application context to release resources.
     */
    @Override
    public void stop() {
        // Close the Spring application context, which handles the graceful shutdown
        // of all beans and releases resources managed by Spring.
        context.close();
    }

}
