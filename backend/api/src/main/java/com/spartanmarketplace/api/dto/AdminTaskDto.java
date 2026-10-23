package com.spartanmarketplace.api.dto;

import com.spartanmarketplace.api.entity.AdminTask;

import java.time.Instant;
import java.util.UUID;

public final class AdminTaskDto {

    private AdminTaskDto() {
    }

    public record Response(UUID id, AdminTask.Type type, UUID vendorId, UUID userId, String note,
                           AdminTask.Status status, Instant createdAt) {

        public static Response from(AdminTask t) {
            return new Response(t.getId(), t.getType(),
                    t.getVendor() == null ? null : t.getVendor().getId(),
                    t.getUser() == null ? null : t.getUser().getId(), t.getNote(), t.getStatus(),
                    t.getCreatedAt());
        }
    }
}
