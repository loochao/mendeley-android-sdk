package com.mendeley.sdk.request;

import android.net.Uri;

import com.mendeley.sdk.AuthTokenManager;
import com.mendeley.sdk.AppCredentials;

import org.json.JSONException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public abstract class PostAuthorizedRequest<ResultType> extends HttpUrlConnectionAuthorizedRequest<ResultType> {

    public PostAuthorizedRequest(Uri url, AuthTokenManager authTokenManager, AppCredentials appCredentials) {
        super(url, authTokenManager, appCredentials);
    }


    @Override
    protected HttpURLConnection createConnection(Uri uri) throws IOException {
        final URL url = new URL(uri.toString());
        final HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        return con;
    }

    @Override
    protected void onConnected(HttpURLConnection con) throws Exception {
        // wrapping the OutputStream of the connection in CancellableOutputStream to stop writing when the request is cancelled
        final OutputStream os = new MyCancelableOutputStream(con.getOutputStream());
        writePostBody(os);
        os.close();
    }


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
            return PostAuthorizedRequest.this.isCancelled();
        }
    }


    /**
     * @return writes the data to post in the body of the request
     * @throws JSONException
     */
    protected abstract void writePostBody(OutputStream os) throws Exception;

}
