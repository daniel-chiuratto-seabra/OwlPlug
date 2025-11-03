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


import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.Region;

/**
 * A JavaFX component that represents a single chip in the {@link ChipView}.
 *
 * @param <T> The type of the item that the chip represents.
 */
public class Chip<T> extends Region {

    private final ObjectProperty<T> item = new SimpleObjectProperty<T>(this, "item");

    protected final ChipView<T> view;

    /**
     * Creates a new Chip.
     *
     * @param view The ChipView that this chip belongs to.
     * @param item The item that this chip represents.
     */
    public Chip(final ChipView<T> view, final T item) {
        this.view = view;
        getStyleClass().add("chip");
        setItem(item);
    }

    /**
     * Returns the property for the item that this chip represents.
     *
     * @return The item property.
     */
    public final ObjectProperty<T> itemProperty() {
        return item;
    }

    /**
     * Sets the item that this chip represents.
     *
     * @param value The new item.
     */
    public final void setItem(T value) {
        item.set(value);
    }

    /**
     * Returns the item that this chip represents.
     *
     * @return The item.
     */
    public final T getItem() {
        return item.get();
    }

    /**
     * Returns the ChipView that this chip belongs to.
     *
     * @return The ChipView.
     */
    public final ChipView<T> getChipView() {
        return view;
    }
}