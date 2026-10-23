package com.spartanmarketplace.api.repository;

import com.spartanmarketplace.api.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    List<Message> findByConversationIdOrderByCreatedAtAsc(UUID conversationId);

    @Query("select count(distinct m.sender.id) from Message m where m.createdAt >= :since")
    long countActiveSendersSince(Instant since);
}
