package com.mendeley.api.request.procedure;

import android.net.Uri;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.request.NetworkUtils;

import org.json.JSONException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import javax.net.ssl.HttpsURLConnection;

public abstract class PostNetworkRequest<ResultType> extends HttpUrlConnectionRequest<ResultType> {

    public PostNetworkRequest(String url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
        super(url, authTokenManager, clientCredentials);
    }


    @Override
    protected HttpsURLConnection createConnection(Uri uri) throws IOException {
        HttpsURLConnection connection = NetworkUtils.createHttpsGetConnection(uri.toString(), "POST");
        connection.setDoOutput(true);
        connection.setChunkedStreamingMode(0);
        return connection;
    }

    @Override
    protected void onConnected(HttpURLConnection con) throws Exception {
        final OutputStream os = con.getOutputStream();
        writePostBody(os);
        os.close();
    }


    /**
     * @return writes the data to post in the body of the request
     * @throws JSONException
     */
    protected abstract void writePostBody(OutputStream os) throws Exception;

}
