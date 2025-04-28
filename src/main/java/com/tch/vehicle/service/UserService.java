package com.tch.vehicle.service;

import com.tch.vehicle.dto.UserUpdateRequest;
import com.tch.vehicle.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    User createUser(User user);
    User updateUser(User user);
    void deleteUser(Long id);
    Optional<User> getUserById(Long id);
    Optional<User> getUserByUsername(String username);
    List<User> getAllUsers();
    List<User> getUsersByRole(String role);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    User save(User user);
    User findByUsername(String username);
    List<User> findAll();
    void delete(Long id);
    User findById(Long id);
    User updateProfile(String username, UserUpdateRequest updateRequest);
    boolean verifyCurrentPassword(String username, String currentPassword);
} 