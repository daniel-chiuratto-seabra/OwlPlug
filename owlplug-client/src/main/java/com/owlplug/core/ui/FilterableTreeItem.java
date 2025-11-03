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

package com.owlplug.core.ui;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.TreeItem;

import java.util.function.Predicate;

public class FilterableTreeItem<T> extends TreeItem<T> {
    // This cannot be local
    private final FilteredList<TreeItem<T>> filteredChildren;

    private final ObservableList<TreeItem<T>> sourceChildren = FXCollections.observableArrayList();
    private final ObjectProperty<Predicate<T>> predicate = new SimpleObjectProperty<>();

    public FilterableTreeItem(T value) {
        super(value);

        this.filteredChildren = new FilteredList<>(sourceChildren);
        this.filteredChildren.predicateProperty().bind(Bindings.createObjectBinding(() -> child -> {
            if (child instanceof FilterableTreeItem) {
                ((FilterableTreeItem<T>) child).predicateProperty().set(predicate.get());
            }
            if (this.predicate.get() == null || !child.getChildren().isEmpty() || child.getValue() == null) {
                return true;
            }

            return predicate.get().test(child.getValue());
        }, this.predicate));

        this.filteredChildren.addListener((ListChangeListener<TreeItem<T>>) c -> {
            while (c.next()) {
                getChildren().removeAll(c.getRemoved());
                getChildren().addAll(c.getAddedSubList());
            }
        });
    }

    public ObservableList<TreeItem<T>> getInternalChildren() {
        return this.sourceChildren;
    }

    public ObjectProperty<Predicate<T>> predicateProperty() {
        return this.predicate;
    }

} 
