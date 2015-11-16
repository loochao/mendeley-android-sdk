package com.mendeley.api.request.procedure;

import com.mendeley.api.AuthTokenManager;
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
