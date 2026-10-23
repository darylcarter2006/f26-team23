package com.spartanmarketplace.api.service;

import com.spartanmarketplace.api.dto.ConversationDto;
import com.spartanmarketplace.api.dto.MessageDto;
import com.spartanmarketplace.api.entity.Conversation;
import com.spartanmarketplace.api.entity.Message;
import com.spartanmarketplace.api.entity.User;
import com.spartanmarketplace.api.exception.BadRequestException;
import com.spartanmarketplace.api.exception.ForbiddenException;
import com.spartanmarketplace.api.exception.NotFoundException;
import com.spartanmarketplace.api.repository.ConversationRepository;
import com.spartanmarketplace.api.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

// persistent 1:1 messaging thread per pair of users (US-27)
@Service
@RequiredArgsConstructor
@Transactional
public class ConversationService {

    private final ConversationRepository conversations;
    private final MessageRepository messages;
    private final AccessService access;

    public ConversationDto.Response open(ConversationDto.Request req) {
        User user = access.requireVerified(req.userId());
        User other = access.getUser(req.otherUserId());
        return ConversationDto.Response.from(findOrCreate(user, other));
    }

    @Transactional(readOnly = true)
    public List<ConversationDto.Response> forUser(UUID userId) {
        access.getUser(userId);
        return conversations.findByUserAIdOrUserBIdOrderByLastMessageAtDesc(userId, userId).stream()
                .map(ConversationDto.Response::from).toList();
    }

    @Transactional(readOnly = true)
    public List<MessageDto.Response> messages(UUID conversationId, UUID userId) {
        Conversation conversation = find(conversationId);
        requireParticipant(conversation, userId);
        return messages.findByConversationIdOrderByCreatedAtAsc(conversationId).stream()
                .map(MessageDto.Response::from).toList();
    }

    public MessageDto.Response send(UUID conversationId, MessageDto.Request req) {
        Conversation conversation = find(conversationId);
        User sender = access.requireVerified(req.senderId());
        requireParticipant(conversation, sender.getId());
        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setBody(req.body());
        messages.save(message);
        conversation.setLastMessageAt(message.getCreatedAt());
        return MessageDto.Response.from(message);
    }

    // shared with the buy-request and transaction flows
    public Conversation findOrCreate(User a, User b) {
        if (a.getId().equals(b.getId())) {
            throw new BadRequestException("Cannot start a conversation with yourself");
        }
        // canonical ordering so a pair of users never ends up with two threads
        User first = a.getId().compareTo(b.getId()) < 0 ? a : b;
        User second = first == a ? b : a;
        return conversations.findByUserAIdAndUserBId(first.getId(), second.getId()).orElseGet(() -> {
            Conversation created = new Conversation();
            created.setUserA(first);
            created.setUserB(second);
            created.setLastMessageAt(Instant.now());
            return conversations.save(created);
        });
    }

    public Conversation find(UUID id) {
        return conversations.findById(id).orElseThrow(() -> new NotFoundException("Conversation not found: " + id));
    }

    private void requireParticipant(Conversation conversation, UUID userId) {
        if (!conversation.getUserA().getId().equals(userId) && !conversation.getUserB().getId().equals(userId)) {
            throw new ForbiddenException("Not a participant in this conversation");
        }
    }
}
