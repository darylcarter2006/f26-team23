package com.spartanmarketplace.backend.vendor;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface VendorRepository extends CrudRepository<Vendor, UUID> {

    Optional<Vendor> findByUserId(UUID userId);
}
