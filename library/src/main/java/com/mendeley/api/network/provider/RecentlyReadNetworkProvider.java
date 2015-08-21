package com.mendeley.api.network.provider;

import com.mendeley.api.auth.AccessTokenProvider;
import com.mendeley.api.auth.AuthenticationManager;
import com.mendeley.api.callbacks.read_position.ReadPositionList;
import com.mendeley.api.model.ReadPosition;
import com.mendeley.api.network.JsonParser;
import com.mendeley.api.network.procedure.GetNetworkProcedure;
import com.mendeley.api.network.procedure.PostNetworkProcedure;

import org.json.JSONException;

import java.text.ParseException;

import static com.mendeley.api.network.NetworkUtils.API_URL;

/**
 * NetworkProvider class for Recently read API calls
 */
public class RecentlyReadNetworkProvider {
	public static final String RECENTLY_READ_BASE_URL = API_URL + "recently_read";

    private final AccessTokenProvider accessTokenProvider;

    public RecentlyReadNetworkProvider(AccessTokenProvider accessTokenProvider) {
        this.accessTokenProvider = accessTokenProvider;
    }

    public static String getGetRecentlyReadUrl(String groupId, String fileId, int limit) {
        StringBuilder url = new StringBuilder();
        url.append(RECENTLY_READ_BASE_URL);

        boolean firstParam = true;

        // groupId
        if (groupId != null) {
            url.append(firstParam ? "?" : "&").append("groupId=" + groupId);
            firstParam = false;
        }
        // fileId
        if (fileId != null) {
            url.append(firstParam ? "?" : "&").append("fileId=" + fileId);
            firstParam = false;
        }
        // limit
        url.append(firstParam ? "?" : "&").append("limit=" + limit);

        return url.toString();
    }

    /* PROCEDURES */

    public static class GetRecentlyReadProcedure extends GetNetworkProcedure<ReadPositionList> {
        public GetRecentlyReadProcedure(String url, AuthenticationManager authenticationManager) {
            super(url, "application/vnd.mendeley-recently-read.1+json", authenticationManager);
        }

        @Override
        protected ReadPositionList processJsonString(String jsonString) throws JSONException, ParseException {
            return new ReadPositionList(JsonParser.parseReadPositionList(jsonString), serverDate);
        }
    }


    public static class PostRecentlyReadProcedure extends PostNetworkProcedure<ReadPosition> {
        public PostRecentlyReadProcedure(ReadPosition readPosition, AuthenticationManager authenticationManager) throws JSONException {
            super(RECENTLY_READ_BASE_URL, "application/vnd.mendeley-recently-read.1+json", JsonParser.jsonFromReadPosition(readPosition), authenticationManager);
        }

        @Override
        protected ReadPosition processJsonString(String jsonString) throws JSONException, ParseException {
            return JsonParser.parseReadPosition(jsonString);
        }
    }

}
