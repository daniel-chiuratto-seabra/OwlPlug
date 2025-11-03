package com.owlplug.controls;

import javafx.scene.control.Label;
import javafx.scene.text.Text;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;
import javafx.scene.Scene;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ApplicationExtension.class)
public class DialogLayoutTest {

    private DialogLayout dialogLayout;

    @BeforeAll
    static void enableHeadless() {
        // Use Monocle headless platform
        System.setProperty("javafx.platform", "Monocle");
        System.setProperty("monocle.platform", "Headless");
        System.setProperty("monocle.headless", "true");
        // Optional: disable Prism hardware acceleration
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.verbose", "false");
        // Prevent AWT from trying to use X11 on Linux
        System.setProperty("java.awt.headless", "true");
    }

    @Start
    public void start(Stage stage) {
        dialogLayout = new DialogLayout();
        stage.setScene(new Scene(dialogLayout, 800, 600));
        // Do not show the window in headless mode
        // stage.show();
    }

    @Test
    public void testSetHeading() {
        Label heading = new Label("Heading");
        javafx.application.Platform.runLater(() -> {
            dialogLayout.setHeading(heading);
            assertEquals(1, dialogLayout.getHeading().size());
            assertEquals(heading, dialogLayout.getHeading().get(0));
        });
    }

    @Test
    public void testSetBody() {
        Text body = new Text("This is the body");
        javafx.application.Platform.runLater(() -> {
            dialogLayout.setBody(body);
            assertEquals(1, dialogLayout.getBody().size());
            assertEquals(body, dialogLayout.getBody().get(0));
        });
    }

    @Test
    public void testSetActions() {
        Label action1 = new Label("Action 1");
        Label action2 = new Label("Action 2");
        javafx.application.Platform.runLater(() -> {
            dialogLayout.setActions(action1, action2);
            assertEquals(2, dialogLayout.getActions().size());
            assertEquals(action1, dialogLayout.getActions().get(0));
            assertEquals(action2, dialogLayout.getActions().get(1));
        });
    }
}
