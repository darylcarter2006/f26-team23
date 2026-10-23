package com.spartanmarketplace.api.controller;

import com.spartanmarketplace.api.dto.ListingDto;
import com.spartanmarketplace.api.dto.TransactionDto;
import com.spartanmarketplace.api.service.ListingService;
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
public class ListingController {

    private final ListingService service;
    private final TransactionService transactions;

    // sort: newest (default), priceAsc, priceDesc
    @GetMapping("/listings")
    public List<ListingDto.Response> search(@RequestParam(required = false) UUID categoryId,
                                            @RequestParam(required = false) Integer minPrice,
                                            @RequestParam(required = false) Integer maxPrice,
                                            @RequestParam(required = false) String q,
                                            @RequestParam(required = false) String sort) {
        return service.search(categoryId, minPrice, maxPrice, q, sort);
    }

    @GetMapping("/listings/pending")
    public List<ListingDto.Response> pending(@RequestHeader("X-Admin-Id") UUID adminId) {
        return service.pending(adminId);
    }

    @GetMapping("/listings/{id}")
    public ListingDto.Response get(@PathVariable UUID id) {
        return service.get(id);
    }

    @GetMapping("/users/{userId}/listings")
    public List<ListingDto.Response> bySeller(@PathVariable UUID userId) {
        return service.bySeller(userId);
    }

    @PostMapping("/listings")
    @ResponseStatus(HttpStatus.CREATED)
    public ListingDto.Response create(@Valid @RequestBody ListingDto.Request req) {
        return service.create(req);
    }

    @PutMapping("/listings/{id}")
    public ListingDto.Response update(@PathVariable UUID id, @Valid @RequestBody ListingDto.Request req) {
        return service.update(id, req);
    }

    @PutMapping("/listings/{id}/sold")
    public ListingDto.Response markSold(@PathVariable UUID id, @RequestParam UUID userId) {
        return service.markSold(id, userId);
    }

    @PutMapping("/listings/{id}/approve")
    public ListingDto.Response approve(@RequestHeader("X-Admin-Id") UUID adminId, @PathVariable UUID id) {
        return service.decide(adminId, id, true);
    }

    @PutMapping("/listings/{id}/reject")
    public ListingDto.Response reject(@RequestHeader("X-Admin-Id") UUID adminId, @PathVariable UUID id) {
        return service.decide(adminId, id, false);
    }

    @DeleteMapping("/listings/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, @RequestParam UUID userId) {
        service.delete(id, userId);
    }

    @PostMapping("/listings/{id}/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionDto.Response startPurchase(@PathVariable UUID id,
                                                 @Valid @RequestBody TransactionDto.PurchaseRequest req) {
        return transactions.startListingPurchase(id, req);
    }
}
