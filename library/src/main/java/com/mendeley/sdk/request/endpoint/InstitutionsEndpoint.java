package com.mendeley.sdk.request.endpoint;

import android.net.Uri;
import android.text.TextUtils;
import android.util.JsonReader;

import com.mendeley.sdk.AuthTokenManager;
import com.mendeley.sdk.ClientCredentials;
import com.mendeley.sdk.Request;
import com.mendeley.sdk.model.Institution;
import com.mendeley.sdk.request.GetAuthorizedRequest;
import com.mendeley.sdk.request.JsonParser;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import static com.mendeley.sdk.Request.MENDELEY_API_BASE_URL;

/**
 * Class with the implementation of typical {@link Request}s against the /institutions endpoint.
 * {@see http://dev.mendeley.com/methods/#groups}
 */
public class InstitutionsEndpoint {
    private static final String INSTITUTIONS_BASE_URL = MENDELEY_API_BASE_URL + "institutions";


    public static class GetInstitutionsRequest extends GetAuthorizedRequest<List<Institution>> {
        public static final String INSTITUTIONS_CONTENT_TYPE = "application/vnd.mendeley-institution.1+json";

        private static Uri getInstitutionsUrl(Parameters params) {
            final Uri.Builder bld = Uri.parse(INSTITUTIONS_BASE_URL).buildUpon();

            if (!TextUtils.isEmpty(params.hint)) {
                bld.appendQueryParameter("hint", params.hint);
            }
            if (!TextUtils.isEmpty(params.email)) {
                bld.appendQueryParameter("email", params.email);
            }
            if (params.limit != null) {
                bld.appendQueryParameter("limit", String.valueOf(params.limit));
            }
            if (!TextUtils.isEmpty(params.name)) {
                bld.appendQueryParameter("name", params.name);
            }
            if (!TextUtils.isEmpty(params.city)) {
                bld.appendQueryParameter("city", params.city);
            }
            if (!TextUtils.isEmpty(params.state)) {
                bld.appendQueryParameter("state", params.state);
            }
            if (!TextUtils.isEmpty(params.country)) {
                bld.appendQueryParameter("country", params.country);
            }

            return bld.build();
        }

        public GetInstitutionsRequest(Parameters params, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            this(getInstitutionsUrl(params), authTokenManager, clientCredentials);
        }

        public GetInstitutionsRequest(Uri url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(url, authTokenManager, clientCredentials);
        }

        @Override
        protected List<Institution> manageResponse(InputStream is) throws JSONException, IOException, ParseException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.institutionsFromJson(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", INSTITUTIONS_CONTENT_TYPE);
        }


        public static class Parameters {
            public final String hint;
            public final String email;
            public final Integer limit;
            public final String name;
            public final String city;
            public final String state;
            public final String country;

            private Parameters(String hint, String email, Integer limit, String name, String city, String state, String country) {
                this.hint = hint;
                this.email = email;
                this.limit = limit;
                this.name = name;
                this.city = city;
                this.state = state;
                this.country = country;
            }

            public static class Builder {
                private String hint;
                private String email;
                private Integer limit;
                private String name;
                private String city;
                private String state;
                private String country;

                public Builder setHint(String hint) {
                    this.hint = hint;
                    return this;
                }

                public Builder setEmail(String email) {
                    this.email = email;
                    return this;
                }

                public Builder setLimit(Integer limit) {
                    this.limit = limit;
                    return this;
                }

                public Builder setName(String name) {
                    this.name = name;
                    return this;
                }

                public Builder setCity(String city) {
                    this.city = city;
                    return this;
                }

                public Builder setState(String state) {
                    this.state = state;
                    return this;
                }

                public Builder setCountry(String country) {
                    this.country = country;
                    return this;
                }

                public Parameters build() {
                    return new Parameters(hint, email, limit, name, city, state, country);
                }
            }
        }
    }



}
