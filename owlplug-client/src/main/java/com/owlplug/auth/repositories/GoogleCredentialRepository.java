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

package com.owlplug.auth.repositories;

import com.owlplug.auth.model.GoogleCredential;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.stream.Stream;

/**
 * A Spring Data JPA repository for managing {@link GoogleCredential} entities.
 * This interface provides standard CRUD (Create, Read, Update, Delete) operations
 * for Google API credentials, along with custom query methods to retrieve credentials
 * by key, access token, or to fetch all keys and credentials as a stream.
 */
@Repository
public interface GoogleCredentialRepository extends CrudRepository<GoogleCredential, Long> {

    /**
     * Finds a {@link GoogleCredential} entity by its unique key.
     *
     * @param key The unique key of the credential to find.
     * @return The {@link GoogleCredential} associated with the given key, or {@code null} if not found.
     */
    GoogleCredential findByKey(String key);

    /**
     * Finds a {@link GoogleCredential} entity by its access token.
     *
     * @param accessToken The access token of the credential to find.
     * @return The {@link GoogleCredential} associated with the given access token, or {@code null} if not found.
     */
    GoogleCredential findByAccessToken(String accessToken);

    /**
     * Retrieves a {@link Set} of all unique keys for stored {@link GoogleCredential} entities.
     * This uses a native SQL query to directly select the 'key' column.
     *
     * @return A {@link Set} containing all unique credential keys.
     */
    @Query(value = "select key from GOOGLE_CREDENTIAL", nativeQuery = true)
    Set<String> findAllKeys();

    /**
     * Retrieves all {@link GoogleCredential} entities as a {@link Stream}.
     * This allows for efficient processing of potentially large numbers of credentials
     * without loading all of them into memory at once.
     *
     * @return A {@link Stream} of all {@link GoogleCredential} entities.
     */
    @Query("select c from GoogleCredential c")
    Stream<GoogleCredential> findAllCredentialAsStream();

    /**
     * Deletes a {@link GoogleCredential} entity by its unique key.
     *
     * @param key The unique key of the credential to delete.
     */
    void deleteByKey(String key);

}
