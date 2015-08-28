package com.mendeley.integration;

import android.test.AndroidTestCase;

import com.mendeley.api.BlockingSdk;
import com.mendeley.api.callbacks.group.GroupList;
import com.mendeley.api.callbacks.group.GroupMembersList;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.model.Group;
import com.mendeley.api.model.UserRole;
import com.mendeley.api.params.GroupRequestParameters;
import com.mendeley.testUtils.SignInException;
import com.mendeley.testUtils.TestUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.Collections.sort;

public class GroupNetworkBlockingTest extends AndroidTestCase {

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

    private BlockingSdk sdk;

    @Override
    protected void setUp() throws InterruptedException, SignInException {
        sdk = TestUtils.signIn(getContext().getAssets());
    }

    public void testGetGroups() throws MendeleyException {
        GroupList groupList = sdk.getGroups(new GroupRequestParameters());
        sort(groupList.groups, GROUP_COMPARATOR);

        assertEquals("expected three groups", 3, groupList.groups.size());
        assertEquals("group name incorrect", "Artificial Neural Networks", groupList.groups.get(0).name);
        assertEquals("group name incorrect", "Polyphasic sleep", groupList.groups.get(1).name);
        assertEquals("group name incorrect", "Technology of Music", groupList.groups.get(2).name);
    }

    public void testGetGroup() throws MendeleyException {
        Group testGroup = getTestGroup();

        Group received = sdk.getGroup(testGroup.id);
        assertEquals("group name incorrect", "Artificial Neural Networks", received.name);
    }

    public void testGetGroupMembers() throws MendeleyException {
        Group testGroup = getTestGroup();

        GroupMembersList groupMembersList = sdk.getGroupMembers(new GroupRequestParameters(), testGroup.id);

        List<String> profileIds = new ArrayList<String>();

        for (UserRole userRole : groupMembersList.userRoles) {
                profileIds.add(userRole.profileId);
        }
        assertTrue("profile id incorrect", profileIds.contains("87777129-2222-3800-9e1c-fa76c68201d7"));
        assertTrue("profile id incorrect", profileIds.contains("f38dc0c8-df12-32a0-ae70-28ab4f3409cd"));
    }

    private Group getTestGroup() throws MendeleyException {
        GroupList groupList = sdk.getGroups(new GroupRequestParameters());
        assertTrue("at least one group required", groupList.groups.size() >= 1);
        sort(groupList.groups, GROUP_COMPARATOR);
        return groupList.groups.get(0);
    }
}

