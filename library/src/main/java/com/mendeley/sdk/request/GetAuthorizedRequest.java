package com.mendeley.sdk.request;

import android.net.Uri;

import com.mendeley.sdk.ClientCredentials;
import com.mendeley.sdk.AuthTokenManager;

import org.json.JSONException;

import java.io.InputStream;

/**
 * Request against the Mendeley API using the GET method.
 */
public  class GetAuthorizedRequest<ResultType> extends OkHttpAuthorizedRequest<ResultType> {

    protected GetAuthorizedRequest(Uri url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
        super(url, authTokenManager, clientCredentials);
    }

    @Override
    protected void setMethod(okhttp3.Request.Builder requestBld) throws JSONException, Exception {
        requestBld.get();
    }

    @Override
    protected ResultType manageResponse(InputStream is) throws Exception {
        return null;
    }

}

