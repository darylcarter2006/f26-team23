package com.spartanmarketplace.api.repository;

import com.spartanmarketplace.api.entity.VendorApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VendorApplicationRepository extends JpaRepository<VendorApplication, UUID> {

    List<VendorApplication> findByStatus(VendorApplication.Status status);

    List<VendorApplication> findByUserId(UUID userId);

    boolean existsByUserIdAndStatus(UUID userId, VendorApplication.Status status);

    long countByStatus(VendorApplication.Status status);
}
