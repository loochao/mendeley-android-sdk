package com.mendeley.sdk.request;

import android.net.Uri;

import com.mendeley.sdk.AppCredentials;
import com.mendeley.sdk.AuthTokenManager;

import org.json.JSONException;

import okhttp3.RequestBody;

/**
 * Request against the Mendeley API using the POST method.
 */
public abstract class PostAuthorizedRequest<ResultType> extends OkHttpAuthorizedRequest<ResultType> {

    public PostAuthorizedRequest(Uri url, AuthTokenManager authTokenManager, AppCredentials appCredentials) {
        super(url, authTokenManager, appCredentials);
    }


    @Override
    protected final void setMethod(okhttp3.Request.Builder requestBld) throws Exception {
        requestBld.post(getBody());
    }

    protected abstract RequestBody getBody() throws JSONException;


}
