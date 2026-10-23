package com.spartanmarketplace.api.repository;

import com.spartanmarketplace.api.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    List<Product> findByVendorId(UUID vendorId);

    // atomic stock hold: only succeeds while enough stock remains, so concurrent requests cannot double-hold
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Product p set p.stockCount = p.stockCount - :qty "
            + "where p.id = :id and p.status = com.spartanmarketplace.api.entity.Product.Status.ACTIVE "
            + "and p.stockCount >= :qty")
    int holdStock(@Param("id") UUID id, @Param("qty") int qty);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Product p set p.stockCount = p.stockCount + :qty where p.id = :id")
    int releaseStock(@Param("id") UUID id, @Param("qty") int qty);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Product p set p.status = :to where p.vendor.id = :vendorId and p.status = :from")
    int changeStatusForVendor(@Param("vendorId") UUID vendorId, @Param("from") Product.Status from,
                              @Param("to") Product.Status to);

    @Query("select c.id, c.name, count(p) from Product p join p.category c group by c.id, c.name")
    List<Object[]> countPerCategory();
}
