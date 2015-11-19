package com.mendeley.api.request;

import android.net.Uri;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.BuildConfig;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.exceptions.MendeleyException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * HTTP request launched against the Mendeley Web API.
 *
 * @param <ResultType>
 */
public abstract class Request<ResultType> {

    // RFC 7231 format, used for Dates in HTTP headers.
    private final static SimpleDateFormat httpHeaderDateFormat;

    public static final String MENDELEY_API_BASE_URL = BuildConfig.WEB_API_BASE_URL;
    protected static final int CONNECTION_TIMEOUT = 1500;
    protected static final int READ_TIMEOUT = 15000 ;

    // Number of times to retry failed HTTP requests due to IOExceptions.
    protected static final int MAX_HTTP_RETRIES = 0;

    static {
        httpHeaderDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy kk:mm:ss 'GMT'", Locale.US);
        httpHeaderDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private static Date parseHeaderDate(String serverDateStr) {
        try {
            return httpHeaderDateFormat.parse(serverDateStr);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not parse server date header", e);
        }
    }


    private boolean cancelled;
    protected final AuthTokenManager authTokenManager;
    protected final ClientCredentials clientCredentials;

    public abstract Response run() throws MendeleyException;

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

    /**
     * Response of the @{link Request}
     */
    public class Response {

        public final ResultType resource;
        public final Uri next;

        public final Date serverDate;

        public Response(ResultType resource, Date serverDate, Uri next) {
            this.resource = resource;
            this.next = next;
            this.serverDate = serverDate;
        }

        public Response(ResultType resource, String serverDateStr, Uri next) {
            this(resource, parseHeaderDate(serverDateStr), next);
        }

        public Response(ResultType resource, String serverDateStr) {
            this(resource, serverDateStr, null);
        }

    }
}
