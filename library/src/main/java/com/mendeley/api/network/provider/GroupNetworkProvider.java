package com.mendeley.api.network.provider;

import com.mendeley.api.auth.AccessTokenProvider;
import com.mendeley.api.auth.AuthenticationManager;
import com.mendeley.api.callbacks.RequestHandle;
import com.mendeley.api.callbacks.group.GetGroupCallback;
import com.mendeley.api.callbacks.group.GetGroupMembersCallback;
import com.mendeley.api.callbacks.group.GetGroupsCallback;
import com.mendeley.api.callbacks.group.GroupList;
import com.mendeley.api.callbacks.group.GroupMembersList;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.exceptions.NoMorePagesException;
import com.mendeley.api.model.Group;
import com.mendeley.api.model.UserRole;
import com.mendeley.api.network.Environment;
import com.mendeley.api.network.JsonParser;
import com.mendeley.api.network.NullRequest;
import com.mendeley.api.network.procedure.GetNetworkProcedure;
import com.mendeley.api.network.task.GetNetworkTask;
import com.mendeley.api.params.GroupRequestParameters;
import com.mendeley.api.params.Page;

import org.json.JSONException;

import java.util.List;

import static com.mendeley.api.network.NetworkUtils.API_URL;

/**
 * NetworkProvider class for Group API calls
 */

public class GroupNetworkProvider {
    private static String groupsUrl = API_URL + "groups";

    private final Environment environment;
    private final AccessTokenProvider accessTokenProvider;

    public GroupNetworkProvider(Environment environment, AccessTokenProvider accessTokenProvider) {
       this.environment = environment;
        this.accessTokenProvider = accessTokenProvider;
    }

    /**
     * @param params group request parameters object
     * @param callback GetGroupsCallback callback object
     */
    public RequestHandle doGetGroups(GroupRequestParameters params, GetGroupsCallback callback) {
        String[] paramsArray = new String[] { getGetGroupsUrl(params) };
        GetGroupsTask getGroupsTask = new GetGroupsTask(callback);
        getGroupsTask.executeOnExecutor(environment.getExecutor(), paramsArray);
        return getGroupsTask;
    }

    /**
     * @param next reference to next page
     */
    public RequestHandle doGetGroups(Page next, GetGroupsCallback callback) {
        if (Page.isValidPage(next)) {
            String[] paramsArray = new String[]{next.link};
            GetGroupsTask getGroupsTask = new GetGroupsTask(callback);
            new GetGroupsTask(callback).executeOnExecutor(environment.getExecutor(), paramsArray);
            return getGroupsTask;
        } else {
            callback.onGroupsNotReceived(new NoMorePagesException());
            return NullRequest.get();
        }
    }

    /**
     * @param groupId the group id to get
     */
    public void doGetGroup(String groupId, GetGroupCallback callback) {
        String[] paramsArray = new String[] { getGetGroupUrl(groupId) };
        new GetGroupTask(callback).executeOnExecutor(environment.getExecutor(), paramsArray);
    }

    /**
     * @param groupId the group id
     */
    public void doGetGroupMembers(GroupRequestParameters params, String groupId, GetGroupMembersCallback callback) {
        String[] paramsArray = new String[] { getGetGroupsUrl(params, getGetGroupMembersUrl(groupId)) };
        new GetGroupMembersTask(callback, groupId).executeOnExecutor(environment.getExecutor(), paramsArray);
    }

    /**
     * @param next reference to next page
     */
    public void doGetGroupMembers(Page next, String groupId, GetGroupMembersCallback callback) {
        if (Page.isValidPage(next)) {
            String[] paramsArray = new String[] { next.link };
            new GetGroupMembersTask(callback, groupId).executeOnExecutor(environment.getExecutor(), paramsArray);
        } else {
            callback.onGroupMembersNotReceived(new NoMorePagesException());
        }
    }

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

    /* TASKS */

    private class GetGroupsTask extends GetNetworkTask {
        private final GetGroupsCallback callback;

        List<Group> groups;

        private GetGroupsTask(GetGroupsCallback callback) {
            this.callback = callback;
        }

        @Override
        protected void processJsonString(String jsonString) throws JSONException {
            groups = JsonParser.parseGroupList(jsonString);
        }

        @Override
        protected String getContentType() {
            return "application/vnd.mendeley-group.1+json";
        }

        @Override
        protected AccessTokenProvider getAccessTokenProvider() {
            return accessTokenProvider;
        }

        @Override
        protected void onSuccess() {
            callback.onGroupsReceived(groups, next);
        }

        @Override
        protected void onFailure(MendeleyException exception) {
            callback.onGroupsNotReceived(exception);
        }
    }

    private class GetGroupTask extends GetNetworkTask {
        private final GetGroupCallback callback;

        Group group;

        private GetGroupTask(GetGroupCallback callback) {
            this.callback = callback;
        }

        @Override
        protected void processJsonString(String jsonString) throws JSONException {
            group = JsonParser.parseGroup(jsonString);
        }

        @Override
        protected String getContentType() {
            return "application/vnd.mendeley-group.1+json";
        }

        @Override
        protected AccessTokenProvider getAccessTokenProvider() {
            return accessTokenProvider;
        }

        @Override
        protected void onSuccess() {
            callback.onGroupReceived(group);
        }

        @Override
        protected void onFailure(MendeleyException exception) {
            callback.onGroupNotReceived(exception);
        }
    }

    private class GetGroupMembersTask extends GetNetworkTask {
        private final GetGroupMembersCallback callback;

        private List<UserRole> userRoles;
        private final String groupId;

        private GetGroupMembersTask(GetGroupMembersCallback callback, String groupId) {
            this.callback = callback;
            this.groupId = groupId;
        }

        @Override
        protected void processJsonString(String jsonString) throws JSONException {
            userRoles = JsonParser.parseUserRoleList(jsonString);
        }

        @Override
        protected String getContentType() {
            return "application/vnd.mendeley-membership.1+json";
        }

        @Override
        protected AccessTokenProvider getAccessTokenProvider() {
            return accessTokenProvider;
        }

        @Override
        protected void onSuccess() {
            callback.onGroupMembersReceived(groupId, userRoles, next);
        }

        @Override
        protected void onFailure(MendeleyException exception) {
            callback.onGroupMembersNotReceived(exception);
        }
    }

    /* PROCEDURES */

    public static class GetGroupsProcedure extends GetNetworkProcedure<GroupList> {
        public GetGroupsProcedure(String url, AuthenticationManager authenticationManager) {
            super(url, "application/vnd.mendeley-group.1+json", authenticationManager);
        }

        @Override
        protected GroupList processJsonString(String jsonString) throws JSONException {
            return new GroupList(JsonParser.parseGroupList(jsonString), next);
        }
    }

    public static class GetGroupProcedure extends GetNetworkProcedure<Group> {
        public GetGroupProcedure(String url, AuthenticationManager authenticationManager) {
            super(url, "application/vnd.mendeley-group.1+json", authenticationManager);
        }

        @Override
        protected Group processJsonString(String jsonString) throws JSONException {
            return JsonParser.parseGroup(jsonString);
        }
    }

    public static class GetGroupMembersProcedure extends GetNetworkProcedure<GroupMembersList> {
        public GetGroupMembersProcedure(String url, AuthenticationManager authenticationManager) {
            super(url, "application/vnd.mendeley-membership.1+json", authenticationManager);
        }

        @Override
        protected GroupMembersList processJsonString(String jsonString) throws JSONException {
            return new GroupMembersList(JsonParser.parseUserRoleList(jsonString), next);
        }
    }
}
