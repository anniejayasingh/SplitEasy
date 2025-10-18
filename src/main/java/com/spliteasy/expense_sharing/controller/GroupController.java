package com.spliteasy.expense_sharing.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spliteasy.expense_sharing.entity.Group;
import com.spliteasy.expense_sharing.entity.User;
import com.spliteasy.expense_sharing.service.ExpenseService;
import com.spliteasy.expense_sharing.service.GroupService;
import com.spliteasy.expense_sharing.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Role;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Group Controller", description = "Operations related to groups")

public class GroupController {

    private final GroupService groupService;
    private final UserService userService;

    /**
     * Create a new group
     */
    @PostMapping
    @Operation(summary = "Create a new group", description = "Creates a new expense sharing group")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Group created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<Group> createGroup(@RequestBody Group group,@RequestHeader("Authorization") String authHeader) {
        log.info("Entering createGroup with group name: {}", group.getName());

        Group savedGroup = groupService.createGroup(group,authHeader);
        
        log.info("Exiting createGroup with group ID={}", savedGroup.getId());
        return new ResponseEntity<>(savedGroup, HttpStatus.CREATED);
    }

    /**
     * Add a user to a group
     */
    @PostMapping("/{groupId}/users/{userId}")
    @Operation(summary = "Add user to group", description = "Adds a user to an existing group")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User added to group successfully"),
        @ApiResponse(responseCode = "404", description = "Group or user not found")
    })
    public ResponseEntity<Group> addUserToGroup(@PathVariable Long groupId, @PathVariable Long userId) {
        log.info("Entering addUserToGroup: groupId={}, userId={}", groupId, userId);
        
        Group updatedGroup = groupService.addUserToGroup(groupId, userId);
        
        log.info("Exiting addUserToGroup: updated group ID={}", updatedGroup.getId());
        
        return new ResponseEntity<>(updatedGroup, HttpStatus.CREATED);
    }

    /**
     * Remove a user from a group
     */
    @DeleteMapping("/{groupId}/users/{userId}")
    @Operation(summary = "Remove user from group", description = "Removes a user from a group")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User removed from group successfully"),
        @ApiResponse(responseCode = "404", description = "Group or user not found")
    })
    public ResponseEntity<Void> removeUserFromGroup(@PathVariable Long groupId, @PathVariable Long userId) {
        log.info("Entering removeUserFromGroup: groupId={}, userId={}", groupId, userId);
        
        groupService.removeUserFromGroup(groupId, userId);
 
        log.info("Exiting removeUserFromGroup for groupId={}, userId={}", groupId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get a group by ID
     */
    @GetMapping("/{groupId}")
    public ResponseEntity<Group> getGroup(@PathVariable Long groupId) {
        log.info("Entering getGroup with groupId={}", groupId);
        return groupService.getGroup(groupId)
                .map(group -> {
                    log.info("Exiting getGroup, group found ID={}", group.getId());
                    return ResponseEntity.ok(group);
                })
                .orElseGet(() -> {
                    log.warn("Group not found ID={}", groupId);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                });
    }

    /**
     * Get all groups for a specific user
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get groups for a user", description = "Fetches all groups a specific user belongs to")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Groups fetched successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<List<Group>> getGroupsForUser(@PathVariable Long userId) {
        log.info("Entering getGroupsForUser with userId={}", userId);
        List<Group> groups = groupService.getGroupsForUser(userId);
        if (groups.isEmpty()) {
            log.info("No groups found for userId={}", userId);
            return ResponseEntity.noContent().build();
        }
        log.info("Exiting getGroupsForUser, total groups={}", groups.size());
        return ResponseEntity.ok(groups);
    }

    /**
     * Delete a group
     */
    @DeleteMapping("/{groupId}")
    @Operation(summary = "Delete group", description = "Deletes a group by its ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Group deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    public ResponseEntity<Void> deleteGroup(@PathVariable Long groupId,@RequestHeader("Authorization") String authHeader) {
        log.info("Entering deleteGroup with groupId={}", groupId);
        
       
        groupService.deleteGroup(groupId,authHeader);
        
        log.info("Exiting deleteGroup, groupId={} deleted", groupId);
        return ResponseEntity.noContent().build();
    }
    
}
