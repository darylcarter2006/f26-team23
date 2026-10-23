package com.spartanmarketplace.api.dto;

import com.spartanmarketplace.api.entity.Review;
import com.spartanmarketplace.api.entity.ReviewReply;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public final class ReviewDto {

    private ReviewDto() {
    }

    public record Request(
            @NotNull UUID reviewerId,
            @NotNull @Min(1) @Max(5) Integer rating,
            @Size(max = 2000) String comment) {
    }

    public record ReplyRequest(@NotNull UUID authorId, @NotBlank @Size(max = 2000) String body) {
    }

    public record ReplyResponse(UUID id, UUID authorId, String body, Instant createdAt) {

        public static ReplyResponse from(ReviewReply r) {
            return new ReplyResponse(r.getId(), r.getAuthor().getId(), r.getBody(), r.getCreatedAt());
        }
    }

    public record Response(UUID id, UUID transactionId, UUID reviewerId, String reviewerName, UUID vendorId,
                           UUID productId, int rating, String comment, ReplyResponse reply, Instant createdAt) {

        public static Response from(Review r) {
            return new Response(r.getId(), r.getTransaction().getId(), r.getReviewer().getId(),
                    r.getReviewer().getDisplayName(), r.getVendor().getId(), r.getProduct().getId(),
                    r.getRating(), r.getComment(),
                    r.getReply() == null ? null : ReplyResponse.from(r.getReply()), r.getCreatedAt());
        }
    }
}
