package com.expensesharingapp.spliteasy.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.expensesharingapp.spliteasy.dto.SettlementSummary;
import com.expensesharingapp.spliteasy.dto.UserDashboardResponse;
import com.expensesharingapp.spliteasy.dto.UserRequest;
import com.expensesharingapp.spliteasy.dto.UserResponse;
import com.expensesharingapp.spliteasy.entity.Settlement;
import com.expensesharingapp.spliteasy.entity.User;
import com.expensesharingapp.spliteasy.exception.ResourceNotFoundException;
import com.expensesharingapp.spliteasy.repository.SettlementRepository;
import com.expensesharingapp.spliteasy.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class handling all User operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final SettlementRepository settlementRepository;
    
    
    /**
     * Returns a minimal dashboard view for a user:
     * totals for owed and owes + optional settlements list.
     */
    public UserDashboardResponse getDashboardForUser(Long userId) {
    	    log.info("Fetching dashboard for user: {}", userId);

    	    
    	    User user = userRepository.findById(userId)
    	            .orElseThrow(() -> new ResourceNotFoundException("User not found", userId.toString(), ""));
    	    // Fetch all settlements involving this user
    	    List<Settlement> settlements = settlementRepository.findByUserId(userId);

    	    BigDecimal totalOwedByUser = BigDecimal.ZERO;
    	    BigDecimal totalOwedToUser = BigDecimal.ZERO;

    	    List<SettlementSummary> summaryList = new ArrayList<>();

    	    for (Settlement s : settlements) {
    	        SettlementSummary summary = SettlementSummary.builder()
    	                .payerId(s.getPayer().getId())
    	                .receiverId(s.getReceiver().getId())
    	                .amount(s.getAmount())
    	                .status(s.getStatus())
    	                .build();

    	        summaryList.add(summary);

    	        // Update totals
    	        if (s.getPayer().getId().equals(userId)) {
    	            totalOwedByUser = totalOwedByUser.add(s.getAmount());
    	        } else if (s.getReceiver().getId().equals(userId)) {
    	            totalOwedToUser = totalOwedToUser.add(s.getAmount());
    	        }
    	    }

    	    return UserDashboardResponse.builder()
    	            .userId(userId)
    	            .userName(user.getName())
    	            .totalOwedByUser(totalOwedByUser)
    	            .totalOwedToUser(totalOwedToUser)
    	            .settlements(summaryList)
    	            .build();
    	}



    /**
     * Create a new user
     */
    public UserResponse createUser(UserRequest userRequest) {
        log.info("Entering createUser with request: {}", userRequest);

        User user = User.builder()
                .name(userRequest.getName())
                .email(userRequest.getEmail())
                .oauthId(userRequest.getOauthId())
                .role("USER") // default role
                .build();

        User savedUser = userRepository.save(user);

        UserResponse response = mapToResponse(savedUser);
        log.info("Exiting createUser with response: {}", response);
        return response;
    }

    /**
     * Get user by ID
     */
    public UserResponse getUserById(Long id) {
        log.info("Entering getUserById with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        UserResponse response = mapToResponse(user);
        log.info("Exiting getUserById with response: {}", response);
        return response;
    }

    /**
     * Get all users
     */
    public List<UserResponse> getAllUsers() {
        log.info("Entering getAllUsers");

        List<UserResponse> users = userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        log.info("Exiting getAllUsers with {} users", users.size());
        return users;
    }

    /**
     * Delete user by ID
     */
    public void deleteUser(Long id) {
        log.info("Entering deleteUser with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        userRepository.delete(user);
        log.info("Exiting deleteUser for id: {}", id);
    }

    /**
     * Map User entity to UserResponse DTO
     */
    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .oauthId(user.getOauthId())
                .role(user.getRole())
                .build();
    }

}
