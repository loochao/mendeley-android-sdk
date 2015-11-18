package com.mendeley.api.request.endpoint;

import android.net.Uri;
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
import java.text.ParseException;
import java.util.Map;

import static com.mendeley.api.request.Request.MENDELEY_API_BASE_URL;

public class ProfilesEndpoint {
	public static String PROFILES_URL = MENDELEY_API_BASE_URL + "profiles/";

    public ProfilesEndpoint() {
    }

    public static class GetProfileRequest extends GetAuthorizedRequest<Profile> {
        public GetProfileRequest(String profileId, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(Uri.parse(ProfilesEndpoint.PROFILES_URL + profileId), authTokenManager, clientCredentials);
        }

        @Override
        protected Profile manageResponse(InputStream is) throws JSONException, IOException, ParseException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.parseProfile(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", "application/vnd.mendeley-profiles.1+json");
        }
    }

}
