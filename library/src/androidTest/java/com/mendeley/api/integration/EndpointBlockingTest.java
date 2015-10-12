package com.mendeley.api.integration;

import android.test.AndroidTestCase;

import com.mendeley.api.impl.AsyncMendeleySdk;
import com.mendeley.api.testUtils.SignInException;
import com.mendeley.api.testUtils.TestAccountSetupUtils;
import com.mendeley.api.testUtils.TestUtils;

import java.util.Date;
import java.util.Random;

public abstract class EndpointBlockingTest extends AndroidTestCase {

    // TODO: this should not be an Async but a normal MendeleySdk, but we keep it like this for now as the hierarchy needs to be refactored
    private AsyncMendeleySdk sdk;
    private TestAccountSetupUtils testAccountSetupUtils;
    private Random random;

    @Override
    protected void setUp() throws InterruptedException, SignInException {
        random = new Random();

        sdk = TestUtils.signIn(getContext().getAssets());
        testAccountSetupUtils = new TestAccountSetupUtils(sdk);

        // reset account
        testAccountSetupUtils.cleanAll();
    }

    protected final AsyncMendeleySdk getSdk() {
        return sdk;
    }

    protected final TestAccountSetupUtils getTestAccountSetupUtils() {
        return testAccountSetupUtils;
    }

    protected final Random getRandom() {
        return random;
    }

    protected Date getServerDate() throws Exception {
        // wait a bit, so we get a "fresh" date
        Thread.sleep(1000);
        return getSdk().getDocuments().serverDate;
    }
}
