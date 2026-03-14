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

package com.owlplug.auth.ui;

/**
 * Represents a generic item that can be displayed in an account-related UI component,
 * typically a list or a menu. This interface provides a common contract for objects
 * that need to expose a unique identifier.
 */
public interface AccountItem {

    /**
     * Returns the unique identifier of the account item.
     *
     * @return The unique ID of the account item.
     */
    Long getId();

}
