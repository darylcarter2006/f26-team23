package com.spartanmarketplace.api.dto;

import com.spartanmarketplace.api.entity.Product;
import com.spartanmarketplace.api.entity.ProductPhoto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ProductDto {

    private ProductDto() {
    }

    // vendorId is used on create only; actorId must belong to the vendor's team
    public record Request(
            @NotNull UUID actorId,
            UUID vendorId,
            @NotBlank String title,
            @Size(max = 2000) String description,
            @NotNull @Min(0) Integer price,
            @NotNull @Min(0) Integer stockCount,
            UUID categoryId,
            List<String> photoUrls) {
    }

    public record Response(UUID id, UUID vendorId, String vendorName, String title, String description,
                           int price, int stockCount, Product.Status status, UUID categoryId,
                           String categoryName, List<String> photoUrls, Instant createdAt, Instant updatedAt) {

        public static Response from(Product p) {
            return new Response(p.getId(), p.getVendor().getId(), p.getVendor().getBusinessName(), p.getTitle(),
                    p.getDescription(), p.getPrice(), p.getStockCount(), p.getStatus(),
                    p.getCategory() == null ? null : p.getCategory().getId(),
                    p.getCategory() == null ? null : p.getCategory().getName(),
                    p.getPhotos().stream().map(ProductPhoto::getUrl).toList(), p.getCreatedAt(),
                    p.getUpdatedAt());
        }
    }
}
