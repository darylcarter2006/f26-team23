package com.spartanmarketplace.api.repository;

import com.spartanmarketplace.api.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByBuyerIdOrSellerIdOrderByCreatedAtDesc(UUID buyerId, UUID sellerId);

    List<Transaction> findByProductVendorIdOrderByCreatedAtDesc(UUID vendorId);

    long countByProductVendorIdAndStatus(UUID vendorId, Transaction.Status status);

    @Query("select t.product.vendor.businessName, t.product.vendor.id, count(t) from Transaction t "
            + "where t.product is not null group by t.product.vendor.businessName, t.product.vendor.id "
            + "order by count(t) desc")
    List<Object[]> transactionsPerVendor();
}
