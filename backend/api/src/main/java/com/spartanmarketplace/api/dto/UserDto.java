package com.spartanmarketplace.api.dto;

import com.spartanmarketplace.api.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public final class UserDto {

    private UserDto() {
    }

    public record Request(
            @NotBlank @Email String email,
            @Size(max = 100) String displayName,
            @Size(max = 1000) String bio) {
    }

    public record StatusRequest(@NotNull User.Status status) {
    }

    public record Response(UUID id, String email, String displayName, String bio, User.Role role,
                           User.Status status, Instant eduVerifiedAt, Instant createdAt) {

        public static Response from(User u) {
            return new Response(u.getId(), u.getEmail(), u.getDisplayName(), u.getBio(), u.getRole(),
                    u.getStatus(), u.getEduVerifiedAt(), u.getCreatedAt());
        }
    }
}
