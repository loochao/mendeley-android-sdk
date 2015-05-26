package com.mendeley.api.request.provider;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.model.Profile;
import com.mendeley.api.request.JsonParser;
import com.mendeley.api.request.procedure.GetNetworkRequest;

import org.json.JSONException;

import static com.mendeley.api.request.NetworkUtils.API_URL;

/**
 * NetworkProvider class for Profile API calls
 */
public class ProfileNetworkProvider {
	public static String PROFILES_URL = API_URL + "profiles/";



    public ProfileNetworkProvider() {
    }

    public static class GetProfileRequest extends GetNetworkRequest<Profile> {
        public GetProfileRequest(String url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(url, "application/vnd.mendeley-profiles.1+json", authTokenManager, clientCredentials);
        }

        @Override
        protected Profile parseJsonString(String jsonString) throws JSONException {
            return JsonParser.parseProfile(jsonString);
        }
    }
}
