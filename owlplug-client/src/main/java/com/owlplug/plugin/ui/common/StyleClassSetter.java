package com.owlplug.plugin.ui.common;

import com.owlplug.plugin.model.PluginState;
import javafx.scene.shape.Circle;

public class StyleClassSetter {

    public static void setStyleClass(final PluginState pluginState, final Circle circle) {
        switch (pluginState) {
            case INSTALLED -> circle.getStyleClass().add("shape-state-installed");
            case DISABLED -> circle.getStyleClass().add("shape-state-disabled");
            case UNSTABLE -> circle.getStyleClass().add("shape-state-unstable");
            case ACTIVE -> circle.getStyleClass().add("shape-state-active");
        }
    }

}
