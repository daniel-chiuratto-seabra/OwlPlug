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

package com.owlplug.auth.services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;
import com.owlplug.auth.JPADataStoreFactory;
import com.owlplug.auth.components.OwlPlugCredentials;
import com.owlplug.auth.events.AccountChangedEvent;
import com.owlplug.auth.model.UserAccount;
import com.owlplug.auth.model.UserAccountProvider;
import com.owlplug.auth.repositories.GoogleCredentialRepository;
import com.owlplug.auth.repositories.UserAccountRepository;
import com.owlplug.auth.utils.AuthenticationException;
import com.owlplug.core.components.ApplicationDefaults;
import com.owlplug.core.components.ApplicationPreferences;
import com.owlplug.core.services.BaseService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Manages OAuth authentication flows for OwlPlug users, primarily with Google.
 * This service handles the entire authentication process, including requesting
 * API permissions, storing user access tokens, and managing user accounts.
 * It utilizes an IP Loopback receiver for the OAuth flow.
 * <p>
 * Due to its stateful nature during an authentication flow, this class is not
 * thread-safe, and only one authentication process should be active at any given time.
 * </p>
 */
@Service
public class AuthenticationService extends BaseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationService.class);

    /**
     * The JSON factory used for parsing and generating JSON content in Google API interactions.
     */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    /**
     * Component providing OwlPlug-specific credentials for Google API access.
     */
    private final OwlPlugCredentials owlPlugCredentials;
    /**
     * Repository for managing persistence of Google API credentials.
     */
    private final GoogleCredentialRepository googleCredentialRepository;
    /**
     * Repository for managing persistence of user account information.
     */
    private final UserAccountRepository userAccountRepository;
    /**
     * Publisher for application events, used to notify other components of account changes.
     */
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * The local server receiver used for the OAuth 2.0 authorization code flow.
     * This field is stateful and is set during the authentication process.
     */
    private LocalServerReceiver receiver = null;

    /**
     * Constructs an {@code AuthenticationService} with necessary dependencies.
     *
     * @param applicationDefaults        Provides default application settings.
     * @param applicationPreferences     Manages application-specific user preferences.
     * @param owlPlugCredentials         Provides OwlPlug's Google API client ID and secret.
     * @param googleCredentialRepository Repository for Google API credentials.
     * @param userAccountRepository      Repository for user account data.
     * @param applicationEventPublisher  Publisher for application-wide events.
     */
    public AuthenticationService(final ApplicationDefaults applicationDefaults, final ApplicationPreferences applicationPreferences,
                                 final OwlPlugCredentials owlPlugCredentials, final GoogleCredentialRepository googleCredentialRepository,
                                 final UserAccountRepository userAccountRepository, final ApplicationEventPublisher applicationEventPublisher) {
        // Call the superclass constructor to initialize common base service properties.
        super(applicationDefaults, applicationPreferences);
        // Inject OwlPlug-specific credentials.
        this.owlPlugCredentials = owlPlugCredentials;
        // Inject the repository for Google API credentials.
        this.googleCredentialRepository = googleCredentialRepository;
        // Inject the repository for user account data.
        this.userAccountRepository = userAccountRepository;
        // Inject the application event publisher.
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * Initiates the OAuth 2.0 authentication flow to create and authenticate a new user account.
     * This method guides the user through the Google authorization process using an IP Loopback
     * receiver, obtains necessary API permissions, and stores the resulting credentials.
     * It then creates or updates a {@link UserAccount} with the retrieved user information.
     *
     * @throws AuthenticationException if any error occurs during the authentication flow,
     *                                 such as network issues, security exceptions, or
     *                                 problems with the authorization process.
     */
    public void createAccountAndAuth() throws AuthenticationException {
        // Retrieve Google AI client ID and secret from OwlPlug's credentials.
        final var clientId = owlPlugCredentials.getGoogleAppId();
        final var clientSecret = owlPlugCredentials.getGoogleSecret();
        // Define the required OAuth scopes for user profile information.
        final var scopes = new ArrayList<String>();
        scopes.add("https://www.googleapis.com/auth/userinfo.profile");

        try {
            // Initialize the HTTP transport for Google API communication.
            NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            // Create a JPA-backed DataStoreFactory for persisting Google credentials.
            DataStoreFactory dataStore = new JPADataStoreFactory(googleCredentialRepository);
            // Build the Google Authorization Code Flow.
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientId,
                    clientSecret, scopes)
                    .setDataStoreFactory(dataStore) // Use the JPA data store for credentials.
                    .setAccessType("offline") // Request offline access for refresh tokens.
                    .setApprovalPrompt("force") // Force approval prompt to ensure a refresh token is granted.
                    .build();

            // Create a temporary UserAccount to hold the key during the authorization process.
            UserAccount userAccount = new UserAccount();
            userAccountRepository.save(userAccount);

            // Initialize the local server receiver for the authorization code.
            receiver = new LocalServerReceiver();

            // Authorize the application and get user credentials.
            AuthorizationCodeInstalledApp authCodeAccess = new AuthorizationCodeInstalledApp(flow, receiver);
            Credential credential = authCodeAccess.authorize(userAccount.getKey());

            // Build an Oauth2 service client to fetch user information.
            Oauth2 oauth2 = new Oauth2.Builder(new NetHttpTransport(), JSON_FACTORY, credential)
                    .setApplicationName("OwlPlug").build();
            // Execute the request to get detailed user profile information.
            Userinfo userinfo = oauth2.userinfo().get().execute();

            // Update the UserAccount with information retrieved from the user's profile.
            userAccount.setName(userinfo.getName());
            userAccount.setIconUrl(userinfo.getPicture());
            userAccount.setAccountProvider(UserAccountProvider.GOOGLE);
            // Link the GoogleCredential to the UserAccount.
            userAccount.setCredential(googleCredentialRepository.findByKey(userAccount.getKey()));

            // Save the updated UserAccount.
            userAccountRepository.save(userAccount);
            // Set the newly created account as the selected account in application preferences.
            getApplicationPreferences().putLong(ApplicationDefaults.SELECTED_ACCOUNT_KEY, userAccount.getId());

            // Publish an event to notify other parts of the application about the account change.
            applicationEventPublisher.publishEvent(new AccountChangedEvent(this));
        } catch (GeneralSecurityException | IOException e) {
            // Log any security or I/O errors during authentication.
            LOGGER.error("Error during authentication", e);
            // Wrap and re-throw as an AuthenticationException.
            throw new AuthenticationException(e);
        } finally {
            // Ensure that any incomplete or invalid accounts created during a failed
            // authentication attempt are cleaned up.
            userAccountRepository.deleteInvalidAccounts();
        }
    }

    /**
     * Deletes a specified user account and its associated Google credentials.
     * This operation is transactional, ensuring that both the user account
     * and its linked credentials are removed consistently. After deletion,
     * an {@link AccountChangedEvent} is published to notify other parts
     * of the application.
     *
     * @param userAccount The {@link UserAccount} to be deleted.
     */
    @Transactional
    public void deleteAccount(UserAccount userAccount) {
        // Delete the Google credentials associated with the user account using its key.
        googleCredentialRepository.deleteByKey(userAccount.getKey());
        // Delete the user account itself from the repository.
        userAccountRepository.delete(userAccount);
        // Publish an event to notify other parts of the application about the account deletion.
        applicationEventPublisher.publishEvent(new AccountChangedEvent(this));
    }

    /**
     * Retrieves all registered {@link UserAccount} entities from the data store.
     * This method provides access to a collection of all user accounts managed
     * by the application.
     *
     * @return An {@link Iterable} collection of all {@link UserAccount} instances.
     */
    public Iterable<UserAccount> getAccounts() {
        // Delegate the retrieval of all user accounts to the user account repository.
        return userAccountRepository.findAll();
    }

    /**
     * Retrieves a {@link UserAccount} by its unique identifier.
     * This method provides a way to fetch a specific user account
     * from the data store using its primary key.
     *
     * @param id The unique ID of the user account to retrieve.
     * @return An {@link Optional} containing the {@link UserAccount} if found,
     * or an empty {@link Optional} if no account matches the given ID.
     */
    public Optional<UserAccount> getUserAccountById(Long id) {
        // Delegate the retrieval of a user account by its ID to the user account repository.
        return userAccountRepository.findById(id);
    }

    /**
     * Forces the OAuth authentication flow's local server receiver to stop.
     * This method is typically called when the user explicitly cancels the
     * authentication process, ensuring that the receiver is properly shut down
     * and any incomplete user accounts are cleaned up.
     */
    public void stopAuthReceiver() {
        try {
            // Delete any user accounts that were created but not fully set up
            // during a canceled authentication attempt.
            userAccountRepository.deleteInvalidAccounts();
            // If the local server receiver was initialized, stop it.
            if (receiver != null) {
                receiver.stop();
            }
        } catch (IOException e) {
            // Log any I/O errors that occur while attempting to stop the receiver.
            LOGGER.error("Error while stopping local authentication receiver", e);
        }
    }

}
