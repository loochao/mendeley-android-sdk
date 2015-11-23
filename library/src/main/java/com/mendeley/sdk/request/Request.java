package com.mendeley.sdk.request;

import android.net.Uri;
import android.os.AsyncTask;

import com.mendeley.sdk.AuthTokenManager;
import com.mendeley.sdk.BuildConfig;
import com.mendeley.sdk.ClientCredentials;
import com.mendeley.sdk.exceptions.MendeleyException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Executor;

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

    private final Uri uri;

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

    public Request(Uri uri, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
        this.uri = uri;
        this.authTokenManager = authTokenManager;
        this.clientCredentials = clientCredentials;
    }

    public final Uri getUrl() {
        return uri;
    }

    public abstract Response run() throws MendeleyException;

    public final void runAsync(final RequestCallback<ResultType> callback) {
        runAsync(callback, AsyncTask.SERIAL_EXECUTOR);
    }

    public final void runAsync(final RequestCallback<ResultType> callback, Executor executor) {
        final RequestAsyncTask task = new RequestAsyncTask(callback);
        task.executeOnExecutor(executor);
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

    /**
     * {@link AsyncTask} to execute the {@link Request} asynchronously
     */
    public final class RequestAsyncTask extends AsyncTask<Void, Void, RequestResponseMaybe> {

        private final RequestCallback<ResultType> callback;

        public RequestAsyncTask(RequestCallback<ResultType> callback) {
            this.callback = callback;
        }

        @Override
        protected RequestResponseMaybe doInBackground(Void... params) {
            try {
                return new RequestResponseMaybe(Request.this.run());
            } catch (MendeleyException e) {
                return new RequestResponseMaybe(e);
            }
        }


        @Override
        protected final void onCancelled() {
            super.onCancelled();
            if (callback != null) {
                callback.onCancelled();
            }
        }

        @Override
        protected final void onPostExecute(RequestResponseMaybe maybe) {
            super.onPostExecute(maybe);

            if (Request.this.isCancelled()) {
                callback.onCancelled();
            } else if (maybe.error != null) {
                callback.onFailure(maybe.error);
            } else {
                callback.onSuccess(maybe.response.resource, maybe.response.next, maybe.response.serverDate);
            }
        }
    }


    /**
     * Callback for when the {@link Request} is executed asynchronously
     *
     * @param <ResultType>
     */
    public interface RequestCallback<ResultType> {
        void onSuccess(ResultType resource, Uri next, Date serverDate);

        void onFailure(MendeleyException mendeleyException);

        void onCancelled();
    }

    /**
     * Class for holding the result or the exception of the request
     */
    private class RequestResponseMaybe {

        public final Response response;
        public final MendeleyException error;

        public RequestResponseMaybe(Response response) {
            this(response, null);
        }

        public RequestResponseMaybe(MendeleyException error) {
            this(null, error);
        }

        private RequestResponseMaybe(Response response, MendeleyException error) {
            this.response = response;
            this.error = error;
        }
    }

}
