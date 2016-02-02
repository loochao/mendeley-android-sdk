package com.mendeley.sdk.request;


import android.net.Uri;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.SmallTest;

import com.mendeley.sdk.Request;
import com.mendeley.sdk.exceptions.MendeleyException;
import com.mendeley.sdk.testUtils.MutableReference;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class RequestTest extends AndroidTestCase {

    @SmallTest
    public void test_run_executesRequestInTheCallingThread() throws MendeleyException {
        // GIVEN a request that returns the thread it run in

        final Request<Thread> request = new Request<Thread>(null) {
            @Override
            public Response doRun() throws MendeleyException {
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
        final MutableReference<Thread> actualThread = new MutableReference<>();

        // GIVEN a request that returns the thread it run in
        final Request<Thread> request = new Request<Thread>(null) {
            @Override
            public Response doRun() throws MendeleyException {
                return new Response(Thread.currentThread(), (Date) null, null);
            }
        };

        // WHEN it runs;
        final CountDownLatch latch = new CountDownLatch(1);

        request.runAsync(new Request.RequestCallback<Thread>() {
            @Override
            public void onSuccess(Thread resource, Uri next, Date serverDate) {
                actualThread.value = resource;
                latch.countDown();
            }

            @Override
            public void onFailure(MendeleyException mendeleyException) {
            }

            @Override
            public void onCancelled() {
            }
        });
        latch.await(3, TimeUnit.SECONDS);

        // THEN it has not run in the current thread
        final String notExpectedThreadName = Thread.currentThread().getName();
        final String actualThreadName = actualThread.value.getName();

        assertNotSame("Running thread of the request", notExpectedThreadName, actualThreadName);
    }

    @SmallTest
    public void test_runAsync_executesRequestInABackgroundThread_whenPassingCustomExecutor() throws InterruptedException {
        final MutableReference<Thread> actualThread = new MutableReference<>();

        // GIVEN a request that returns the thread it run in
        final Request<Thread> request = new Request<Thread>(null) {
            @Override
            public Response doRun() throws MendeleyException {
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

        request.runAsync(new Request.RequestCallback<Thread>() {
            @Override
            public void onSuccess(Thread resource, Uri next, Date serverDate) {
                actualThread.value = resource;
                latch.countDown();
            }

            @Override
            public void onFailure(MendeleyException mendeleyException) {
            }

            @Override
            public void onCancelled() {
            }
        }, executor);

        latch.await(3, TimeUnit.SECONDS);

        // THEN it has run in the thread of the executor
        final String actualThreadName = actualThread.value.getName();

        assertEquals("Running thread of the request", expectedThreadName, actualThreadName);
    }


    @LargeTest
    public void test_run_invokesOnSuccessOverTheCallback_whenRunIsSuccesfull() throws InterruptedException {
        final MutableReference<Boolean> callbackCalled = new MutableReference<Boolean>();
        final CountDownLatch latch = new CountDownLatch(1);

        // GIVEN a request that runs until cancelled
        final Request<Void> request = new Request<Void>(null) {
            @Override
            public Response doRun() throws MendeleyException {
                return new Response(null, new Date(), null);
            }
        };

        // WHEN the asynctask runs and finishes with exception
        request.runAsync(new Request.RequestCallback<Void>() {
            @Override
            public void onSuccess(Void resource, Uri next, Date serverDate) {
                callbackCalled.value = true;
                latch.countDown();
            }

            @Override
            public void onFailure(MendeleyException mendeleyException) {

            }

            @Override
            public void onCancelled() {

            }
        });

        latch.await(10, TimeUnit.SECONDS);

        // THEN the right callback was invoked
        assertTrue("OnSuccess callback invoked", callbackCalled.value);
    }

    @SmallTest
    public void test_run_invokesOnSuccessOverTheCallback_whenRunFails() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final MutableReference<Exception> exceptionReceived = new MutableReference<Exception>();

        // GIVEN a request that runs until cancelled
        final Request<Void> request = new Request<Void>(null) {
            @Override
            public Response doRun() throws MendeleyException {
                throw new MendeleyException("crap");
            }
        };

        // WHEN the asynctask runs and finishes with success
        request.runAsync(new Request.RequestCallback<Void>() {
            @Override
            public void onSuccess(Void resource, Uri next, Date serverDate) {
            }

            @Override
            public void onFailure(MendeleyException mendeleyException) {
                exceptionReceived.value = mendeleyException;
                latch.countDown();
            }

            @Override
            public void onCancelled() {

            }
        });
        latch.await(10, TimeUnit.SECONDS);

        // THEN the right callback was invoked
        assertNotNull("OnFailure callback invoked", exceptionReceived.value);
;
    }


    @SmallTest
    public void test_cancel_invokesOnCanceledOverTheCallback() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final MutableReference<Boolean> callbackCalled = new MutableReference<Boolean>();

        // GIVEN a request that runs until cancelled
        final Request<Void> request = new Request<Void>(null) {
            @Override
            public Response doRun() throws MendeleyException {
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
        request.runAsync(new Request.RequestCallback<Void>() {
            @Override
            public void onSuccess(Void resource, Uri next, Date serverDate) {
            }

            @Override
            public void onFailure(MendeleyException mendeleyException) {
            }

            @Override
            public void onCancelled() {
                callbackCalled.value = true;
                latch.countDown();
            }
        });
        request.cancel();

        latch.await(10, TimeUnit.SECONDS);

        // THEN the right callback was invoked
        assertTrue("Cancelled callback invoked", callbackCalled.value);
    }

}
