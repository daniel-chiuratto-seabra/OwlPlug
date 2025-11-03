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

import com.owlplug.controls.skins.ChipViewSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * ChipView inspired by JFXChipView from JFoenix.
 */
@Deprecated
public class ChipView<T> extends Control {

    private static final String DEFAULT_STYLE_CLASS = "chip-view";

    /**
     * Converts the user-typed input (when the ChipArea is
     * editable to an object of type T, such that
     * the input may be retrieved via the property.)
     */
    private final ObjectProperty<StringConverter<T>> converter = new SimpleObjectProperty<StringConverter<T>>(this, "converter", ChipView.defaultStringConverter());

    private final StringProperty promptText = new SimpleStringProperty(this, "promptText", "");

    private final AutoCompletePopup<T> autoCompletePopup = new ChipViewSkin.ChipsAutoComplete<>();

    private final ObservableList<T> chips = FXCollections.observableArrayList();

    private final ObjectProperty<BiPredicate<T, String>> predicate = new SimpleObjectProperty<>(
            (item, text) -> {
                StringConverter<T> converter = getConverter();
                String itemString = converter != null ? converter.toString(item) : item.toString();
                return itemString.toLowerCase().contains(text.toLowerCase());
            }
    );

    private ObjectProperty<BiFunction<ChipView<T>, T, Chip<T>>> chipFactory;
    private ObjectProperty<Function<T, T>> selectionHandler;

    private static <T> StringConverter<T> defaultStringConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(T t) {
                return t == null ? null : t.toString();
            }

            @Override
            public T fromString(String string) {
                return (T) string;
            }
        };
    }

    public ChipView() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Skin<?> createDefaultSkin() {
        return new ChipViewSkin<T>(this);
    }

    /**
     * Returns the chip factory used to create chips for the items in the ChipView.
     *
     * @return The chip factory.
     */
    public BiFunction<ChipView<T>, T, Chip<T>> getChipFactory() {
        return chipFactory == null ? null : chipFactory.get();
    }

    /**
     * Returns the property for the chip factory.
     *
     * @return The chip factory property.
     */
    public ObjectProperty<BiFunction<ChipView<T>, T, Chip<T>>> chipFactoryProperty() {
        if (chipFactory == null) {
            chipFactory = new SimpleObjectProperty<>(this, "chipFactory");
        }
        return chipFactory;
    }

    /**
     * Sets the chip factory to be used for creating chips.
     *
     * @param chipFactory The new chip factory.
     */
    public void setChipFactory(BiFunction<ChipView<T>, T, Chip<T>> chipFactory) {
        chipFactoryProperty().set(chipFactory);
    }

    /**
     * Returns the selection handler, which is a function that is called when a chip is selected.
     *
     * @return The selection handler.
     */
    public Function<T, T> getSelectionHandler() {
        return selectionHandler == null ? null : selectionHandler.get();
    }

    /**
     * Returns the property for the selection handler.
     *
     * @return The selection handler property.
     */
    public ObjectProperty<Function<T, T>> selectionHandlerProperty() {
        if (selectionHandler == null) {
            selectionHandler = new SimpleObjectProperty<>(this, "selectionHandler");
        }
        return selectionHandler;
    }

    /**
     * Sets the selection handler.
     *
     * @param selectionHandler The new selection handler.
     */
    public void setSelectionHandler(Function<T, T> selectionHandler) {
        selectionHandlerProperty().set(selectionHandler);
    }

    /**
     * Returns the property for the prompt text.
     *
     * @return The prompt text property.
     */
    public final StringProperty promptTextProperty() {
        return promptText;
    }

    /**
     * Returns the prompt text.
     *
     * @return The prompt text.
     */
    public final String getPromptText() {
        return promptText.get();
    }

    /**
     * Sets the prompt text.
     *
     * @param value The new prompt text.
     */
    public final void setPromptText(String value) {
        promptText.set(value);
    }

    /**
     * Returns the auto-complete popup used by the ChipView.
     *
     * @return The auto-complete popup.
     */
    public AutoCompletePopup<T> getAutoCompletePopup() {
        return autoCompletePopup;
    }

    /**
     * Returns the list of suggestions for the auto-complete popup.
     *
     * @return The list of suggestions.
     */
    public ObservableList<T> getSuggestions() {
        return autoCompletePopup.getSuggestions();
    }

    /**
     * Sets the cell factory for the suggestions in the auto-complete popup.
     *
     * @param factory The new cell factory.
     */
    public void setSuggestionsCellFactory(Callback<ListView<T>, ListCell<T>> factory) {
        autoCompletePopup.setSuggestionsCellFactory(factory);
    }

    /**
     * Returns the predicate used to filter the suggestions in the auto-complete popup.
     *
     * @return The predicate.
     */
    public BiPredicate<T, String> getPredicate() {
        return predicate.get();
    }

    /**
     * Returns the property for the predicate used to filter the suggestions.
     *
     * @return The predicate property.
     */
    public ObjectProperty<BiPredicate<T, String>> predicateProperty() {
        return predicate;
    }

    /**
     * Sets the predicate to be used for filtering the suggestions.
     *
     * @param predicate The new predicate.
     */
    public void setPredicate(BiPredicate<T, String> predicate) {
        this.predicate.set(predicate);
    }

    /**
     * Returns the list of chips currently displayed in the ChipView.
     *
     * @return The list of chips.
     */
    public ObservableList<T> getChips() {
        return chips;
    }

    /**
     * Returns the property for the converter used to convert user input to an object of type T.
     *
     * @return The converter property.
     */
    public ObjectProperty<StringConverter<T>> converterProperty() {
        return converter;
    }

    /**
     * Sets the converter to be used for converting user input to an object of type T.
     *
     * @param value The new converter.
     */
    public final void setConverter(StringConverter<T> value) {
        converterProperty().set(value);
    }

    /**
     * Returns the converter used to convert user input to an object of type T.
     *
     * @return The converter.
     */
    public final StringConverter<T> getConverter() {
        return converterProperty().get();
    }
}