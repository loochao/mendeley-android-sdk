package com.mendeley.api.impl;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.mendeley.api.ClientCredentials;
import com.mendeley.api.auth.AuthenticationManager;
import com.mendeley.api.callbacks.MendeleySignInInterface;
import com.mendeley.api.network.NetworkUtils;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Internal version of MendeleySdk.
 * <p>
 * This is used to run integration tests on the SDK, in which sign is handled via the resource owner
 * password flow.
 * <p>
 * Developer applications should not use this class.
 */
public class InternalMendeleySdk extends AsyncMendeleySdk {
    public static final String GRANT_TYPE_PASSWORD = "password";

    private String username;
    private String password;

    public InternalMendeleySdk(String username, String password, ClientCredentials clientCredentials) {
        authenticationManager = new AuthenticationManager(
                new InMemoryCredentialsManager(),
                createAuthenticationInterface(),
                clientCredentials.clientId,
                clientCredentials.clientSecret,
                clientCredentials.redirectUri);

        this.username = username;
        this.password = password;
    }

    @Override
    public void signIn(Activity activity, MendeleySignInInterface signInCallback, boolean showSplash) {
        this.mendeleySignInInterface = signInCallback;

        initProviders();

        if (authenticationManager.isSignedIn()) {
            return;
        }
        new PasswordAuthenticationTask().execute();
    }

    /**
     * AsyncTask class that obtains an access token from username and password.
     */
    private class PasswordAuthenticationTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            String result = null;
            try {
                HttpResponse response = doPasswordPost();
                String jsonTokenString = NetworkUtils.readInputStream(response.getEntity().getContent());
                authenticationManager.setTokenDetails(jsonTokenString);
                result = "ok";
            } catch (IOException e) {
                Log.e(TAG, "", e);
            } catch (JSONException e) {
                Log.e(TAG, "", e);
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null) {
                authenticationManager.failedToAuthenticate();
            } else {
                authenticationManager.authenticated(true);
            }
        }
    }

    /**
     * Helper method for executing http post request for password-based authentication.
     */
    private HttpResponse doPasswordPost() throws ClientProtocolException, IOException {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(AuthenticationManager.TOKENS_URL);

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("grant_type", GRANT_TYPE_PASSWORD));
        nameValuePairs.add(new BasicNameValuePair("scope", AuthenticationManager.SCOPE));
        nameValuePairs.add(new BasicNameValuePair("client_id", authenticationManager.getClientId()));
        nameValuePairs.add(new BasicNameValuePair("client_secret", authenticationManager.getClientSecret()));
        nameValuePairs.add(new BasicNameValuePair("username", username));
        nameValuePairs.add(new BasicNameValuePair("password", password));

        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

        HttpResponse response = httpclient.execute(httppost);

        return response;
    }

}
