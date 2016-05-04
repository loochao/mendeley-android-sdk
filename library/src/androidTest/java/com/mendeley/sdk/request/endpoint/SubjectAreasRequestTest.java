package com.mendeley.sdk.request.endpoint;


import com.mendeley.sdk.request.SignedInTest;

import junit.framework.Assert;

import java.util.List;

public class SubjectAreasRequestTest extends SignedInTest {

    public void test_getSubjectAreasWithRequestFactory_receivesSubjectAreas() throws Exception {
        // GIVEN a request factory
        // When getting subject areas
        final List<String> subjectAreas = getRequestFactory().newGetSubjectAreasRequest().run().resource;

        // THEN we have the expected list
        Assert.assertFalse("Subject areas list is empty", subjectAreas.isEmpty());
        Assert.assertNotNull("Subject area is null", subjectAreas.get(0));
    }
}
