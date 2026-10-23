package com.spartanmarketplace.api.dto;

import com.spartanmarketplace.api.entity.BuyRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public final class BuyRequestDto {

    private BuyRequestDto() {
    }

    public record CreateRequest(@NotNull UUID buyerId, @Min(1) Integer quantity) {
    }

    // vendor accepting or rejecting
    public record DecisionRequest(@NotNull UUID actorId) {
    }

    public record Response(UUID id, UUID productId, String productTitle, UUID buyerId, int quantity,
                           BuyRequest.Status status, Instant requestedAt, Instant respondedAt,
                           Instant expiresAt, UUID transactionId) {

        public static Response from(BuyRequest b) {
            return new Response(b.getId(), b.getProduct().getId(), b.getProduct().getTitle(),
                    b.getBuyer().getId(), b.getQuantity(), b.getStatus(), b.getRequestedAt(),
                    b.getRespondedAt(), b.getExpiresAt(),
                    b.getTransaction() == null ? null : b.getTransaction().getId());
        }
    }
}
