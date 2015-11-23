package com.mendeley.sdk.request;


import android.net.Uri;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.SmallTest;

import com.mendeley.sdk.exceptions.MendeleyException;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class RequestTest extends AndroidTestCase {

    @SmallTest
    public void test_run_executesRequestInTheCallingThread() throws MendeleyException {
        // GIVEN a request that returns the thread it run in

        final Request<Thread> request = new Request<Thread>(null, null, null) {
            @Override
            public Response run() throws MendeleyException {
                return new Response(Thread.currentThread(), (Date) null, null);
            }
        };

        // WHEN it runs
        final String actualThreadName = request.run().resource.getName();

        // THEN it has run in the current thread
        final String expectedThreadName = Thread.currentThread().getName();

        assertEquals("Running thread of the request", expectedThreadName, actualThreadName);
    }


    @SmallTest
    public void test_runAsync_executesRequestInABackgroundThread() throws InterruptedException {
        // GIVEN a request that returns the thread it run in
        final Request<Thread> request = new Request<Thread>(null, null, null) {
            @Override
            public Response run() throws MendeleyException {
                return new Response(Thread.currentThread(), (Date) null, null);
            }
        };

        // WHEN it runs;
        final CountDownLatch latch = new CountDownLatch(1);
        final TestRequestCallback<Thread> callback = new TestRequestCallback<>(latch);
        request.runAsync(callback);
        latch.await(3, TimeUnit.SECONDS);

        // THEN it has not run in the current thread
        final String notExpectedThreadName = Thread.currentThread().getName();
        final String actualThreadName = callback.resource.getName();

        assertNotSame("Running thread of the request", notExpectedThreadName, actualThreadName);
    }

    @SmallTest
    public void test_runAsync_executesRequestInABackgroundThread_whenPassingCustomExecutor() throws InterruptedException {
        // GIVEN a request that returns the thread it run in
        final Request<Thread> request = new Request<Thread>(null, null, null) {
            @Override
            public Response run() throws MendeleyException {
                return new Response(Thread.currentThread(), new Date(), null);
            }
        };

        // and a custom executor

        final String expectedThreadName = "myBackgroundThread";
        final Executor executor = new Executor() {
            @Override
            public void execute(Runnable command) {
                new Thread(command, expectedThreadName).start();
            }
        };

        // WHEN it runs
        final CountDownLatch latch = new CountDownLatch(1);

        final TestRequestCallback<Thread> callback = new TestRequestCallback<>(latch);
        request.runAsync(callback, executor);

        latch.await(3, TimeUnit.SECONDS);

        // THEN it has run in the thread of the executor
        final String actualThreadName = callback.resource.getName();

        assertEquals("Running thread of the request", expectedThreadName, actualThreadName);
    }


    @LargeTest
    public void test_run_invokesOnSuccessOverTheCallback_whenRunIsSuccesfull() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        // GIVEN a request that runs until cancelled
        final Request<Void> request = new Request<Void>(null, null, null) {
            @Override
            public Response run() throws MendeleyException {
                return new Response(null, new Date(), null);
            }
        };

        // WHEN the asynctask is cancelled
        final TestRequestCallback<Void> callback = new TestRequestCallback<>(latch);
        request.runAsync(callback);

        latch.await(10, TimeUnit.SECONDS);

        // THEN the right callback was invoked
        assertTrue("OnSuccess callback invoked", callback.onSuccessExecuted);
    }

    @SmallTest
    public void test_run_invokesOnSuccessOverTheCallback_whenRunFails() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        // GIVEN a request that runs until cancelled
        final Request<Void> request = new Request<Void>(null, null, null) {
            @Override
            public Response run() throws MendeleyException {
                throw new MendeleyException("crap");
            }
        };

        // WHEN the asynctask is cancelled
        final TestRequestCallback<Void> callback = new TestRequestCallback<>(latch);
        request.runAsync(callback);

        latch.await(10, TimeUnit.SECONDS);

        // THEN the right callback was invoked
        assertTrue("OnSuccess callback invoked", callback.onFailureExecuted);
    }

    @SmallTest
    public void test_cancel_invokesOnCanceledOverTheCallback() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        // GIVEN a request that runs until cancelled
        final Request<Void> request = new Request<Void>(null, null, null) {
            @Override
            public Response run() throws MendeleyException {
                try {
                    while (!isCancelled()) {
                        Thread.sleep(1000);
                    }
                } catch (Exception ignored) {
                }
                return null;
            }
        };

        // WHEN the asynctask is cancelled
        final TestRequestCallback<Void> callback = new TestRequestCallback<>(latch);
        request.runAsync(callback);
        request.cancel();

        latch.await(10, TimeUnit.SECONDS);

        // THEN the right callback was invoked
        assertTrue("Cancelled callback invoked", callback.onCancelledExecuted);
    }

    private class TestRequestCallback<T> implements Request.RequestCallback<T> {
        private final CountDownLatch latch;
        public T resource;

        private boolean onSuccessExecuted;
        private boolean onFailureExecuted;
        private boolean onCancelledExecuted;

        public TestRequestCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(T resource, Uri next, Date serverDate) {
            this.resource = resource;
            onSuccessExecuted = true;
            latch.countDown();
        }

        @Override
        public void onFailure(MendeleyException mendeleyException) {
            onFailureExecuted = true;
            latch.countDown();
        }

        @Override
        public void onCancelled() {
            onCancelledExecuted = true;
            latch.countDown();
        }

    }
}
