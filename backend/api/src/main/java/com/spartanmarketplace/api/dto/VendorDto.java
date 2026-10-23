package com.spartanmarketplace.api.dto;

import com.spartanmarketplace.api.entity.Vendor;
import com.spartanmarketplace.api.entity.VendorApplication;
import com.spartanmarketplace.api.entity.VendorTeamMember;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class VendorDto {

    private VendorDto() {
    }

    public record ApplicationRequest(
            @NotNull UUID userId,
            @NotBlank String businessName,
            @Size(max = 2000) String businessDescription,
            @NotBlank String whatSelling,
            String licensesHeld,
            String desiredSellingLocation,
            @Min(1) Integer teamSize,
            @NotBlank String studentId) {
    }

    public record ApplicationResponse(UUID id, UUID userId, String businessName, String businessDescription,
                                      String whatSelling, String licensesHeld, String desiredSellingLocation,
                                      Integer teamSize, String studentId, VendorApplication.Status status,
                                      UUID reviewedBy, Instant reviewedAt, Instant createdAt) {

        public static ApplicationResponse from(VendorApplication a) {
            return new ApplicationResponse(a.getId(), a.getUser().getId(), a.getBusinessName(),
                    a.getBusinessDescription(), a.getWhatSelling(), a.getLicensesHeld(),
                    a.getDesiredSellingLocation(), a.getTeamSize(), a.getStudentId(), a.getStatus(),
                    a.getReviewedBy() == null ? null : a.getReviewedBy().getId(), a.getReviewedAt(),
                    a.getCreatedAt());
        }
    }

    public record UpdateRequest(
            @NotNull UUID actorId,
            @NotBlank String businessName,
            @Size(max = 2000) String businessDescription,
            List<String> photos) {
    }

    public record Response(UUID id, UUID userId, String businessName, String businessDescription,
                           List<String> photos, Instant createdAt) {

        public static Response from(Vendor v) {
            return new Response(v.getId(), v.getUser().getId(), v.getBusinessName(), v.getBusinessDescription(),
                    List.copyOf(v.getPhotos()), v.getCreatedAt());
        }
    }

    public record TeamMemberRequest(@NotNull UUID actorId, @NotNull UUID userId) {
    }

    public record TeamMemberResponse(UUID id, UUID vendorId, UUID userId, String email,
                                     VendorTeamMember.Role role, Instant addedAt) {

        public static TeamMemberResponse from(VendorTeamMember m) {
            return new TeamMemberResponse(m.getId(), m.getVendor().getId(), m.getUser().getId(),
                    m.getUser().getEmail(), m.getRole(), m.getAddedAt());
        }
    }
}
