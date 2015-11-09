package com.mendeley.api.request.procedure;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.request.JsonParser;

import org.json.JSONException;

import java.text.ParseException;
import java.util.List;

/**
 * NetworkProvider class for Application features API call
 */
public class ApplicationFeaturesNetworkProvider {

    public static class GetApplicationFeaturesProcedure extends GetNetworkRequest<List<String>> {
        public GetApplicationFeaturesProcedure(AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(com.mendeley.api.request.NetworkUtils.API_URL + "/application_features", "application/vnd.mendeley-document.1+json", authTokenManager, clientCredentials);
        }

        @Override
        protected List<String> parseJsonString(String jsonString) throws JSONException, ParseException {
            return JsonParser.parseApplicationFeatures(jsonString);
        }
    }
}
