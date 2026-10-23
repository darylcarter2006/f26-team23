package com.spartanmarketplace.api.repository;

import com.spartanmarketplace.api.entity.BuyRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BuyRequestRepository extends JpaRepository<BuyRequest, UUID> {

    List<BuyRequest> findByProductVendorIdOrderByRequestedAtDesc(UUID vendorId);

    List<BuyRequest> findByBuyerIdOrderByRequestedAtDesc(UUID buyerId);

    List<BuyRequest> findByStatusAndExpiresAtBefore(BuyRequest.Status status, Instant cutoff);

    Optional<BuyRequest> findByTransactionId(UUID transactionId);
}
