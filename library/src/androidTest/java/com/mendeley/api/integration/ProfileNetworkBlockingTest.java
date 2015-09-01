package com.mendeley.api.integration;

import android.test.AndroidTestCase;

import com.mendeley.api.BlockingSdk;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.model.Profile;
import com.mendeley.api.testUtils.SignInException;
import com.mendeley.api.testUtils.TestUtils;

public class ProfileNetworkBlockingTest extends AndroidTestCase {
    private BlockingSdk sdk;

    @Override
    protected void setUp() throws InterruptedException, SignInException {
        sdk = TestUtils.signIn(getContext().getAssets());
    }

    public void testGetMyProfile() throws InterruptedException, MendeleyException {
        Profile profileRcvd = sdk.getMyProfile();

        assertNotNull("profileRcvd must not be null", profileRcvd);
        assertEquals("first name incorrect", "Mobile", profileRcvd.firstName);
        assertEquals("last name incorrect", "Android", profileRcvd.lastName);
        assertEquals("email incorrect", "mobile+androidtest@mendeley.com", profileRcvd.email);
    }

    public void testGetProfile() throws InterruptedException, MendeleyException {
        Profile profileRcvd = sdk.getProfile("a0ca90e0-94e9-39e1-9bb6-5ca1a10dacca");

        assertNotNull("profileRcvd must not be null", profileRcvd);
        assertEquals("first name incorrect", "Darth", profileRcvd.firstName);
        assertEquals("last name incorrect", "Vader", profileRcvd.lastName);
    }
}
