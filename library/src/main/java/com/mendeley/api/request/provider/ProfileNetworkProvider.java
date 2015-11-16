package com.mendeley.api.request.provider;

import android.util.JsonReader;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.model.Profile;
import com.mendeley.api.request.GetAuthorizedRequest;
import com.mendeley.api.request.JsonParser;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import static com.mendeley.api.request.Request.API_URL;

/**
 * NetworkProvider class for Profile API calls
 */
public class ProfileNetworkProvider {
	public static String PROFILES_URL = API_URL + "profiles/";

    public ProfileNetworkProvider() {
    }

    public static class GetProfileRequest extends GetAuthorizedRequest<Profile> {
        public GetProfileRequest(String url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(url, authTokenManager, clientCredentials);
        }

        @Override
        protected Profile manageResponse(InputStream is) throws JSONException, IOException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.parseProfile(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", "application/vnd.mendeley-profiles.1+json");
        }
    }
}
