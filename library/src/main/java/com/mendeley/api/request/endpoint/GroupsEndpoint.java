package com.mendeley.api.request.endpoint;

import android.net.Uri;
import android.util.JsonReader;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.model.Group;
import com.mendeley.api.model.UserRole;
import com.mendeley.api.request.GetAuthorizedRequest;
import com.mendeley.api.request.JsonParser;
import com.mendeley.api.request.params.GroupRequestParameters;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import static com.mendeley.api.request.Request.MENDELEY_API_BASE_URL;

/**
 * NetworkProvider class for Group API calls
 */

public class GroupsEndpoint {
    private static final String GROUP_BASE_URL = MENDELEY_API_BASE_URL + "groups";
    public static final String GROUP_CONTENT_TYPE = "application/vnd.mendeley-group.1+json";
    public static final String MEMBERSHIP_CONTENT_TYPE = "application/vnd.mendeley-membership.1+json";

    /* URLS */

    public static Uri getGetGroupsUrl(GroupRequestParameters params) {
        return getGetGroupsUrl(params, null);
    }

    /**
     * Builds the url for get groups
     *
     * @param params group request parameters object
     * @return the url string
     */
    public static Uri getGetGroupsUrl(GroupRequestParameters params, String requestUrl) {
        StringBuilder url = new StringBuilder();

        url.append(requestUrl == null ? GROUP_BASE_URL : requestUrl);

        if (params != null) {
            boolean firstParam = true;
            if (params.limit != null) {
                url.append(firstParam ? "?" : "&").append("limit=" + params.limit);
                firstParam = false;
            }
        }
        return Uri.parse(url.toString());
    }

    /**
     * Building the url for get group
     *
     * @param groupId the group id to get
     * @return the url string
     */
    public static Uri getGetGroupUrl(String groupId) {
        return Uri.parse(GROUP_BASE_URL + "/" + groupId);
    }

    /**
     * Building the url for get group members
     *
     * @param groupId the group id
     * @return the url string
     */
    public static String getGetGroupMembersUrl(String groupId) {
        return GROUP_BASE_URL + "/" + groupId + "/members";
    }


    /* PROCEDURES */

    public static class GetGroupsRequest extends GetAuthorizedRequest<List<Group>> {
        public GetGroupsRequest(Uri url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(url, authTokenManager, clientCredentials);
        }

        public GetGroupsRequest(GroupRequestParameters parameters, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            this(getGetGroupsUrl(parameters), authTokenManager, clientCredentials);
        }

        @Override
        protected List<Group> manageResponse(InputStream is) throws JSONException, IOException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.parseGroupList(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", GROUP_CONTENT_TYPE);
        }
    }

    public static class GetGroupRequest extends GetAuthorizedRequest<Group> {
        public GetGroupRequest(Uri url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(url, authTokenManager, clientCredentials);
        }

        public GetGroupRequest(String groupId, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(GroupsEndpoint.getGetGroupUrl(groupId), authTokenManager, clientCredentials);
        }

        @Override
        protected Group manageResponse(InputStream is) throws JSONException, IOException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.parseGroup(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", GROUP_CONTENT_TYPE);
        }
    }

    public static class GetGroupMembersRequest extends GetAuthorizedRequest<List<UserRole>> {
        public GetGroupMembersRequest(Uri url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(url, authTokenManager, clientCredentials);
        }

        public GetGroupMembersRequest(GroupRequestParameters parameters, String groupId, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            this(getGetGroupsUrl(parameters, getGetGroupMembersUrl(groupId)), authTokenManager, clientCredentials);
        }

        @Override
        protected List<UserRole> manageResponse(InputStream is) throws JSONException, IOException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.parseUserRoleList(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", MEMBERSHIP_CONTENT_TYPE);
        }
    }
}
