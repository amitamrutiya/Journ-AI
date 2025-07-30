package com.journai.server.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.journai.server.model.User;
import com.journai.server.repository.UserRepository;

@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    /**
     * Create a new user
     */
    public User createUser(String id, String email, String name, String imageUrl) {
        logger.info("Creating user with id: {}, email: {}", id, email);

        // Check if user already exists
        if (userRepository.existsById(id)) {
            logger.warn("User with id {} already exists", id);
            throw new IllegalArgumentException("User with id " + id + " already exists");
        }

        // Check if email is already taken
        Optional<User> existingUserByEmail = userRepository.findByEmail(email);
        if (existingUserByEmail.isPresent()) {
            logger.warn("User with email {} already exists", email);
            throw new IllegalArgumentException("User with email " + email + " already exists");
        }

        User user = new User(id, email, name, imageUrl);
        User savedUser = userRepository.save(user);

        logger.info("Successfully created user with id: {}", savedUser.getId());
        return savedUser;
    }

    /**
     * Update an existing user
     */
    public User updateUser(String id, String email, String name, String imageUrl) {
        logger.info("Updating user with id: {}", id);

        Optional<User> existingUserOpt = userRepository.findById(id);
        if (existingUserOpt.isEmpty()) {
            logger.error("User with id {} not found for update", id);
            throw new IllegalArgumentException("User with id " + id + " not found");
        }

        User existingUser = existingUserOpt.get();

        // Check if new email conflicts with another user
        if (email != null && !email.equals(existingUser.getEmail())) {
            Optional<User> userWithNewEmail = userRepository.findByEmail(email);
            if (userWithNewEmail.isPresent() && !userWithNewEmail.get().getId().equals(id)) {
                logger.warn("Email {} is already taken by another user", email);
                throw new IllegalArgumentException("Email " + email + " is already taken");
            }
            existingUser.setEmail(email);
        }

        // Update fields if provided
        if (name != null) {
            existingUser.setName(name);
        }
        if (imageUrl != null) {
            existingUser.setImageUrl(imageUrl);
        }

        User updatedUser = userRepository.save(existingUser);
        logger.info("Successfully updated user with id: {}", updatedUser.getId());
        return updatedUser;
    }

    /**
     * Delete a user
     */
    public void deleteUser(String id) {
        logger.info("Deleting user with id: {}", id);

        if (!userRepository.existsById(id)) {
            logger.warn("User with id {} not found for deletion", id);
            throw new IllegalArgumentException("User with id " + id + " not found");
        }

        userRepository.deleteById(id);
        logger.info("Successfully deleted user with id: {}", id);
    }

    /**
     * Find user by ID
     */
    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    /**
     * Find user by email
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Check if user exists
     */
    public boolean existsById(String id) {
        return userRepository.existsById(id);
    }
}
