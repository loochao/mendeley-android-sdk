package com.mendeley.api.network.provider;

import com.mendeley.api.auth.AuthenticationManager;
import com.mendeley.api.network.JsonParser;
import com.mendeley.api.network.procedure.GetNetworkProcedure;

import org.json.JSONException;

import java.text.ParseException;
import java.util.List;

import static com.mendeley.api.network.NetworkUtils.API_URL;

/**
 * NetworkProvider class for Application features API call
 */
public class ApplicationFeaturesNetworkProvider {

    public static class GetApplicationFeaturesProcedure extends GetNetworkProcedure<List<String>> {
        public GetApplicationFeaturesProcedure(AuthenticationManager authenticationManager) {
            super(API_URL + "application_features", "application/vnd.mendeley-features.1+json", authenticationManager);
        }

        @Override
        protected List<String> processJsonString(String jsonString) throws JSONException, ParseException {
            return JsonParser.parseApplicationFeatures(jsonString);
        }
    }
}
