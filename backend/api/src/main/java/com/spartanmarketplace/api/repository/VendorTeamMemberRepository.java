package com.spartanmarketplace.api.repository;

import com.spartanmarketplace.api.entity.VendorTeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VendorTeamMemberRepository extends JpaRepository<VendorTeamMember, UUID> {

    List<VendorTeamMember> findByVendorId(UUID vendorId);

    Optional<VendorTeamMember> findByVendorIdAndUserId(UUID vendorId, UUID userId);

    boolean existsByVendorIdAndUserId(UUID vendorId, UUID userId);
}
