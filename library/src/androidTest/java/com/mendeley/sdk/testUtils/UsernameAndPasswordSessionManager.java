package com.mendeley.sdk.testUtils;

import android.net.Uri;
import android.util.Log;

import com.mendeley.sdk.AuthTokenManager;
import com.mendeley.sdk.AppCredentials;
import com.mendeley.sdk.exceptions.AuthenticationException;
import com.mendeley.sdk.util.NetworkUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


public class UsernameAndPasswordSessionManager {

    private final String username;
    private final String password;

    private final AppCredentials appCredentials;
    private final AuthTokenManager authTokenManager;

    private static final String TAG = InMemoryAuthTokenManager.class.getSimpleName();


    public UsernameAndPasswordSessionManager(AppCredentials appCredentials, AuthTokenManager authTokenManager, String username, String password) {
        this.appCredentials = appCredentials;
        this.username = username;
        this.password = password;
        this.authTokenManager = authTokenManager;
    }

    public void signIn() {
        try {
            String jsonToken = doPasswordPost();
            onJsonStringResult(jsonToken);
        } catch (Exception e) {
            Log.e(TAG, "Problem when authenticating", e);
            throw new RuntimeException("Problem when authenticating", e);
        }
    }

    /**
     * Helper method for executing http post request for password-based authentication.
     */
    private String doPasswordPost() throws Exception {
        HttpsURLConnection con = null;
        try {
            con = (HttpsURLConnection) new URL(AuthTokenManager.TOKENS_URL).openConnection();
            con.setRequestMethod("POST");
            con.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            con.setDoInput(true);
            con.setDoOutput(true);

            final String urlEncodedForm = new Uri.Builder()
                    .appendQueryParameter("grant_type", "password")
                    .appendQueryParameter("scope", "all")
                    .appendQueryParameter("client_id", appCredentials.clientId)
                    .appendQueryParameter("client_secret", appCredentials.clientSecret)
                    .appendQueryParameter("username", username)
                    .appendQueryParameter("password", password)
                    .build()
                    .getEncodedQuery();

            //Send request
            DataOutputStream wr = new DataOutputStream(con.getOutputStream ());
            wr.writeBytes (urlEncodedForm);
            wr.flush ();
            wr.close ();

            final int statusCode = con.getResponseCode();

            if (statusCode != 200) {
                throw new Exception("Could not get auth token: " + con.getResponseMessage());
            }

            return NetworkUtils.readInputStream(con.getInputStream());
        } catch (Exception e) {
            throw new AuthenticationException("Cannot refresh token", e);
        } finally {
            if (con != null) {
                con.disconnect();
            }
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

}
