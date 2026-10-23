package com.spartanmarketplace.api.controller;

import com.spartanmarketplace.api.dto.ReviewDto;
import com.spartanmarketplace.api.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService service;

    @GetMapping
    public List<ReviewDto.Response> list(@RequestHeader("X-Admin-Id") UUID adminId) {
        return service.listAll(adminId);
    }

    @GetMapping("/{id}")
    public ReviewDto.Response get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping("/{id}/replies")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewDto.Response reply(@PathVariable UUID id, @Valid @RequestBody ReviewDto.ReplyRequest req) {
        return service.reply(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestHeader("X-Admin-Id") UUID adminId, @PathVariable UUID id) {
        service.delete(adminId, id);
    }
}
