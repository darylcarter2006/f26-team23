package com.spartanmarketplace.api.dto;

import com.spartanmarketplace.api.entity.Message;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public final class MessageDto {

    private MessageDto() {
    }

    public record Request(@NotNull UUID senderId, @NotBlank @Size(max = 2000) String body) {
    }

    public record Response(UUID id, UUID conversationId, UUID senderId, String body, Instant createdAt) {

        public static Response from(Message m) {
            return new Response(m.getId(), m.getConversation().getId(), m.getSender().getId(), m.getBody(),
                    m.getCreatedAt());
        }
    }
}
