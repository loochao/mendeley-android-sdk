package com.mendeley.sdk.request;

import android.net.Uri;

import com.mendeley.sdk.AuthTokenManager;
import com.mendeley.sdk.AppCredentials;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A NetworkProcedure specialised for making HTTP GET requests.
 */
public abstract class GetAuthorizedRequest<ResultType> extends HttpUrlConnectionAuthorizedRequest<ResultType> {

    protected GetAuthorizedRequest(Uri url, AuthTokenManager authTokenManager, AppCredentials appCredentials) {
        super(url, authTokenManager, appCredentials);
    }

    @Override
    protected final void onConnected(HttpURLConnection con) throws Exception {
    }

    @Override
    protected final HttpURLConnection createConnection(Uri uri) throws IOException {
        final URL url = new URL(uri.toString());
        final HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        return con;
    }

}
