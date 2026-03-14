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

package com.owlplug.auth.model;

import com.owlplug.auth.ui.AccountItem;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a user account within the OwlPlug application, persisted as a JPA entity.
 * This class stores essential user information such as a unique identifier, name,
 * profile icon URL, and the associated authentication provider. It also maintains
 * a one-to-one relationship with {@link GoogleCredential} for storing OAuth 2.0 tokens.
 * Implements {@link AccountItem} for compatibility with UI components.
 */
@Data
@Entity
@NoArgsConstructor
public class UserAccount implements AccountItem {

    /**
     * The unique identifier for the user account in the database.
     * This is an auto-generated primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * The display name of the user account.
     */
    private String name;

    /**
     * The URL to the user's profile icon or avatar.
     */
    private String iconUrl;

    /**
     * The authentication provider associated with this user account (e.g., Google).
     */
    private UserAccountProvider accountProvider;

    /**
     * The Google API credential associated with this user account, if applicable.
     * This relationship is one-to-one and the credential will be removed if the account is deleted.
     */
    @OneToOne(cascade = CascadeType.REMOVE, optional = true)
    private GoogleCredential credential;

    /**
     * Returns a unique key for this user account, derived from its database ID.
     * This method is part of the {@link AccountItem} interface, providing a
     * string representation suitable for identification in UI components or maps.
     *
     * @return A string representation of the user account's unique identifier.
     */
    public String getKey() {
        // Convert the Long type 'id' to its String representation.
        return Long.toString(id);
    }
}
