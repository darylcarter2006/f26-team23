package com.spartanmarketplace.api.dto;

import com.spartanmarketplace.api.entity.Conversation;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public final class ConversationDto {

    private ConversationDto() {
    }

    public record Request(@NotNull UUID userId, @NotNull UUID otherUserId) {
    }

    public record Response(UUID id, UUID userAId, UUID userBId, Instant createdAt, Instant lastMessageAt) {

        public static Response from(Conversation c) {
            return new Response(c.getId(), c.getUserA().getId(), c.getUserB().getId(), c.getCreatedAt(),
                    c.getLastMessageAt());
        }
    }
}
