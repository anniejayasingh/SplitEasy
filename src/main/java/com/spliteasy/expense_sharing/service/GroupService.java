package com.spliteasy.expense_sharing.service;

import com.spliteasy.expense_sharing.config.JwtConfig;
import com.spliteasy.expense_sharing.entity.Group;
import com.spliteasy.expense_sharing.entity.User;
import com.spliteasy.expense_sharing.exception.ApiException;
import com.spliteasy.expense_sharing.repository.GroupRepository;
import com.spliteasy.expense_sharing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Role;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ExpenseService expenseService;
    private final UserService userService;
    private final JwtConfig jwtConfig;

    /**
     * Create a new group
     *
     * @param group Group entity to create
     * @return saved Group
     */
    @Transactional
    public Group createGroup(Group group,String authHeader) {
        log.info("Entering createGroup with group name: {}", group.getName());
        
     // Fetch creator
        User creator = userService.getUserByEmail(jwtConfig.extractEmail(authHeader))
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Add creator as a member
        group.getMembers().add(creator);
        
        Group savedGroup = groupRepository.save(group);
        log.info("Exiting createGroup with created group ID: {}", savedGroup.getId());
        return savedGroup;
    }

    /**
     * Retrieve a group by ID
     *
     * @param groupId ID of the group
     * @return Optional containing Group if found
     */
    public Optional<Group> getGroup(Long groupId) {
        log.info("Entering getGroup with groupId: {}", groupId);
        Optional<Group> group = groupRepository.findById(groupId);
        log.info("Exiting getGroup, group found: {}", group.isPresent());
        return group;
    }

    /**
     * Add a user to a group
     *
     * @param groupId ID of the group
     * @param userId  ID of the user to add
     * @return updated Group
     */
    @Transactional
    public Group addUserToGroup(Long groupId, Long userId) {
        log.info("Entering addUserToGroup: groupId={}, userId={}", groupId, userId);

        // Fetch group and user, throw exception if not found
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Add user only if not already a member
        if (!group.getMembers().contains(user)) {
            log.debug("Adding user ID {} to group ID {}", userId, groupId);
            group.getMembers().add(user);
        } else {
            log.debug("User ID {} already a member of group ID {}", userId, groupId);
        }

        Group updatedGroup = groupRepository.save(group);
        log.info("Exiting addUserToGroup: updated group ID={}", updatedGroup.getId());
        return updatedGroup;
    }

    /**
     * Remove a user from a group
     *
     * @param groupId ID of the group
     * @param userId  ID of the user to remove
     * @return updated Group
     */
    @Transactional
    public Group removeUserFromGroup(Long groupId, Long userId) {
        log.info("Entering removeUserFromGroup: groupId={}, userId={}", groupId, userId);

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // User cannot leave group with unsettled balances
        Map<User, BigDecimal> balances = expenseService.calculateBalances(groupId);
        if (balances.get(user).compareTo(BigDecimal.ZERO) != 0) {
        	throw new ApiException("Cannot leave group with unsettled balance", HttpStatus.FORBIDDEN);
        }
        
        if (group.getMembers().remove(user)) {
            log.debug("User ID {} removed from group ID {}", userId, groupId);
        } else {
            log.debug("User ID {} was not a member of group ID {}", userId, groupId);
        }

        Group updatedGroup = groupRepository.save(group);
        log.info("Exiting removeUserFromGroup: updated group ID={}", updatedGroup.getId());
        return updatedGroup;
    }

    /**
     * Delete a group by ID
     *
     * @param groupId ID of the group to delete
     */
    @Transactional
    public void deleteGroup(Long groupId,String authHeader) {
        log.info("Entering deleteGroup with groupId={}", groupId);
        User currentUser = userService.getUserByEmail(jwtConfig.extractEmail(authHeader))
                .orElseThrow(() -> new RuntimeException("User not found"));

        Group group = getGroup(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        if (!group.getMembers().contains(currentUser)) {
        	throw new ApiException("Invalid User", HttpStatus.FORBIDDEN);
        }

        if (!group.getMembers().iterator().next().getId().equals(currentUser.getId())
                && currentUser.getRole() != Role.ADMIN) {
        	throw new ApiException("Only ADMIN can delete Group", HttpStatus.FORBIDDEN);
        }
        groupRepository.deleteById(groupId);
        log.info("Exiting deleteGroup: groupId={} deleted", groupId);
    }

    /**
     * Get all groups for a specific user
     *
     * @param userId ID of the user
     * @return List of groups the user belongs to
     */
    public List<Group> getGroupsForUser(Long userId) {
        log.info("Entering getGroupsForUser with userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Filter all groups to find the ones containing the user
        List<Group> groups = groupRepository.findAll().stream()
                .filter(group -> group.getMembers().contains(user))
                .collect(Collectors.toList());

        log.info("Exiting getGroupsForUser, total groups found={}", groups.size());
        return groups;
    }
    
    
}
