package com.mendeley.sdk.request.endpoint;

import android.net.Uri;
import android.text.TextUtils;
import android.util.JsonReader;

import com.mendeley.sdk.AuthTokenManager;
import com.mendeley.sdk.ClientCredentials;
import com.mendeley.sdk.Request;
import com.mendeley.sdk.model.Employment;
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
 * Class with the implementation of typical {@link Request}s against the /employment endpoint.
 */
public class EmploymentEndpoint {

	public static final String EMPLOYMENT_BASE_URL = MENDELEY_API_BASE_URL + "employment";

    public static final String EMPLOYMENT_CONTENT_TYPE = "application/vnd.mendeley-employment.1+json";
    public static final String EMPLOYMENT_NEW_CONTENT_TYPE = "application/vnd.mendeley-new-employment.1+json";
    public static final String EMPLOYMENT_CUSTOM_INSTITUTION_CONTENT_TYPE = "application/vnd.mendeley-employment-custom-institution.1+json";

    public static class GetEmploymentRequest extends GetAuthorizedRequest<Employment> {

        public GetEmploymentRequest(String employmentId, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(Uri.parse(EMPLOYMENT_BASE_URL).buildUpon().appendPath(employmentId).build(), authTokenManager, clientCredentials);
        }

        @Override
        protected Employment manageResponse(InputStream is) throws JSONException, IOException, ParseException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.employmentFromJson(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", EMPLOYMENT_CONTENT_TYPE);
        }
    }


    public static class PostEmploymentRequest extends PostAuthorizedRequest<Employment> {

        final private Employment employment;

        public PostEmploymentRequest(Employment employment, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(Uri.parse(EMPLOYMENT_BASE_URL), authTokenManager, clientCredentials);
            this.employment = employment;
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", getContentType(employment));
        }

        @Override
        protected RequestBody getBody() throws JSONException {
            final String content = employmentToJson(employment).toString();
            return RequestBody.create(MediaType.parse(getContentType(employment)), content);
        }


        public String getContentType(Employment employment) {
            if (TextUtils.isEmpty(employment.institution.id)) {
                return EMPLOYMENT_CUSTOM_INSTITUTION_CONTENT_TYPE;
            } else {
                return EMPLOYMENT_NEW_CONTENT_TYPE;
            }
        }

        @Override
        protected Employment manageResponse(InputStream is) throws Exception {
            final JsonReader reader = new JsonReader(new InputStreamReader(is));
            return JsonParser.employmentFromJson(reader);
        }

        private JSONObject employmentToJson(Employment employment) throws JSONException {
            final JSONObject json = new JSONObject();

            if (!TextUtils.isEmpty(employment.position)) {
                json.put("position", employment.position);
            }
            if (employment.startDate != null) {
                json.put("start_date", DateUtils.formatYearMonthDayDate(employment.startDate));
            }
            if (employment.endDate != null) {
                json.put("end_date", DateUtils.formatYearMonthDayDate(employment.endDate));
            }

            if (employment.institution != null) {
                // depending on whether the institution is custom or has and id, the JSON is different
                if (TextUtils.isEmpty(employment.institution.id)) {
                    json.put("institution_name", employment.institution.name);
                    // NOTE: there is a bug in the API where POSTing employment without website returns 500
                    json.put("website", !TextUtils.isEmpty(employment.website) ? employment.website : JSONObject.NULL);

                } else {
                    json.put("institution_id", employment.institution.id);
                }
            }


            return json;
        }
    }

    public static class DeleteEmployment extends DeleteAuthorizedRequest<Void> {
        public DeleteEmployment(String employmentId, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(Uri.parse(EMPLOYMENT_BASE_URL).buildUpon().appendPath(employmentId).build(), authTokenManager, clientCredentials);
        }
    }



}
