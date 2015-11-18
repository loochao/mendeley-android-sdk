package com.mendeley.api.request;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.BuildConfig;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.model.RequestResponse;

/**
 * HTTP request launched against the Mendeley Web API.
 *
 * @param <ResultType>
 */
public abstract class Request<ResultType> {

    protected static final String TAG = Request.class.getSimpleName();

    public static final int CONNECTION_TIMEOUT = 1500;
    public static final int READ_TIMEOUT = 15000 ;
    public static final String MENDELEY_API_BASE_URL = BuildConfig.WEB_API_BASE_URL;

    protected final AuthTokenManager authTokenManager;
    protected final ClientCredentials clientCredentials;

    // Number of times to retry failed HTTP requests due to IOExceptions.
    protected static final int MAX_HTTP_RETRIES = 0;

    public abstract RequestResponse<ResultType> run() throws MendeleyException;

    private boolean cancelled;

    public Request(AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
        this.authTokenManager = authTokenManager;
        this.clientCredentials = clientCredentials;
    }

    public final void cancel() {
        cancelled = true;
    }

    public final boolean isCancelled() {
        return cancelled;
    }

}
