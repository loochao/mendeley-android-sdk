package com.mendeley.sdk.request;

import android.content.res.AssetManager;
import android.test.AndroidTestCase;

import com.mendeley.sdk.AuthTokenManager;
import com.mendeley.sdk.ClientCredentials;
import com.mendeley.sdk.Mendeley;
import com.mendeley.sdk.RequestsFactory;
import com.mendeley.sdk.testUtils.ClientCredentialsFromAssetsFactory;
import com.mendeley.sdk.testUtils.InMemoryAuthTokenManager;
import com.mendeley.sdk.testUtils.TestAccountSetupUtils;
import com.mendeley.sdk.testUtils.UsernameAndPasswordSessionManagerFactory;

import java.util.Date;
import java.util.Random;

public abstract class SignedInTest extends AndroidTestCase {

    private RequestsFactory requestsFactory;
    private ClientCredentials clientCredentials;
    private AuthTokenManager authTokenManager;

    private TestAccountSetupUtils testAccountSetupUtils;
    private Random random;

    @Override
    protected void setUp() throws InterruptedException {
        random = new Random();

        final AssetManager assetManager =  getContext().getAssets();
        clientCredentials = ClientCredentialsFromAssetsFactory.create(assetManager);
        authTokenManager = new InMemoryAuthTokenManager();
        UsernameAndPasswordSessionManagerFactory.create(assetManager, clientCredentials, authTokenManager).signIn();

        requestsFactory = new Mendeley.RequestFactoryImpl(authTokenManager, clientCredentials);
        testAccountSetupUtils = new TestAccountSetupUtils(authTokenManager, requestsFactory);

        // reset account
        testAccountSetupUtils.cleanAll();
    }


    protected final RequestsFactory getRequestFactory() {
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
        return getRequestFactory().newGetDocumentsRequest().run().serverDate;
    }
}
