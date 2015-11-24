package com.mendeley.sdk.request;

import android.net.Uri;

import com.mendeley.sdk.AuthTokenManager;
import com.mendeley.sdk.ClientCredentials;
import com.mendeley.sdk.exceptions.AuthenticationException;
import com.mendeley.sdk.exceptions.HttpResponseException;
import com.mendeley.sdk.exceptions.MendeleyException;
import com.mendeley.sdk.util.NetworkUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class AuthTokenRefreshRequest extends Request<Void> {

    public AuthTokenRefreshRequest(AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
        super(Uri.parse(AuthTokenManager.TOKENS_URL), authTokenManager, clientCredentials);
    }

    @Override
    public Response run() throws MendeleyException {
        HttpsURLConnection con = null;
        try {
            con = (HttpsURLConnection) new URL(getUrl().toString()).openConnection();
            con.setRequestMethod("POST");
            con.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            con.setDoInput(true);
            con.setDoOutput(true);

            final String urlEncodedForm = new Uri.Builder()
                    .appendQueryParameter("grant_type", "refresh_token")
                    .appendQueryParameter("redirect_uri", clientCredentials.redirectUri)
                    .appendQueryParameter("client_id", clientCredentials.clientId)
                    .appendQueryParameter("client_secret", clientCredentials.clientSecret)
                    .appendQueryParameter("refresh_token", authTokenManager.getRefreshToken())
                    .build()
                    .getEncodedQuery();


            //Send request
            DataOutputStream wr = new DataOutputStream(con.getOutputStream ());
            wr.writeBytes (urlEncodedForm);
            wr.flush ();
            wr.close ();

            final int statusCode = con.getResponseCode();

            if (statusCode != 200) {
                String responseBody = "";
                try {
                    responseBody = NetworkUtils.readInputStream(con.getInputStream());
                } catch (IOException ignored) {
                }
                throw new HttpResponseException(statusCode, con.getResponseMessage(), getUrl(), responseBody);
            }

            final String responseString = NetworkUtils.readInputStream(con.getInputStream());
            onJsonStringResult(responseString);

        } catch (Exception e) {
            throw new AuthenticationException("Cannot refresh token", e);
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }

        return null;
    }

    private void onJsonStringResult(String jsonTokenString) throws JSONException {
        final JSONObject tokenObject = new JSONObject(jsonTokenString);

        final String accessToken = tokenObject.getString("access_token");
        final String refreshToken = tokenObject.getString("refresh_token");
        final String tokenType = tokenObject.getString("token_type");
        final int expiresIn = tokenObject.getInt("expires_in");

        authTokenManager.saveTokens(accessToken, refreshToken, tokenType, expiresIn);
    }

}
