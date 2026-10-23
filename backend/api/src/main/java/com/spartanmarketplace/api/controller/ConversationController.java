package com.spartanmarketplace.api.controller;

import com.spartanmarketplace.api.dto.ConversationDto;
import com.spartanmarketplace.api.dto.MessageDto;
import com.spartanmarketplace.api.service.ConversationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService service;

    // returns the existing thread if the pair already has one
    @PostMapping("/conversations")
    @ResponseStatus(HttpStatus.CREATED)
    public ConversationDto.Response open(@Valid @RequestBody ConversationDto.Request req) {
        return service.open(req);
    }

    @GetMapping("/users/{userId}/conversations")
    public List<ConversationDto.Response> forUser(@PathVariable UUID userId) {
        return service.forUser(userId);
    }

    @GetMapping("/conversations/{id}/messages")
    public List<MessageDto.Response> messages(@PathVariable UUID id, @RequestParam UUID userId) {
        return service.messages(id, userId);
    }

    @PostMapping("/conversations/{id}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageDto.Response send(@PathVariable UUID id, @Valid @RequestBody MessageDto.Request req) {
        return service.send(id, req);
    }
}
