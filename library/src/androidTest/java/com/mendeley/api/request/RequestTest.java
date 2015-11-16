package com.mendeley.api.request;

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

public abstract class RequestTest extends AndroidTestCase {

    private RequestsFactory requestsFactory;
    private ClientCredentials clientCredentials;
    private AuthTokenManager authTokenManager;

    private TestAccountSetupUtils testAccountSetupUtils;
    private Random random;

    @Override
    protected void setUp() throws InterruptedException, SignInException {
        random = new Random();

        final AssetManager assetManager =  getContext().getAssets();
        clientCredentials = ClientCredentialsFromAssetsFactory.create(assetManager);
        authTokenManager = new InMemoryAuthTokenManager();
        UsernameAndPasswordSessionManagerFactory.create(assetManager, clientCredentials, authTokenManager).signIn();

        requestsFactory = new RequestFactoryImpl(authTokenManager, clientCredentials);
        testAccountSetupUtils = new TestAccountSetupUtils(authTokenManager, requestsFactory);

        // reset account
        testAccountSetupUtils.cleanAll();
    }


    protected final RequestsFactory getSdk() {
        return requestsFactory;
    }

    protected final AuthTokenManager getAuthTokenManager() {
        return authTokenManager;
    }

    protected final ClientCredentials getClientCredentials() {
        return clientCredentials;
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
