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

import com.owlplug.core.components.ApplicationDefaults;
import com.owlplug.core.components.ApplicationPreferences;
import com.owlplug.core.components.DialogManager;
import com.owlplug.core.components.ImageCache;
import com.owlplug.core.controllers.BaseController;
import com.owlplug.core.services.TelemetryService;
import com.owlplug.plugin.model.PluginComponent;
import com.owlplug.plugin.services.PluginService;
import com.owlplug.plugin.ui.PluginStateView;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.Collection;

import static com.owlplug.core.utils.OperationUtils.setPluginImage;
import static com.owlplug.core.utils.OperationUtils.updateCommonLabel;

@Controller
public class ComponentInfoController extends BaseController {

    private final PluginService pluginService;
    private final ImageCache imageCache;

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
    private Label pluginReferenceLabel;

    private final Collection<String> knownPluginImages = new ArrayList<>();

    public ComponentInfoController(final ApplicationDefaults applicationDefaults, final ApplicationPreferences applicationPreferences,
                                   final TelemetryService telemetryService, final DialogManager dialogManager, PluginService pluginService, ImageCache imageCache) {
        super(applicationDefaults, applicationPreferences, telemetryService, dialogManager);
        this.pluginService = pluginService;
        this.imageCache = imageCache;
    }

    /**
     * FXML initialize method.
     */
    @FXML
    public void initialize() {
        pluginScreenshotPane.setEffect(new ColorAdjust(0, 0, -0.6, 0));
    }

    public void setComponent(PluginComponent component) {
        pluginFormatIcon.setImage(getApplicationDefaults().getPluginFormatIcon(component.getPlugin().getFormat()));
        pluginFormatLabel.setText(component.getPlugin().getFormat().getText() + " Plugin Component");
        updateCommonLabel(pluginTitleLabel, component.getName(), pluginNameLabel, component.getDescriptiveName(),
                pluginVersionLabel, component.getVersion(), pluginManufacturerLabel, component.getManufacturerName(),
                pluginIdentifierLabel, component.getUid(), pluginCategoryLabel, component.getCategory());
        pluginStateView.setPluginState(pluginService.getPluginState(component.getPlugin()));
        pluginReferenceLabel.setText(component.getIdentifier());

        setPluginImage(component.getPlugin(), pluginService, knownPluginImages, imageCache,
                getApplicationDefaults(), pluginScreenshotPane);
    }
}
