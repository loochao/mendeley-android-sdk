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

public class EducationRequestTest extends SignedInTest {

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
    public void test_postEducationUsingCustomInstitutionWithWebsite_createsEducationInServer() throws Exception {

        // GIVEN an education
        final Education postingEducation = new Education.Builder(createEducation(createRandomInstitution()))
                .setWebsite(generateRandomString(20))
                .build();


        // WHEN posting it
        final Education returnedEducation = new EducationEndpoint.PostEducationRequest(postingEducation, getAuthTokenManager(), getClientCredentials()).run().resource;

        // THEN we receive the same education back, with id filled
        AssertUtils.assertEducation(postingEducation, returnedEducation);
        Assert.assertNotNull(returnedEducation.id);

        // ...and the education exists in the server
        AssertUtils.assertEducation(new EducationEndpoint.GetEducationRequest(returnedEducation.id, getAuthTokenManager(), getClientCredentials()).run().resource, postingEducation);
    }

    @LargeTest
    public void test_postEducationWithCustomInstitutionWithoutWebsite_createsEducationInServer() throws Exception {
        // NOTE: there is a bug in the API where POSTing education without website returns 500

        // GIVEN an education
        final Education postingEducation = new Education.Builder(createEducation(createRandomInstitution()))
                .setWebsite(null)
                .build();

        // WHEN posting it
        final Education returnedEducation = new EducationEndpoint.PostEducationRequest(postingEducation, getAuthTokenManager(), getClientCredentials()).run().resource;

        // THEN we receive the same education back, with id filled
        AssertUtils.assertEducation(postingEducation, returnedEducation);
        Assert.assertNotNull(returnedEducation.id);

        // ...and the education exists in the server
        AssertUtils.assertEducation(new EducationEndpoint.GetEducationRequest(returnedEducation.id, getAuthTokenManager(), getClientCredentials()).run().resource, postingEducation);
    }

    @LargeTest
    public void test_postEducationWithoutEndDate_createsEducationInServer() throws Exception {
        // NOTE: there is a bug in the API where POSTing education without website returns 500

        // GIVEN an education
        final Education postingEducation = new Education.Builder(createEducation(createRandomInstitution()))
                .setEndDate(null)
                .build();

        // WHEN posting it
        final Education returnedEducation = new EducationEndpoint.PostEducationRequest(postingEducation, getAuthTokenManager(), getClientCredentials()).run().resource;

        // THEN we receive the same education back, with id filled
        AssertUtils.assertEducation(postingEducation, returnedEducation);
        Assert.assertNotNull(returnedEducation.id);

        // ...and the education exists in the server
        AssertUtils.assertEducation(new EducationEndpoint.GetEducationRequest(returnedEducation.id, getAuthTokenManager(), getClientCredentials()).run().resource, postingEducation);
    }


    @LargeTest
    public void test_postEducationUsingExistingInstitution_createsEducationInServer() throws Exception {

        // GIVEN an education
        final Education postingEducation = createEducation(getRandomExistingInstitution());

        // WHEN posting it
        final Education returnedEducation = new EducationEndpoint.PostEducationRequest(postingEducation, getAuthTokenManager(), getClientCredentials()).run().resource;

        // THEN we receive the same education back, with id filled
        AssertUtils.assertEducation(postingEducation, returnedEducation);
        Assert.assertNotNull(returnedEducation.id);

        // ...and the education exists in the server
        AssertUtils.assertEducation(new EducationEndpoint.GetEducationRequest(returnedEducation.id, getAuthTokenManager(), getClientCredentials()).run().resource, postingEducation);
    }

    @LargeTest
    public void test_deleteEducation_deletesEducationFromServer() throws Exception {

        // GIVEN an education that exists in the server
        final Education postingEducation = new Education.Builder(createEducation(createRandomInstitution()))
                .setWebsite(generateRandomString(20))
                .build();


        final Education existingEducation = new EducationEndpoint.PostEducationRequest(postingEducation, getAuthTokenManager(), getClientCredentials()).run().resource;

        // WHEN deleting it
        new EducationEndpoint.DeleteEducation(existingEducation.id, getAuthTokenManager(), getClientCredentials()).run();

        // THEN the education does not exist in the server
        try {
            new EducationEndpoint.GetEducationRequest(existingEducation.id, getAuthTokenManager(), getClientCredentials()).run();
            Assert.fail();
        } catch (HttpResponseException e) {
            Assert.assertEquals(404, e.httpReturnCode);
        }
    }

    private Education createEducation(Institution institution) throws ParseException {
        return new Education.Builder()
                .setDegree(generateRandomString(20))
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
