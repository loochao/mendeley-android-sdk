package com.mendeley.api.callbacks.group;

import com.mendeley.api.model.UserRole;
import com.mendeley.api.params.Page;

import java.util.List;

public class GroupMembersList {
    public final List<UserRole> userRoles;
    public final Page next;

    public GroupMembersList(List<UserRole> userRoles, Page next) {
        this.userRoles = userRoles;
        this.next = next;
    }
}
