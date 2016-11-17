package com.mendeley.sdk.request.endpoint;

import android.test.suitebuilder.annotation.LargeTest;

import com.mendeley.sdk.exceptions.HttpResponseException;
import com.mendeley.sdk.exceptions.MendeleyException;
import com.mendeley.sdk.model.Education;
import com.mendeley.sdk.model.Employment;
import com.mendeley.sdk.model.Institution;
import com.mendeley.sdk.model.Profile;
import com.mendeley.sdk.request.SignedInTest;
import com.mendeley.sdk.testUtils.AssertUtils;
import com.mendeley.sdk.util.DateUtils;

import junit.framework.Assert;

import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

public class EmploymentRequestTest extends SignedInTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // clean me profile, to avoid residues of employments and educations
        final Profile meProfile = new Profile.Builder()
                .setEducation(new LinkedList<Education>())
                .setEmployment(new LinkedList<Employment>())
                .build();

        getTestAccountSetupUtils().setupMeRemoteProfile(meProfile);
    }

    @LargeTest
    public void test_postEmploymentUsingCustomInstitutionWithWebsite_createsEmploymentInServer() throws Exception {

        // GIVEN an employment
        final Employment postingEmployment = new Employment.Builder(createEmployment(createRandomInstitution()))
                .setWebsite(generateRandomString(20))
                .build();


        // WHEN posting it
        final Employment returnedEmployment = new EmploymentEndpoint.PostEmploymentRequest(postingEmployment, getAuthTokenManager(), getClientCredentials()).run().resource;

        // THEN we receive the same employment back, with id filled
        AssertUtils.assertEmployment(postingEmployment, returnedEmployment);
        Assert.assertNotNull(returnedEmployment.id);

        // ...and the employment exists in the server
        AssertUtils.assertEmployment(new EmploymentEndpoint.GetEmploymentRequest(returnedEmployment.id, getAuthTokenManager(), getClientCredentials()).run().resource, postingEmployment);
    }

    @LargeTest
    public void test_postEmploymentWithCustomInstitutionWithoutWebsite_createsEmploymentInServer() throws Exception {
        // NOTE: there is a bug in the API where POSTing employment without website returns 500

        // GIVEN an employment
        final Employment postingEmployment = new Employment.Builder(createEmployment(createRandomInstitution()))
                .setWebsite(null)
                .build();

        // WHEN posting it
        final Employment returnedEmployment = new EmploymentEndpoint.PostEmploymentRequest(postingEmployment, getAuthTokenManager(), getClientCredentials()).run().resource;

        // THEN we receive the same employment back, with id filled
        AssertUtils.assertEmployment(postingEmployment, returnedEmployment);
        Assert.assertNotNull(returnedEmployment.id);

        // ...and the employment exists in the server
        AssertUtils.assertEmployment(new EmploymentEndpoint.GetEmploymentRequest(returnedEmployment.id, getAuthTokenManager(), getClientCredentials()).run().resource, postingEmployment);
    }


    @LargeTest
    public void test_postEmploymentUsingExistingInstitution_createsEmploymentInServer() throws Exception {

        // GIVEN an employment
        final Employment postingEmployment = createEmployment(getRandomExistingInstitution());

        // WHEN posting it
        final Employment returnedEmployment = new EmploymentEndpoint.PostEmploymentRequest(postingEmployment, getAuthTokenManager(), getClientCredentials()).run().resource;

        // THEN we receive the same employment back, with id filled
        AssertUtils.assertEmployment(postingEmployment, returnedEmployment);
        Assert.assertNotNull(returnedEmployment.id);

        // ...and the employment exists in the server
        AssertUtils.assertEmployment(new EmploymentEndpoint.GetEmploymentRequest(returnedEmployment.id, getAuthTokenManager(), getClientCredentials()).run().resource, postingEmployment);
    }

    @LargeTest
    public void test_deleteEmployment_deletesEmploymentFromServer() throws Exception {

        // GIVEN an employment that exists in the server
        final Employment postingEmployment = new Employment.Builder(createEmployment(createRandomInstitution()))
                .setWebsite(generateRandomString(20))
                .build();


        final Employment existingEmployment = new EmploymentEndpoint.PostEmploymentRequest(postingEmployment, getAuthTokenManager(), getClientCredentials()).run().resource;

        // WHEN deleting it
        new EmploymentEndpoint.DeleteEmployment(existingEmployment.id, getAuthTokenManager(), getClientCredentials()).run();

        // THEN the employment does not exist in the server
        try {
            new EmploymentEndpoint.GetEmploymentRequest(existingEmployment.id, getAuthTokenManager(), getClientCredentials()).run();
            Assert.fail();
        } catch (HttpResponseException e) {
            Assert.assertEquals(404, e.httpReturnCode);
        }
    }

    private Employment createEmployment(Institution institution) throws ParseException {
        return new Employment.Builder()
                .setPosition(generateRandomString(20))
                .setStartDate(DateUtils.parseYearMonthDayDate("2000-01-01"))
                .setEndDate(DateUtils.parseYearMonthDayDate("2010-10-10"))
                .setInstitution(institution)
                .build();
    }

    private Institution createRandomInstitution() {
        return new Institution.Builder()
                .setName(generateRandomString(20))
                .build();
    }

    private Institution getRandomExistingInstitution() throws MendeleyException {
        final InstitutionsEndpoint.GetInstitutionsRequest.Parameters params = new InstitutionsEndpoint.GetInstitutionsRequest.Parameters.Builder()
                .setHint("Madrid")
                .build();

        final InstitutionsEndpoint.GetInstitutionsRequest getInstitutionsRequest = new InstitutionsEndpoint.GetInstitutionsRequest(params, getAuthTokenManager(), getClientCredentials());
        final List<Institution> existingInstitutions = getInstitutionsRequest.run().resource;

        return existingInstitutions.get(getRandom().nextInt(existingInstitutions.size()));
    }

}
