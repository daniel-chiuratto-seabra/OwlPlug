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

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.owlplug.auth.JPADataStoreFactory;
import com.owlplug.auth.components.OwlPlugCredentials;
import com.owlplug.auth.dao.GoogleCredentialDAO;
import com.owlplug.auth.dao.UserAccountDAO;
import com.owlplug.auth.model.UserAccount;
import com.owlplug.auth.model.UserAccountProvider;
import com.owlplug.auth.utils.AuthenticationException;
import com.owlplug.core.components.ApplicationDefaults;
import com.owlplug.core.controllers.MainController;
import com.owlplug.core.services.BaseService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Optional;

/**
 * OAuth Authentication service. Authenticates users from known providers using
 * IP Loopback and requests API call permissions for OwlPlug. This service
 * stores users access tokens for next calls. Only one Authentication flow must
 * be performed at time as this class is not thread safe and maintain states
 * during Authentication.
 *
 */
@Service
public class AuthenticationService extends BaseService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationService.class);

  private static final JsonFactory JSON_FACTORY = new JacksonFactory();

  private final OwlPlugCredentials owlPlugCredentials;
  private final GoogleCredentialDAO googleCredentialDAO;
  private final UserAccountDAO userAccountDAO;
  private final MainController mainController;

  private LocalServerReceiver receiver = null;

    public AuthenticationService(OwlPlugCredentials owlPlugCredentials, GoogleCredentialDAO googleCredentialDAO, UserAccountDAO userAccountDAO, MainController mainController) {
        this.owlPlugCredentials = owlPlugCredentials;
        this.googleCredentialDAO = googleCredentialDAO;
        this.userAccountDAO = userAccountDAO;
        this.mainController = mainController;
    }

    /**
   * Creates a new account by starting the Authentication flow.
   * 
   * @throws AuthenticationException if an error occurs during Authentication
   *                                   flow.
   */
  public void createAccountAndAuth() throws AuthenticationException {

    var clientId = owlPlugCredentials.getGoogleAppId();
    var clientSecret = owlPlugCredentials.getGoogleSecret();
    var scopes = new ArrayList<String>();
    scopes.add("https://www.googleapis.com/auth/userinfo.profile");

    try {
      var httpTransport = GoogleNetHttpTransport.newTrustedTransport();
      var dataStoreFactory = new JPADataStoreFactory(googleCredentialDAO);
      var googleAuthorizationCodeFlow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientId,
          clientSecret, scopes).setDataStoreFactory(dataStoreFactory).setAccessType("offline").setApprovalPrompt("force").build();

      final var userAccount = new UserAccount();
      userAccountDAO.save(userAccount);

      receiver = new LocalServerReceiver();

      var authorizationCodeInstalledApp = new AuthorizationCodeInstalledApp(googleAuthorizationCodeFlow, receiver);
      var credential = authorizationCodeInstalledApp.authorize(userAccount.getKey());

      var oauth2 = new Oauth2.Builder(new NetHttpTransport(), new JacksonFactory(), credential)
          .setApplicationName("OwlPlug").build();

      var userinfo = oauth2.userinfo().get().execute();

      userAccount.setName(userinfo.getName());
      userAccount.setIconUrl(userinfo.getPicture());
      userAccount.setAccountProvider(UserAccountProvider.GOOGLE);
      userAccount.setCredential(googleCredentialDAO.findByKey(userAccount.getKey()));

      userAccountDAO.save(userAccount);
      this.getPreferences().putLong(ApplicationDefaults.SELECTED_ACCOUNT_KEY, userAccount.getId());

    } catch (final GeneralSecurityException | IOException e) {
      LOGGER.error("Error during authentication", e);
      throw new AuthenticationException(e);
    } finally {
      // Delete accounts without complete setup
      userAccountDAO.deleteInvalidAccounts();
    }

  }

  /**
   * Deletes a user account.
   * 
   * @param userAccount user account to delete
   */
  @Transactional
  public void deleteAccount(UserAccount userAccount) {
    googleCredentialDAO.deleteByKey(userAccount.getKey());
    userAccountDAO.delete(userAccount);
    mainController.refreshAccounts();
  }
  
  /**
   * Returns all registered UserAccounts.
   * @return list of user accounts
   */
  public Iterable<UserAccount> getAccounts() {
    return userAccountDAO.findAll();
  }
  
  /**
   * Returns a user account matching a given id.
   * @param id - user account unique id
   * @return related user account
   */
  public Optional<UserAccount> getUserAccountById(Long id) {
    return userAccountDAO.findById(id);
  }

  /**
   * Force the Authentication flow authReceiver to stop. Called when the user
   * cancels authentication.
   */
  public void stopAuthReceiver() {
    try {
      userAccountDAO.deleteInvalidAccounts();
      if (receiver != null) {
        receiver.stop();
      }
    } catch (IOException e) {
      LOGGER.error("Error while stopping local authentication receiver", e);
    }
  }

}
