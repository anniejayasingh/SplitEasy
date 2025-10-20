package com.expensesharingapp.spliteasy.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.expensesharingapp.spliteasy.dto.GroupMembersRequest;
import com.expensesharingapp.spliteasy.dto.GroupRequest;
import com.expensesharingapp.spliteasy.dto.GroupResponse;
import com.expensesharingapp.spliteasy.service.GroupService;
import com.expensesharingapp.spliteasy.util.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for Group-related endpoints.
 * 
 * Exposes REST APIs for creating, fetching, and deleting groups.
 * Each endpoint logs entry and exit points for better debugging.
 * Delete operation checks the role of the user performing the action.
 */
@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
@Slf4j
public class GroupController {

    private final GroupService groupService;

    /**
     * Create a new group.
     * 
     * @param request JSON payload containing group name.
     * @return ApiResponse with created group details.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<GroupResponse>> createGroup(@RequestBody @Valid GroupRequest request) {
        log.info("Entering createGroup with request: {}", request);

        GroupResponse response = groupService.createGroup(request);

        log.info("Exiting createGroup with response: {}", response);
        return new ResponseEntity<>(ApiResponse.<GroupResponse>builder()
                .success(true)
                .message("Group created successfully")
                .data(response)
                .build(), HttpStatus.CREATED);
    }

    /**
     * Get a group by its ID.
     * 
     * @param id Group ID
     * @return ApiResponse with group details.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GroupResponse>> getGroupById(@PathVariable Long id) {
        log.info("Entering getGroupById with id: {}", id);

        GroupResponse response = groupService.getGroupById(id);

        log.info("Exiting getGroupById with response: {}", response);
        return ResponseEntity.ok(ApiResponse.<GroupResponse>builder().success(true).data(response).build());
    }

    /**
     * Get all groups.
     * 
     * @return ApiResponse with list of all groups.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<GroupResponse>>> getAllGroups() {
        log.info("Entering getAllGroups");

        List<GroupResponse> response = groupService.getAllGroups();

        log.info("Exiting getAllGroups with {} groups", response.size());
        return ResponseEntity.ok(ApiResponse.<List<GroupResponse>>builder().success(true).data(response).build());
    }

    
    /**
     * Add members to an existing group
     */
    @PostMapping("/{groupId}/members")
    public ResponseEntity<ApiResponse<Void>> addMembersToGroup(
            @PathVariable Long groupId,
            @RequestBody GroupMembersRequest request) {

        log.info("Entering addMembersToGroup for groupId: {} with members: {}", groupId, request.getMemberIds());

        groupService.addMembersToGroup(groupId, request.getMemberIds());

        log.info("Exiting addMembersToGroup with updated groupID: {}", groupId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Members added successfully")
                .build());
    }
    
    
    /**
     * Delete a group by ID.
     * Checks if the user performing the action has admin privileges.
     * 
     * @param id     Group ID
     * @param userId ID of the user performing deletion (used to check role)
     * @return ApiResponse indicating success or failure.
     */
    @DeleteMapping("/{groupId}")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(
            @PathVariable Long groupId,
            @RequestParam Long userId) {

        log.info("Entering deleteGroup with id: {} by userId: {}", groupId, userId);

        groupService.deleteGroup(groupId, userId);

        log.info("Exiting deleteGroup for id: {}", groupId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Group deleted successfully")
                .build());
    }
    
    
    
}
