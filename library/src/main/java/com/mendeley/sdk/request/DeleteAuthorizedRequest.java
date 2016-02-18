package com.mendeley.sdk.request;

import android.net.Uri;

import com.mendeley.sdk.AppCredentials;
import com.mendeley.sdk.AuthTokenManager;

import java.io.InputStream;

import okhttp3.Request;

/**
 * A NetworkProcedure specialised for making HTTP DELETE requests.
 */
public class DeleteAuthorizedRequest<ResultType> extends OkHttpAuthorizedRequest<ResultType> {

    public DeleteAuthorizedRequest(Uri url, AuthTokenManager authTokenManager, AppCredentials appCredentials) {
        super(url, authTokenManager, appCredentials);
    }

    @Override
    protected void setMethod(Request.Builder requestBld) throws Exception {
        requestBld.delete();
    }

    @Override
    protected ResultType manageResponse(InputStream is) throws Exception {
        return null;
    }
}
