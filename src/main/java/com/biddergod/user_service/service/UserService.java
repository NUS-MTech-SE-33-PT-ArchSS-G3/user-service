package com.biddergod.user_service.service;

import com.biddergod.user_service.entity.User;
import com.biddergod.user_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Find user by ID
     * @param userId The user ID to search for
     * @return Optional containing the user if found
     */
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    /**
     * Find user by username
     * @param username The username to search for
     * @return Optional containing the user if found
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Find user by email
     * @param email The email to search for
     * @return Optional containing the user if found
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Get all users
     * @return List of all users
     */
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * Check if user exists by ID
     * @param userId The user ID to check
     * @return true if user exists, false otherwise
     */
    public boolean existsById(Long userId) {
        return userRepository.existsById(userId);
    }

    /**
     * Check if username is taken
     * @param username The username to check
     * @return true if username exists, false otherwise
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Check if email is taken
     * @param email The email to check
     * @return true if email exists, false otherwise
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Save or update a user
     * @param user The user to save
     * @return The saved user
     */
    public User save(User user) {
        return userRepository.save(user);
    }

    /**
     * Delete user by ID
     * @param userId The user ID to delete
     */
    public void deleteById(Long userId) {
        userRepository.deleteById(userId);
    }
}