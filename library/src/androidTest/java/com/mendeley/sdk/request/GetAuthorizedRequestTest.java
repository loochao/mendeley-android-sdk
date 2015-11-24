package com.mendeley.sdk.request;

import android.net.Uri;

import org.json.JSONObject;

import java.io.InputStream;

import static com.mendeley.sdk.request.NetworkUtils.readInputStream;

public class GetAuthorizedRequestTest extends AuthorizedRequestTest {

    @Override
    protected AuthorizedRequest<JSONObject> createRequest() {
        return new GetAuthorizedRequest<JSONObject>(Uri.parse("https://httpbin.org/get"), getAuthTokenManager(), getClientCredentials()) {
            @Override
            protected JSONObject manageResponse(InputStream is) throws Exception {
                String responseString = readInputStream(is);
                return new JSONObject(responseString);
            }
        };
    }

}
