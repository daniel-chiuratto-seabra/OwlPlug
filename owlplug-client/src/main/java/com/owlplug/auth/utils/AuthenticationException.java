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

package com.owlplug.auth.utils;

/**
 * Custom exception class for authentication-related errors in the OwlPlug application.
 * This exception is thrown to indicate failures during the authentication process,
 * such as issues with OAuth flows, credential handling, or API interactions.
 */
public class AuthenticationException extends Exception {

    /**
     * Constructs a new {@code AuthenticationException} with the specified cause.
     * This constructor is used to wrap an underlying exception that led to the
     * authentication failure, preserving the original cause.
     *
     * @param e The cause of the authentication exception (e.g., an {@link IOException} or {@link GeneralSecurityException}).
     */
    public AuthenticationException(Exception e) {
        // Call the superclass constructor, passing the cause exception.
        super(e);
    }

}
