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
import com.owlplug.core.services.TelemetryService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.ResourceBundle;

import static com.owlplug.core.utils.PlatformUtils.openDefaultBrowser;

@Controller
public class DonateDialogController extends AbstractDialogController implements Initializable {

    private final LazyViewRegistry lazyViewRegistry;

    @FXML private Button donateButton;
    @FXML private Button roadmapButton;
    @FXML private Button featureRequestButton;
    @FXML private Button aboutButton;
    @FXML private Button cancelButton;

    DonateDialogController(final ApplicationDefaults applicationDefaults, final ApplicationPreferences applicationPreferences,
                           final TelemetryService telemetryService, final DialogManager dialogManager, LazyViewRegistry lazyViewRegistry) {
        super(applicationDefaults, applicationPreferences, telemetryService, dialogManager, 550, 480);
        this.lazyViewRegistry = lazyViewRegistry;
        setOverlayClose(false);
    }

    /**
     * FXML initialize.
     */
    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        donateButton.setOnAction(e -> {
            openDefaultBrowser(getApplicationDefaults().getEnvProperty("owlplug.donate.url"));
            close();
            getDialogManager().newSimpleInfoDialog("Thank  you !", "Thank you so much for contributing to OwlPlug development.\nYour donation will help me to release new versions, stay tuned !")
                    .show();
        });

        roadmapButton.setOnAction(e -> openDefaultBrowser(getApplicationDefaults().getEnvProperty("owlplug.roadmap.url")));
        featureRequestButton.setOnAction(e -> openDefaultBrowser(getApplicationDefaults().getEnvProperty("owlplug.github.issues.url")));
        aboutButton.setOnAction(e -> openDefaultBrowser(getApplicationDefaults().getEnvProperty("owlplug.about.url")));
        cancelButton.setOnAction(e -> close());
    }

    @Override
    protected DialogLayout getLayout() {
        DialogLayout dialogLayout = new DialogLayout();

        Label title = new Label("Owlplug is free !");
        title.getStyleClass().add("heading-3");

        dialogLayout.setHeading(title);

        dialogLayout.setBody(lazyViewRegistry.get(LazyViewRegistry.DONATE_VIEW));

        return dialogLayout;
    }

}
