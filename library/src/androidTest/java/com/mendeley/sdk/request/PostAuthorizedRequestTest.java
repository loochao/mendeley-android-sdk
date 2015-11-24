package com.mendeley.sdk.request;

import android.net.Uri;
import android.test.suitebuilder.annotation.LargeTest;

import com.mendeley.sdk.exceptions.UserCancelledException;
import com.mendeley.sdk.testUtils.MutableReference;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.mendeley.sdk.request.NetworkUtils.readInputStream;

public class PostAuthorizedRequestTest extends AuthorizedRequestTest {

    private String postedBody;

    @Override
    protected void setUp() throws InterruptedException {
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
    public void test_run_writesThePostPayload() throws Exception {

        final JSONObject httpBinResponse = getRequest().run().resource;
        final String actual = httpBinResponse.getString("data");

        assertTrue("Posted and returned content", actual.contains(postedBody));
    }

    @LargeTest
    public void test_cancel_interruptsWritingToTheOuputStream() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        final MutableReference<Integer> writes = new MutableReference();
        final int maxWrites = 100;

        // GIVEN a request
        final Request<Void> request = new PostAuthorizedRequest<Void>(Uri.parse("https://httpbin.org/post"), getAuthTokenManager(), getClientCredentials()) {

            @Override
            protected void writePostBody(OutputStream os) throws Exception {
                byte[] buffer = new byte[256];

                for (writes.value = 0; writes.value < maxWrites; writes.value++) {
                    os.write(buffer, 0 , buffer.length);
                    Thread.sleep(50);
                }
            }

            @Override
            protected Void manageResponse(InputStream is) throws Exception {
                try {
                    return null;
                } finally {
                    latch.countDown();
                }
            }
        };

        // WHEN running and cancelling it

        Exception exception = null;
        try {
            request.cancel();
            request.run();
        } catch (Exception e) {
            exception = e;
        }

        latch.await(3, TimeUnit.SECONDS);

        // THEN we have received a cancelled exception
        assertTrue("writing to the output stream should have been interrupted ", writes.value < maxWrites);
        assertNotNull("cancellation exception should be received", exception);
        assertEquals("cancellation exception should be received", exception.getClass(), UserCancelledException.class);
    }
}
