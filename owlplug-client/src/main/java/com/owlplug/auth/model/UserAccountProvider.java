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

/**
 * Enumerates the supported OAuth account providers for user authentication within OwlPlug.
 * Each constant represents a different third-party service that users can link
 * to their OwlPlug account for various functionalities, such as cloud storage
 * or credential management.
 */
public enum UserAccountProvider {
    /**
     * Represents Google as an authentication and service provider.
     * Used for integrating with Google APIs and services.
     */
    GOOGLE,
    /**
     * Represents Dropbox as a cloud storage and service provider.
     * Used for integrating with Dropbox APIs and services.
     */
    DROPBOX
}
