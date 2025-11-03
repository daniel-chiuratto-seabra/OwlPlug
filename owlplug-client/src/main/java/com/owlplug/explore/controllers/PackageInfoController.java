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

package com.owlplug.explore.controllers;

import com.owlplug.core.components.ApplicationDefaults;
import com.owlplug.core.components.ApplicationPreferences;
import com.owlplug.core.components.DialogManager;
import com.owlplug.core.components.ImageCache;
import com.owlplug.core.controllers.BaseController;
import com.owlplug.core.services.TelemetryService;
import com.owlplug.core.ui.SideBar;
import com.owlplug.core.utils.PlatformUtils;
import com.owlplug.explore.model.PackageBundle;
import com.owlplug.explore.model.PackageTag;
import com.owlplug.explore.model.RemotePackage;
import com.owlplug.explore.ui.PackageBundlesView;
import com.owlplug.explore.ui.PackageSourceBadgeView;
import com.owlplug.plugin.model.PluginType;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.springframework.stereotype.Controller;

import static com.owlplug.explore.controllers.common.CommonOperations.setBackground;

@Controller
public class PackageInfoController extends BaseController {

    private final ExploreController exploreController;
    private final ImageCache imageCache;

    @FXML private Pane packageInfoContainer;
    @FXML private Pane packageInfoContent;
    @FXML private Pane screenshotBackgroundPane;
    @FXML private Button closeButton;
    @FXML private Label nameLabel;
    @FXML private Label remoteSourceLabel;
    @FXML private Button donateButton;
    @FXML private Button browsePageButton;
    @FXML private Button installButton;
    @FXML private Hyperlink creatorLink;
    @FXML private Label licenseLabel;
    @FXML private Label versionLabel;
    @FXML private Label typeLabel;
    @FXML private FlowPane tagContainer;
    @FXML private Label descriptionLabel;
    @FXML private Pane bundlesContainer;
    @FXML private Pane headerContainer;

    private PackageBundlesView bundlesView;

    private SideBar sidebar;

    public PackageInfoController(final ApplicationDefaults applicationDefaults, final ApplicationPreferences applicationPreferences,
                                 final TelemetryService telemetryService, final DialogManager dialogManager,
                                 final ExploreController exploreController, final ImageCache imageCache) {
        super(applicationDefaults, applicationPreferences, telemetryService, dialogManager);

        this.exploreController = exploreController;
        this.imageCache = imageCache;
    }

    /**
     * FXML initialize.
     */
    public void initialize() {
        // Wrap info content inside a proxy sidebar
        packageInfoContainer.getChildren().remove(packageInfoContent);
        sidebar = new SideBar(400, packageInfoContent);
        sidebar.collapse();
        packageInfoContainer.getChildren().add(sidebar);

        creatorLink.setOnAction(e -> exploreController.addSearchChip(creatorLink.getText()));
        closeButton.setOnAction(e -> sidebar.collapse());

        bundlesView = new PackageBundlesView(getApplicationDefaults());
        bundlesContainer.getChildren().add(bundlesView);
    }

    public void setPackage(final RemotePackage remotePackage) {
        configureHeader(remotePackage);
        configureBody(remotePackage);

    }

    private void configureHeader(final RemotePackage remotePackage) {
        // Header badge configuration
        headerContainer.getChildren().clear();
        headerContainer.getChildren().add(new PackageSourceBadgeView(remotePackage.getRemoteSource(),
                getApplicationDefaults(), true));

        // Screenshot display
        Image screenshot = imageCache.get(remotePackage.getScreenshotUrl());
        setBackground(screenshot, screenshotBackgroundPane);

        // Name and source display
        nameLabel.setText(remotePackage.getName());
        remoteSourceLabel.setText(remotePackage.getRemoteSource().getName());

        // Redirect button configuration
        browsePageButton.setOnAction(e -> {
            PlatformUtils.openDefaultBrowser(remotePackage.getPageUrl());
        });
        if (remotePackage.getDonateUrl() != null) {
            donateButton.setVisible(true);
            donateButton.setOnAction(e -> {
                PlatformUtils.openDefaultBrowser(remotePackage.getDonateUrl());
            });
        } else {
            donateButton.setVisible(false);
        }

        // Activate and configure the install button
        installButton.setDisable(false);
        installButton.setOnAction(e -> {
            boolean installStarted = exploreController.installPackage(remotePackage);
            if (installStarted) {
                installButton.setDisable(true);
            }
        });
    }

    private void configureBody(final RemotePackage remotePackage) {

        // General fields binding
        creatorLink.setText(remotePackage.getCreator());
        descriptionLabel.setText(remotePackage.getDescription());

        // License display
        if (remotePackage.getLicense() != null) {
            licenseLabel.setText(remotePackage.getLicense());
        } else {
            licenseLabel.setText("Unknown license");
        }

        // Version display
        if (remotePackage.getVersion() != null) {
            versionLabel.setVisible(true);
            versionLabel.setText(remotePackage.getVersion());
        } else {
            versionLabel.setVisible(false);
        }

        // Type display
        if (remotePackage.getType() == PluginType.INSTRUMENT) {
            typeLabel.setText("Instrument (VSTi)");
        } else if (remotePackage.getType() == PluginType.EFFECT) {
            typeLabel.setText("Effect (VST)");
        }

        // Tag display
        tagContainer.getChildren().clear();
        for (PackageTag tag : remotePackage.getTags()) {
            Node chip = new FakeChip(tag.getName());
            chip.getStyleClass().add("chip");
            chip.getStyleClass().add("fake-chip");
            chip.setOnMouseClicked(e -> {
                exploreController.addSearchChip(tag.getName());
            });
            tagContainer.getChildren().add(chip);
        }

        // Bundle list display
        bundlesView.clear();
        for (PackageBundle bundle : remotePackage.getBundles()) {
            bundlesView.addPackageBundle(bundle, e -> exploreController.installBundle(bundle));
        }

    }

    public void show() {
        if (sidebar.isCollapsed()) {
            sidebar.expand();
        }
    }

    public void hide() {
        sidebar.collapse();
    }

    public void toggleVisibility() {
        sidebar.toggle();
    }

    private static class FakeChip extends HBox {
        public FakeChip(final String text) {
            final var label = new Label(text);
            label.setWrapText(true);
            label.setMaxWidth(100);
            getChildren().add(label);
            getStyleClass().add("chip-label");
        }
    }

}
