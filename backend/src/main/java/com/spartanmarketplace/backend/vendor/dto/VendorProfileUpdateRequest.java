package com.spartanmarketplace.backend.vendor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Matches what the Vendor Profile Page (backlog story 34) actually edits:
 * business name, description, and storefront photos. Team/access management
 * (story 41) is a separate endpoint, not part of this DTO.
 */
public record VendorProfileUpdateRequest(
        @NotBlank
        @Size(max = 200)
        String businessName,

        @Size(max = 2000)
        String businessDescription,

        List<String> photos
) {
}
