package com.spartanmarketplace.api.controller;

import com.spartanmarketplace.api.dto.ReviewDto;
import com.spartanmarketplace.api.dto.TransactionDto;
import com.spartanmarketplace.api.service.ReviewService;
import com.spartanmarketplace.api.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService service;
    private final ReviewService reviews;

    @GetMapping("/transactions/{id}")
    public TransactionDto.Response get(@PathVariable UUID id, @RequestParam UUID actorId) {
        return service.get(id, actorId);
    }

    @GetMapping("/users/{userId}/transactions")
    public List<TransactionDto.Response> forUser(@PathVariable UUID userId) {
        return service.forUser(userId);
    }

    @PostMapping("/transactions/{id}/meetup")
    public TransactionDto.Response meetup(@PathVariable UUID id, @Valid @RequestBody TransactionDto.MeetupRequest req) {
        return service.chooseMeetup(id, req);
    }

    @PostMapping("/transactions/{id}/confirm-intent")
    public TransactionDto.Response confirmIntent(@PathVariable UUID id,
                                                 @Valid @RequestBody TransactionDto.ActorRequest req) {
        return service.confirmIntent(id, req.actorId());
    }

    @PostMapping("/transactions/{id}/confirm-sale")
    public TransactionDto.Response confirmSale(@PathVariable UUID id,
                                               @Valid @RequestBody TransactionDto.ActorRequest req) {
        return service.confirmSale(id, req.actorId());
    }

    @PostMapping("/transactions/{id}/cancel")
    public TransactionDto.Response cancel(@PathVariable UUID id, @Valid @RequestBody TransactionDto.ActorRequest req) {
        return service.cancel(id, req.actorId());
    }

    @PostMapping("/transactions/{id}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewDto.Response review(@PathVariable UUID id, @Valid @RequestBody ReviewDto.Request req) {
        return reviews.create(id, req);
    }
}
