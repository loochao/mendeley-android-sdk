package com.mendeley.sdk.request;

import android.net.Uri;

import com.mendeley.sdk.AuthTokenManager;
import com.mendeley.sdk.ClientCredentials;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * A NetworkProcedure specialised for making HTTP GET requests.
 */
public abstract class GetAuthorizedRequest<ResultType> extends HttpUrlConnectionAuthorizedRequest<ResultType> {

    protected GetAuthorizedRequest(Uri url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
        super(url, authTokenManager, clientCredentials);
    }

    @Override
    protected final void onConnected(HttpURLConnection con) throws Exception {
    }

    @Override
    protected final HttpsURLConnection createConnection(Uri uri) throws IOException {
        final URL url = new URL(uri.toString());
        final HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        return con;
    }

}
