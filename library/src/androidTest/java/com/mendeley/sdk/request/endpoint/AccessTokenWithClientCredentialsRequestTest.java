package com.mendeley.sdk.request.endpoint;

import android.content.res.AssetManager;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.mendeley.sdk.ClientCredentials;
import com.mendeley.sdk.testUtils.ClientCredentialsFromAssetsFactory;
import com.mendeley.sdk.testUtils.InMemoryAuthTokenManager;

import junit.framework.Assert;

import org.json.JSONObject;

import java.util.Map;


public class AccessTokenWithClientCredentialsRequestTest extends AndroidTestCase {

    private ClientCredentials clientCredentials;
    private InMemoryAuthTokenManager authTokenManager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        final AssetManager assetManager =  getContext().getAssets();
        clientCredentials = ClientCredentialsFromAssetsFactory.create(assetManager);

        authTokenManager = new InMemoryAuthTokenManager();
    }

    @SmallTest
    public void test_AccessTokenWithClientCredentialsRequestTest_obtainsAnAccessToken() throws Exception {

        // WHEN running the  request
        final OAuthTokenEndpoint.AccessTokenWithClientCredentialsRequest request = new OAuthTokenEndpoint.AccessTokenWithClientCredentialsRequest(clientCredentials);
        final JSONObject response = request.run().resource;

        // THEN we have an access token that lets us perform non-user-related requests
        OAuthTokenEndpoint.saveTokens(authTokenManager, response);

        final Map<String, String> resource = new DocumentTypesEndpoint.GetDocumentTypesRequest(authTokenManager, clientCredentials).run().resource;
        Assert.assertNotNull("Empty response", resource);
    }

}