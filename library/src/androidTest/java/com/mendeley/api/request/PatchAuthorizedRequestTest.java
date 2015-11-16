package com.mendeley.api.request;

import android.test.suitebuilder.annotation.SmallTest;

import com.mendeley.api.testUtils.SignInException;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import java.io.InputStream;

import static com.mendeley.api.request.NetworkUtils.readInputStream;

public class PatchAuthorizedRequestTest extends AuthorizedRequestTest {

    private String patchedBody;

    @Override
    protected void setUp() throws InterruptedException, SignInException {
        super.setUp();
        patchedBody = "the patched body";
    }

    @Override
    protected AuthorizedRequest<JSONObject> createRequest() {
        return new PatchAuthorizedRequest<JSONObject>("https://httpbin.org/patch", null, getAuthTokenManager(), getClientCredentials()) {
            @Override
            protected HttpEntity createPatchingEntity() throws Exception {
                return new StringEntity(patchedBody);
            }

            @Override
            protected JSONObject manageResponse(InputStream is) throws Exception {
                String responseString = readInputStream(is);
                return new JSONObject(responseString);
            }
        };
    }



    @SmallTest
    public void test_patchResponse() throws Exception {

        final JSONObject httpBinResponse = getRequest().run().resource;
        final String actual = httpBinResponse.getString("data");

        assertTrue("Posted and returned content", actual.contains(patchedBody));
    }
}
