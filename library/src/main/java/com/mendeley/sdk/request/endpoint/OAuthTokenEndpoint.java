package com.mendeley.sdk.request.endpoint;


import android.net.Uri;

import com.mendeley.sdk.ClientCredentials;
import com.mendeley.sdk.AuthTokenManager;
import com.mendeley.sdk.Request;
import com.mendeley.sdk.exceptions.HttpResponseException;
import com.mendeley.sdk.exceptions.JsonParsingException;
import com.mendeley.sdk.exceptions.MendeleyException;
import com.mendeley.sdk.util.NetworkUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Class with the implementation of typical {@link Request}s against the /oauth/token endpoint.
 */
public class OAuthTokenEndpoint {

    public final static String TOKENS_URL = Request.MENDELEY_API_BASE_URL + "/oauth/token";
    public final static String OAUTH2_URL = Request.MENDELEY_API_BASE_URL + "oauth/authorize";

    private final static String REDIRECT_URI = "http://localhost/auth_return";

    public static void saveTokens(AuthTokenManager authTokenManager, JSONObject serverResponse) throws JsonParsingException {
        try {
            final String accessToken = serverResponse.getString("access_token");
            final String refreshToken = serverResponse.getString("refresh_token");
            final String tokenType = serverResponse.getString("token_type");
            final int expiresIn = serverResponse.getInt("expires_in");

            authTokenManager.saveTokens(accessToken, refreshToken, tokenType, expiresIn);
        } catch (JSONException e) {
            throw new JsonParsingException("Could not parse the server response with the access token", e);
        }
    }

    /**
     * Base class for every {@link Request} related to the OAuth process.
     */
    private static abstract class OAuthTokenRequest extends Request<JSONObject> {

        private final ClientCredentials clientCredentials;

        public OAuthTokenRequest(ClientCredentials clientCredentials) {
            super(Uri.parse(TOKENS_URL));
            this.clientCredentials = clientCredentials;
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

                final Map<String, String> oauthParams = new HashMap<>();
                appendOAuthParams(oauthParams);

                final Uri.Builder urlEncodedFormBld = new Uri.Builder();
                urlEncodedFormBld.appendQueryParameter("client_id", clientCredentials.clientId);
                urlEncodedFormBld.appendQueryParameter("client_secret", clientCredentials.clientSecret);
                urlEncodedFormBld.appendQueryParameter("grant_type", getGrantType());
                urlEncodedFormBld.appendQueryParameter("scope", "all");

                for (final String key : oauthParams.keySet()) {
                    urlEncodedFormBld.appendQueryParameter(key, oauthParams.get(key));
                }

                final String body = urlEncodedFormBld.build().getEncodedQuery();

                //Send request
                DataOutputStream wr = new DataOutputStream(con.getOutputStream ());
                wr.writeBytes (body);
                wr.flush ();
                wr.close ();

                final int statusCode = con.getResponseCode();

                if (statusCode != 200) {
                    String responseBody = "";
                    try {
                        responseBody = NetworkUtils.readInputStream(con.getErrorStream());
                    } catch (IOException ignored) {
                    }
                    throw new HttpResponseException(statusCode, con.getResponseMessage(), url, responseBody, con.getHeaderField("X-Mendeley-Trace-Id"));
                }

                final String responseString = NetworkUtils.readInputStream(con.getInputStream());
                final JSONObject jsonResponse = new JSONObject(responseString);
                return new Response(jsonResponse, con.getHeaderField("Date"));

            } catch (MendeleyException me) {
                throw me;
            } catch (Exception e) {
                throw new MendeleyException("Cannot obtain token", e);
            } finally {
                if (con != null) {
                    con.disconnect();
                }
            }
        }

        public abstract String getGrantType();

        protected abstract void appendOAuthParams(Map<String, String> oauthParams);
    }


    /**
     * {@link Request} that obtains an access token. The token will only grant authorization to
     * the client, but not to perform operations on behalf of any specific user.
     */
    public static class AccessTokenWithClientCredentialsRequest extends OAuthTokenRequest {

        public AccessTokenWithClientCredentialsRequest(ClientCredentials clientCredentials) {
            super(clientCredentials);
        }

        @Override
        public String getGrantType() {
            return "client_credentials";
        }

        @Override
        protected void appendOAuthParams(Map<String, String> oauthParams) {
        }
    }

    /**
     * {@link Request} that obtains an access token using one username and password. The token will let perform operations on behalf
     * of the user.
     */
    public static class AccessTokenWithPasswordRequest extends OAuthTokenRequest {
        private final String username;
        private final String password;

        public AccessTokenWithPasswordRequest(ClientCredentials clientCredentials, String username, String password) {
            super(clientCredentials);
            this.username = username;
            this.password = password;
        }

        @Override
        public String getGrantType() {
            return "password";
        }

        @Override
        protected void appendOAuthParams(Map<String, String> oauthParams) {
            oauthParams.put("username", username);
            oauthParams.put("password", password);
        }
    }

    /**
     * {@link Request} that obtains an access token using one authorization token. The token will let perform operations on behalf
     * of the user.
     */
    public static class AccessTokenWithAuthorizationCodeRequest extends OAuthTokenRequest {

        private final String authorizationCode;

        public AccessTokenWithAuthorizationCodeRequest(ClientCredentials clientCredentials, String authorizationCode) {
            super(clientCredentials);
            this.authorizationCode = authorizationCode;
        }

        @Override
        public String getGrantType() {
            return "authorization_code";
        }

        @Override
        protected void appendOAuthParams(Map<String, String> oauthParams) {
            oauthParams.put("redirect_uri", REDIRECT_URI);
            oauthParams.put("code", authorizationCode);
        }
    }

    /**
     * {@link Request} that provides a n updated access token using one existing refresh token.
     */
    public static class RefreshTokenRequest extends OAuthTokenRequest {

        private final String refreshToken;

        public RefreshTokenRequest(ClientCredentials clientCredentials, String refreshToken) {
            super(clientCredentials);
            this.refreshToken = refreshToken;
        }

        @Override
        public String getGrantType() {
            return "refresh_token";
        }

        @Override
        protected void appendOAuthParams(Map<String, String> oauthParams) {
            oauthParams.put("refresh_token", refreshToken);
        }
    }
}
