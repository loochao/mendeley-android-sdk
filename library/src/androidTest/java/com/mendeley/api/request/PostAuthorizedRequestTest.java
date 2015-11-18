package com.mendeley.api.request;

import android.net.Uri;
import android.test.suitebuilder.annotation.LargeTest;

import com.mendeley.api.testUtils.SignInException;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

import static com.mendeley.api.request.NetworkUtils.readInputStream;

public class PostAuthorizedRequestTest extends AuthorizedRequestTest {

    private String postedBody;

    @Override
    protected void setUp() throws InterruptedException, SignInException {
        super.setUp();
        postedBody = "the posted body";
    }

    @Override
    protected AuthorizedRequest<JSONObject> createRequest() {
        return new PostAuthorizedRequest<JSONObject>(Uri.parse("https://httpbin.org/post"), getAuthTokenManager(), getClientCredentials()) {
            @Override
            protected void writePostBody(OutputStream os) throws Exception {
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(postedBody);
                writer.flush();
            }

            @Override
            protected JSONObject manageResponse(InputStream is) throws Exception {
                String responseString = readInputStream(is);
                return new JSONObject(responseString);
            }

            @Override
            protected void appendHeaders(Map<String, String> headers) {
                super.appendHeaders(headers);
                headers.put("Content-Type", "text/plain");
            }
        };
    }


    @LargeTest
    public void test_postResponse() throws Exception {

        final JSONObject httpBinResponse = getRequest().run().resource;
        final String actual = httpBinResponse.getString("data");

        assertTrue("Posted and returned content", actual.contains(postedBody));
    }
}
