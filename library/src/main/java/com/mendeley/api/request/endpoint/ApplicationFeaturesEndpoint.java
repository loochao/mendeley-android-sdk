package com.mendeley.api.request.endpoint;

import android.net.Uri;
import android.util.JsonReader;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.request.GetAuthorizedRequest;
import com.mendeley.api.request.JsonParser;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import static com.mendeley.api.request.Request.MENDELEY_API_BASE_URL;

public class ApplicationFeaturesEndpoint {

    private final static String APPLICATION_FEATURES_BASE_URL = MENDELEY_API_BASE_URL + "/application_features";
    private static String APPLICATION_FEATURES_CONTENT_TYPE;

    public static class GetApplicationFeaturesProcedure extends GetAuthorizedRequest<List<String>> {

        public GetApplicationFeaturesProcedure(AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(Uri.parse(APPLICATION_FEATURES_BASE_URL), authTokenManager, clientCredentials);
        }

        @Override
        protected List<String> manageResponse(InputStream is) throws JSONException, ParseException, IOException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.parseApplicationFeatures(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            APPLICATION_FEATURES_CONTENT_TYPE = "application/vnd.mendeley-features.1+json";
            headers.put("Content-type", APPLICATION_FEATURES_CONTENT_TYPE);
        }
    }
}
