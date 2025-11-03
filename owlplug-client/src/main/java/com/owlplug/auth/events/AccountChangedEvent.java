/* OwlPlug
 * Copyright (C) 2021-2024 Arthur <dropsnorz@gmail.com>
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

package com.owlplug.auth.events;

import org.springframework.context.ApplicationEvent;

/**
 * An application event that is published when a user account's state or details have changed.
 * This event can be used by various parts of the application to react to account modifications,
 * such as updating UI elements, refreshing data, or triggering other business logic.
 */
public class AccountChangedEvent extends ApplicationEvent {
    /**
     * Constructs a new {@code AccountChangedEvent}.
     *
     * @param source The object on which the event initially occurred (e.g., the service that modified the account).
     *               Must not be {@code null}.
     */
    public AccountChangedEvent(final Object source) {
        // Call the superclass constructor to initialize the ApplicationEvent with the event source.
        super(source);
    }
}
