package com.tch.vehicle.dto;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String role;
    private boolean active;
    private String lastLoginDate;
    private String createdAt;
} 