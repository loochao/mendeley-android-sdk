package com.mendeley.api.auth;

import android.os.AsyncTask;
import android.util.Log;

import com.mendeley.api.callbacks.RequestHandle;
import com.mendeley.api.exceptions.AuthenticationException;
import com.mendeley.api.exceptions.HttpResponseException;
import com.mendeley.api.exceptions.JsonParsingException;
import com.mendeley.api.exceptions.MendeleyException;

import com.mendeley.api.network.NetworkUtils;
import com.mendeley.api.util.Utils;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.mendeley.api.impl.BaseMendeleySdk.Command;

public class AuthenticationManager implements AccessTokenProvider {
    public static final String TOKENS_URL = NetworkUtils.API_URL + "/oauth/token";
    public static final String GRANT_TYPE_AUTH = "authorization_code";
    public static final String GRANT_TYPE_REFRESH = "refresh_token";
    public static final String SCOPE = "all";
    public static final String RESPONSE_TYPE = "code";

    // Only use tokens which don't expire in the next 5 mins:
    private static final int MIN_TOKEN_VALIDITY_SEC = 300;

    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

	private final CredentialsManager credentialsManager;
	private final AuthenticationInterface authInterface;
	
	private static final String TAG = AuthenticationManager.class.getSimpleName();

    public AuthenticationManager(CredentialsManager credentialsManager, AuthenticationInterface authInterface,
                                 String clientId, String clientSecret, String redirectUri) {

        this.credentialsManager = credentialsManager;
        this.authInterface = authInterface;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }

    public boolean isSignedIn() {
        return credentialsManager.hasCredentials();
    }

    public void clearCredentials() {
        credentialsManager.clearCredentials();
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public String getAccessToken() {
        return credentialsManager.getAccessToken();
    }

    /**
     * Extracts the token details from the token string and sends them to the CredentialManager.
     *
     * @param tokenString
     * @throws JSONException
     */
    public void setTokenDetails(String tokenString) throws JSONException {
        String accessToken;
        String refreshToken;
        String tokenType;
        int expiresIn;

        try {
            JSONObject tokenObject = new JSONObject(tokenString);

            accessToken = tokenObject.getString("access_token");
            refreshToken = tokenObject.getString("refresh_token");
            tokenType = tokenObject.getString("token_type");
            expiresIn = tokenObject.getInt("expires_in");
        } catch(JSONException e) {
            // If the client credentials are incorrect, the tokenString contains an error message
            Log.e(TAG, "Error token string: " + tokenString);
            throw e;
        }

        credentialsManager.setCredentials(accessToken, refreshToken, tokenType, expiresIn);
    }

    public void authenticated(boolean manualSignIn) {
        authInterface.onAuthenticated(manualSignIn);
    }

    public void failedToAuthenticate() {
        authInterface.onAuthenticationFail();
    }

    /**
     * Checks if the current access token will expire soon (or isn't valid at all).
     */
    public boolean willExpireSoon() {
        if (!credentialsManager.hasCredentials() || credentialsManager.getExpiresAt() == null) {
            return true;
        }
        Date now = new Date();
        Date expires = null;
        try {
            expires = Utils.dateFormat.parse(credentialsManager.getExpiresAt());
        } catch (ParseException e) {
            return true;
        }

        long timeToExpiryMs = expires.getTime() - now.getTime();
        long timeToExpirySec = TimeUnit.MILLISECONDS.toSeconds(timeToExpiryMs);

        return timeToExpirySec < MIN_TOKEN_VALIDITY_SEC;
    }

    /**
     * Start the refresh process, and return a provisional RequestHandle
     */
    public RequestHandle refreshToken(Command command) {
        RefreshTokenTask refreshTask = new RefreshTokenTask(command);
        RequestHandle requestHandle = refreshTask.getRequestHandle();
        refreshTask.execute();
        return requestHandle;
    }

    /**
     * Refresh the token. Blocks until done.
     */
    public void refreshToken() throws MendeleyException {
        try {
            final HttpResponse response = doRefreshPost();
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                final HttpResponseException cause = HttpResponseException.create(response, TOKENS_URL);
                throw new AuthenticationException("Cannot refresh token", cause);
            }
            final String responseString = NetworkUtils.readInputStream(response.getEntity().getContent());
            setTokenDetails(responseString);
        } catch (IOException e) {
            throw new MendeleyException("Cannot refresh token", e);
        } catch (JSONException e) {
            throw new JsonParsingException("Cannot refresh token", e);
        }
    }

    /**
	 * Task to refresh the access token.
	 */
    private class RefreshTokenTask extends AsyncTask<Void, Void, Boolean> {
        private Command command;
        private ChainedRequestHandle chainedRequestHandle;

        public RefreshTokenTask(Command command) {
            this.command = command;
            chainedRequestHandle = new ChainedRequestHandle();
        }

        public RequestHandle getRequestHandle() {
            return chainedRequestHandle;
        }

		@Override
		protected Boolean doInBackground(Void... params) {
			boolean result = false;
            try {
                HttpResponse response = doRefreshPost();
                String jsonTokenString = NetworkUtils.readInputStream(response.getEntity().getContent());
                setTokenDetails(jsonTokenString);
                result = true;
            } catch (IOException ignored) {
            } catch (JSONException ignored) {
            }
            return result;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
            if (!chainedRequestHandle.isCancelled()) {
                RequestHandle innerRequestHandle = command.exec();
                chainedRequestHandle.setInnerRequestHandle(innerRequestHandle);
            }
		}
    }

    private class ChainedRequestHandle implements RequestHandle {
        private boolean cancelled;
        private RequestHandle innerRequestHandle;

        public ChainedRequestHandle() {
            cancelled = false;
        }

        public boolean isCancelled() {
            return cancelled;
        }

        public void setInnerRequestHandle(RequestHandle handle) {
            innerRequestHandle = handle;
        }

        @Override
        public void cancel() {
            cancelled = true;
            if (innerRequestHandle != null) {
                innerRequestHandle.cancel();
            }
        }
    }

    /**
	 * Helper method for executing http post request for token refresh.
	 */
	private HttpResponse doRefreshPost() throws ClientProtocolException, IOException {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(TOKENS_URL);
        
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("grant_type", GRANT_TYPE_REFRESH));
        nameValuePairs.add(new BasicNameValuePair("redirect_uri", redirectUri));
        nameValuePairs.add(new BasicNameValuePair("client_id", clientId));
        nameValuePairs.add(new BasicNameValuePair("client_secret", clientSecret));
        nameValuePairs.add(new BasicNameValuePair("refresh_token", credentialsManager.getRefreshToken()));
        
        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

        HttpResponse response = httpclient.execute(httppost);
		  
		return response;  
	}

}
