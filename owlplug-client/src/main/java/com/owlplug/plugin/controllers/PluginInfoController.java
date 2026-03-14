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

package com.owlplug.plugin.controllers;

import com.owlplug.controls.Dialog;
import com.owlplug.controls.DialogLayout;
import com.owlplug.core.components.ApplicationDefaults;
import com.owlplug.core.components.ApplicationPreferences;
import com.owlplug.core.components.DialogManager;
import com.owlplug.core.components.ImageCache;
import com.owlplug.core.controllers.BaseController;
import com.owlplug.core.services.TelemetryService;
import com.owlplug.core.utils.PlatformUtils;
import com.owlplug.plugin.components.PluginTaskFactory;
import com.owlplug.plugin.controllers.dialogs.DisablePluginDialogController;
import com.owlplug.plugin.model.Plugin;
import com.owlplug.plugin.model.PluginComponent;
import com.owlplug.plugin.services.PluginService;
import com.owlplug.plugin.ui.PluginComponentCellFactory;
import com.owlplug.plugin.ui.PluginStateView;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import org.controlsfx.control.ToggleSwitch;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import static com.owlplug.core.utils.OperationUtils.setPluginImage;
import static com.owlplug.core.utils.OperationUtils.updateCommonLabel;

@Controller
public class PluginInfoController extends BaseController {

    private final PluginsController pluginsController;
    private final PluginService pluginService;
    private final ImageCache imageCache;
    private final PluginTaskFactory pluginTaskFactory;
    private final DisablePluginDialogController disableController;

    @FXML
    private Pane pluginScreenshotPane;
    @FXML
    private ImageView pluginFormatIcon;
    @FXML
    private Label pluginFormatLabel;
    @FXML
    private Label pluginTitleLabel;
    @FXML
    private Label pluginNameLabel;
    @FXML
    private Label pluginVersionLabel;
    @FXML
    private Label pluginIdentifierLabel;
    @FXML
    private Label pluginManufacturerLabel;
    @FXML
    private Label pluginCategoryLabel;
    @FXML
    private PluginStateView pluginStateView;
    @FXML
    private Label pluginPathLabel;
    @FXML
    private Button openDirectoryButton;
    @FXML
    private Button enableButton;
    @FXML
    private Button disableButton;
    @FXML
    private Button uninstallButton;
    @FXML
    private ListView<PluginComponent> pluginComponentListView;
    @FXML
    private ToggleSwitch nativeDiscoveryToggleButton;

    private Plugin plugin = null;

    private final Collection<String> knownPluginImages = new ArrayList<>();

    public PluginInfoController(final ApplicationDefaults applicationDefaults, final ApplicationPreferences applicationPreferences,
                                final TelemetryService telemetryService, final DialogManager dialogManager, @Lazy final PluginsController pluginsController,
                                final PluginService pluginService, final ImageCache imageCache, final PluginTaskFactory pluginTaskFactory,
                                final DisablePluginDialogController disableController) {
        super(applicationDefaults, applicationPreferences, telemetryService, dialogManager);
        this.pluginsController = pluginsController;
        this.pluginService = pluginService;
        this.imageCache = imageCache;
        this.pluginTaskFactory = pluginTaskFactory;
        this.disableController = disableController;
    }

    /**
     * FXML initialize method.
     */
    @FXML
    public void initialize() {

        pluginScreenshotPane.setEffect(new ColorAdjust(0, 0, -0.6, 0));

        openDirectoryButton.setGraphic(new ImageView(getApplicationDefaults().directoryImage));
        openDirectoryButton.setText("");
        openDirectoryButton.setOnAction(e -> {
            File pluginFile = new File(pluginPathLabel.getText());
            PlatformUtils.openFromDesktop(pluginFile.getParentFile());
        });

        uninstallButton.setOnAction(e -> showUninstallDialog());

        disableButton.setOnAction(e -> {
            if (getApplicationPreferences().getBoolean(ApplicationDefaults.SHOW_DIALOG_DISABLE_PLUGIN_KEY, true)) {
                disableController.setPlugin(plugin);
                disableController.show();
            } else {
                disableController.disablePluginWithoutPrompt(plugin);
            }
        });

        enableButton.setOnAction(e -> {
            pluginService.enablePlugin(plugin);
            setPlugin(plugin);
            pluginsController.refresh();
        });

        pluginComponentListView.setCellFactory(new PluginComponentCellFactory(getApplicationDefaults()));

        nativeDiscoveryToggleButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (plugin != null && plugin.getFootprint() != null) {
                plugin.getFootprint().setNativeDiscoveryEnabled(newValue);
                pluginService.save(plugin.getFootprint());
            }
        });

    }

    public void setPlugin(final Plugin plugin) {
        this.plugin = plugin;
        refresh();
    }

    public void refresh() {

        if (plugin == null) {
            return;
        }
        pluginFormatIcon.setImage(getApplicationDefaults().getPluginFormatIcon(plugin.getFormat()));
        pluginFormatLabel.setText(plugin.getFormat().getText() + " Plugin");
        updateCommonLabel(pluginTitleLabel, plugin.getName(), pluginNameLabel, plugin.getDescriptiveName(),
                pluginVersionLabel, plugin.getVersion(), pluginManufacturerLabel, plugin.getManufacturerName(),
                pluginIdentifierLabel, plugin.getUid(), pluginCategoryLabel, plugin.getCategory());
        pluginStateView.setPluginState(pluginService.getPluginState(plugin));
        pluginPathLabel.setText(plugin.getPath());

        File file = new File(plugin.getPath());
        uninstallButton.setDisable(!file.canWrite());

        if (plugin.isDisabled()) {
            enableButton.setManaged(true);
            enableButton.setVisible(true);
            disableButton.setManaged(false);
            disableButton.setVisible(false);
        } else {
            enableButton.setManaged(false);
            enableButton.setVisible(false);
            disableButton.setManaged(true);
            disableButton.setVisible(true);

        }

        if (plugin.getFootprint() != null) {
            nativeDiscoveryToggleButton.setSelected(plugin.getFootprint().isNativeDiscoveryEnabled());
        }

        final var components = FXCollections.observableList(new ArrayList<>(plugin.getComponents()));
        pluginComponentListView.setItems(components);

        setPluginImage(plugin, pluginService, knownPluginImages, imageCache, getApplicationDefaults(), pluginScreenshotPane);
    }

    private void showUninstallDialog() {

        Dialog dialog = getDialogManager().newDialog();
        DialogLayout layout = new DialogLayout();

        layout.setHeading(new Label("Remove plugin"));
        layout.setBody(new Label("Do you really want to remove " + plugin.getName()
                + " ? This will permanently delete the file from your hard drive."));

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(cancelEvent -> dialog.close());

        Button removeButton = new Button("Remove");
        removeButton.setOnAction(removeEvent -> {
            dialog.close();
            pluginTaskFactory.createPluginRemoveTask(plugin)
                    .setOnSucceeded(x -> pluginsController.displayPlugins()).schedule();
        });
        removeButton.getStyleClass().add("button-danger");

        layout.setActions(removeButton, cancelButton);
        dialog.setContent(layout);
        dialog.show();
    }

}
