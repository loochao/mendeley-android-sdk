package com.mendeley.api.request;

import org.json.JSONObject;

import java.io.InputStream;

import static com.mendeley.api.request.NetworkUtils.readInputStream;

public class DeleteAuthorizedRequestTest extends AuthorizedRequestTest {

    @Override
    protected AuthorizedRequest<JSONObject> createRequest() {
        return new DeleteAuthorizedRequest<JSONObject>("https://httpbin.org/delete", getAuthTokenManager(), getClientCredentials()) {
            @Override
            protected JSONObject manageResponse(InputStream is) throws Exception {
                String responseString = readInputStream(is);
                return new JSONObject(responseString);
            }
        };
    }


}
