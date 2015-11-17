package com.mendeley.api.request;

import android.net.Uri;
import android.util.Log;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.exceptions.AuthenticationException;
import com.mendeley.api.exceptions.HttpResponseException;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.model.RequestResponse;

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

public class AuthTokenRefreshRequest extends Request<Void> {

    public AuthTokenRefreshRequest(AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
        super(authTokenManager, clientCredentials);
    }

    @Override
    public RequestResponse<Void> run() throws MendeleyException {
        try {
            final HttpClient httpclient = new DefaultHttpClient();
            final HttpPost httppost = new HttpPost(AuthTokenManager.TOKENS_URL);

            final List<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair("grant_type", AuthTokenManager.GRANT_TYPE_REFRESH));
            nameValuePairs.add(new BasicNameValuePair("redirect_uri", clientCredentials.redirectUri));
            nameValuePairs.add(new BasicNameValuePair("client_id", clientCredentials.clientId));
            nameValuePairs.add(new BasicNameValuePair("client_secret", clientCredentials.clientSecret));
            nameValuePairs.add(new BasicNameValuePair("refresh_token", authTokenManager.getRefreshToken()));

            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            final HttpResponse response = httpclient.execute(httppost);
            final int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != 200) {
                String responseString = null;
                try {
                    responseString = NetworkUtils.readInputStream(response.getEntity().getContent());
                } catch (IOException ignored) {
                }
                throw new HttpResponseException(statusCode, response.getStatusLine().getReasonPhrase(), Uri.parse(AuthTokenManager.TOKENS_URL), responseString);
            }

            final String responseString = NetworkUtils.readInputStream(response.getEntity().getContent());
            onJsonStringResult(responseString);
        } catch (Exception e) {
            throw new AuthenticationException("Cannot refresh token", e);
        }

        return null;
    }


    private boolean onJsonStringResult(String jsonTokenString) {
        try {
            final JSONObject tokenObject = new JSONObject(jsonTokenString);

            final String accessToken = tokenObject.getString("access_token");
            final String refreshToken = tokenObject.getString("refresh_token");
            final String tokenType = tokenObject.getString("token_type");
            final int expiresIn = tokenObject.getInt("expires_in");

            authTokenManager.saveTokens(accessToken, refreshToken, tokenType, expiresIn);
            return true;
        } catch (JSONException e) {
            // If the client credentials are incorrect, the tokenString contains an error message
            Log.e(TAG, "Could not parse the json response with the auth tokens: " + jsonTokenString, e);
            return false;
        }
    }

}
