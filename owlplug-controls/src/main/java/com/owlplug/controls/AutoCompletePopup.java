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

package com.owlplug.controls;

import com.owlplug.controls.skins.AutoCompletePopupSkin;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.scene.layout.Region;
import javafx.stage.Window;
import javafx.util.Callback;

import java.util.List;
import java.util.function.Predicate;

/**
 * AutoCompletePopup is an animated popup list view that allow filtering.
 * Based on JFXAutocompletePopup from Jfoenix.
 * Deprecated, alternatives must be used.
 *
 */
@Deprecated
public class AutoCompletePopup<T> extends PopupControl {

  private final ObservableList<T> suggestions = FXCollections.observableArrayList();
  private final ObjectProperty<EventHandler<AutoCompleteEvent<T>>> selectionHandler = new SimpleObjectProperty<>();
  private final FilteredList<T> filteredData = new FilteredList<T>(suggestions, s -> true);
  private final ObjectProperty<Callback<ListView<T>, ListCell<T>>> suggestionsCellFactory = new SimpleObjectProperty<Callback<ListView<T>, ListCell<T>>>();

  /**
   * limits the number of cells to be shown, used to compute the list size.
   */
  private IntegerProperty cellLimit = new SimpleIntegerProperty(
          AutoCompletePopup.this, "cellLimit", 10);

  private DoubleProperty fixedCellSize = new SimpleDoubleProperty(
          AutoCompletePopup.this, "fixedCellSize", 40d);
  private static final String DEFAULT_STYLE_CLASS = "autocomplete-popup";


  public AutoCompletePopup() {
    super();
    setAutoFix(true);
    setAutoHide(true);
    setHideOnEscape(true);
    getStyleClass().add(DEFAULT_STYLE_CLASS);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Skin<?> createDefaultSkin() {
    return new AutoCompletePopupSkin<T>(this);
  }

  /**
   * Shows the auto-complete popup.
   *
   * @param node The node to which the popup should be attached.
   */
  public void show(Node node) {
    if (!isShowing()) {
      if (node.getScene() == null || node.getScene().getWindow() == null) {
        throw new IllegalStateException("Can not show popup. The node must be attached to a scene/window.");
      }
      Window parent = node.getScene().getWindow();
      this.show(parent, parent.getX() + node.localToScene(0, 0).getX()
                      + node.getScene().getX(),
              parent.getY() + node.localToScene(0, 0).getY()
                      + node.getScene().getY() + ((Region)node).getHeight());
      ((AutoCompletePopupSkin<T>)getSkin()).animate();
    }
  }

  /**
   * Returns the list of suggestions for the auto-complete popup.
   *
   * @return The list of suggestions.
   */
  public ObservableList<T> getSuggestions() {
    return suggestions;
  }

  /**
   * Filters the suggestions in the auto-complete popup using the given predicate.
   *
   * @param predicate The predicate to use for filtering.
   */
  public void filter(Predicate<T> predicate) {
    filteredData.setPredicate(predicate);
  }

  /**
   * Returns the filtered list of suggestions for the auto-complete popup.
   *
   * @return The filtered list of suggestions.
   */
  public ObservableList<T> getFilteredSuggestions() {
    return filteredData;
  }

  /**
   * Returns the selection handler, which is called when a suggestion is selected.
   *
   * @return The selection handler.
   */
  public EventHandler<AutoCompleteEvent<T>> getSelectionHandler() {
    return selectionHandler.get();
  }

  /**
   * Sets the selection handler.
   *
   * @param selectionHandler The new selection handler.
   */
  public void setSelectionHandler(EventHandler<AutoCompleteEvent<T>> selectionHandler) {
    this.selectionHandler.set(selectionHandler);
  }

  /**
   * Returns the property for the suggestions cell factory.
   *
   * @return The suggestions cell factory property.
   */
  public final ObjectProperty<Callback<ListView<T>, ListCell<T>>> suggestionsCellFactoryProperty() {
    return this.suggestionsCellFactory;
  }


  /**
   * Returns the suggestions cell factory.
   *
   * @return The suggestions cell factory.
   */
  public final Callback<ListView<T>, ListCell<T>> getSuggestionsCellFactory() {
    return this.suggestionsCellFactoryProperty().get();
  }


  /**
   * Sets the suggestions cell factory.
   *
   * @param suggestionsCellFactory The new suggestions cell factory.
   */
  public final void setSuggestionsCellFactory(
          final Callback<ListView<T>, ListCell<T>> suggestionsCellFactory) {
    this.suggestionsCellFactoryProperty().set(suggestionsCellFactory);
  }

  /**
   * Sets the maximum number of cells to be shown in the popup.
   *
   * @param value The new cell limit.
   */
  public final void setCellLimit(int value) {
    cellLimitProperty().set(value);
  }

  /**
   * Returns the maximum number of cells to be shown in the popup.
   *
   * @return The cell limit.
   */
  public final int getCellLimit() {
    return cellLimitProperty().get();
  }

  /**
   * Returns the property for the maximum number of cells to be shown in the popup.
   *
   * @return The cell limit property.
   */
  public final IntegerProperty cellLimitProperty() {
    return cellLimit;
  }

  /**
   * Sets the fixed cell size for the cells in the popup.
   *
   * @param value The new fixed cell size.
   */
  public final void setFixedCellSize(double value) {
    fixedCellSizeProperty().set(value);
  }

  /**
   * Returns the fixed cell size for the cells in the popup.
   *
   * @return The fixed cell size.
   */
  public final double getFixedCellSize() {
    return fixedCellSizeProperty().get();
  }

  /**
   * Returns the property for the fixed cell size.
   *
   * @return The fixed cell size property.
   */
  public final DoubleProperty fixedCellSizeProperty() {
    return fixedCellSize;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
    return getClassCssMetaData();
  }

  public static class AutoCompleteEvent<T> extends Event {

    private final T object;

    public AutoCompleteEvent(EventType<? extends Event> eventType, T object) {
      super(eventType);
      this.object = object;
    }

    public T getObject() {
      return object;
    }

    public static final EventType<AutoCompleteEvent> SELECTION =
            new EventType<>(Event.ANY, "AUTOCOMPLETE_SELECTION");

  }
}