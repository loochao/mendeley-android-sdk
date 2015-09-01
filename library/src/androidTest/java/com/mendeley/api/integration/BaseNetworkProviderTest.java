package com.mendeley.api.integration;

import android.test.AndroidTestCase;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class BaseNetworkProviderTest extends AndroidTestCase {
    private static final int API_CALL_TIMEOUT_MS = 10 * 1000;

    private CountDownLatch apiCallLatch;

    protected void expectSdkCall() {
        apiCallLatch = new CountDownLatch(1);
    }

    protected void waitForSdkResponse(String action) {
        waitForSdkResponse(action, API_CALL_TIMEOUT_MS);
    }

    protected void waitForSdkResponse(String action, int timeoutMs) {
        try {
            if (!apiCallLatch.await(API_CALL_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                fail("timeout " + action);
            }
        } catch (InterruptedException e) {
            fail("interrupted " + action);
        }
    }

    protected void reportSuccess() {
        apiCallLatch.countDown();
    }
}
