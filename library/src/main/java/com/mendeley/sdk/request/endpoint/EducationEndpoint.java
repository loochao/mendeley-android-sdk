package com.mendeley.sdk.request.endpoint;

import android.net.Uri;
import android.text.TextUtils;
import android.util.JsonReader;

import com.mendeley.sdk.AuthTokenManager;
import com.mendeley.sdk.ClientCredentials;
import com.mendeley.sdk.Request;
import com.mendeley.sdk.model.Education;
import com.mendeley.sdk.request.DeleteAuthorizedRequest;
import com.mendeley.sdk.request.GetAuthorizedRequest;
import com.mendeley.sdk.request.JsonParser;
import com.mendeley.sdk.request.PostAuthorizedRequest;
import com.mendeley.sdk.util.DateUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;

import static com.mendeley.sdk.Request.MENDELEY_API_BASE_URL;

/**
 * Class with the implementation of typical {@link Request}s against the /education endpoint.
 */
public class EducationEndpoint {

    public static final String EDUCATION_BASE_URL = MENDELEY_API_BASE_URL + "education";

    public static final String EDUCATION_CONTENT_TYPE = "application/vnd.mendeley-education.1+json";
    public static final String EDUCATION_NEW_CONTENT_TYPE = "application/vnd.mendeley-new-education.1+json";
    public static final String EDUCATION_CUSTOM_INSTITUTION_CONTENT_TYPE = "application/vnd.mendeley-education-custom-institution.1+json";

    public static class GetEducationRequest extends GetAuthorizedRequest<Education> {

        public GetEducationRequest(String educationId, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(Uri.parse(EDUCATION_BASE_URL).buildUpon().appendPath(educationId).build(), authTokenManager, clientCredentials);
        }

        @Override
        protected Education manageResponse(InputStream is) throws JSONException, IOException, ParseException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.educationFromJson(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", EDUCATION_CONTENT_TYPE);
        }
    }


    public static class PostEducationRequest extends PostAuthorizedRequest<Education> {

        final private Education education;

        public PostEducationRequest(Education education, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(Uri.parse(EDUCATION_BASE_URL), authTokenManager, clientCredentials);
            this.education = education;
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", getContentType(education));
        }

        @Override
        protected RequestBody getBody() throws JSONException {
            final String content = educationToJson(education).toString();
            return RequestBody.create(MediaType.parse(getContentType(education)), content);
        }


        public String getContentType(Education education) {
            if (TextUtils.isEmpty(education.institution.id)) {
                return EDUCATION_CUSTOM_INSTITUTION_CONTENT_TYPE;
            } else {
                return EDUCATION_NEW_CONTENT_TYPE;
            }
        }

        @Override
        protected Education manageResponse(InputStream is) throws Exception {
            final JsonReader reader = new JsonReader(new InputStreamReader(is));
            return JsonParser.educationFromJson(reader);
        }

        private JSONObject educationToJson(Education education) throws JSONException {
            final JSONObject json = new JSONObject();

            if (!TextUtils.isEmpty(education.degree)) {
                json.put("degree", education.degree);
            }
            if (education.startDate != null) {
                json.put("start_date", DateUtils.formatYearMonthDayDate(education.startDate));
            }
            if (education.endDate != null) {
                json.put("end_date", DateUtils.formatYearMonthDayDate(education.endDate));
            }

            // NOTE: there is a bug in the API where POSTing education without end date returns 500
            json.put("end_date", education.endDate != null ? DateUtils.formatYearMonthDayDate(education.endDate) : JSONObject.NULL);

            if (education.institution != null) {
                // depending on whether the institution is custom or has and id, the JSON is different
                if (TextUtils.isEmpty(education.institution.id)) {
                    json.put("institution_name", education.institution.name);
                    // NOTE: there is a bug in the API where POSTing education without website returns 500
                    json.put("website", !TextUtils.isEmpty(education.website) ? education.website : JSONObject.NULL);

                } else {
                    json.put("institution_id", education.institution.id);
                }
            }


            return json;
        }
    }

    public static class DeleteEducation extends DeleteAuthorizedRequest<Void> {
        public DeleteEducation(String educationId, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(Uri.parse(EDUCATION_BASE_URL).buildUpon().appendPath(educationId).build(), authTokenManager, clientCredentials);
        }
    }



}
