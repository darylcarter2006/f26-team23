package com.spartanmarketplace.api.controller;

import com.spartanmarketplace.api.dto.BuyRequestDto;
import com.spartanmarketplace.api.dto.ReviewDto;
import com.spartanmarketplace.api.dto.StatisticsDto;
import com.spartanmarketplace.api.dto.VendorDto;
import com.spartanmarketplace.api.service.BuyRequestService;
import com.spartanmarketplace.api.service.ReviewService;
import com.spartanmarketplace.api.service.StatisticsService;
import com.spartanmarketplace.api.service.VendorService;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService service;
    private final StatisticsService statistics;
    private final ReviewService reviews;
    private final BuyRequestService buyRequests;

    @GetMapping
    public List<VendorDto.Response> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public VendorDto.Response get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    public VendorDto.Response update(@PathVariable UUID id, @Valid @RequestBody VendorDto.UpdateRequest req) {
        return service.update(id, req);
    }

    @GetMapping("/{id}/team")
    public List<VendorDto.TeamMemberResponse> team(@PathVariable UUID id, @RequestParam UUID actorId) {
        return service.listTeam(id, actorId);
    }

    @PostMapping("/{id}/team")
    @ResponseStatus(HttpStatus.CREATED)
    public VendorDto.TeamMemberResponse addMember(@PathVariable UUID id,
                                                  @Valid @RequestBody VendorDto.TeamMemberRequest req) {
        return service.addMember(id, req);
    }

    @DeleteMapping("/{id}/team/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(@PathVariable UUID id, @PathVariable UUID userId, @RequestParam UUID actorId) {
        service.removeMember(id, userId, actorId);
    }

    @GetMapping("/{id}/statistics")
    public StatisticsDto.VendorStats statistics(@PathVariable UUID id, @RequestParam UUID actorId) {
        return statistics.vendor(id, actorId);
    }

    @GetMapping("/{id}/reviews")
    public List<ReviewDto.Response> reviews(@PathVariable UUID id) {
        return reviews.forVendor(id);
    }

    @GetMapping("/{id}/buy-requests")
    public List<BuyRequestDto.Response> buyRequests(@PathVariable UUID id, @RequestParam UUID actorId) {
        return buyRequests.forVendor(id, actorId);
    }

    @PutMapping("/{id}/hide")
    public Map<String, Integer> hide(@RequestHeader("X-Admin-Id") UUID adminId, @PathVariable UUID id) {
        return Map.of("productsHidden", service.hideProducts(adminId, id));
    }

    @PutMapping("/{id}/unhide")
    public Map<String, Integer> unhide(@RequestHeader("X-Admin-Id") UUID adminId, @PathVariable UUID id) {
        return Map.of("productsRestored", service.unhideProducts(adminId, id));
    }
}
