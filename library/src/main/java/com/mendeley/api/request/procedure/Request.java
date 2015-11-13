package com.mendeley.api.request.procedure;

import android.text.TextUtils;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.exceptions.HttpResponseException;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.exceptions.NotSignedInException;
import com.mendeley.api.model.RequestResponse;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public abstract class Request<ResultType> {

    protected static final String TAG = Request.class.getSimpleName();

    protected final AuthTokenManager authTokenManager;
    protected final ClientCredentials clientCredentials;

    // Number of times to retry failed HTTP requests due to IOExceptions.
    protected static final int MAX_HTTP_RETRIES = 0;


    private boolean cancelled;

    public Request(AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
        this.authTokenManager = authTokenManager;
        this.clientCredentials = clientCredentials;
    }

    public final RequestResponse<ResultType> run() throws MendeleyException {
        if (TextUtils.isEmpty(authTokenManager.getAccessToken())) {
            // Must call startSignInProcess first - caller error!
            throw new NotSignedInException();
        }
        if (willExpireSoon()) {
            refreshToken();
        }
        try {
            return doRun();
        } catch (HttpResponseException e) {
            if (e.httpReturnCode == 401 && e.getMessage().contains("Token has expired")) {
                // The refresh-token-in-advance logic did not work for some reason: force a refresh now
                refreshToken();
                return doRun();
            } else {
                throw e;
            }
        }
    }

    protected abstract RequestResponse<ResultType> doRun() throws MendeleyException;

    private final void refreshToken() throws MendeleyException {
        new AuthTokenRefreshRequest(authTokenManager, clientCredentials).doRun();
    }

    public final void cancel() {
        cancelled = true;
    }

    public final boolean isCancelled() {
        return cancelled;
    }

    // TODO: consider drop this to reduce complexity
    /**
     * Checks if the current access token will expire soon (or isn't valid at all).
     */
    private boolean willExpireSoon() {
        if (TextUtils.isEmpty(authTokenManager.getAccessToken()) || authTokenManager.getAuthTenExpiresAt() == null) {
            return true;
        }
        Date now = new Date();
        Date expires = authTokenManager.getAuthTenExpiresAt();
        long timeToExpiryMs = expires.getTime() - now.getTime();
        long timeToExpirySec = TimeUnit.MILLISECONDS.toSeconds(timeToExpiryMs);
        return timeToExpirySec < AuthTokenManager.MIN_TOKEN_VALIDITY_SEC;
    }


}
