package com.expensesharingapp.spliteasy.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.expensesharingapp.spliteasy.dto.UserDashboardResponse;
import com.expensesharingapp.spliteasy.dto.UserRequest;
import com.expensesharingapp.spliteasy.dto.UserResponse;
import com.expensesharingapp.spliteasy.service.UserService;
import com.expensesharingapp.spliteasy.util.ApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for User-related endpoints
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

	private final UserService userService;
	
	
	@GetMapping("/dashboard/{userId}")
	public ResponseEntity<ApiResponse<UserDashboardResponse>> getUserDashboard(@PathVariable Long userId) {
	    log.info("Fetching dashboard for user: {}", userId);
	    
	    UserDashboardResponse dashboard = userService.getDashboardForUser(userId);
	    
	    return ResponseEntity.ok(ApiResponse.<UserDashboardResponse>builder()
	            .success(true)
	            .data(dashboard)
	            .build());
	}

	/**
	 * Create a new user
	 */
	@PostMapping("/addUser")
	public ResponseEntity<ApiResponse<UserResponse>> createUser(@RequestBody UserRequest userRequest) {
		log.info("Entering createUser with request: {}", userRequest);
		
		UserResponse createdUser = userService.createUser(userRequest);
		
		log.info("Exiting createUser with response: {}", createdUser);
		return new ResponseEntity<>(ApiResponse.<UserResponse>builder().success(true)
				.message("User created successfully").data(createdUser).build(), HttpStatus.CREATED);
	}

	/**
	 * Get user by ID
	 */
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
		log.info("Entering getUserById with id: {}", id);
		
		UserResponse user = userService.getUserById(id);
		
		log.info("Exiting getUserById with response: {}", user);
		return ResponseEntity.ok(ApiResponse.<UserResponse>builder().success(true).data(user).build());
	}

	/**
	 * Get all users
	 */
	@GetMapping
	public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
		log.info("Entering getAllUsers");
		
		List<UserResponse> users = userService.getAllUsers();
		
		log.info("Exiting getAllUsers with {} users", users.size());
		return ResponseEntity.ok(ApiResponse.<List<UserResponse>>builder().success(true).data(users).build());
	}

	/**
	 * Delete user by ID
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
		log.info("Entering deleteUser with id: {}", id);
		
		userService.deleteUser(id);
		
		log.info("Exiting deleteUser for id: {}", id);
		return ResponseEntity
				.ok(ApiResponse.<Void>builder().success(true).message("User deleted successfully").build());
	}
}
