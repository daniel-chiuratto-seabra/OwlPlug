package com.owlplug.explore.controllers.common;

import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class CommonOperations {
    public static void setBackground(Image screenshot, Pane screenshotBackgroundPane) {
        if (screenshot != null) {
            BackgroundImage bgImg = new BackgroundImage(screenshot, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true));
            screenshotBackgroundPane.setBackground(new Background(bgImg));
        }
        screenshotBackgroundPane.setEffect(new InnerShadow(25, Color.BLACK));
    }
}
