package com.mendeley.sdk.request;

import android.content.res.AssetManager;
import android.test.AndroidTestCase;

import com.mendeley.sdk.AuthTokenManager;
import com.mendeley.sdk.ClientCredentials;
import com.mendeley.sdk.testUtils.InMemoryAuthTokenManager;
import com.mendeley.sdk.Mendeley;
import com.mendeley.sdk.RequestsFactory;
import com.mendeley.sdk.request.endpoint.DocumentEndpoint;
import com.mendeley.sdk.request.endpoint.OAuthTokenEndpoint;
import com.mendeley.sdk.testUtils.ClientCredentialsFromAssetsFactory;
import com.mendeley.sdk.testUtils.TestAccountSetupUtils;
import com.mendeley.sdk.testUtils.EmailAndPasswordFromAssetsFactory;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

public abstract class SignedInTest extends AndroidTestCase {

    private RequestsFactory requestsFactory;
    private ClientCredentials clientCredentials;
    private AuthTokenManager authTokenManager;

    private TestAccountSetupUtils testAccountSetupUtils;
    private Random random;

    @Override
    protected void setUp() throws Exception {
        random = new Random();

        final AssetManager assetManager =  getContext().getAssets();
        clientCredentials = ClientCredentialsFromAssetsFactory.create(assetManager);
        authTokenManager = new InMemoryAuthTokenManager();

        // sign in
        final EmailAndPasswordFromAssetsFactory.UsernameAndPassword usernameAndPassword = EmailAndPasswordFromAssetsFactory.create(assetManager);
        new OAuthTokenEndpoint.AccessTokenWithPasswordRequest(authTokenManager, clientCredentials, usernameAndPassword.username, usernameAndPassword.password).run();

        requestsFactory = new Mendeley.RequestFactoryImpl(authTokenManager, clientCredentials);
        testAccountSetupUtils = new TestAccountSetupUtils(authTokenManager, clientCredentials, requestsFactory);

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

    public String generateRandomString(int length) {
        int asciiFirst = 33;
        int asciiLast = 126;
        Integer[] exceptions = {34, 39, 96};

        List<Integer> exceptionsList = Arrays.asList(exceptions);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int charIndex;
            do {
                charIndex = random.nextInt(asciiLast - asciiFirst + 1) + asciiFirst;
            }
            while (exceptionsList.contains(charIndex));

            builder.append((char) charIndex);
        }

        return builder.toString();
    }

    protected Date getServerDate() throws Exception {
        // wait a bit, so we get a "fresh" date
        Thread.sleep(1000);
        return getRequestFactory().newGetDocumentsRequest((DocumentEndpoint.DocumentRequestParameters) null).run().serverDate;
    }
}
