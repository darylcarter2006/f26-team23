package com.spartanmarketplace.api.dto;

import com.spartanmarketplace.api.entity.Listing;
import com.spartanmarketplace.api.entity.ListingPhoto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ListingDto {

    private ListingDto() {
    }

    public record Request(
            @NotNull UUID sellerId,
            @NotBlank String title,
            @Size(max = 2000) String description,
            @NotNull @Min(0) Integer price,
            UUID categoryId,
            List<String> photoUrls) {
    }

    public record Response(UUID id, UUID sellerId, String sellerName, String title, String description,
                           int price, UUID categoryId, String categoryName, Listing.Status status,
                           List<String> photoUrls, Instant createdAt) {

        public static Response from(Listing l) {
            return new Response(l.getId(), l.getSeller().getId(), l.getSeller().getDisplayName(), l.getTitle(),
                    l.getDescription(), l.getPrice(),
                    l.getCategory() == null ? null : l.getCategory().getId(),
                    l.getCategory() == null ? null : l.getCategory().getName(), l.getStatus(),
                    l.getPhotos().stream().map(ListingPhoto::getUrl).toList(), l.getCreatedAt());
        }
    }
}
