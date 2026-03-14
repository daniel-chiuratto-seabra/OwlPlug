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

import com.google.api.client.auth.oauth2.StoredCredential;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

/**
 * Represents a Google API credential as a JPA entity for persistence in the OwlPlug application.
 * This entity stores OAuth 2.0 tokens (access token, refresh token) and their expiration
 * details, along with metadata such as creation and last update timestamps.
 * It is designed to work with {@link com.google.api.client.auth.oauth2.StoredCredential}
 * to manage user authentication securely and persistently.
 */
@Data
@Entity
@NoArgsConstructor
public class GoogleCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true)
    private String key;

    private String accessToken;

    private Long expirationTimeMilliseconds;

    private String refreshToken;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    /**
     * Constructs a new {@code GoogleCredential} entity from a unique key and a {@link StoredCredential} object.
     * This constructor is used to initialize a persistent representation of Google AI credentials,
     * mapping the relevant properties from the {@code StoredCredential} to this entity.
     * Creation and update timestamps are automatically set to the current instant.
     *
     * @param key The unique identifier for this credential, typically a user ID.
     * @param credential The {@link StoredCredential} containing the access token, refresh token, and expiration time.
     */
    public GoogleCredential(final String key, final StoredCredential credential) {
        // Assign the unique key for this credential.
        this.key = key;
        // Copy the access token from the StoredCredential.
        this.accessToken = credential.getAccessToken();
        // Copy the expiration time in milliseconds from the StoredCredential.
        this.expirationTimeMilliseconds = credential.getExpirationTimeMilliseconds();
        // Copy the refresh token from the StoredCredential.
        this.refreshToken = credential.getRefreshToken();
        // Set the creation timestamp to the current instant.
        this.createdAt = Instant.now();
        // Set the update timestamp to the current instant.
        this.updatedAt = Instant.now();
    }

    /**
     * Applies the properties from a given {@link StoredCredential} object to this {@code GoogleCredential} entity.
     * This method updates the access token, expiration time, and refresh token of the current
     * entity with the values from the provided {@code StoredCredential}. The {@code updatedAt}
     * timestamp is also updated to the current instant.
     *
     * @param credential The {@link StoredCredential} whose properties are to be applied.
     */
    public void apply(StoredCredential credential) {
        // Update the access token with the value from the provided StoredCredential.
        this.accessToken = credential.getAccessToken();
        // Update the expiration time in milliseconds with the value from the provided StoredCredential.
        this.expirationTimeMilliseconds = credential.getExpirationTimeMilliseconds();
        // Update the refresh token with the value from the provided StoredCredential.
        this.refreshToken = credential.getRefreshToken();
        // Update the 'updatedAt' timestamp to the current instant, indicating a modification.
        this.updatedAt = Instant.now();
    }
}
