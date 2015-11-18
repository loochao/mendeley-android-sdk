package com.mendeley.api.request;

import com.mendeley.api.model.Group;
import com.mendeley.api.model.RequestResponse;
import com.mendeley.api.model.UserRole;
import com.mendeley.api.request.params.GroupRequestParameters;
import com.mendeley.api.request.params.Page;
import com.mendeley.api.testUtils.AssertUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class GroupRequestTest extends SignedInTest {

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
        final List<Group> actual = getRequestFactory().getGroups(new GroupRequestParameters()).run().resource;

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
        RequestResponse<List<Group>> response = getRequestFactory().getGroups(params).run();


        // THEN we receive a group list...
        for (int page = 0; page < pageCount; page++) {
            actual.addAll(response.resource);

            //... with a link to the next page if it was not the last page
            if (page < pageCount - 1) {
                assertTrue("page must be valid", Page.isValidPage(response.next));
                response = getRequestFactory().getGroups(response.next).run();
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
        Group expected = getTestAccountSetupUtils().getGroups().get(0);

        // WHEN getting the group by id
        final Group actual = getRequestFactory().getGroup(expected.id).run().resource;

        // THEN we have the expected group
        AssertUtils.assertGroup(expected, actual);
    }

    public void test_getGroupMembers_receivesTheCorrectGroupMembers() throws Exception {
        // GIVEN a group
        List<Group> groups = getTestAccountSetupUtils().getGroups();

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
        List<UserRole> actual = getRequestFactory().getGroupMembers(new GroupRequestParameters(), group.id).run().resource;

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

