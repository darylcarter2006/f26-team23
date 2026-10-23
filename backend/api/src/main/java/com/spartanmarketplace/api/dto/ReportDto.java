package com.spartanmarketplace.api.dto;

import com.spartanmarketplace.api.entity.Report;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public final class ReportDto {

    private ReportDto() {
    }

    // at least one of transactionId / conversationId is required
    public record Request(
            @NotNull UUID reporterId,
            UUID transactionId,
            UUID conversationId,
            @NotBlank @Size(max = 2000) String reason) {
    }

    public record StatusRequest(@NotNull Report.Status status) {
    }

    public record Response(UUID id, UUID reporterId, UUID transactionId, UUID conversationId, String reason,
                           Report.Status status, UUID reviewedBy, Instant createdAt) {

        public static Response from(Report r) {
            return new Response(r.getId(), r.getReporter().getId(),
                    r.getTransaction() == null ? null : r.getTransaction().getId(),
                    r.getConversation() == null ? null : r.getConversation().getId(), r.getReason(),
                    r.getStatus(), r.getReviewedBy() == null ? null : r.getReviewedBy().getId(),
                    r.getCreatedAt());
        }
    }
}
