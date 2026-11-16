package com.spartanmarketplace.backend.vendor.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record VendorProfileResponse(
        UUID id,
        UUID userId,
        String businessName,
        String businessDescription,
        List<String> photos,
        Instant createdAt
) {
}
