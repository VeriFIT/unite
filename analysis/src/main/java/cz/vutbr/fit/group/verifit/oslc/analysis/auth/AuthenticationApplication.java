// Start of user code Copyright
/* ## License for manual implementation (enclosed in "Start/End user code ..." tags) ##
 *
 * Copyright (C) 2020 Ondřej Vašíček <ondrej.vasicek.0@gmail.com>, <xvasic25@stud.fit.vutbr.cz>
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

/* ## License for generated code: ## */
/*
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Simple
 */
// End of user code

package cz.vutbr.fit.group.verifit.oslc.analysis.auth;

import java.util.Base64;
import java.io.IOException;
import javax.servlet.ServletException;
import java.io.UnsupportedEncodingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.lyo.server.oauth.core.Application;
import org.eclipse.lyo.server.oauth.core.token.LRUCache;
import org.eclipse.lyo.server.oauth.core.AuthenticationException;

// Start of user code imports
import cz.vutbr.fit.group.verifit.oslc.analysis.properties.VeriFitAnalysisProperties;
// End of user code

// Start of user code pre_class_code
// End of user code

public class AuthenticationApplication implements Application {
    // Start of user code class_attributes
	static String USERNAME = VeriFitAnalysisProperties.AUTHENTICATION_USERNAME;
	static String PASSWORD = VeriFitAnalysisProperties.AUTHENTICATION_PASSWORD;
    // End of user code

    // Start of user code class_methods
    // End of user code

    public final static String APPLICATION_NAME = "VeriFit Analysis";
    public final static String OAUTH_REALM = "VeriFit Analysis";
    protected final static String APPLICATION_CONNECTOR_SESSION_ATTRIBUTE = "cz.vutbr.fit.group.verifit.oslc.analysis.auth.ApplicationConnector";
    protected final static String APPLICATION_CONNECTOR_ADMIN_SESSION_ATTRIBUTE = "cz.vutbr.fit.group.verifit.oslc.analysis.auth.AdminSession";

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String WWW_AUTHENTICATE_HEADER = "WWW-Authenticate";
    private static final String BASIC_AUTHORIZATION_PREFIX = "Basic ";
    public static final String BASIC_AUTHENTICATION_CHALLENGE = BASIC_AUTHORIZATION_PREFIX + "realm=\"" + OAUTH_REALM + "\"";
    private static final String OAUTH_AUTHORIZATION_PREFIX = "OAuth ";
    public static final String OAUTH_AUTHENTICATION_CHALLENGE = OAUTH_AUTHORIZATION_PREFIX + "realm=\"" + OAUTH_REALM + "\"";

    private static AuthenticationApplication authenticationApplication;
    private String oslcConsumerStoreFilename;
    // TODO: Cleanup this cache so that entries from old keys/token are removed.
    // Currently, this list simply grows all the time.
    private LRUCache<String, String> oauth1TokenToApplicationConnector;

    private AuthenticationApplication() {
        // Start of user code constructor_init
        // End of user code
        oslcConsumerStoreFilename= "./oslcOAuthStore.xml";
        oauth1TokenToApplicationConnector = new LRUCache<String, String>(2000);
        // Start of user code constructor_finalize
        // End of user code
    }

    public static AuthenticationApplication getApplication() {
        if (null == authenticationApplication) {
            synchronized ("authenticationApplication") {
                if (null == authenticationApplication) {
                    authenticationApplication = new AuthenticationApplication();
                }
            }
        }
        return authenticationApplication;
    }

    public String getOslcConsumerStoreFilename() {
        return oslcConsumerStoreFilename;
    }

    @Override
    public String getName() {
        // Display name for this application.
        return APPLICATION_NAME;
    }

    @Override
    public String getRealm(HttpServletRequest request) {
        return OAUTH_REALM;
    }

    @Override
    public void login(HttpServletRequest request, String username, String password) throws AuthenticationException {
        // Start of user code login
        //TODO: replace with code with real logic to login. You should also decide whether the login is admin or not.
        if ((username.equals(USERNAME) && password.equals(PASSWORD))) {
            bindApplicationConnectorToSession(request, username);
        }
        else {
            throw new AuthenticationException("Login failed!");
        }
        /* TODO guide to add an admin in the future
        if (username.equals("admin") && password.equals("admin")) {
            request.getSession().setAttribute(APPLICATION_CONNECTOR_ADMIN_SESSION_ATTRIBUTE, true);
        }
        else {
            request.getSession().setAttribute(APPLICATION_CONNECTOR_ADMIN_SESSION_ATTRIBUTE, false);
        }
        */
        // End of user code
        return;
    }

    /**
     * Login based on the credentials in the <code>Authorization</code> request header
     * if successful, bind the credentials to the request session. 
     * @throws UnauthorizedException
     *      if the request did not contain an <code>Authorization</code> header
     *      or, on problems reading the credentials from the <code>Authorization</code> request header
     */
    public void loginWithBasicAuthentication(HttpServletRequest request) throws AuthenticationException {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authorizationHeader == null || "".equals(authorizationHeader)) {
            throw new AuthenticationException("No basic authentication header identified in request.");
        }
    
        if (!authorizationHeader.startsWith(BASIC_AUTHORIZATION_PREFIX)) {
            throw new AuthenticationException("Only basic access authentication is supported.");
        }
        
        String encodedString = authorizationHeader.substring(BASIC_AUTHORIZATION_PREFIX.length());
        try {
            String unencodedString = new String(Base64.getDecoder().decode(encodedString), "UTF-8");
            int seperator = unencodedString.indexOf(':');
            if (seperator == -1) {
                throw new AuthenticationException("Invalid Authorization header value.");
            }
            String username = unencodedString.substring(0, seperator);
            String password = unencodedString.substring(seperator + 1);
            login(request, username, password);
        } catch (UnsupportedEncodingException e) {
            throw new AuthenticationException(e);
        } catch (AuthenticationException e) {
            throw new AuthenticationException(e);
        }
        return;
    }

    @Override
    public boolean isAuthenticated(HttpServletRequest request) {
        return (null != getApplicationConnectorFromSession(request));
    }

    @Override
    public boolean isAdminSession(HttpServletRequest request) {
        return Boolean.TRUE.equals(request.getSession().getAttribute(APPLICATION_CONNECTOR_ADMIN_SESSION_ATTRIBUTE));
    }

    // TODO: instead of saving to a session, consider saving to a cookie, so that
    // the login survives longer than a single web session.
    public void bindApplicationConnectorToSession(HttpServletRequest request, String applicationConnector) {
        request.getSession().setAttribute(APPLICATION_CONNECTOR_SESSION_ATTRIBUTE, applicationConnector);
    }

    public String getApplicationConnectorFromSession(HttpServletRequest request) {
        return (String) request.getSession().getAttribute(APPLICATION_CONNECTOR_SESSION_ATTRIBUTE);
    }

    public String getApplicationConnector(String oauth1Token) {
        return oauth1TokenToApplicationConnector.get(oauth1Token);
    }

    public void putApplicationConnector(String oauth1Token, String applicationConnector) {
        oauth1TokenToApplicationConnector.put(oauth1Token, applicationConnector);
    }

    public void moveApplicationConnector(String oldOauth1Token, String newOauth1Token) {
        String applicationConnector = oauth1TokenToApplicationConnector.remove(oldOauth1Token);
        oauth1TokenToApplicationConnector.put(newOauth1Token, applicationConnector);
    }

    public void removeForOauth1Token(String oauth1Token) {
        oauth1TokenToApplicationConnector.remove(oauth1Token);
    }

    /**
     * Send error response when the request was not authorized
     * 
     * @param response      an error response
     * @param e             Exception with error message
     * @param authChallenge OAuth challenge
     * @throws IOException
     * @throws ServletException
     */
    public void sendUnauthorizedResponse(HttpServletResponse response, Exception e) throws IOException, ServletException {
        // Accept basic access or OAuth authentication.
        response.addHeader(WWW_AUTHENTICATE_HEADER, OAUTH_AUTHENTICATION_CHALLENGE);
        response.addHeader(WWW_AUTHENTICATE_HEADER, BASIC_AUTHENTICATION_CHALLENGE);
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }
}
