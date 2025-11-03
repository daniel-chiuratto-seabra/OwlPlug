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

package com.owlplug.auth.components;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * A Spring component responsible for loading and providing Google OAuth 2.0 client credentials.
 * This class retrieves the Google API client ID and client secret from the
 * {@code credentials.properties} file located in the classpath, making them
 * accessible throughout the OwlPlug application for authentication purposes.
 */
@Getter
@Component
@PropertySource("classpath:credentials.properties")
public class OwlPlugCredentials {

    /**
     * The Google API client ID, loaded from the {@code credentials.properties} file.
     * This ID identifies the OwlPlug application to Google's OAuth 2.0 service.
     */
    @Value("${owlplug.credentials.google.appId}")
    private String googleAppId;

    /**
     * The Google API client secret, loaded from the {@code credentials.properties} file.
     * This secret is used in conjunction with the client ID to authenticate the OwlPlug
     * application with Google's OAuth 2.0 service.
     */
    @Value("${owlplug.credentials.google.secret}")
    private String googleSecret;

}
