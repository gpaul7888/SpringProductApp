package com.company.project.auth.user;

import java.time.Instant;
import java.util.UUID;

/** Public view of a user - never exposes the password hash. */
public record UserResponse(UUID id, String email, String fullName, Role role, Instant createdAt) {

    public static UserResponse from(AppUser user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRole(), user.getCreatedAt());
    }
}
