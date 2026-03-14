package com.owlplug.controls;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * A JavaFX component that provides a layout for a dialog with a heading, body, and actions.
 */
public class DialogLayout extends VBox {
    private final StackPane heading = new StackPane();
    private final StackPane body = new StackPane();
    private final FlowPane actions = new FlowPane();

    /**
     * Creates an empty dialog layout.
     */
    public DialogLayout() {
        initialize();
        heading.getStyleClass().addAll("dialog-layout-heading", "title");
        body.getStyleClass().add("dialog-layout-body");
        VBox.setVgrow(body, Priority.ALWAYS);
        actions.getStyleClass().add("dialog-layout-actions");
        getChildren().setAll(heading, body, actions);
    }

    /**
     * Returns the list of nodes in the heading of the dialog.
     *
     * @return The list of nodes in the heading.
     */
    public ObservableList<Node> getHeading() {
        return heading.getChildren();
    }

    /**
     * Sets the content of the heading of the dialog.
     *
     * @param titleContent The nodes to be set as the heading content.
     */
    public void setHeading(Node... titleContent) {
        this.heading.getChildren().setAll(titleContent);
    }

    /**
     * Returns the list of nodes in the body of the dialog.
     *
     * @return The list of nodes in the body.
     */
    public ObservableList<Node> getBody() {
        return body.getChildren();
    }

    /**
     * Sets the content of the body of the dialog.
     *
     * @param body The nodes to be set as the body content.
     */
    public void setBody(Node... body) {
        this.body.getChildren().setAll(body);
    }

    /**
     * Returns the list of nodes in the actions section of the dialog.
     *
     * @return The list of nodes in the actions section.
     */
    public ObservableList<Node> getActions() {
        return actions.getChildren();
    }

    /**
     * Sets the content of the actions section of the dialog.
     *
     * @param actions The nodes to be set as the actions content.
     */
    public void setActions(Node... actions) {
        this.actions.getChildren().setAll(actions);
    }

    /**
     * Sets the content of the actions section of the dialog.
     *
     * @param actions The list of nodes to be set as the actions content.
     */
    public void setActions(List<? extends Node> actions) {
        this.actions.getChildren().setAll(actions);
    }

    private static final String DEFAULT_STYLE_CLASS = "dialog-layout";

    private void initialize() {
        this.getStyleClass().add(DEFAULT_STYLE_CLASS);
    }
}