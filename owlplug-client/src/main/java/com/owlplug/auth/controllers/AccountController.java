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

package com.owlplug.auth.controllers;

import com.owlplug.auth.services.AuthenticationService;
import com.owlplug.controls.DialogLayout;
import com.owlplug.core.components.ApplicationDefaults;
import com.owlplug.core.components.ApplicationPreferences;
import com.owlplug.core.components.DialogManager;
import com.owlplug.core.components.LazyViewRegistry;
import com.owlplug.core.controllers.MainController;
import com.owlplug.core.controllers.dialogs.AbstractDialogController;
import com.owlplug.core.services.TelemetryService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * JavaFX Controller for managing user account authentication and creation.
 * This controller handles the UI interactions for initiating the Google OAuth flow,
 * displaying progress, and managing the state of the authentication process.
 * It extends {@link AbstractDialogController} to provide dialog-specific functionalities
 * and implements {@link Initializable} for FXML initialization.
 */
@Controller
public class AccountController extends AbstractDialogController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountController.class);

    /**
     * Service responsible for handling user authentication processes.
     */
    private final AuthenticationService authenticationService;
    /**
     * The main application controller, used for refreshing account information in the UI.
     */
    private final MainController mainController;
    /**
     * Registry for lazily loaded views, used to retrieve the new account view.
     */
    private final LazyViewRegistry lazyViewRegistry;

    /**
     * FXML element: Pane containing authentication action buttons.
     */
    @FXML private HBox buttonPane;
    /**
     * FXML element: Button to initiate Google authentication.
     */
    @FXML private Button googleButton;
    /**
     * FXML element: Progress indicator displayed during authentication.
     */
    @FXML private ProgressIndicator authProgressIndicator;
    /**
     * FXML element: Label to display messages to the user during authentication.
     */
    @FXML private Label messageLabel;
    /**
     * FXML element: Button to cancel the authentication process.
     */
    @FXML private Button cancelButton;
    /**
     * FXML element: Button to close the dialog.
     */
    @FXML private Button closeButton;

    /**
     * Flag indicating whether the user has pressed the cancel button during authentication.
     * This is used to differentiate between user-initiated cancellation and other authentication failures.
     */
    private boolean cancelFlag = false;

    /**
     * Constructs an {@code AccountController} with the necessary dependencies injected by Spring.
     *
     * @param applicationDefaults Provides default application settings.
     * @param applicationPreferences Manages application-specific user preferences.
     * @param telemetryService Service for collecting and sending telemetry data.
     * @param dialogManager Manages the display and lifecycle of dialogs.
     * @param authenticationService Service for handling user authentication flows.
     * @param mainController The main application controller (lazily injected to prevent circular dependencies).
     * @param lazyViewRegistry Registry for lazily loaded views.
     */
    public AccountController(final ApplicationDefaults applicationDefaults, final ApplicationPreferences applicationPreferences,
                             final TelemetryService telemetryService, final DialogManager dialogManager,
                             final AuthenticationService authenticationService, @Lazy final MainController mainController,
                             final LazyViewRegistry lazyViewRegistry) {
        // Call the superclass constructor to initialize common dialog controller dependencies.
        super(applicationDefaults, applicationPreferences, telemetryService, dialogManager);
        // Inject the authentication service.
        this.authenticationService = authenticationService;
        // Inject the main application controller.
        this.mainController = mainController;
        // Inject the lazy view registry.
        this.lazyViewRegistry = lazyViewRegistry;
    }

    /**
     * Initializes the controller after its root element has been completely processed.
     * This method sets up event handlers for UI components, specifically for the
     * Google authentication button, cancel button, and close button. It also
     * configures the visibility and managed properties of various UI elements
     * to control their layout behavior.
     *
     * @param location The location used to resolve relative paths for the root object, or {@code null} if the location is not known.
     * @param resources The resources used to localize the root object, or {@code null} if the root object was not localized.
     */
    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        // Set action for the Google authentication button.
        googleButton.setOnAction(_ -> {
            // Hide the button pane and show the progress indicator and a message.
            buttonPane.setVisible(false);
            authProgressIndicator.setVisible(true);
            messageLabel.setText("Your default browser is opening... Proceed to sign in and come back here.");
            messageLabel.setVisible(true);

            // Create a new background task for Google authentication.
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    LOGGER.debug("Google auth task started");
                    // Call the authentication service to start the Google OAuth flow.
                    authenticationService.createAccountAndAuth();
                    // Update progress to indicate completion.
                    updateProgress(1, 1);
                    return null;
                }
            };

            // Set a handler for successful completion of the authentication task.
            task.setOnSucceeded(_ -> {
                LOGGER.debug("Google auth task complete");
                // Hide the progress indicator and button pane, show the close button.
                authProgressIndicator.setVisible(false);
                buttonPane.setVisible(false);
                cancelButton.setVisible(false);
                closeButton.setVisible(true);
                // Display a success message.
                messageLabel.setText("Your account has been successfully added");
                messageLabel.setVisible(true);
                // Reset cancel flag.
                cancelFlag = false;
                // Refresh accounts in the main controller.
                mainController.refreshAccounts();
            });

            // Set a handler for a failed authentication task.
            task.setOnFailed(_ -> {
                LOGGER.debug("Google auth task failed");
                // Hide the progress indicator, show the button pane, show the close button.
                authProgressIndicator.setVisible(false);
                buttonPane.setVisible(true);
                cancelButton.setVisible(false);
                closeButton.setVisible(true);
                messageLabel.setVisible(false);

                // If the user did not cancel authentication, display an error message.
                if (!cancelFlag) {
                    messageLabel.setText("En error occurred during authentication");
                    messageLabel.setVisible(true);
                }
                // Reset cancel flag.
                cancelFlag = false;
            });

            // Show the cancel button and hide the close button before starting the task.
            cancelButton.setVisible(true);
            closeButton.setVisible(false);
            // Start the authentication task in a new thread.
            new Thread(task).start();
        });

        // Set action for the cancel button.
        cancelButton.setOnAction(_ -> {
            // Set the cancel flag and stop the authentication receiver.
            cancelFlag = true;
            authenticationService.stopAuthReceiver();
        });

        // Set action for the close button to close the dialog.
        closeButton.setOnAction(_ -> close());

        // Bind managed properties to visible properties to prevent invisible nodes from affecting the layout.
        buttonPane.managedProperty().bind(buttonPane.visibleProperty());
        authProgressIndicator.managedProperty().bind(authProgressIndicator.visibleProperty());
        messageLabel.managedProperty().bind(messageLabel.visibleProperty());
    }

    /**
     * Callback method invoked when the account dialog is shown.
     * This method resets the UI state to its initial configuration,
     * making the authentication buttons visible and hiding other elements
     * like the cancel button and message label.
     */
    @Override
    protected void onDialogShow() {
        // Make the button pane visible, allowing the user to initiate authentication.
        buttonPane.setVisible(true);
        // Hide the cancel button, as authentication has not yet started.
        cancelButton.setVisible(false);
        // Make the close button visible, allowing the user to close the dialog.
        closeButton.setVisible(true);
        // Hide any previous messages displayed to the user.
        messageLabel.setVisible(false);
    }

    /**
     * Provides the layout configuration for the account dialog.
     * This method constructs a {@link DialogLayout} and sets its body
     * to the new account view retrieved from the {@link LazyViewRegistry}.
     *
     * @return A {@link DialogLayout} object configured for the account dialog.
     */
    @Override
    protected DialogLayout getLayout() {
        // Create a new DialogLayout instance.
        final var layout = new DialogLayout();
        // Set the body of the dialog layout to the new account view,
        // retrieved lazily from the view registry.
        layout.setBody(lazyViewRegistry.get(LazyViewRegistry.NEW_ACCOUNT_VIEW));
        return layout; // Return the configured dialog layout.
    }
}
