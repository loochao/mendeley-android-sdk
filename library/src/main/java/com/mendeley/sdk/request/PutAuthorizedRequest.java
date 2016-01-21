package com.mendeley.sdk.request;

import android.net.Uri;

import com.mendeley.sdk.AppCredentials;
import com.mendeley.sdk.AuthTokenManager;
import com.mendeley.sdk.Request;
import com.mendeley.sdk.util.NetworkUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Request against the Mendeley API using the PUT method.
 */
public abstract class PutAuthorizedRequest<ResultType> extends HttpUrlConnectionAuthorizedRequest<ResultType> {

    public PutAuthorizedRequest(Uri url, AuthTokenManager authTokenManager, AppCredentials appCredentials) {
        super(url, authTokenManager, appCredentials);
    }

    @Override
    protected HttpURLConnection createConnection(Uri uri) throws IOException {
        final URL url = new URL(uri.toString());
        final HttpURLConnection con = (HttpURLConnection) url.openConnection();
        NetworkUtils.setRequestMethodUsingWorkaroundForJREBug(con, "PUT");
        return con;
    }

    @Override
    protected void onConnected(HttpURLConnection con) throws Exception {
        // wrapping the OutputStream of the connection in CancellableOutputStream to stop writing when the request is cancelled
        final OutputStream os = new MyCancelableOutputStream(con.getOutputStream());
        writePutBody(os);
        os.close();
    }

    protected abstract void writePutBody(OutputStream os) throws Exception;


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
            return PutAuthorizedRequest.this.isCancelled();
        }
    }

}
