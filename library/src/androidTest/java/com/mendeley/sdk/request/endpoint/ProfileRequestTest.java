package com.mendeley.sdk.request.endpoint;

import com.mendeley.sdk.model.Profile;
import com.mendeley.sdk.request.SignedInTest;
import com.mendeley.sdk.testUtils.AssertUtils;

public class ProfileRequestTest extends SignedInTest {

    /**
     * As we don't have an API to setup profiles for the test,
     * the tests rely on profiles we added manually
     * through the web interface https://www.mendeley.com
     */

    public void test_getProfile_receivesCorrectProfile() throws Exception {
        // GIVEN a profile on the server
        Profile expected = createTestProfile();

        // WHEN getting the profile
        final Profile actual = getRequestFactory().newGetProfileRequest(expected.id).run().resource;

        // THEN we have the expected profile
        AssertUtils.assertProfile(expected, actual);
    }

    public void test_getMeProfile_receivesCorrectMeProfile() throws Exception {
        // GIVEN the user profile on the server
        Profile expected = createTestProfile();

        // WHEN getting the profile
        final Profile actual = getRequestFactory().newGetMyProfileRequest().run().resource;

        // THEN we have the expected profile
        AssertUtils.assertProfile(expected, actual);
    }

    /**
     * Cannot run this test as delete /profiles is not available on staging at the moment
     */
//    public void test_postProfile_createsProfileInServer() throws Exception {
//        if (BuildConfig.FLAVOR.equals("staging")) {
//            // GIVEN a profile
//            final Profile postingProfile = new Profile.Builder().
//                    setFirstName("Alfred").
//                    setLastName("Schnittke").
//                    setEmail("alfred_schnitke@androidtest.com").
//                    setDiscipline(new Discipline("Arts and Humanities")).
//                    setAcademicStatus("Other").
//                    setMarketing(false).
//                    build();
//
//            // WHEN posting it
//            final Profile returnedProfile = getRequestFactory().newPostProfileRequest(postingProfile, "userPassword").run().resource;
//
//            // THEN we receive the same profile back, with id filled
//            AssertUtils.assertNewProfile(postingProfile, returnedProfile);
//            assertNotNull(returnedProfile.id);
//
//            // AND the profile exists in the server
//            AssertUtils.assertNewProfile(postingProfile, getRequestFactory().newGetProfileRequest(returnedProfile.id).run().resource);
//
//            // delete profile from server
//            getRequestFactory().newDeleteProfileRequest(returnedProfile.id).run();
//        }
//    }

    private Profile createTestProfile() {
        Profile profile = new Profile.Builder()
                .setId("f38dc0c8-df12-32a0-ae70-28ab4f3409cd")
                .setFirstName("Mobile")
                .setLastName("Android")
                .build();

        return profile;
    }
}
