package com.mendeley.sdk.request;

import android.net.Uri;

import com.mendeley.sdk.AuthTokenManager;
import com.mendeley.sdk.AppCredentials;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A NetworkProcedure specialised for making HTTP DELETE requests.
 */
public class DeleteAuthorizedRequest<ResultType> extends HttpUrlConnectionAuthorizedRequest<ResultType> {

    public DeleteAuthorizedRequest(Uri url, AuthTokenManager authTokenManager, AppCredentials appCredentials) {
        super(url, authTokenManager, appCredentials);
    }

    @Override
    protected HttpURLConnection createConnection(Uri uri) throws IOException {
        final URL url = new URL(uri.toString());
        final HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("DELETE");
        return con;
    }

    @Override
    protected void onConnected(HttpURLConnection con) throws Exception {

    }

    @Override
    protected ResultType manageResponse(InputStream is) throws Exception {
        return null;
    }
}
