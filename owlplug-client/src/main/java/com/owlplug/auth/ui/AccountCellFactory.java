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

package com.owlplug.auth.ui;

import com.owlplug.auth.model.UserAccount;
import com.owlplug.auth.services.AuthenticationService;
import com.owlplug.core.components.ImageCache;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.Callback;

/**
 * A custom {@link Callback} implementation for creating {@link ListCell} instances
 * to display {@link AccountItem} objects in a JavaFX {@link ListView}.
 * This factory provides a flexible way to render user account information,
 * including an icon, name, and an optional delete button, with customizable alignment.
 */
public class AccountCellFactory implements Callback<ListView<AccountItem>, ListCell<AccountItem>> {

    /**
     * Flag to determine if a delete button should be shown for each account item.
     * Defaults to {@code false}.
     */
    private boolean showDeleteButton = false;
    /**
     * The authentication service, used for deleting user accounts.
     * This is {@code null} if {@code showDeleteButton} is {@code false}.
     */
    private AuthenticationService authenticationService = null;
    /**
     * The alignment of the content within the list cell.
     * Defaults to {@link Pos#CENTER_LEFT}.
     */
    private Pos align = Pos.CENTER_LEFT;

    /**
     * The image cache instance used for loading and caching user account icons.
     */
    private final ImageCache imageCache;

    /**
     * Constructs an {@code AccountCellFactory} with a specified image cache and content alignment.
     * This constructor is suitable when no delete functionality is required for the account items.
     *
     * @param imageCache The {@link ImageCache} instance to use for loading images.
     * @param align The {@link Pos} alignment for the cell's content.
     */
    public AccountCellFactory(ImageCache imageCache, Pos align) {
        // Initialize the image cache.
        this.imageCache = imageCache;
        // Set the content alignment.
        this.align = align;
    }

    /**
     * Constructs an {@code AccountCellFactory} with an authentication service, image cache,
     * and a flag to control the visibility of the delete button.
     * This constructor is used when account deletion functionality is desired.
     *
     * @param authenticationService The {@link AuthenticationService} instance for handling account deletions.
     * @param imageCache The {@link ImageCache} instance to use for loading images.
     * @param showDeleteButton A boolean flag; if {@code true}, a delete button will be displayed for each account.
     */
    public AccountCellFactory(AuthenticationService authenticationService, ImageCache imageCache,
                              boolean showDeleteButton) {

        // Set the flag to show or hide the delete button.
        this.showDeleteButton = showDeleteButton;
        // Initialize the authentication service.
        this.authenticationService = authenticationService;
        // Initialize the image cache.
        this.imageCache = imageCache;
    }

    /**
     * Creates a new {@link ListCell} for the given {@link ListView}.
     * This method is part of the {@link Callback} interface and is responsible
     * for instantiating and configuring the visual representation of an {@link AccountItem}.
     *
     * @param listView The {@link ListView} for which the cell is being created.
     * @return A new {@link ListCell} instance configured to display an {@link AccountItem}.
     */
    @Override
    public ListCell<AccountItem> call(ListView<AccountItem> listView) {
        return new ListCell<>() {
            /**
             * Updates the item in the cell. This method is called by the ListView
             * to update the contents of the cell with new data.
             *
             * @param item The new item to display in the cell.
             * @param empty Whether the cell is empty (i.e., not displaying any data).
             */
            @Override
            protected void updateItem(AccountItem item, boolean empty) {
                super.updateItem(item, empty);
                // Set the alignment of the cell's content.
                setAlignment(align);

                // If the item is a UserAccount, configure the cell to display user details.
                if (item instanceof UserAccount account) {
                    // Create an HBox to arrange the cell's content horizontally.
                    HBox cell = new HBox();
                    cell.setSpacing(5); // Set spacing between elements.
                    cell.setAlignment(align); // Align content within the HBox.

                    // If the account has an icon URL, load and display the icon.
                    if (account.getIconUrl() != null) {
                        // Retrieve the image from the cache.
                        Image image = imageCache.get(account.getIconUrl());
                        ImageView imageView = new ImageView(image);
                        imageView.setFitWidth(32); // Set icon width.
                        imageView.setFitHeight(32); // Set icon height.
                        cell.getChildren().add(imageView); // Add icon to the cell.
                    }

                    // Create and add a label for the account name.
                    Label label = new Label(account.getName());
                    cell.getChildren().add(label);

                    // If the showDeleteButton flag is true, add a delete button.
                    if (showDeleteButton) {
                        // Add a growing region to push the delete button to the right.
                        Region growingArea = new Region();
                        HBox.setHgrow(growingArea, Priority.ALWAYS);
                        cell.getChildren().add(growingArea);
                        // Create and configure the delete button.
                        Hyperlink deleteButton = new Hyperlink("X");
                        deleteButton.getStyleClass().add("hyperlink-button");
                        cell.getChildren().add(deleteButton);

                        // Set action for the delete button to delete the account.
                        deleteButton.setOnAction(e -> authenticationService.deleteAccount(account));
                    }

                    // Set the HBox as the graphic content of the cell.
                    setGraphic(cell);
                    setText(null); // Clear any text content.
                    return; // Exit the method.
                }

                // If the item is an AccountMenuItem, configure the cell to display menu item text.
                if (item instanceof AccountMenuItem accountMenuItem) {
                    setGraphic(null); // Clear any graphic content.
                    setText(accountMenuItem.getText()); // Set the text content.
                }
            }
        };
    }

}
