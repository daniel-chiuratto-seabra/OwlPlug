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

package com.owlplug.auth;

import com.google.api.client.util.store.DataStoreFactory;
import com.owlplug.auth.repositories.GoogleCredentialRepository;
import lombok.RequiredArgsConstructor;

/**
 * A factory class for creating and managing {@link JPADataStore} instances.
 * This factory integrates with Spring Data JPA repositories to provide
 * persistent storage for Google API client credentials, enabling secure
 * and reliable authentication flows within the OwlPlug application.
 * It implements {@link com.google.api.client.util.store.DataStoreFactory}
 * to be compatible with Google API client libraries.
 */
@RequiredArgsConstructor
public class JPADataStoreFactory implements DataStoreFactory {

    /**
     * The repository used for persisting and retrieving Google credentials.
     * This field is injected via Lombok's {@code @RequiredArgsConstructor}.
     */
    private final GoogleCredentialRepository repository;

    /**
     * Creates a new {@link JPADataStore} instance with the given ID.
     * This method is part of the {@link DataStoreFactory} interface and is
     * responsible for providing a data store for Google API client libraries.
     * The created {@code JPADataStore} will use this factory and the
     * {@link GoogleCredentialRepository} for its persistence operations.
     *
     * @param id The identifier for the data store.
     * @return A new {@link JPADataStore} instance.
     */
    @Override
    @SuppressWarnings("unchecked")
    public JPADataStore getDataStore(String id) {
        // Create and return a new JPADataStore, passing this factory, the provided ID,
        // and the GoogleCredentialRepository for data persistence.
        return new JPADataStore(this, id, repository);
    }

}
