package com.mendeley.sdk.request;

import android.net.Uri;
import android.test.suitebuilder.annotation.LargeTest;

import com.mendeley.sdk.exceptions.UserCancelledException;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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

    @LargeTest
    public void test_cancel_interruptsReadingFromTheInputStream() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        // GIVEN a request
        final Request<JSONObject> request = new GetAuthorizedRequest<JSONObject>(Uri.parse("https://httpbin.org/get"), getAuthTokenManager(), getClientCredentials()) {
            @Override
            protected JSONObject manageResponse(InputStream is) throws Exception {
                try {
                    String responseString = readInputStream(is);
                    return new JSONObject(responseString);
                } finally {
                    latch.countDown();
                }
            }
        };

        // WHEN running and cancelling it


        JSONObject resource = null;
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
