package com.spartanmarketplace.backend.vendor;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Mirrors the `vendors` table (database/schema.sql). Existence of a row IS
 * vendor status — there's no separate "role" flag. Rows are created by the
 * (not yet built) vendor-application-approval flow, so this repository only
 * supports reading/updating an existing vendor for now, not creating one.
 */
@Table("vendors")
public record Vendor(
        @Id UUID id,
        UUID userId,
        String businessName,
        String businessDescription,
        String[] photos,
        Instant createdAt
) {
}
