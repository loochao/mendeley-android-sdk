package com.mendeley.integration;

import android.test.AndroidTestCase;

import com.mendeley.api.impl.AsyncMendeleySdk;
import com.mendeley.testUtils.AssertUtils;
import com.mendeley.testUtils.SignInException;
import com.mendeley.testUtils.TestAccountSetupUtils;
import com.mendeley.testUtils.TestUtils;

import java.util.Random;

public abstract class BlockingNetworkProviderTest extends AndroidTestCase {

    // TODO: this should not be an Async but a normal MendeleySdk, but we keep it like this for now as the hierarchy needs to be refactored
    private AsyncMendeleySdk sdk;
    private TestAccountSetupUtils testAccountSetupUtils;
    private AssertUtils assertUtils;
    private Random random;

    @Override
    protected void setUp() throws InterruptedException, SignInException {
        random = new Random();

        sdk = TestUtils.signIn(getContext().getAssets());
        testAccountSetupUtils = new TestAccountSetupUtils(sdk);
        assertUtils = new AssertUtils();

        // reset account
        testAccountSetupUtils.cleanAll();
    }

    protected final AsyncMendeleySdk getSdk() {
        return sdk;
    }

    protected final TestAccountSetupUtils getTestAccountSetupUtils() {
        return testAccountSetupUtils;
    }

    protected final AssertUtils getAssertUtils() {
        return assertUtils;
    }

    protected final Random getRandom() {
        return random;
    }

}
