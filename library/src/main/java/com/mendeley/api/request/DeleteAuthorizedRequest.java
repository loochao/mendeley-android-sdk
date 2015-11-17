package com.mendeley.api.request;

import android.net.Uri;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * A NetworkProcedure specialised for making HTTP DELETE requests.
 */
public class DeleteAuthorizedRequest<ResultType> extends HttpUrlConnectionAuthorizedRequest<ResultType> {

    public DeleteAuthorizedRequest(Uri url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
        super(url, authTokenManager, clientCredentials);
    }

    @Override
    protected HttpURLConnection createConnection(Uri uri) throws IOException {
        final URL url = new URL(uri.toString());
        final HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setConnectTimeout(CONNECTION_TIMEOUT);
        con.setReadTimeout(READ_TIMEOUT);
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
