package com.mendeley.integration;

import com.mendeley.api.MendeleySdk;
import com.mendeley.api.callbacks.group.GetGroupCallback;
import com.mendeley.api.callbacks.group.GetGroupMembersCallback;
import com.mendeley.api.callbacks.group.GetGroupsCallback;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.model.Group;
import com.mendeley.api.model.UserRole;
import com.mendeley.api.params.GroupRequestParameters;
import com.mendeley.api.params.Page;
import com.mendeley.testUtils.SignInException;
import com.mendeley.testUtils.TestUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.Collections.sort;


public class GroupNetworkProviderTest  extends BaseNetworkProviderTest {

    private static final String[] GROUPS = {
            "Artificial Neural Networks",
            "Polyphasic sleep",
            "Technology of Music"
    };

    private static final String[] PROFILE_IDS = {
            "87777129-2222-3800-9e1c-fa76c68201d7",
            "f38dc0c8-df12-32a0-ae70-28ab4f3409cd"
    };

    private static final Comparator<Group> GROUP_COMPARATOR = new Comparator<Group>() {
        @Override
        public int compare(Group group1, Group group2) {
            return group1.name.compareTo(group2.name);
        }
    };

    private static final Comparator<UserRole> USER_ROLE_COMPARATOR = new Comparator<UserRole>() {
        @Override
        public int compare(UserRole userRole1, UserRole userRole2) {
            return userRole1.profileId.compareTo(userRole2.profileId);
        }
    };

    private MendeleySdk sdk;

    private List<Group> groupsRcvd;
    private List<Group> groupRcvd;
    private List<UserRole> userRolesRecvd;

    private GetGroupCallback getGroupCallback;
    private GetGroupsCallback getGroupsCallback;
    private GetGroupMembersCallback getGroupMembersCallback;

    @Override
    protected void setUp() throws InterruptedException, SignInException {
        getGroupsCallback = new GetGroupsCallback() {
            @Override
            public void onGroupsReceived(List<Group> groups, Page next) {
                setGroups(groups);
                reportSuccess();
            }

            @Override
            public void onGroupsNotReceived(MendeleyException mendeleyException) {
                fail("groups not received");
            }
        };

        getGroupCallback = new GetGroupCallback() {
            @Override
            public void onGroupReceived(Group group) {
                setGroup(group);
                reportSuccess();
            }

            @Override
            public void onGroupNotReceived(MendeleyException mendeleyException) {
                fail("group not received");
            }
        };

        getGroupMembersCallback = new GetGroupMembersCallback() {
            @Override
            public void onGroupMembersReceived(String groupId, List<UserRole> userRoles, Page next) {
                setMembers(userRoles);
                reportSuccess();
            }

            @Override
            public void onGroupMembersNotReceived(MendeleyException mendeleyException) {
                fail("members not received");
            }
        };

        sdk = TestUtils.signIn(getContext().getAssets());
    }

    public void testGetGroups() throws InterruptedException {

        getGroups();

        assertEquals("expected three groupsRcvd", 3, groupsRcvd.size());
        assertEquals("group name incorrect", "Artificial Neural Networks", groupsRcvd.get(0).name);
        assertEquals("group name incorrect", "Polyphasic sleep", groupsRcvd.get(1).name);
        assertEquals("group name incorrect", "Technology of Music", groupsRcvd.get(2).name);
    }

    public void testGetGroup() throws InterruptedException {

        getGroup();

        assertEquals("expected one groupRcvd", 1, groupRcvd.size());
        assertEquals("group name incorrect", "Artificial Neural Networks", groupsRcvd.get(0).name);
    }

    public void testGetGroupMembers() throws InterruptedException {

        getMembers();

        List<String> profIds = new ArrayList<String>();

	for (UserRole ur : userRolesRecvd) {
            profIds.add(ur.profileId);
	}
        assertTrue("profile id incorrect", profIds.contains("87777129-2222-3800-9e1c-fa76c68201d7"));
        assertTrue("profile id incorrect", profIds.contains("f38dc0c8-df12-32a0-ae70-28ab4f3409cd"));
    }

    private void getGroups() {
        expectSdkCall();
        sdk.getGroups(new GroupRequestParameters(), getGroupsCallback);
        waitForSdkResponse("getting groupsRcvd");
    }

    private void getGroup() {
        expectSdkCall();
        sdk.getGroups(new GroupRequestParameters(), getGroupsCallback);
        waitForSdkResponse("getting groupsRcvd");
        Group testGroup = groupsRcvd.get(0);

        expectSdkCall();
        sdk.getGroup(testGroup.id, getGroupCallback);
        waitForSdkResponse("getting groupRcvd");
    }

    private void getMembers() {
        expectSdkCall();
        sdk.getGroups(new GroupRequestParameters(), getGroupsCallback);
        waitForSdkResponse("getting groupsRcvd");
        Group testGroup = groupsRcvd.get(0);

        expectSdkCall();
        sdk.getGroupMembers(new GroupRequestParameters(), testGroup.id, getGroupMembersCallback);
        waitForSdkResponse("getting userRolesRecvd");
    }

    private void setGroups(List<Group> groups) {
        sort(groups, GROUP_COMPARATOR);
        this.groupsRcvd = groups;
    }

    private void setGroup(Group group) {
        this.groupRcvd = new ArrayList<Group>();
        this.groupRcvd.add(group);
    }

    private void setMembers(List<UserRole> userRoles) {
        sort(userRoles, USER_ROLE_COMPARATOR);
        this.userRolesRecvd = userRoles;
    }
}

