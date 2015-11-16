package com.mendeley.api.request;


import android.test.suitebuilder.annotation.SmallTest;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.model.Profile;
import com.mendeley.api.testUtils.AssertUtils;

public class AuthTokenRefreshRequestTest extends RequestTest {

    @SmallTest
    public void test_AuthTokenRefreshRequest_updatesTheAccessToken() throws Exception {

        final Profile expected = getSdk().getMyProfile().run().resource;

        // GIVEN an invalid access token
        final String invalidAccessToken = "invalid";
        final AuthTokenManager authTokenManager = getAuthTokenManager();

        authTokenManager.saveTokens(invalidAccessToken, authTokenManager.getRefreshToken(), authTokenManager.getTokenType(), 1000);

        // WHEN running the refresh token request
        final AuthTokenRefreshRequest refreshRequest = new AuthTokenRefreshRequest(getAuthTokenManager(), getClientCredentials());
        refreshRequest.run();


        // THEN we have new access token
        assertNotSame("Access token updated", invalidAccessToken, authTokenManager.getAccessToken());

        // ...that indeed allows as to perform valid request
        final Profile actual = getSdk().getMyProfile().run().resource;
        AssertUtils.assertProfile(expected, actual);
    }

}
