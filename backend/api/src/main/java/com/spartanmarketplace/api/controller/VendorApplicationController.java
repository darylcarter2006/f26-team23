package com.spartanmarketplace.api.controller;

import com.spartanmarketplace.api.dto.VendorDto;
import com.spartanmarketplace.api.entity.VendorApplication;
import com.spartanmarketplace.api.service.VendorService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vendor-applications")
@RequiredArgsConstructor
public class VendorApplicationController {

    private final VendorService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VendorDto.ApplicationResponse apply(@Valid @RequestBody VendorDto.ApplicationRequest req) {
        return service.apply(req);
    }

    @GetMapping
    public List<VendorDto.ApplicationResponse> list(@RequestHeader("X-Admin-Id") UUID adminId,
                                                    @RequestParam(required = false) VendorApplication.Status status) {
        return service.listApplications(adminId, status);
    }

    @GetMapping("/{id}")
    public VendorDto.ApplicationResponse get(@RequestHeader("X-Admin-Id") UUID adminId, @PathVariable UUID id) {
        return service.getApplication(adminId, id);
    }

    @PutMapping("/{id}/approve")
    public VendorDto.ApplicationResponse approve(@RequestHeader("X-Admin-Id") UUID adminId, @PathVariable UUID id) {
        return service.decide(adminId, id, true);
    }

    @PutMapping("/{id}/reject")
    public VendorDto.ApplicationResponse reject(@RequestHeader("X-Admin-Id") UUID adminId, @PathVariable UUID id) {
        return service.decide(adminId, id, false);
    }
}
