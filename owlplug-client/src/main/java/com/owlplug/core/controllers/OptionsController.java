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

package com.owlplug.core.controllers;

import com.owlplug.controls.Dialog;
import com.owlplug.controls.DialogLayout;
import com.owlplug.core.components.ApplicationDefaults;
import com.owlplug.core.components.ApplicationPreferences;
import com.owlplug.core.components.DialogManager;
import com.owlplug.core.controllers.dialogs.DonateDialogController;
import com.owlplug.core.controllers.fragments.PluginPathFragmentController;
import com.owlplug.core.model.OperatingSystem;
import com.owlplug.core.services.OptionsService;
import com.owlplug.core.services.TelemetryService;
import com.owlplug.core.ui.SlidingLabel;
import com.owlplug.core.utils.PlatformUtils;
import com.owlplug.host.loaders.NativePluginLoader;
import com.owlplug.plugin.controllers.dialogs.ListDirectoryDialogController;
import com.owlplug.plugin.services.NativeHostService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import static com.owlplug.core.components.ApplicationDefaults.AU_DIRECTORY_KEY;
import static com.owlplug.core.components.ApplicationDefaults.AU_DISCOVERY_ENABLED_KEY;
import static com.owlplug.core.components.ApplicationDefaults.AU_EXTRA_DIRECTORY_KEY;
import static com.owlplug.core.components.ApplicationDefaults.LV2_DIRECTORY_KEY;
import static com.owlplug.core.components.ApplicationDefaults.LV2_DISCOVERY_ENABLED_KEY;
import static com.owlplug.core.components.ApplicationDefaults.LV2_EXTRA_DIRECTORY_KEY;
import static com.owlplug.core.components.ApplicationDefaults.NATIVE_HOST_ENABLED_KEY;
import static com.owlplug.core.components.ApplicationDefaults.STORE_BY_CREATOR_ENABLED_KEY;
import static com.owlplug.core.components.ApplicationDefaults.STORE_DIRECTORY_ENABLED_KEY;
import static com.owlplug.core.components.ApplicationDefaults.STORE_DIRECTORY_KEY;
import static com.owlplug.core.components.ApplicationDefaults.STORE_SUBDIRECTORY_ENABLED;
import static com.owlplug.core.components.ApplicationDefaults.SYNC_FILE_STAT_KEY;
import static com.owlplug.core.components.ApplicationDefaults.SYNC_PLUGINS_STARTUP_KEY;
import static com.owlplug.core.components.ApplicationDefaults.TELEMETRY_ENABLED_KEY;
import static com.owlplug.core.components.ApplicationDefaults.VST2_DISCOVERY_ENABLED_KEY;
import static com.owlplug.core.components.ApplicationDefaults.VST2_EXTRA_DIRECTORY_KEY;
import static com.owlplug.core.components.ApplicationDefaults.VST3_DIRECTORY_KEY;
import static com.owlplug.core.components.ApplicationDefaults.VST3_DISCOVERY_ENABLED_KEY;
import static com.owlplug.core.components.ApplicationDefaults.VST3_EXTRA_DIRECTORY_KEY;
import static com.owlplug.core.components.ApplicationDefaults.VST_DIRECTORY_KEY;
import static com.owlplug.core.components.ApplicationDefaults.getContributors;
import static com.owlplug.core.components.ApplicationDefaults.getLogDirectory;
import static com.owlplug.core.utils.OperationUtils.initializeNativeHostSettings;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Controller
public class OptionsController extends BaseController {

    public static final String VST2_PLUGIN = "VST2";
    public static final String VST3_PLUGIN = "VST3";
    public static final String AUDIO_UNIT_PLUGIN = "AU";
    public static final String LV2_PLUGIN = "LV2";
    private final OptionsService optionsService;
    private final NativeHostService nativeHostService;
    private final ListDirectoryDialogController listDirectoryDialogController;
    private final DonateDialogController donateDialogController;

    @FXML private CheckBox pluginNativeCheckbox;
    @FXML private ComboBox<NativePluginLoader> pluginNativeComboBox;
    @FXML private CheckBox syncPluginsCheckBox;
    @FXML private CheckBox syncFileStatCheckbox;
    @FXML private Button removeDataButton;
    @FXML private Label versionLabel;

    @FXML private Button clearCacheButton;
    @FXML private CheckBox storeSubDirectoryCheckBox;
    @FXML private CheckBox storeByCreatorCheckBox;
    @FXML private Label storeByCreatorLabel;
    @FXML private Label storeSubDirectoryLabel;
    @FXML private Label warningSubDirectory;
    @FXML private CheckBox storeDirectoryCheckBox;
    @FXML private TextField storeDirectoryTextField;
    @FXML private Label storeDirectorySeparator;
    @FXML private Hyperlink owlplugWebsiteLink;
    @FXML private VBox pluginPathContainer;

    @FXML private CheckBox telemetryCheckBox;
    @FXML private Hyperlink telemetryHyperlink;
    @FXML private Button moreFeaturesButton;
    @FXML private Button openLogsButton;
    @FXML private TextFlow versionTextFlow;

    private PluginPathFragmentController vst2PluginPathFragment;
    private PluginPathFragmentController vst3PluginPathFragment;
    private PluginPathFragmentController auPluginPathFragment;
    private PluginPathFragmentController lv2PluginPathFragment;

    public OptionsController(final OptionsService optionsService, final NativeHostService nativeHostService,
                             @Lazy final ListDirectoryDialogController listDirectoryDialogController,
                             final DonateDialogController donateDialogController, final ApplicationDefaults applicationDefaults,
                             final ApplicationPreferences applicationPreferences, final TelemetryService telemetryService,
                             final DialogManager dialogManager) {
        super(applicationDefaults, applicationPreferences, telemetryService, dialogManager);
        this.optionsService = optionsService;
        this.nativeHostService = nativeHostService;
        this.listDirectoryDialogController = listDirectoryDialogController;
        this.donateDialogController = donateDialogController;
    }

    /**
     * FXML initialize method.
     */
    @FXML
    public void initialize() {
        vst2PluginPathFragment = new PluginPathFragmentController(VST2_PLUGIN,
                VST2_DISCOVERY_ENABLED_KEY,
                VST_DIRECTORY_KEY,
                VST2_EXTRA_DIRECTORY_KEY,
                getApplicationPreferences(),
                listDirectoryDialogController,
                getApplicationDefaults());

        vst3PluginPathFragment = new PluginPathFragmentController(VST3_PLUGIN,
                VST3_DISCOVERY_ENABLED_KEY,
                VST3_DIRECTORY_KEY,
                VST3_EXTRA_DIRECTORY_KEY,
                getApplicationPreferences(),
                listDirectoryDialogController,
                getApplicationDefaults());

        auPluginPathFragment = new PluginPathFragmentController(AUDIO_UNIT_PLUGIN,
                AU_DISCOVERY_ENABLED_KEY,
                AU_DIRECTORY_KEY,
                AU_EXTRA_DIRECTORY_KEY,
                getApplicationPreferences(),
                listDirectoryDialogController,
                getApplicationDefaults());

        lv2PluginPathFragment = new PluginPathFragmentController(LV2_PLUGIN,
                LV2_DISCOVERY_ENABLED_KEY,
                LV2_DIRECTORY_KEY,
                LV2_EXTRA_DIRECTORY_KEY,
                getApplicationPreferences(),
                listDirectoryDialogController,
                getApplicationDefaults());

        pluginPathContainer.getChildren().add(vst2PluginPathFragment.getNode());
        pluginPathContainer.getChildren().add(vst3PluginPathFragment.getNode());
        pluginPathContainer.getChildren().add(auPluginPathFragment.getNode());
        pluginPathContainer.getChildren().add(lv2PluginPathFragment.getNode());

        storeByCreatorLabel.setVisible(false);
        storeSubDirectoryLabel.setVisible(false);

        initializeNativeHostSettings(getApplicationPreferences(), pluginNativeCheckbox, pluginNativeComboBox, nativeHostService);

        syncPluginsCheckBox.selectedProperty().addListener((observable, oldValue, newValue) ->
                getApplicationPreferences().putBoolean(SYNC_PLUGINS_STARTUP_KEY, newValue));

        syncFileStatCheckbox.selectedProperty().addListener((observable, oldValue, newValue) ->
                getApplicationPreferences().putBoolean(SYNC_FILE_STAT_KEY, newValue));

        storeSubDirectoryCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            getApplicationPreferences().putBoolean(STORE_SUBDIRECTORY_ENABLED, newValue);
            warningSubDirectory.setVisible(!newValue);
            storeSubDirectoryLabel.setVisible(newValue);
        });

        warningSubDirectory.managedProperty().bind(warningSubDirectory.visibleProperty());
        storeSubDirectoryLabel.managedProperty().bind(storeSubDirectoryLabel.visibleProperty());
        storeDirectorySeparator.managedProperty().bind(storeDirectorySeparator.visibleProperty());
        storeDirectoryTextField.managedProperty().bind(storeDirectoryTextField.visibleProperty());

        storeDirectoryCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            getApplicationPreferences().putBoolean(STORE_DIRECTORY_ENABLED_KEY, newValue);
            storeDirectorySeparator.setVisible(newValue);
            storeDirectoryTextField.setVisible(newValue);
        });

        storeByCreatorCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            getApplicationPreferences().putBoolean(STORE_BY_CREATOR_ENABLED_KEY, newValue);
            storeByCreatorLabel.setVisible(newValue);
        });

        storeByCreatorLabel.managedProperty().bind(storeByCreatorLabel.visibleProperty());

        storeDirectoryTextField.textProperty().addListener((observable, oldValue, newValue) ->
                getApplicationPreferences().put(STORE_DIRECTORY_KEY, newValue));

        telemetryCheckBox.selectedProperty().addListener((observable, oldValue, newValue) ->
                getApplicationPreferences().putBoolean(TELEMETRY_ENABLED_KEY, newValue));

        telemetryHyperlink.setOnAction(e -> PlatformUtils.openDefaultBrowser(getApplicationDefaults().getEnvProperty("owlplug.github.wiki.url") + "/Telemetry"));

        clearCacheButton.setOnAction(e -> optionsService.clearCache());

        removeDataButton.setOnAction(e -> {
            Dialog dialog = getDialogManager().newDialog();
            DialogLayout layout = new DialogLayout();
            layout.setHeading(new Label("Remove user data"));
            layout.setBody(new Label("""
                    Do you really want to remove all user data including accounts, \
                    stores and custom settings ?\s
                    
                    You must restart OwlPlug for a complete reset."""));

            Button cancelButton = new Button("Cancel");
            cancelButton.setOnAction(cancelEvent -> dialog.close());

            Button removeButton = new Button("Remove data");
            removeButton.setOnAction(removeEvent -> {
                dialog.close();
                optionsService.clearAllUserData();
                refreshView();

                // User data cleared twice because the refreshView() triggers UI changes that may be replicated in data
                optionsService.clearAllUserData();

            });
            removeButton.getStyleClass().add("button-danger");

            layout.setActions(removeButton, cancelButton);
            dialog.setContent(layout);
            dialog.show();
        });

        versionLabel.setText(getApplicationDefaults().getVersion());
        owlplugWebsiteLink.setOnAction(e -> PlatformUtils.openDefaultBrowser(owlplugWebsiteLink.getText()));
        moreFeaturesButton.setOnAction(e -> donateDialogController.show());
        openLogsButton.setOnAction(e -> PlatformUtils.openFromDesktop(getLogDirectory()));
        versionTextFlow.getChildren().add(new SlidingLabel(getContributors()));

        refreshView();
    }

    public void refreshView() {
        vst2PluginPathFragment.refresh();
        vst3PluginPathFragment.refresh();
        auPluginPathFragment.refresh();
        lv2PluginPathFragment.refresh();

        pluginNativeCheckbox.setDisable(!nativeHostService.isNativeHostAvailable());
        pluginNativeComboBox.setDisable(!nativeHostService.isNativeHostAvailable());
        pluginNativeCheckbox.setSelected(getApplicationPreferences().getBoolean(NATIVE_HOST_ENABLED_KEY, false));
        syncPluginsCheckBox.setSelected(getApplicationPreferences().getBoolean(SYNC_PLUGINS_STARTUP_KEY, false));
        syncFileStatCheckbox.setSelected(getApplicationPreferences().getBoolean(SYNC_FILE_STAT_KEY, true));
        storeSubDirectoryCheckBox.setSelected(getApplicationPreferences().getBoolean(STORE_SUBDIRECTORY_ENABLED, true));
        warningSubDirectory.setVisible(!getApplicationPreferences().getBoolean(STORE_SUBDIRECTORY_ENABLED, true));
        storeDirectoryCheckBox.setSelected(getApplicationPreferences().getBoolean(STORE_DIRECTORY_ENABLED_KEY, false));
        storeByCreatorCheckBox.setSelected(getApplicationPreferences().getBoolean(STORE_BY_CREATOR_ENABLED_KEY, false));
        storeDirectoryTextField.setText(getApplicationPreferences().get(STORE_DIRECTORY_KEY, EMPTY));
        telemetryCheckBox.setSelected(getApplicationPreferences().getBoolean(TELEMETRY_ENABLED_KEY, true));

        NativePluginLoader pluginLoader = nativeHostService.getCurrentPluginLoader();
        pluginNativeComboBox.getSelectionModel().select(pluginLoader);

        if (!storeDirectoryCheckBox.isSelected()) {
            storeDirectoryTextField.setVisible(false);
        }
        if (!storeByCreatorCheckBox.isSelected()) {
            storeByCreatorLabel.setVisible(false);
        }

        // Disable AU options for non-MAC users
        if (!getApplicationDefaults().getRuntimePlatform().getOperatingSystem().equals(OperatingSystem.MAC)) {
            auPluginPathFragment.disable();
        }
    }

}
