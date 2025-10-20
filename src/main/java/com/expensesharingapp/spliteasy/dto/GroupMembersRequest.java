package com.expensesharingapp.spliteasy.dto;

import java.util.List;

import lombok.Data;

@Data
public class GroupMembersRequest {
    private List<Long> memberIds; // list of user IDs to add
}
