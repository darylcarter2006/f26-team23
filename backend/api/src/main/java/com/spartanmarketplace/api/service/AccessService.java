package com.spartanmarketplace.api.service;

import com.spartanmarketplace.api.entity.User;
import com.spartanmarketplace.api.entity.Vendor;
import com.spartanmarketplace.api.entity.VendorTeamMember;
import com.spartanmarketplace.api.exception.ForbiddenException;
import com.spartanmarketplace.api.exception.NotFoundException;
import com.spartanmarketplace.api.repository.UserRepository;
import com.spartanmarketplace.api.repository.VendorTeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

// Shared role / account-standing checks. There is no login in this milestone, so callers
// identify themselves by id (X-Admin-Id header, actorId, userId) and are checked here.
@Service
@RequiredArgsConstructor
public class AccessService {

    private final UserRepository users;
    private final VendorTeamMemberRepository team;

    public User getUser(UUID id) {
        return users.findById(id).orElseThrow(() -> new NotFoundException("User not found: " + id));
    }

    public User requireAdmin(UUID id) {
        User user = getUser(id);
        if (user.getRole() != User.Role.ADMIN || user.getStatus() != User.Status.ACTIVE) {
            throw new ForbiddenException("Admin access required");
        }
        return user;
    }

    // restricted and banned accounts cannot perform write actions
    public User requireActive(UUID id) {
        User user = getUser(id);
        if (user.getStatus() != User.Status.ACTIVE) {
            throw new ForbiddenException("Account is " + user.getStatus().name().toLowerCase());
        }
        return user;
    }

    // authenticated actions need an active, .edu-verified account
    public User requireVerified(UUID id) {
        User user = requireActive(id);
        if (user.getEduVerifiedAt() == null) {
            throw new ForbiddenException(".edu email verification required");
        }
        return user;
    }

    @Transactional(readOnly = true)
    public boolean isTeamMember(Vendor vendor, UUID userId) {
        return vendor.getUser().getId().equals(userId) || team.existsByVendorIdAndUserId(vendor.getId(), userId);
    }

    @Transactional(readOnly = true)
    public User requireTeamMember(Vendor vendor, UUID actorId) {
        User actor = requireActive(actorId);
        if (!isTeamMember(vendor, actorId)) {
            throw new ForbiddenException("Not a member of this vendor's team");
        }
        return actor;
    }

    @Transactional(readOnly = true)
    public User requireTeamOwner(Vendor vendor, UUID actorId) {
        User actor = requireActive(actorId);
        boolean owner = vendor.getUser().getId().equals(actorId)
                || team.findByVendorIdAndUserId(vendor.getId(), actorId)
                .map(m -> m.getRole() == VendorTeamMember.Role.OWNER).orElse(false);
        if (!owner) {
            throw new ForbiddenException("Vendor owner access required");
        }
        return actor;
    }
}
