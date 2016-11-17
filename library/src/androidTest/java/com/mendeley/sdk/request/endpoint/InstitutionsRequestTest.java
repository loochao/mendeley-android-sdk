package com.mendeley.sdk.request.endpoint;


import android.test.suitebuilder.annotation.LargeTest;

import com.mendeley.sdk.model.Institution;
import com.mendeley.sdk.request.SignedInTest;

import junit.framework.Assert;

import java.util.List;

public class InstitutionsRequestTest extends SignedInTest {

    @LargeTest
    public void test_getInstitutionsUsingHint_returnsCorrectMetadata() throws Exception {

        // GIVEN one institution hint
        final String hint = "complu";

        // WHEN getting institutions for that hint
        final InstitutionsEndpoint.GetInstitutionsRequest.Parameters params = new InstitutionsEndpoint.GetInstitutionsRequest.Parameters.Builder()
                .setHint(hint)
                .build();

        final InstitutionsEndpoint.GetInstitutionsRequest getInstitutionsRequest = new InstitutionsEndpoint.GetInstitutionsRequest(params, getAuthTokenManager(), getClientCredentials());
        final List<Institution> actual = getInstitutionsRequest.run().resource;

        // THEN all the institutions have the hint in their name
        for (Institution institution : actual) {
            Assert.assertTrue("Hint contained in the name", institution.name.toLowerCase().contains(hint.toLowerCase()));
        }
    }

    @LargeTest
    public void test_getInstitutionsUsingLimit_returnsCorrectMetadata() throws Exception {

        // GIVEN one limit
        final int limit = 3;

        // WHEN getting institutions for that hint
        final InstitutionsEndpoint.GetInstitutionsRequest.Parameters params = new InstitutionsEndpoint.GetInstitutionsRequest.Parameters.Builder()
                .setHint("university")
                .setLimit(limit)
                .build();

        final InstitutionsEndpoint.GetInstitutionsRequest getInstitutionsRequest = new InstitutionsEndpoint.GetInstitutionsRequest(params, getAuthTokenManager(), getClientCredentials());
        final int actual = getInstitutionsRequest.run().resource.size();

        // THEN we get one $limit results
        Assert.assertEquals("Response is limited", limit, actual);
    }

}
