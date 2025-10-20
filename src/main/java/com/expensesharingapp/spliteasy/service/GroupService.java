package com.expensesharingapp.spliteasy.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.expensesharingapp.spliteasy.dto.GroupRequest;
import com.expensesharingapp.spliteasy.dto.GroupResponse;
import com.expensesharingapp.spliteasy.dto.UserResponse;
import com.expensesharingapp.spliteasy.entity.Group;
import com.expensesharingapp.spliteasy.entity.User;
import com.expensesharingapp.spliteasy.exception.ResourceNotFoundException;
import com.expensesharingapp.spliteasy.exception.UnAuthorizedActionException;
import com.expensesharingapp.spliteasy.repository.GroupRepository;
import com.expensesharingapp.spliteasy.repository.UserRepository;
import com.expensesharingapp.spliteasy.util.ApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class for handling Group business logic.
 * 
 * Contains methods to create, fetch, list, and delete groups.
 * All delete operations validate the role of the user performing the action.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    /**
     * Create a new group in the database.
     * 
     * @param request GroupRequest containing group name.
     * @return GroupResponse with saved group details.
     */
    public GroupResponse createGroup(GroupRequest request) {
        log.info("Creating group with name: {}", request.getName());

        Group group = Group.builder()
                .name(request.getName())
                .build();

        Group saved = groupRepository.save(group);

        log.info("Created group with id: {}", saved.getId());
        return GroupResponse.builder().id(saved.getId()).name(saved.getName()).build();
    }

    /**
     * Get a group by its ID.
     * 
     * @param id Group ID
     * @return GroupResponse with group details
     * @throws ResourceNotFoundException if group does not exist
     */
    public GroupResponse getGroupById(Long id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: ","group",id));

        return GroupResponse.builder().id(group.getId()).name(group.getName()).build();
    }

    /**
     * Get all groups in the database.
     * 
     * @return List of GroupResponse
     */
    public List<GroupResponse> getAllGroups() {
        return groupRepository.findAll().stream()
            .map(group -> {
                List<UserResponse> memberResponses = group.getMembers().stream()
                        .map(u -> new UserResponse(u.getId(), u.getName(), u.getEmail(),u.getOauthId(), u.getRole()))
                        .toList();
                return GroupResponse.builder()
                        .id(group.getId())
                        .name(group.getName())
                        .members(memberResponses)
                        .build();
            }).toList();
    }
    
    /**
     * Add members to a group by groupId and list of userIds
     */
    public void addMembersToGroup(Long groupId, List<Long> memberIds) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: ",groupId.toString(),memberIds));

        List<User> users = userRepository.findAllById(memberIds);
        if (users.isEmpty()) {
            throw new ResourceNotFoundException("No valid users found for given IDs",groupId.toString(),memberIds);
        }

        // Add members (avoid duplicates)
        users.forEach(user -> {
            if (!group.getMembers().contains(user)) {
                group.getMembers().add(user);
            }
        });

        groupRepository.save(group);
        log.info("Added members to group : {}");


    }


    /**
     * Delete a group by ID.
     * Checks if the user performing deletion has ADMIN role.
     * 
     * @param groupId ID of the group to delete
     * @param userId  ID of the user performing deletion
     * @throws ResourceNotFoundException     if group or user does not exist
     * @throws UnauthorizedActionException  if user is not admin
     */
    public void deleteGroup(Long groupId, Long userId) {
        // Fetch user performing the action
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found user id and group id: ", userId.toString(),groupId));

        // Check role
        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new UnAuthorizedActionException("Only admin can delete groups",userId,groupId);
        }

        // Check group existence
        if (!groupRepository.existsById(groupId)) {
            throw new ResourceNotFoundException("Group not found with id: ","groupId :", groupId);
        }

        groupRepository.deleteById(groupId);
        log.info("Deleted group with id: {}", groupId);
    }
}
