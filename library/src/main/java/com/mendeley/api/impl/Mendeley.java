package com.mendeley.api.impl;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.SignInCallback;
import com.mendeley.api.activity.SignInActivity;
import com.mendeley.api.activity.SignInOrSignUpActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class Mendeley {

    private static final String TAG = Mendeley.class.getSimpleName();
    private static Mendeley instance;

    private final ClientCredentials clientCredentials;
    private final AuthTokenManager authTokenManager;
    private final RequestsFactory requestsFactory;

    public static void sdkInitialise(Context context, ClientCredentials clientCredentials) {
        final AuthTokenManager authTokenManager = new SharedPreferencesAuthTokenManager(context.getSharedPreferences("auth", Context.MODE_PRIVATE));
        final RequestFactoryImpl requestFactory = new RequestFactoryImpl(authTokenManager, clientCredentials);
        instance = new Mendeley(clientCredentials, authTokenManager, requestFactory);
    }


    /**
     * Return the MendeleySdk singleton.
     */
    public static Mendeley getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Sdk is not initialised. You must call #sdkInitialise() first.");
        }
        return instance;
    }

    private Mendeley(ClientCredentials clientCredentials, AuthTokenManager authTokenManager, RequestsFactory requestsFactory) {
        this.clientCredentials = clientCredentials;
        this.authTokenManager = authTokenManager;
        this.requestsFactory = requestsFactory;
    }

    /**
     * Sign the user in.
     * @param activity used for creating the sign-in activity.
     * @param showSignUpScreen whether to show the screen with UI to create a new account before the sign-in dialog
     */
    public void signIn(Activity activity, boolean showSignUpScreen) {
        final Intent intent;
        if (showSignUpScreen) {
            intent = new Intent(activity, SignInOrSignUpActivity.class);
        } else {
            intent = new Intent(activity, SignInActivity.class);
        }
        activity.startActivityForResult(intent, SignInActivity.AUTH_REQUEST_CODE);
    }

    public void signOut() {
        authTokenManager.clearTokens();
    }


    public final boolean onActivityResult(int requestCode, int resultCode, Intent data, SignInCallback signInCallback) {
        switch (requestCode) {
            case SignInActivity.AUTH_REQUEST_CODE:
                onLoginActivityResult(resultCode, data, signInCallback);
                return true;
            default:
                return false;
        }
    }

    private void onLoginActivityResult(int resultCode, Intent data, SignInCallback signInCallback) {
        if (resultCode == Activity.RESULT_OK && onJsonStringResult(data.getStringExtra(SignInActivity.EXTRA_JSON_TOKENS))) {
            signInCallback.onSignedIn();
        } else {
            signInCallback.onSignInFailure();
        }
    }

    private boolean onJsonStringResult(String jsonTokenString) {
        try {
            JSONObject tokenObject = new JSONObject(jsonTokenString);

            String accessToken = tokenObject.getString("access_token");
            String refreshToken = tokenObject.getString("refresh_token");
            String tokenType = tokenObject.getString("token_type");
            int expiresIn = tokenObject.getInt("expires_in");

            authTokenManager.saveTokens(accessToken, refreshToken, tokenType, expiresIn);
            return true;
        } catch (JSONException e) {
            // If the client credentials are incorrect, the tokenString contains an error message
            Log.e(TAG, "Could not parse the json response with the auth tokens: " + jsonTokenString, e);
            return false;
        }
    }

    public boolean isSignedIn() {
        return !TextUtils.isEmpty(authTokenManager.getAccessToken());
    }

    public ClientCredentials getClientCredentials() {
        return clientCredentials;
    }

    public AuthTokenManager getAuthTokenManager() {
        return authTokenManager;
    }

    public RequestsFactory getRequestFactory() {return requestsFactory; }

}
