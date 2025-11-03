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

import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.util.store.AbstractDataStore;
import com.google.api.client.util.store.DataStore;
import com.owlplug.auth.model.GoogleCredential;
import com.owlplug.auth.repositories.GoogleCredentialRepository;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implements a Google API client {@link DataStore} that persists {@link StoredCredential}
 * objects using a JPA (Java Persistence API) provider. This class leverages a
 * {@link GoogleCredentialRepository} to store and retrieve OAuth 2.0 credentials
 * in a relational database, ensuring that user authentication tokens are
 * durably stored and managed across application sessions.
 */
public class JPADataStore extends AbstractDataStore<StoredCredential> {

    private final GoogleCredentialRepository repository;

    /**
     * Constructs a new {@code JPADataStore} instance.
     * This constructor initializes the data store with its factory, a unique identifier,
     * and the repository responsible for handling {@link GoogleCredential} persistence.
     *
     * @param dataStoreFactory The factory that created this data store.
     * @param id The unique identifier for this data store instance.
     * @param repository The {@link GoogleCredentialRepository} used for database operations.
     */
    protected JPADataStore(final JPADataStoreFactory dataStoreFactory, final String id, final GoogleCredentialRepository repository) {
        // Call the superclass constructor to initialize the AbstractDataStore with the factory and ID.
        super(dataStoreFactory, id);
        // Assign the provided GoogleCredentialRepository for use in persistence operations.
        this.repository = repository;
    }

    /**
     * Returns the number of key-value mappings in this data store.
     * This method delegates to the underlying {@link GoogleCredentialRepository}
     * to count the total number of stored credentials.
     *
     * @return The number of key-value mappings in this data store.
     * @throws IOException if an I/O error occurs during the count operation.
     */
    @Override
    public int size() throws IOException {
        // Delegate the counting of stored credentials to the repository.
        return (int) repository.count();
    }

    /**
     * Returns {@code true} if this data store contains no key-value mappings.
     * This method checks if the size of the data store is zero.
     *
     * @return {@code true} if this data store contains no key-value mappings, {@code false} otherwise.
     * @throws IOException if an I/O error occurs during the size check.
     */
    @Override
    public boolean isEmpty() throws IOException {
        // Determine if the data store is empty by checking if its size is zero.
        return size() == 0;
    }

    /**
     * Returns {@code true} if this data store contains a mapping for the specified key.
     * This method checks for the existence of a {@link GoogleCredential} associated
     * with the given key in the underlying repository.
     *
     * @param key The key whose presence in this data store is to be tested.
     * @return {@code true} if this data store contains a mapping for the specified key, {@code false} otherwise.
     */
    @Override
    public boolean containsKey(String key) {
        // Check if a GoogleCredential exists in the repository for the given key.
        // If findByKey returns a non-null value, the key exists.
        return repository.findByKey(key) != null;
    }

    /**
     * Returns {@code true} if this data store maps one or more keys to the specified value.
     * This implementation checks for the existence of a {@link GoogleCredential}
     * with a matching access token in the underlying repository.
     *
     * @param value The value whose presence in this data store is to be tested.
     * @return {@code true} if this data store maps one or more keys to the specified value, {@code false} otherwise.
     */
    @Override
    public boolean containsValue(StoredCredential value) {
        // Check if a GoogleCredential exists in the repository with the given access token.
        // If findByAccessToken returns a non-null value, the value exists.
        return repository.findByAccessToken(value.getAccessToken()) != null;
    }

    /**
     * Returns a {@link Set} of all keys contained in this data store.
     * This method delegates to the underlying {@link GoogleCredentialRepository}
     * to retrieve all stored credential keys.
     *
     * @return A {@link Set} of all keys present in this data store.
     */
    @Override
    public Set<String> keySet() {
        // Delegate the retrieval of all keys to the repository.
        return repository.findAllKeys();
    }

    /**
     * Returns a {@link Collection} of all {@link StoredCredential} values contained in this data store.
     * This method retrieves all {@link GoogleCredential} entities from the repository,
     * converts them into {@link StoredCredential} objects, and returns them as a collection.
     *
     * @return A {@link Collection} of all {@link StoredCredential} values present in this data store.
     * @throws IOException if an I/O error occurs during the retrieval or conversion of credentials.
     */
    @Override
    public Collection<StoredCredential> values() throws IOException {
        // Retrieve all GoogleCredential entities as a stream from the repository.
        return repository.findAllCredentialAsStream().map(c -> {
            // For each GoogleCredential entity, create a new StoredCredential object.
            StoredCredential credential = new StoredCredential();
            // Map the properties from GoogleCredential to StoredCredential.
            credential.setAccessToken(c.getAccessToken());
            credential.setRefreshToken(c.getRefreshToken());
            credential.setExpirationTimeMilliseconds(c.getExpirationTimeMilliseconds());
            return credential; // Return the populated StoredCredential.
        }).collect(Collectors.toList()); // Collect all StoredCredential objects into a List.
    }

    /**
     * Returns the {@link StoredCredential} to which the specified key is mapped,
     * or {@code null} if this data store contains no mapping for the key.
     * This method retrieves a {@link GoogleCredential} from the repository
     * using the provided key and converts it to a {@link StoredCredential}.
     *
     * @param key The key whose associated value is to be returned.
     * @return The {@link StoredCredential} to which the specified key is mapped, or {@code null} if no mapping exists.
     * @throws IOException if an I/O error occurs during the retrieval or conversion of the credential.
     */
    @Override
    public StoredCredential get(String key) throws IOException {
        // Retrieve the GoogleCredential entity from the repository using the provided key.
        GoogleCredential googleCredential = repository.findByKey(key);
        // If no credential is found for the key, return null.
        if (googleCredential == null) {
            return null;
        }
        // Create a new StoredCredential object.
        StoredCredential credential = new StoredCredential();
        // Map the properties from the retrieved GoogleCredential to the StoredCredential.
        credential.setAccessToken(googleCredential.getAccessToken());
        credential.setRefreshToken(googleCredential.getRefreshToken());
        credential.setExpirationTimeMilliseconds(googleCredential.getExpirationTimeMilliseconds());
        return credential; // Return the populated StoredCredential.
    }

    /**
     * Associates the specified value with the specified key in this data store.
     * If the data store previously contained a mapping for the key, the old value
     * is replaced by the specified value. This method either creates a new
     * {@link GoogleCredential} or updates an existing one in the repository.
     *
     * @param key The key with which the specified value is to be associated.
     * @param value The value to be associated with the specified key.
     * @return This {@link DataStore} instance, allowing for method chaining.
     * @throws IOException if an I/O error occurs during the save operation.
     */
    @Override
    public DataStore<StoredCredential> set(String key, StoredCredential value) throws IOException {
        // Attempt to find an existing GoogleCredential by key.
        GoogleCredential googleCredential = repository.findByKey(key);

        // If no existing credential is found, create a new one.
        if (googleCredential == null) {
            googleCredential = new GoogleCredential(key, value);
        }

        // Apply the properties from the StoredCredential value to the GoogleCredential entity.
        googleCredential.apply(value);
        // Save the GoogleCredential entity to the repository (either new or updated).
        repository.save(googleCredential);
        return this; // Return this DataStore instance for chaining.
    }

    /**
     * Removes all of the mappings from this data store.
     * This method delegates to the underlying {@link GoogleCredentialRepository}
     * to delete all stored credentials.
     *
     * @return This {@link DataStore} instance, allowing for method chaining.
     * @throws IOException if an I/O error occurs during the delete operation.
     */
    @Override
    public DataStore<StoredCredential> clear() throws IOException {
        // Delete all stored credentials from the repository.
        repository.deleteAll();
        return this; // Return this DataStore instance for chaining.
    }

    /**
     * Removes the mapping for a key from this data store if it is present.
     * This method delegates to the underlying {@link GoogleCredentialRepository}
     * to delete the stored credential associated with the specified key.
     *
     * @param key The key whose mapping is to be removed from the data store.
     * @return This {@link DataStore} instance, allowing for method chaining.
     * @throws IOException if an I/O error occurs during the delete operation.
     */
    @Override
    public DataStore<StoredCredential> delete(String key) throws IOException {
        // Delete the stored credential associated with the given key from the repository.
        repository.deleteByKey(key);
        return this; // Return this DataStore instance for chaining.
    }
}