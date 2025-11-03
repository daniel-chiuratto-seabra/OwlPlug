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

package com.owlplug.core.controllers;

/**
 * Defines a contract for controllers that manage the creation or updating of entities.
 * This interface provides methods to initiate either a creation sequence for a new entity
 * or an update sequence for an existing entity of a specified type {@code T}.
 * Implementing classes are expected to handle the UI flow and business logic
 * associated with these operations.
 *
 * @param <T> The type of the entity that this controller manages for creation or update.
 */
public interface EntityCreateOrUpdate<T> {

    /**
     * Initiates the sequence for creating a new entity.
     * This method typically prepares the UI for new data entry and
     * sets up the necessary state for a creation operation.
     */
    void startCreateSequence();

    /**
     * Initiates the sequence for updating an existing entity.
     * This method typically loads the provided entity's data into the UI
     * and sets up the necessary state for an update operation.
     *
     * @param entity The entity to be updated.
     */
    void startUpdateSequence(T entity);

}
