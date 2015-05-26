package com.mendeley.api.testUtils;

import android.util.Log;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.request.NetworkUtils;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class UsernameAndPasswordSessionManager {

    private final String username;
    private final String password;

    private final ClientCredentials clientCredentials;
    private final AuthTokenManager authTokenManager;

    public static final String GRANT_TYPE_PASSWORD = "password";
    private static final String TAG = InMemoryAuthTokenManager.class.getSimpleName();


    public UsernameAndPasswordSessionManager(ClientCredentials clientCredentials, AuthTokenManager authTokenManager, String username, String password) {
        this.clientCredentials = clientCredentials;
        this.username = username;
        this.password = password;
        this.authTokenManager = authTokenManager;
    }

    public void signIn() {
        try {
            HttpResponse response = doPasswordPost();
            String jsonToken = NetworkUtils.readInputStream(response.getEntity().getContent());
            onJsonStringResult(jsonToken);
        } catch (Exception e) {
            Log.e(TAG, "Problem when authenticating", e);
            throw new RuntimeException("Problem when authenticating", e);
        }
    }

    /**
     * Helper method for executing http post request for password-based authentication.
     */
    private HttpResponse doPasswordPost() throws IOException {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(AuthTokenManager.TOKENS_URL);

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("grant_type", GRANT_TYPE_PASSWORD));
        nameValuePairs.add(new BasicNameValuePair("scope", AuthTokenManager.SCOPE));
        nameValuePairs.add(new BasicNameValuePair("client_id", clientCredentials.clientId));
        nameValuePairs.add(new BasicNameValuePair("client_secret", clientCredentials.clientSecret));
        nameValuePairs.add(new BasicNameValuePair("username", username));
        nameValuePairs.add(new BasicNameValuePair("password", password));

        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

        HttpResponse response = httpclient.execute(httppost);

        return response;
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
