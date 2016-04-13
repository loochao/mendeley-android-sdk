package com.mendeley.sdk.request.endpoint;


import android.net.Uri;

import com.mendeley.sdk.AuthTokenManager;
import com.mendeley.sdk.ClientCredentials;
import com.mendeley.sdk.Request;
import com.mendeley.sdk.exceptions.HttpResponseException;
import com.mendeley.sdk.exceptions.JsonParsingException;
import com.mendeley.sdk.exceptions.MendeleyException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

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
            ResponseBody responseBody = null;

            try {
                final String url = getUrl().toString();

                final FormBody.Builder formBodyBld = new FormBody.Builder();
                formBodyBld.add("client_id", clientCredentials.clientId);
                formBodyBld.add("client_secret", clientCredentials.clientSecret);
                formBodyBld.add("grant_type", getGrantType());
                formBodyBld.add("scope", "all");

                final Map<String, String> oauthParams = new HashMap<>();
                appendOAuthParams(oauthParams);
                for (final String key : oauthParams.keySet()) {
                    formBodyBld.add(key, oauthParams.get(key));
                }

                final RequestBody postBody  = formBodyBld.build();

                okhttp3.Request okHttpRequest = new okhttp3.Request.Builder()
                        .url(url.toString())
                        .addHeader("Content-Type", "application/x-www-form-urlencoded")
                        .post(postBody)
                        .build();

                final okhttp3.Response okHttpResponse = new OkHttpClient().newCall(okHttpRequest).execute();

                final int responseCode = okHttpResponse.code();
                responseBody = okHttpResponse.body();
                if (responseCode != 200) {
                    throw new HttpResponseException(responseCode, okHttpResponse.message(), url.toString(), responseBody.string(), okHttpResponse.header("X-Mendeley-Trace-Id"));
                }

                final JSONObject jsonResponse = new JSONObject(responseBody.string());
                final Map<String, List<String>> responseHeaders = okHttpResponse.headers().toMultimap();
                return new Response(jsonResponse, getServerDateString(responseHeaders));

            } catch (MendeleyException me) {
                throw me;
            } catch (Exception e) {
                throw new MendeleyException("Cannot obtain token", e);
            } finally {
                if (responseBody != null) {
                    if (responseBody.byteStream() != null) {
                        try {
                            responseBody.byteStream().close();
                        } catch (IOException ignored) {
                        }
                    }
                    responseBody.close();
                }
            }
        }

        private String getServerDateString(Map<String, List<String>> headersMap) throws IOException {
            final List<String> dateHeaders = headersMap.get("Date");
            if (dateHeaders != null) {
                return headersMap.get("Date").get(0);
            }
            return null;
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
