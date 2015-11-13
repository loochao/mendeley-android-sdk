package com.mendeley.api.request;

import android.net.Uri;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.request.procedure.HttpUrlConnectionRequest;

import java.io.IOException;
import java.net.HttpURLConnection;

import javax.net.ssl.HttpsURLConnection;

import static com.mendeley.api.request.NetworkUtils.createHttpsGetConnection;

/**
 * A NetworkProcedure specialised for making HTTP GET requests.
 */
public abstract class GetNetworkRequest<ResultType> extends HttpUrlConnectionRequest<ResultType> {

    protected GetNetworkRequest(String url, String contentType, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
        super(url, contentType, authTokenManager, clientCredentials);
    }

    @Override
    protected final void onConnected(HttpURLConnection con) throws Exception {
    }

    @Override
    protected final HttpsURLConnection createConnection(Uri uri) throws IOException {
        return createHttpsGetConnection(uri.toString(), "GET");
    }

}
