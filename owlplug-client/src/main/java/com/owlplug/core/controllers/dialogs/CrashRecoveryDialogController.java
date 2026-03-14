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

package com.owlplug.core.controllers.dialogs;

import com.owlplug.controls.DialogLayout;
import com.owlplug.core.components.ApplicationDefaults;
import com.owlplug.core.components.ApplicationPreferences;
import com.owlplug.core.components.DialogManager;
import com.owlplug.core.components.LazyViewRegistry;
import com.owlplug.core.controllers.OptionsController;
import com.owlplug.core.services.TelemetryService;
import com.owlplug.core.utils.PlatformUtils;
import com.owlplug.host.loaders.NativePluginLoader;
import com.owlplug.plugin.services.NativeHostService;
import com.owlplug.plugin.services.PluginService;
import com.owlplug.plugin.ui.RecoveredPluginView;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import static com.owlplug.core.components.ApplicationDefaults.NATIVE_HOST_ENABLED_KEY;
import static com.owlplug.core.utils.OperationUtils.initializeNativeHostSettings;
import static com.owlplug.core.utils.PlatformUtils.openDefaultBrowser;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Controller
public class CrashRecoveryDialogController extends AbstractDialogController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrashRecoveryDialogController.class);

    private final LazyViewRegistry lazyViewRegistry;
    private final PluginService pluginService;
    private final NativeHostService nativeHostService;
    private final OptionsController optionsController;

    @FXML protected CheckBox nativeDiscoveryCheckbox;
    @FXML protected ComboBox<NativePluginLoader> pluginNativeComboBox;
    @FXML protected Button closeButton;
    @FXML protected Button openLogsButton;
    @FXML protected Hyperlink troubleshootingLink;
    @FXML protected Hyperlink issuesLink;
    @FXML protected VBox pluginListContainer;
    @FXML protected Pane incompleteSyncPane;

    CrashRecoveryDialogController(final ApplicationDefaults applicationDefaults, final ApplicationPreferences applicationPreferences,
                                  final TelemetryService telemetryService, final DialogManager dialogManager,
                                  final LazyViewRegistry lazyViewRegistry, final PluginService pluginService,
                                  final NativeHostService nativeHostService, final OptionsController optionsController) {
        super(applicationDefaults, applicationPreferences, telemetryService, dialogManager, 600, 550);
        this.lazyViewRegistry = lazyViewRegistry;
        this.pluginService = pluginService;
        this.nativeHostService = nativeHostService;
        this.optionsController = optionsController;
        setOverlayClose(false);
    }

    /**
     * FXML initialize.
     */
    public void initialize() {

        nativeDiscoveryCheckbox.setDisable(nativeHostService.isNativeHostUnavailable());
        nativeDiscoveryCheckbox.setSelected(getApplicationPreferences().getBoolean(NATIVE_HOST_ENABLED_KEY, false));

        initializeNativeHostSettings(getApplicationPreferences(), nativeDiscoveryCheckbox, pluginNativeComboBox, nativeHostService);

        pluginNativeComboBox.setDisable(nativeHostService.isNativeHostUnavailable());
        NativePluginLoader pluginLoader = nativeHostService.getCurrentPluginLoader();
        pluginNativeComboBox.getSelectionModel().select(pluginLoader);

        troubleshootingLink.setOnAction(_ -> openDefaultBrowser(
            getApplicationDefaults().getWikiUrl()
        ));
        issuesLink.setOnAction(_ -> openDefaultBrowser(
            getApplicationDefaults().getIssuesUrl()
        ));

        closeButton.setOnAction(_ -> {
            optionsController.refreshView();
            close();
        });

        openLogsButton.setOnAction(_ -> PlatformUtils.openFromDesktop(ApplicationDefaults.getLogDirectory()));

        final var incompleteSyncPlugins = pluginService.getSyncIncompletePlugins();
        if (isNotEmpty(incompleteSyncPlugins)) {
            incompleteSyncPane.setVisible(true);
            for (final var plugin : incompleteSyncPlugins) {
                LOGGER.info("Last scan for plugin {} is incomplete", plugin.getName());
                RecoveredPluginView pluginView = new RecoveredPluginView(plugin, pluginService, getApplicationDefaults());
                pluginListContainer.getChildren().add(pluginView);

                getTelemetryService().event("/Error/PluginScanIncomplete", p -> {
                    p.put("nativeDiscoveryLoader", getApplicationPreferences().get(ApplicationDefaults.PREFERRED_NATIVE_LOADER, "unknown"));
                    p.put("pluginName", plugin.getName());
                    p.put("pluginFormat", plugin.getFormat().getText());
                });
            }
        } else {
            incompleteSyncPane.setVisible(false);
        }

    }

    protected DialogLayout getLayout() {
        final var layout = new DialogLayout();

        final var title = new Label("Ooh, something wrong happened :(");
        title.getStyleClass().add("heading-3");
        layout.setHeading(title);
        layout.setBody(lazyViewRegistry.get(LazyViewRegistry.CRASH_RECOVERY_VIEW));

        return layout;
    }

}
