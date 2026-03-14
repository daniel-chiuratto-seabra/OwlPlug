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

import lombok.Data;

/**
 * Represents a menu item displayed within an account-related UI component,
 * such as a dropdown menu for user accounts. This class extends {@link AccountItem}
 * to provide a textual representation for menu options that are not directly
 * linked to a specific user account, like "Add Account" or "Manage Accounts".
 */
@Data
public class AccountMenuItem implements AccountItem {

    /**
     * The display text for this account menu item.
     */
    private String text;

    /**
     * Constructs a new {@code AccountMenuItem} with the specified display text.
     *
     * @param text The text to be displayed for this menu item.
     */
    public AccountMenuItem(String text) {
        // Call the superclass constructor (Object's default constructor).
        super();
        // Assign the provided text to the menu item.
        this.text = text;
    }

    /**
     * Returns a unique identifier for this menu item.
     * For {@code AccountMenuItem} instances, a fixed value of -1L is returned,
     * indicating that it does not correspond to a persistent user account.
     *
     * @return A fixed {@code Long} value of -1L.
     */
    @Override
    public Long getId() {
        // Return a fixed ID of -1L to signify that this menu item is not
        // associated with a real, persistent user account.
        return (long) -1;
    }

}
