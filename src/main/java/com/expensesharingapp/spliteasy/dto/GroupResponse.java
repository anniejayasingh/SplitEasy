package com.expensesharingapp.spliteasy.dto;

import java.util.List;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupResponse {
    private Long id;
    private String name;
    private List<UserResponse> members;
}
