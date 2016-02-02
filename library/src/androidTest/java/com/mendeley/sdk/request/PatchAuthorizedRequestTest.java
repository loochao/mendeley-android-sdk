package com.mendeley.sdk.request;

import android.net.Uri;
import android.test.suitebuilder.annotation.SmallTest;

import com.mendeley.sdk.util.DateUtils;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;

import static com.mendeley.sdk.util.NetworkUtils.readInputStream;

public class PatchAuthorizedRequestTest extends AuthorizedRequestTest {

    private String patchedBody;
    private final Date currentDate = new Date(System.currentTimeMillis());

    @Override
    protected void setUp() throws InterruptedException {
        super.setUp();
        patchedBody = "the patched body";
    }

    @Override
    protected AuthorizedRequest<JSONObject> createRequest() {
        return new PatchAuthorizedRequest<JSONObject>(Uri.parse("https://httpbin.org/patch"), currentDate, getAuthTokenManager(), getAppCredentials()) {
            @Override
            protected void writePatchBody(OutputStream os) throws Exception {
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(patchedBody);
                writer.flush();
            }

            @Override
            protected JSONObject manageResponse(InputStream is) throws Exception {
                String responseString = readInputStream(is);
                return new JSONObject(responseString);
            }
        };
    }


    @SmallTest
    public void test_run_writesThePatchPayload() throws Exception {

        final JSONObject httpBinResponse = getRequest().run().resource;
        final String actual = httpBinResponse.getString("form");

        assertTrue("Patched and returned content", actual.contains(patchedBody));
    }

    @SmallTest
    public void test_run_sendsTheUnmodifiedIfHeader() throws Exception {
        final JSONObject httpBinResponse = getRequest().run().resource;
        final JSONObject headersJson = httpBinResponse.getJSONObject("headers");

        final String actual = headersJson.getString("If-Unmodified-Since");
        assertEquals("If-Unmodified-Since header sent", actual, DateUtils.formatMendeleyApiTimestamp(currentDate));

    }
}
