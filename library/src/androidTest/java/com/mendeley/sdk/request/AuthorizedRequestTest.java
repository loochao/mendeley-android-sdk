package com.mendeley.sdk.request;


import android.net.Uri;
import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.SmallTest;

import com.mendeley.sdk.Request;
import com.mendeley.sdk.exceptions.MendeleyException;
import com.mendeley.sdk.exceptions.UserCancelledException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.mendeley.sdk.util.NetworkUtils.readInputStream;

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

    @SmallTest
    public void test_run_returnsTheServerDate() throws Exception {
        final Date serverDate = getRequest().run().serverDate;
        assertTrue("Request reads server date", Math.abs(new Date().getTime() - serverDate.getTime()) < 10000);
    }

    @LargeTest
    public void test_cancel_interruptsReadingFromTheInputStream() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        // GIVEN a request
        final Request<String> request = new GetAuthorizedRequest<String>(Uri.parse("https://httpbin.org/get"), getAuthTokenManager(), getAppCredentials()) {
            @Override
            protected String manageResponse(InputStream is) throws Exception {
                try {
                    return readInputStream(is);
                } finally {
                    latch.countDown();
                }
            }
        };

        // WHEN running and cancelling it


        String resource = null;
        Exception exception = null;
        try {
            request.cancel();
            resource = request.run().resource;
        } catch (Exception e) {
            exception = e;
        }

        latch.await(3, TimeUnit.SECONDS);

        // THEN we have received a cancelled exception
        assertNull("resource should be null", resource);
        assertNotNull("cancellation exception should be received", exception);
        assertEquals("cancellation exception should be received", exception.getClass(), UserCancelledException.class);
    }

}
