package com.mendeley.api.integration;

import com.mendeley.api.callbacks.group.GroupList;
import com.mendeley.api.callbacks.group.GroupMembersList;
import com.mendeley.api.model.Group;
import com.mendeley.api.model.UserRole;
import com.mendeley.api.params.GroupRequestParameters;
import com.mendeley.api.params.Page;
import com.mendeley.api.testUtils.AssertUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class GroupEndpointBlockingTest extends EndpointBlockingTest {

    private static final String[] GROUPS = {
            "Artificial Neural Networks",
            "Polyphasic sleep",
            "Technology of Music"
    };

    private static final String[] PROFILE_IDS = {
            "87777129-2222-3800-9e1c-fa76c68201d7",
            "f38dc0c8-df12-32a0-ae70-28ab4f3409cd"
    };

    /**
     * As we don't have an API to setup groups for the test,
     * the tests rely on groups we joined to manually with the account
     * through the web interface https://www.mendeley.com/groups/
     * The same is true with the profile ids used in the getGroupMembers test
     */

    public void test_getGroups_receivesCorrectGroups() throws Exception {
        // GIVEN some groups on the server
        List<Group> expected = new LinkedList<Group>();
        for (String groupName : GROUPS) {
            expected.add(new Group.Builder().setName(groupName).build());
        }

        // WHEN getting groups
        final GroupList response = getSdk().getGroups(new GroupRequestParameters());
        final List<Group> actual = response.groups;

        Comparator<Group> comparator = new Comparator<Group>() {
            @Override
            public int compare(Group g1, Group g2) {
                return g1.name.compareTo(g2.name);
            }
        };

        // THEN we have the expected groups
        AssertUtils.assertSameElementsInCollection(expected, actual, comparator);
    }

    public void test_getGroups_whenMoreThanOnePage_receivesCorrectGroups() throws Exception {

        // GIVEN a number of groups on the server greater than the page size
        final int pageSize = 2;
        final int pageCount = 2;

        List<Group> expected = new LinkedList<Group>();
        for (String groupName : GROUPS) {
            expected.add(new Group.Builder().setName(groupName).build());
        }

        // WHEN getting groups
        final GroupRequestParameters params = new GroupRequestParameters();
        params.limit = pageSize;

        final List<Group> actual = new LinkedList<Group>();
        GroupList response = getSdk().getGroups(params);


        // THEN we receive a group list...
        for (int page = 0; page < pageCount; page++) {
            actual.addAll(response.groups);

            //... with a link to the next page if it was not the last page
            if (page < pageCount - 1) {
                assertTrue("page must be valid", Page.isValidPage(response.next));
                response = getSdk().getGroups(response.next);
            }
        }

        Comparator<Group> comparator = new Comparator<Group>() {
            @Override
            public int compare(Group g1, Group g2) {
                return g1.name.compareTo(g2.name);
            }
        };

        // THEN we have the expected groups
        AssertUtils.assertSameElementsInCollection(expected, actual, comparator);
    }

    public void test_getGroupById_receivesTheCorrectGroup() throws Exception {
        // GIVEN a group on the server
        Group expected = getTestAccountSetupUtils().getGroups().groups.get(0);

        // WHEN getting the group by id
        final Group actual = getSdk().getGroup(expected.id);

        // THEN we have the expected group
        AssertUtils.assertGroup(expected, actual);
    }

    public void test_getGroupMembers_receivesTheCorrectGroupMembers() throws Exception {
        // GIVEN a group
        List<Group> groups = getTestAccountSetupUtils().getGroups().groups;

        Comparator<Group> groupComparator = new Comparator<Group>() {
            @Override
            public int compare(Group g1, Group g2) {
                return g1.name.compareTo(g2.name);
            }
        };
        Collections.sort(groups, groupComparator);
        Group group = groups.get(1);

        // AND members of this group
        List<UserRole> expected = new LinkedList<UserRole>();
        for (String profileId : PROFILE_IDS) {
            expected.add(new UserRole.Builder().setProfileId(profileId).build());
        }

        // WHEN getting the group members
        GroupMembersList groupMembersList = getSdk().getGroupMembers(new GroupRequestParameters(), group.id);
        List<UserRole> actual = groupMembersList.userRoles;

        // THEN we have the expected members
        Comparator<UserRole> userRoleComparator = new Comparator<UserRole>() {
            @Override
            public int compare(UserRole u1, UserRole u2) {
                return u1.profileId.compareTo(u2.profileId);
            }
        };

        // THEN we have the expected groups
        AssertUtils.assertSameElementsInCollection(expected, actual, userRoleComparator);
    }
}

