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

package com.owlplug.explore.model.search;

import javafx.scene.image.Image;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

public class ExploreFilterCriteria {

    @Setter private Object value;
    @Getter @Setter private Image icon;
    @Getter @Setter private ExploreFilterCriteriaType filterType;

    private String textValue;

    /**
     * Creates a ExploreFilterCriteria.
     *
     * @param value      - criteria value
     * @param filterType - criteria type
     */
    public ExploreFilterCriteria(final Object value, final ExploreFilterCriteriaType filterType) {
        super();
        this.value = value;
        this.filterType = filterType;
    }

    /**
     * Creates a ExploreFilterCriteria.
     *
     * @param value      - criteria value
     * @param filterType - criteria type
     * @param icon       - criteria icon displayed
     */
    public ExploreFilterCriteria(final Object value, final ExploreFilterCriteriaType filterType, final Image icon) {
        super();
        this.value = value;
        this.filterType = filterType;
        this.icon = icon;
    }

    /**
     * Creates a ExploreFilterCriteria.
     *
     * @param value      - criteria value
     * @param filterType - criteria type
     * @param icon       - criteria icon to display
     * @param textValue  - custom text value overwriting original value toString()
     *                   conversion.
     */
    public ExploreFilterCriteria(Object value, ExploreFilterCriteriaType filterType, Image icon, String textValue) {
        super();
        this.value = value;
        this.icon = icon;
        this.filterType = filterType;
        this.textValue = textValue;
    }

    public <T> T getValue() {
        //noinspection unchecked
        return (T) value;
    }

    @Override
    public String toString() {
        if (textValue != null) {
            return textValue;
        }
        return value.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExploreFilterCriteria criteria = (ExploreFilterCriteria) o;
        return Objects.equals(value, criteria.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
