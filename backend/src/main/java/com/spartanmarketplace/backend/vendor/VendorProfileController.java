package com.spartanmarketplace.backend.vendor;

import com.spartanmarketplace.backend.vendor.dto.VendorProfileResponse;
import com.spartanmarketplace.backend.vendor.dto.VendorProfileUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Backs the Vendor Profile Page (backlog story 34). No auth/ownership check
 * yet — that depends on real login existing — so for now anyone who knows a
 * vendorId can hit these. Fine for local testing; must be locked down (a
 * vendor can only touch their own profile) before this is ever wired to the
 * real frontend.
 */
@RestController
@RequestMapping("/api/vendors")
public class VendorProfileController {

    private final VendorProfileService vendorProfileService;

    public VendorProfileController(VendorProfileService vendorProfileService) {
        this.vendorProfileService = vendorProfileService;
    }

    @GetMapping("/{vendorId}")
    public ResponseEntity<VendorProfileResponse> getProfile(@PathVariable UUID vendorId) {
        return ResponseEntity.ok(vendorProfileService.getProfile(vendorId));
    }

    @PatchMapping("/{vendorId}")
    public ResponseEntity<VendorProfileResponse> updateProfile(
            @PathVariable UUID vendorId,
            @Valid @RequestBody VendorProfileUpdateRequest request) {
        return ResponseEntity.ok(vendorProfileService.updateProfile(vendorId, request));
    }
}
