package com.mendeley.sdk.request;

import android.net.Uri;

import com.mendeley.sdk.AuthTokenManager;
import com.mendeley.sdk.AppCredentials;
import com.mendeley.sdk.Request;
import com.mendeley.sdk.util.DateUtils;
import com.mendeley.sdk.util.NetworkUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Map;

/**
 * Request against the Mendeley API using the PATCH method.
 */
public abstract class PatchAuthorizedRequest<ResultType> extends HttpUrlConnectionAuthorizedRequest<ResultType> {

    private final Date ifModifiedSinceDate;

    public PatchAuthorizedRequest(Uri url, Date ifModifiedSinceDate, AuthTokenManager authTokenManager, AppCredentials appCredentials) {
        super(url, authTokenManager, appCredentials);
        this.ifModifiedSinceDate = ifModifiedSinceDate;
    }

    @Override
    protected HttpURLConnection createConnection(Uri uri) throws IOException {
        final URL url = new URL(uri.toString());
        final HttpURLConnection con = (HttpURLConnection) url.openConnection();
        NetworkUtils.setRequestMethodUsingWorkaroundForJREBug(con, "PATCH");
        return con;
    }

    @Override
    protected void appendHeaders(Map<String, String> headers) {
        super.appendHeaders(headers);
        headers.put("If-Unmodified-Since", DateUtils.formatMendeleyApiTimestamp(ifModifiedSinceDate));
    }

    @Override
    protected void onConnected(HttpURLConnection con) throws Exception {
        // wrapping the OutputStream of the connection in CancellableOutputStream to stop writing when the request is cancelled
        final OutputStream os = new MyCancelableOutputStream(con.getOutputStream());
        writePatchBody(os);
        os.close();
    }

    protected abstract void writePatchBody(OutputStream os) throws Exception;


    /**
     * Implementation of {@link CancellableOutputStream} that uses the {@link Request} to determine
     * whether it has been cancelled.
     */
    private class MyCancelableOutputStream extends CancellableOutputStream {
        public MyCancelableOutputStream(OutputStream delegate) {
            super(delegate);
        }

        @Override
        protected boolean isCancelled() {
            return PatchAuthorizedRequest.this.isCancelled();
        }
    }

}
