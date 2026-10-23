package com.spartanmarketplace.api.controller;

import com.spartanmarketplace.api.dto.BuyRequestDto;
import com.spartanmarketplace.api.service.BuyRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BuyRequestController {

    private final BuyRequestService service;

    @GetMapping("/users/{userId}/buy-requests")
    public List<BuyRequestDto.Response> forBuyer(@PathVariable UUID userId) {
        return service.forBuyer(userId);
    }

    @PutMapping("/buy-requests/{id}/accept")
    public BuyRequestDto.Response accept(@PathVariable UUID id, @Valid @RequestBody BuyRequestDto.DecisionRequest req) {
        return service.accept(id, req.actorId());
    }

    @PutMapping("/buy-requests/{id}/reject")
    public BuyRequestDto.Response reject(@PathVariable UUID id, @Valid @RequestBody BuyRequestDto.DecisionRequest req) {
        return service.reject(id, req.actorId());
    }
}
