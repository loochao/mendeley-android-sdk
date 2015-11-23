package com.mendeley.api.request;


import android.net.Uri;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.mendeley.api.exceptions.MendeleyException;

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
                return new Response(Thread.currentThread(), (Date) null, null);
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


    private class TestRequestCallback<T> implements Request.RequestCallback<T> {
        private final CountDownLatch latch;
        public T resource;

        public TestRequestCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(T resource, Uri next, Date serverDate) {
            this.resource = resource;
            latch.countDown();
        }

        @Override
        public void onFailure(MendeleyException mendeleyException) {
            latch.countDown();
        }
    }
}
