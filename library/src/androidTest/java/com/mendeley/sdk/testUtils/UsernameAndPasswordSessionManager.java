package com.mendeley.sdk.testUtils;

import android.util.Log;

import com.mendeley.sdk.AuthTokenManager;
import com.mendeley.sdk.ClientCredentials;
import com.mendeley.sdk.request.endpoint.OAuthTokenEndpoint;

import org.json.JSONObject;


public class UsernameAndPasswordSessionManager {

    private final String username;
    private final String password;

    private final ClientCredentials clientCredentials;
    private final AuthTokenManager authTokenManager;

    private static final String TAG = InMemoryAuthTokenManager.class.getSimpleName();


    public UsernameAndPasswordSessionManager(ClientCredentials clientCredentials, AuthTokenManager authTokenManager, String username, String password) {
        this.clientCredentials = clientCredentials;
        this.username = username;
        this.password = password;
        this.authTokenManager = authTokenManager;
    }

    public void signIn() {
        try {
            final JSONObject serverResponse = new OAuthTokenEndpoint.AccessTokenWithPasswordRequest(clientCredentials, username, password).run().resource;
            OAuthTokenEndpoint.saveTokens(authTokenManager, serverResponse);
        } catch (Exception e) {
            Log.e(TAG, "Problem when authenticating", e);
            throw new RuntimeException("Problem when authenticating", e);
        }
    }



}
