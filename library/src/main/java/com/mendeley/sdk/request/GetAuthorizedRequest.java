package com.mendeley.sdk.request;

import android.net.Uri;

import com.mendeley.sdk.AppCredentials;
import com.mendeley.sdk.AuthTokenManager;

import org.json.JSONException;

import java.io.InputStream;

/**
 * Request against the Mendeley API using the GET method.
 */
public  class GetAuthorizedRequest<ResultType> extends OkHttpAuthorizedRequest<ResultType> {

    protected GetAuthorizedRequest(Uri url, AuthTokenManager authTokenManager, AppCredentials appCredentials) {
        super(url, authTokenManager, appCredentials);
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

