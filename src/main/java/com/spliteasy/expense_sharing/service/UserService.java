package com.spliteasy.expense_sharing.service;

import com.spliteasy.expense_sharing.config.JwtConfig;
import com.spliteasy.expense_sharing.entity.User;
import com.spliteasy.expense_sharing.exception.ApiException;
import com.spliteasy.expense_sharing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Role;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    

    /**
     * Retrieve all users (admin only)
     *
     * @return list of all users
     */
    public List<User> getAllUsers() {
        log.info("Entering getAllUsers");
        List<User> users = userRepository.findAll();
        log.info("Exiting getAllUsers, total users found={}", users.size());
        return users;
    }

    /**
     * Get a user by ID
     *
     * @param id user ID
     * @return Optional containing User if found
     */
    public Optional<User> getUserById(Long id) {
        log.info("Entering getUserById with ID={}", id);
        Optional<User> user = userRepository.findById(id);
        log.info("Exiting getUserById, user found={}", user.isPresent());
        return user;
    }

    /**
     * Get a user by email
     *
     * @param email user email
     * @return Optional containing User if found
     */
    public Optional<User> getUserByEmail(String email) {
        log.info("Entering getUserByEmail with email={}", email);
        Optional<User> user = userRepository.findByEmail(email);
        log.info("Exiting getUserByEmail, user found={}", user.isPresent());
        return user;
    }

    /**
     * Create a new user
     *
     * @param user User entity to create
     * @return saved User
     */
    @Transactional
    public User createUser(User user) {
        log.info("Entering createUser with email={}", user.getEmail());
        User savedUser = userRepository.save(user);
        log.info("Exiting createUser with ID={}", savedUser.getId());
        return savedUser;
    }

    /**
     * Update a user's role
     *
     * @param id   user ID
     * @param role new role
     * @return updated User
     */
    @Transactional
    public User updateUserRole(Long id, String role) {
        log.info("Entering updateUserRole with ID={} and role={}", id, role);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with ID={}", id);
                    return new RuntimeException("User not found with ID: " + id);
                });

        try {
            user.setRole(Role.valueOf(role.toUpperCase())); // convert input to upper case
        } catch (IllegalArgumentException e) {
            throw new ApiException("Invalid role: " + role, HttpStatus.BAD_REQUEST);
        }
        User updatedUser = userRepository.save(user);
        log.info("Exiting updateUserRole for ID={}, new role={}", updatedUser.getId(), updatedUser.getRole());
        return updatedUser;
    }
    
    
    
}
