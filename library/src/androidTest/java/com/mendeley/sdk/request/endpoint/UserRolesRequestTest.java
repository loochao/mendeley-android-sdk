package com.mendeley.sdk.request.endpoint;


import com.mendeley.sdk.request.SignedInTest;

import junit.framework.Assert;

import java.util.List;

public class UserRolesRequestTest extends SignedInTest {

    public void test_getUserRolesWithRequestFactory_receivesUserRoles() throws Exception {
        // GIVEN a request factory
        // When getting user roles
        final List<String> userRoles = getRequestFactory().newGetUserRolesRequest().run().resource;

        // THEN we have the expected list
        Assert.assertFalse("User roles list is empty", userRoles.isEmpty());
        Assert.assertNotNull("User role is null", userRoles.get(0));
    }
}
