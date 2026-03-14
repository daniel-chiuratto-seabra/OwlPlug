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

package com.owlplug.plugin.controllers.dialogs;

import com.owlplug.controls.DialogLayout;
import com.owlplug.core.components.ApplicationDefaults;
import com.owlplug.core.components.ApplicationPreferences;
import com.owlplug.core.components.DialogManager;
import com.owlplug.core.components.LazyViewRegistry;
import com.owlplug.core.controllers.OptionsController;
import com.owlplug.core.controllers.dialogs.AbstractDialogController;
import com.owlplug.core.controllers.dialogs.WelcomeDialogController;
import com.owlplug.core.services.TelemetryService;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ListDirectoryDialogController extends AbstractDialogController implements ListChangeListener<String> {

    private final OptionsController optionsController;
    private final WelcomeDialogController welcomeDialogController;
    private final LazyViewRegistry lazyViewRegistry;

    @FXML private Button addDirectoryButton;
    @FXML private ListView<String> directoryListView;
    @FXML private Button closeButton;

    private final String newDirectoryItem = "[New directory] (double-click to update)";
    private String currentPreferenceKey;
    private ObservableList<String> observableItems;

    public ListDirectoryDialogController(final ApplicationDefaults applicationDefaults, final ApplicationPreferences applicationPreferences,
                                         final TelemetryService telemetryService, final DialogManager dialogManager,
                                         @Lazy final OptionsController optionsController, @Lazy final WelcomeDialogController welcomeDialogController,
                                         final LazyViewRegistry lazyViewRegistry) {
        super(applicationDefaults, applicationPreferences, telemetryService, dialogManager);
        this.optionsController = optionsController;
        this.welcomeDialogController = welcomeDialogController;
        this.lazyViewRegistry = lazyViewRegistry;
    }

    public void initialize() {
        addDirectoryButton.setOnAction(e -> observableItems.add(newDirectoryItem));
        directoryListView.setCellFactory(TextFieldListCell.forListView());
        closeButton.setOnAction(e -> close());
    }

    public void configure(String preferenceKey) {
        this.currentPreferenceKey = preferenceKey;

        if (observableItems != null) {
            observableItems.removeListener(this);
        }

        List<String> items = this.getApplicationPreferences().getList(preferenceKey, new ArrayList<>());
        observableItems = FXCollections.observableArrayList(items);

        observableItems.addListener(this);
        directoryListView.setItems(observableItems);
        directoryListView.refresh();
    }


    @Override
    public void onChanged(Change<? extends String> change) {

        ObservableList<? extends String> eventList = change.getList();
        // Remove blank entries in the backed list
        eventList.removeIf(String::isBlank);

        // Create a new list and filter it before saving it to preferences
        List<String> prefList = new ArrayList<>(change.getList());
        prefList.removeIf(x -> x.isBlank() || x.equals(newDirectoryItem));
        this.getApplicationPreferences().putList(currentPreferenceKey, prefList);

        optionsController.refreshView();
        welcomeDialogController.refreshView();
    }


    protected DialogLayout getLayout() {
        DialogLayout layout = new DialogLayout();
        layout.setBody(lazyViewRegistry.get(LazyViewRegistry.LIST_DIRECTORY_VIEW));
        return layout;
    }

}
