package com.spartanmarketplace.api.repository;

import com.spartanmarketplace.api.entity.Listing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface ListingRepository extends JpaRepository<Listing, UUID>, JpaSpecificationExecutor<Listing> {

    List<Listing> findByStatus(Listing.Status status);

    List<Listing> findBySellerId(UUID sellerId);

    long countByStatus(Listing.Status status);

    @Query("select c.id, c.name, count(l) from Listing l join l.category c group by c.id, c.name")
    List<Object[]> countPerCategory();
}
