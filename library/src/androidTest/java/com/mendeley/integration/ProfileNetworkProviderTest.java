package com.mendeley.integration;

import com.mendeley.api.MendeleySdk;
import com.mendeley.api.callbacks.profile.GetProfileCallback;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.model.Profile;
import com.mendeley.testUtils.SignInException;
import com.mendeley.testUtils.TestUtils;

public class ProfileNetworkProviderTest extends BaseNetworkProviderTest {
    private MendeleySdk sdk;

    private Profile profileRcvd;

    private GetProfileCallback callback;

    @Override
    protected void setUp() throws InterruptedException, SignInException {
        callback = new GetProfileCallback() {
            @Override
            public void onProfileReceived(Profile profile) {
                setProfile(profile);
                reportSuccess();
            }

            @Override
            public void onProfileNotReceived(MendeleyException mendeleyException) {
                fail("my profileRcvd not received");
            }
        };
        sdk = TestUtils.signIn(getContext().getAssets());
    }

    public void testGetMyProfile() throws InterruptedException {
        expectSdkCall();
        sdk.getMyProfile(callback);
        waitForSdkResponse("getting my profileRcvd");

        assertNotNull("profileRcvd must not be null", profileRcvd);
        assertEquals("first name incorrect", "Mobile", profileRcvd.firstName);
        assertEquals("last name incorrect", "Android", profileRcvd.lastName);
        assertEquals("email incorrect", "mobile+androidtest@mendeley.com", profileRcvd.email);
    }

    public void testGetProfile() throws InterruptedException {
        expectSdkCall();
        sdk.getProfile("a0ca90e0-94e9-39e1-9bb6-5ca1a10dacca", callback);
        waitForSdkResponse("getting profileRcvd");

        assertNotNull("profileRcvd must not be null", profileRcvd);
        assertEquals("first name incorrect", "Darth", profileRcvd.firstName);
        assertEquals("last name incorrect", "Vader", profileRcvd.lastName);
    }

    private void setProfile(Profile profile) {
    	profileRcvd = profile;
    }
}
