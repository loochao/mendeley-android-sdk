package com.mendeley.api.request.provider;

import android.util.JsonReader;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.model.Group;
import com.mendeley.api.model.UserRole;
import com.mendeley.api.request.JsonParser;
import com.mendeley.api.request.params.GroupRequestParameters;
import com.mendeley.api.request.procedure.GetNetworkRequest;

import org.json.JSONException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import static com.mendeley.api.request.NetworkUtils.API_URL;

/**
 * NetworkProvider class for Group API calls
 */

public class GroupNetworkProvider {
    private static String groupsUrl = API_URL + "groups";





    /* URLS */

    public static String getGetGroupsUrl(GroupRequestParameters params) {
        return getGetGroupsUrl(params, null);
    }

    /**
     * Builds the url for get groups
     *
     * @param params group request parameters object
     * @return the url string
     */
    public static String getGetGroupsUrl(GroupRequestParameters params, String requestUrl) {
        StringBuilder url = new StringBuilder();

        url.append(requestUrl == null ? groupsUrl : requestUrl);

        if (params != null) {
            boolean firstParam = true;
            if (params.limit != null) {
                url.append(firstParam ? "?" : "&").append("limit=" + params.limit);
                firstParam = false;
            }
        }
        return url.toString();
    }

    /**
     * Building the url for get group
     *
     * @param groupId the group id to get
     * @return the url string
     */
    public static String getGetGroupUrl(String groupId) {
        return groupsUrl + "/" + groupId;
    }

    /**
     * Building the url for get group members
     *
     * @param groupId the group id
     * @return the url string
     */
    public static String getGetGroupMembersUrl(String groupId) {
        return groupsUrl + "/" + groupId + "/members";
    }


    /* PROCEDURES */

    public static class GetGroupsRequest extends GetNetworkRequest<List<Group>> {
        public GetGroupsRequest(String url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(url, "application/vnd.mendeley-group.1+json", authTokenManager, clientCredentials);
        }

        @Override
        protected List<Group> parseJsonString(String jsonString) throws JSONException, IOException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new ByteArrayInputStream(jsonString.getBytes())));
            return JsonParser.parseGroupList(reader);
        }
    }

    public static class GetGroupRequest extends GetNetworkRequest<Group> {
        public GetGroupRequest(String url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(url, "application/vnd.mendeley-group.1+json", authTokenManager, clientCredentials);
        }

        @Override
        protected Group parseJsonString(String jsonString) throws JSONException, IOException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new ByteArrayInputStream(jsonString.getBytes())));
            return JsonParser.parseGroup(reader);
        }
    }

    public static class GetGroupMembersRequest extends GetNetworkRequest<List<UserRole>> {
        public GetGroupMembersRequest(String url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(url, "application/vnd.mendeley-membership.1+json", authTokenManager, clientCredentials);
        }

        @Override
        protected List<UserRole> parseJsonString(String jsonString) throws JSONException, IOException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new ByteArrayInputStream(jsonString.getBytes())));
            return JsonParser.parseUserRoleList(reader);
        }
    }
}
