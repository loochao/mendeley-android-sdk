package com.mendeley.sdk.request;


import android.test.suitebuilder.annotation.LargeTest;

import com.mendeley.sdk.exceptions.MendeleyException;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class AuthorizedRequestTest extends SignedInTest {

    private AuthorizedRequest<JSONObject> request;

    @Override
    protected void setUp() throws InterruptedException {
        super.setUp();
        request = createRequest();
    }

    protected abstract AuthorizedRequest<JSONObject> createRequest();

    protected final AuthorizedRequest<JSONObject> getRequest() {
        return request;
    }

    @LargeTest
    public void test_sendsTheAuthTokenAsAHeader() throws MendeleyException, JSONException {

        final JSONObject httpBinResponse = getRequest().run().resource;
        final JSONObject headersJson = httpBinResponse.getJSONObject("headers");

        final String actual = headersJson.getString("Authorization");

        assertEquals("Authorization token sent", actual, "Bearer " + getAuthTokenManager().getAccessToken());
    }


}
