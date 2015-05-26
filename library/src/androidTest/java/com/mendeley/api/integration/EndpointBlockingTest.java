package com.mendeley.api.integration;

import android.content.res.AssetManager;
import android.test.AndroidTestCase;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.impl.RequestFactoryImpl;
import com.mendeley.api.impl.RequestsFactory;
import com.mendeley.api.testUtils.ClientCredentialsFromAssetsFactory;
import com.mendeley.api.testUtils.InMemoryAuthTokenManager;
import com.mendeley.api.testUtils.SignInException;
import com.mendeley.api.testUtils.TestAccountSetupUtils;
import com.mendeley.api.testUtils.UsernameAndPasswordSessionManagerFactory;

import java.util.Date;
import java.util.Random;

public abstract class EndpointBlockingTest extends AndroidTestCase {

    // TODO: this should not be an Async but a normal MendeleySdk, but we keep it like this for now as the hierarchy needs to be refactored
    private RequestsFactory requestsFactory;
    private TestAccountSetupUtils testAccountSetupUtils;
    private Random random;

    @Override
    protected void setUp() throws InterruptedException, SignInException {
        random = new Random();

        final AssetManager assetManager =  getContext().getAssets();
        final ClientCredentials clientCredentials = ClientCredentialsFromAssetsFactory.create(assetManager);
        final AuthTokenManager authTokenManager = new InMemoryAuthTokenManager();
        UsernameAndPasswordSessionManagerFactory.create(assetManager, clientCredentials, authTokenManager).signIn();


        requestsFactory = new RequestFactoryImpl(authTokenManager, clientCredentials);

        testAccountSetupUtils = new TestAccountSetupUtils(authTokenManager, requestsFactory);

        // reset account
        testAccountSetupUtils.cleanAll();
    }

    protected final RequestsFactory getSdk() {
        return requestsFactory;
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
        return getSdk().getDocuments().run().serverDate;
    }
}
