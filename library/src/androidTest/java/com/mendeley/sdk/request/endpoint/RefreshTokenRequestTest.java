package com.mendeley.sdk.request.endpoint;


import android.test.suitebuilder.annotation.SmallTest;

import com.mendeley.sdk.AuthTokenManager;
import com.mendeley.sdk.model.Profile;
import com.mendeley.sdk.request.SignedInTest;
import com.mendeley.sdk.testUtils.AssertUtils;

public class RefreshTokenRequestTest extends SignedInTest {

    @SmallTest
    public void test_run_obtainsAnUpdatedTheAccessToken() throws Exception {

        final Profile expected = getRequestFactory().newGetMyProfileRequest().run().resource;

        // GIVEN an invalid access token
        final String invalidAccessToken = "invalid";
        final AuthTokenManager authTokenManager = getAuthTokenManager();

        authTokenManager.saveTokens(invalidAccessToken, authTokenManager.getRefreshToken(), authTokenManager.getTokenType(), 1000);

        // and the request
        final OAuthTokenEndpoint.RefreshTokenRequest refreshRequest = new OAuthTokenEndpoint.RefreshTokenRequest(authTokenManager, getClientCredentials());

        // WHEN running the request
        refreshRequest.run();

        // THEN we have new access token
        assertNotSame("Access token updated", invalidAccessToken, authTokenManager.getAccessToken());

        // ...that indeed allows as to perform valid request
        final Profile actual = getRequestFactory().newGetMyProfileRequest().run().resource;
        AssertUtils.assertProfile(expected, actual);
    }

}
