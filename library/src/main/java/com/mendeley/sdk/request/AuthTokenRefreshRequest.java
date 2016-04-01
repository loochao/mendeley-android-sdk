package com.mendeley.sdk.request;

import android.net.Uri;

import com.mendeley.sdk.AuthTokenManager;
import com.mendeley.sdk.AppCredentials;
import com.mendeley.sdk.Request;
import com.mendeley.sdk.exceptions.AuthenticationException;
import com.mendeley.sdk.exceptions.HttpResponseException;
import com.mendeley.sdk.exceptions.MendeleyException;
import com.mendeley.sdk.util.NetworkUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Request that refreshes an expired access token.
 * A valid refresh token in the passed {@link AuthTokenManager} is needed.
 */
public class AuthTokenRefreshRequest extends Request<Void> {

    protected final AuthTokenManager authTokenManager;
    protected final AppCredentials appCredentials;

    /**
     * Constructor
     *
     * @param authTokenManager used to get the refresh token
     * @param appCredentials used to refresh the access token
     */
    public AuthTokenRefreshRequest(AuthTokenManager authTokenManager, AppCredentials appCredentials) {
        super(Uri.parse(AuthTokenManager.TOKENS_URL));
        this.authTokenManager = authTokenManager;
        this.appCredentials = appCredentials;
    }

    @Override
    public Response doRun() throws MendeleyException {
        final String url = getUrl().toString();
        HttpURLConnection con = null;

        try {
            con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("POST");
            con.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            con.setDoInput(true);
            con.setDoOutput(true);

            final String urlEncodedForm = new Uri.Builder()
                    .appendQueryParameter("grant_type", "refresh_token")
                    .appendQueryParameter("redirect_uri", AuthTokenManager.REDIRECT_URI)
                    .appendQueryParameter("client_id", appCredentials.clientId)
                    .appendQueryParameter("client_secret", appCredentials.clientSecret)
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
                throw new HttpResponseException(statusCode, con.getResponseMessage(), url, responseBody, con.getHeaderField("X-Mendeley-Trace-Id"));
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
