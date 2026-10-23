package com.spartanmarketplace.api.dto;

import com.spartanmarketplace.api.entity.Transaction;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public final class TransactionDto {

    private TransactionDto() {
    }

    public record PurchaseRequest(@NotNull UUID buyerId) {
    }

    // meetupLocationId null means "Other", which requires liabilityAcknowledged = true
    public record MeetupRequest(@NotNull UUID actorId, UUID meetupLocationId, boolean liabilityAcknowledged) {
    }

    public record ActorRequest(@NotNull UUID actorId) {
    }

    public record Response(UUID id, UUID productId, UUID listingId, UUID buyerId, UUID sellerId,
                           UUID conversationId, UUID meetupLocationId, Instant liabilityAcknowledgedAt,
                           Instant buyerIntentConfirmedAt, Instant sellerIntentConfirmedAt,
                           Instant buyerSaleConfirmedAt, Instant sellerSaleConfirmedAt,
                           Transaction.Status status, Instant createdAt) {

        public static Response from(Transaction t) {
            return new Response(t.getId(),
                    t.getProduct() == null ? null : t.getProduct().getId(),
                    t.getListing() == null ? null : t.getListing().getId(),
                    t.getBuyer().getId(), t.getSeller().getId(), t.getConversation().getId(),
                    t.getMeetupLocation() == null ? null : t.getMeetupLocation().getId(),
                    t.getLiabilityAcknowledgedAt(), t.getBuyerIntentConfirmedAt(),
                    t.getSellerIntentConfirmedAt(), t.getBuyerSaleConfirmedAt(), t.getSellerSaleConfirmedAt(),
                    t.getStatus(), t.getCreatedAt());
        }
    }
}
