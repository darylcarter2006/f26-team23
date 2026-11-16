package com.spartanmarketplace.backend.vendor;

import com.spartanmarketplace.backend.vendor.dto.VendorProfileResponse;
import com.spartanmarketplace.backend.vendor.dto.VendorProfileUpdateRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class VendorProfileService {

    private final VendorRepository vendorRepository;

    public VendorProfileService(VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }

    public VendorProfileResponse getProfile(UUID vendorId) {
        Vendor vendor = findOrThrow(vendorId);
        return toResponse(vendor);
    }

    public VendorProfileResponse updateProfile(UUID vendorId, VendorProfileUpdateRequest request) {
        Vendor existing = findOrThrow(vendorId);

        String[] photos = request.photos() == null
                ? existing.photos()
                : request.photos().toArray(new String[0]);

        // Vendor is a record (immutable) — build the updated version and let
        // Spring Data JDBC's save() issue an UPDATE, since existing.id() is
        // already set (a non-null @Id means "update", not "insert").
        Vendor updated = new Vendor(
                existing.id(),
                existing.userId(),
                request.businessName(),
                request.businessDescription(),
                photos,
                existing.createdAt()
        );

        Vendor saved = vendorRepository.save(updated);
        return toResponse(saved);
    }

    private Vendor findOrThrow(UUID vendorId) {
        return vendorRepository.findById(vendorId)
                .orElseThrow(() -> new VendorNotFoundException(vendorId));
    }

    private VendorProfileResponse toResponse(Vendor vendor) {
        return new VendorProfileResponse(
                vendor.id(),
                vendor.userId(),
                vendor.businessName(),
                vendor.businessDescription(),
                vendor.photos() == null ? List.of() : List.of(vendor.photos()),
                vendor.createdAt()
        );
    }
}
