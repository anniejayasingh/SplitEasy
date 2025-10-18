package com.spliteasy.expense_sharing.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.spliteasy.expense_sharing.config.JwtConfig;
import com.spliteasy.expense_sharing.entity.User;
import com.spliteasy.expense_sharing.service.UserService;
import com.spliteasy.expense_sharing.util.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Role;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Controller", description = "Operations related to users")

public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final JwtConfig jwtConfig;
    
    
    @GetMapping
    @Operation(summary = "Get all users", description = "Fetches a list of all registered users")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Users fetched successfully")
    })
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("GET /users called");
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/loginSuccess")
    public ResponseEntity<String> loginSuccess(@AuthenticationPrincipal OAuth2User principal) {
        String email = principal.getAttribute("email");
        String role = principal.getAttribute("role");
        String token = jwtUtil.generateToken(email,role); // generate JWT
        return ResponseEntity.ok(token);
    }
    
    
    
  

    /**
     * Fetch current logged-in user via OAuth2
     * If user does not exist, auto-create a new one
     */
    @GetMapping("/me")
    @Operation(summary = "Create a new user", description = "Registers a new user in the system")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<User> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        log.info("Fetching current user : {}");

        User user = userService.getUserByEmail(jwtConfig.extractEmail(authHeader))
                .orElseGet(() -> {
                    log.info("User not found, creating new user for email: {}", jwtConfig.extractEmail(authHeader));
                    User newUser = new User();
                    newUser.setEmail(jwtConfig.extractEmail(authHeader));
                    newUser.setName(jwtConfig.extractName(authHeader));
                  //  newUser.setOauthId(jwtConfig.);
                    newUser.setRole(Role.USER);
                    return userService.createUser(newUser);
                });

        return ResponseEntity.ok(user);
    }

    /**
     * Fetch user by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Fetches user details by user ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        log.info("Fetching user by ID: {}", id);
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    /**
     * Admin only: Update user role
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/role")
    public ResponseEntity<User> updateUserRole(@PathVariable Long id, @RequestParam String role) {
        log.info("Updating role for user ID {} to {}", id, role);
        User updatedUser = userService.updateUserRole(id, role);
        return ResponseEntity.ok(updatedUser);
    }
}
