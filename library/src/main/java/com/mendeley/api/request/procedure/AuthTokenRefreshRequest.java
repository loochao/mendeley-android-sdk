package com.mendeley.api.request.procedure;

import android.util.Log;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.exceptions.AuthenticationException;
import com.mendeley.api.exceptions.HttpResponseException;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.model.RequestResponse;
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

public class AuthTokenRefreshRequest extends Request<Void> {

    public AuthTokenRefreshRequest(AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
        super(authTokenManager, clientCredentials);
    }

    @Override
    protected RequestResponse<Void> doRun() throws MendeleyException {
        try {
            final HttpResponse response = doRefreshPost();
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                final HttpResponseException cause = HttpResponseException.create(response, AuthTokenManager.TOKENS_URL);
                throw new AuthenticationException("Cannot refresh token", cause);
            }
            final String responseString = NetworkUtils.readInputStream(response.getEntity().getContent());
            onJsonStringResult(responseString);
        } catch (IOException e) {
            throw new MendeleyException("Cannot refresh token", e);
        }

        return null;
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

    // TODO: convert this to a request
    /**
     * Helper method for executing http post request for token refresh.
     */
    private HttpResponse doRefreshPost() throws IOException {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(AuthTokenManager.TOKENS_URL);

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("grant_type", AuthTokenManager.GRANT_TYPE_REFRESH));
        nameValuePairs.add(new BasicNameValuePair("redirect_uri", clientCredentials.redirectUri));
        nameValuePairs.add(new BasicNameValuePair("client_id", clientCredentials.clientId));
        nameValuePairs.add(new BasicNameValuePair("client_secret", clientCredentials.clientSecret));
        nameValuePairs.add(new BasicNameValuePair("refresh_token", authTokenManager.getRefreshToken()));

        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

        HttpResponse response = httpclient.execute(httppost);

        return response;
    }
}
