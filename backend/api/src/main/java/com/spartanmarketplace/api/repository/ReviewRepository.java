package com.spartanmarketplace.api.repository;

import com.spartanmarketplace.api.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findByVendorIdOrderByCreatedAtDesc(UUID vendorId);

    List<Review> findByProductIdOrderByCreatedAtDesc(UUID productId);

    List<Review> findAllByOrderByCreatedAtDesc();

    boolean existsByTransactionId(UUID transactionId);

    // review-bombing: bad reviews per vendor in a rolling window
    long countByVendorIdAndRatingLessThanEqualAndCreatedAtAfter(UUID vendorId, int rating, Instant since);

    // serial bad reviewer: bad reviews per reviewer in a rolling window
    long countByReviewerIdAndRatingLessThanEqualAndCreatedAtAfter(UUID reviewerId, int rating, Instant since);

    long countByVendorId(UUID vendorId);

    @Query("select avg(r.rating) from Review r where r.vendor.id = :vendorId")
    Double averageRatingForVendor(UUID vendorId);
}
