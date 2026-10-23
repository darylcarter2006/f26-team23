package com.spartanmarketplace.api.repository;

import com.spartanmarketplace.api.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    Optional<Conversation> findByUserAIdAndUserBId(UUID userAId, UUID userBId);

    List<Conversation> findByUserAIdOrUserBIdOrderByLastMessageAtDesc(UUID userAId, UUID userBId);
}
