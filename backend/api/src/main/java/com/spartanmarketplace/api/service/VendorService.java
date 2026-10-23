package com.spartanmarketplace.api.service;

import com.spartanmarketplace.api.dto.VendorDto;
import com.spartanmarketplace.api.entity.Product;
import com.spartanmarketplace.api.entity.User;
import com.spartanmarketplace.api.entity.Vendor;
import com.spartanmarketplace.api.entity.VendorApplication;
import com.spartanmarketplace.api.entity.VendorTeamMember;
import com.spartanmarketplace.api.exception.BadRequestException;
import com.spartanmarketplace.api.exception.ConflictException;
import com.spartanmarketplace.api.exception.NotFoundException;
import com.spartanmarketplace.api.repository.ProductRepository;
import com.spartanmarketplace.api.repository.VendorApplicationRepository;
import com.spartanmarketplace.api.repository.VendorRepository;
import com.spartanmarketplace.api.repository.VendorTeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class VendorService {

    private final VendorRepository vendors;
    private final VendorApplicationRepository applications;
    private final VendorTeamMemberRepository team;
    private final ProductRepository products;
    private final AccessService access;

    // ---- Vendor applications (US-20, US-30) ----

    public VendorDto.ApplicationResponse apply(VendorDto.ApplicationRequest req) {
        User user = access.requireVerified(req.userId());
        if (vendors.existsByUserId(user.getId())) {
            throw new ConflictException("User is already a vendor");
        }
        if (applications.existsByUserIdAndStatus(user.getId(), VendorApplication.Status.PENDING)) {
            throw new ConflictException("A vendor application is already pending for this user");
        }
        VendorApplication app = new VendorApplication();
        app.setUser(user);
        app.setBusinessName(req.businessName());
        app.setBusinessDescription(req.businessDescription());
        app.setWhatSelling(req.whatSelling());
        app.setLicensesHeld(req.licensesHeld());
        app.setDesiredSellingLocation(req.desiredSellingLocation());
        app.setTeamSize(req.teamSize());
        app.setStudentId(req.studentId());
        return VendorDto.ApplicationResponse.from(applications.save(app));
    }

    @Transactional(readOnly = true)
    public List<VendorDto.ApplicationResponse> listApplications(UUID adminId, VendorApplication.Status status) {
        access.requireAdmin(adminId);
        List<VendorApplication> found = status == null ? applications.findAll() : applications.findByStatus(status);
        return found.stream().map(VendorDto.ApplicationResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public VendorDto.ApplicationResponse getApplication(UUID adminId, UUID id) {
        access.requireAdmin(adminId);
        return VendorDto.ApplicationResponse.from(findApplication(id));
    }

    // approving upgrades the applicant to a Vendor and makes them the team owner
    public VendorDto.ApplicationResponse decide(UUID adminId, UUID id, boolean approve) {
        User admin = access.requireAdmin(adminId);
        VendorApplication app = findApplication(id);
        if (app.getStatus() != VendorApplication.Status.PENDING) {
            throw new ConflictException("Application already " + app.getStatus().name().toLowerCase());
        }
        if (approve) {
            if (vendors.existsByUserId(app.getUser().getId())) {
                throw new ConflictException("User is already a vendor");
            }
            Vendor vendor = new Vendor();
            vendor.setUser(app.getUser());
            vendor.setBusinessName(app.getBusinessName());
            vendor.setBusinessDescription(app.getBusinessDescription());
            vendors.save(vendor);
            VendorTeamMember owner = new VendorTeamMember();
            owner.setVendor(vendor);
            owner.setUser(app.getUser());
            owner.setRole(VendorTeamMember.Role.OWNER);
            team.save(owner);
        }
        app.setStatus(approve ? VendorApplication.Status.APPROVED : VendorApplication.Status.REJECTED);
        app.setReviewedBy(admin);
        app.setReviewedAt(Instant.now());
        return VendorDto.ApplicationResponse.from(app);
    }

    // ---- Vendor profile (US-21) ----

    @Transactional(readOnly = true)
    public List<VendorDto.Response> list() {
        return vendors.findAll().stream().map(VendorDto.Response::from).toList();
    }

    @Transactional(readOnly = true)
    public VendorDto.Response get(UUID id) {
        return VendorDto.Response.from(find(id));
    }

    public VendorDto.Response update(UUID id, VendorDto.UpdateRequest req) {
        Vendor vendor = find(id);
        access.requireTeamMember(vendor, req.actorId());
        vendor.setBusinessName(req.businessName());
        vendor.setBusinessDescription(req.businessDescription());
        vendor.setPhotos(req.photos() == null ? new ArrayList<>() : new ArrayList<>(req.photos()));
        return VendorDto.Response.from(vendor);
    }

    // ---- Team access (US-22) ----

    @Transactional(readOnly = true)
    public List<VendorDto.TeamMemberResponse> listTeam(UUID vendorId, UUID actorId) {
        Vendor vendor = find(vendorId);
        access.requireTeamMember(vendor, actorId);
        return team.findByVendorId(vendorId).stream().map(VendorDto.TeamMemberResponse::from).toList();
    }

    public VendorDto.TeamMemberResponse addMember(UUID vendorId, VendorDto.TeamMemberRequest req) {
        Vendor vendor = find(vendorId);
        access.requireTeamOwner(vendor, req.actorId());
        User member = access.requireVerified(req.userId());
        if (access.isTeamMember(vendor, member.getId())) {
            throw new ConflictException("User is already on this vendor's team");
        }
        VendorTeamMember entry = new VendorTeamMember();
        entry.setVendor(vendor);
        entry.setUser(member);
        return VendorDto.TeamMemberResponse.from(team.save(entry));
    }

    public void removeMember(UUID vendorId, UUID userId, UUID actorId) {
        Vendor vendor = find(vendorId);
        access.requireTeamOwner(vendor, actorId);
        if (vendor.getUser().getId().equals(userId)) {
            throw new BadRequestException("The vendor owner cannot be removed");
        }
        VendorTeamMember entry = team.findByVendorIdAndUserId(vendorId, userId)
                .orElseThrow(() -> new NotFoundException("Team member not found: " + userId));
        team.delete(entry);
    }

    // ---- Admin moderation (US-33) ----

    public int hideProducts(UUID adminId, UUID vendorId) {
        access.requireAdmin(adminId);
        find(vendorId);
        return products.changeStatusForVendor(vendorId, Product.Status.ACTIVE, Product.Status.HIDDEN);
    }

    public int unhideProducts(UUID adminId, UUID vendorId) {
        access.requireAdmin(adminId);
        find(vendorId);
        return products.changeStatusForVendor(vendorId, Product.Status.HIDDEN, Product.Status.ACTIVE);
    }

    public Vendor find(UUID id) {
        return vendors.findById(id).orElseThrow(() -> new NotFoundException("Vendor not found: " + id));
    }

    private VendorApplication findApplication(UUID id) {
        return applications.findById(id)
                .orElseThrow(() -> new NotFoundException("Vendor application not found: " + id));
    }
}
