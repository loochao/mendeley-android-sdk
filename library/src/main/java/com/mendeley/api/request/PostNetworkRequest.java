package com.mendeley.api.request;

import android.net.Uri;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;

import org.json.JSONException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public abstract class PostNetworkRequest<ResultType> extends HttpUrlConnectionAuthorizedRequest<ResultType> {

    public PostNetworkRequest(String url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
        super(url, authTokenManager, clientCredentials);
    }


    @Override
    protected HttpsURLConnection createConnection(Uri uri) throws IOException {
        final URL url = new URL(uri.toString());
        final HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setConnectTimeout(CONNECTION_TIMEOUT);
        con.setReadTimeout(READ_TIMEOUT);
        con.setRequestMethod("POST");

        return con;
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
