package com.mendeley.api.request.procedure;

import android.net.Uri;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.request.NetworkUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * A NetworkProcedure specialised for making HTTP DELETE requests.
 */
public class DeleteNetworkRequest extends HttpUrlConnectionRequest<Void> {

    public DeleteNetworkRequest(String url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
        super(url, null, authTokenManager, clientCredentials);
    }

    @Override
    protected HttpURLConnection createConnection(Uri uri) throws IOException {
        return NetworkUtils.createHttpsGetConnection(uri.toString(), "DELETE");
    }

    @Override
    protected void onConnected(HttpURLConnection con) throws Exception {

    }

    @Override
    protected Void manageResponse(InputStream is) throws Exception {
        return null;
    }
}
