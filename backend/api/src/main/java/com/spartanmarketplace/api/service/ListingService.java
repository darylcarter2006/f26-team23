package com.spartanmarketplace.api.service;

import com.spartanmarketplace.api.dto.ListingDto;
import com.spartanmarketplace.api.entity.Category;
import com.spartanmarketplace.api.entity.Listing;
import com.spartanmarketplace.api.entity.ListingPhoto;
import com.spartanmarketplace.api.entity.User;
import com.spartanmarketplace.api.exception.BadRequestException;
import com.spartanmarketplace.api.exception.ConflictException;
import com.spartanmarketplace.api.exception.ForbiddenException;
import com.spartanmarketplace.api.exception.NotFoundException;
import com.spartanmarketplace.api.repository.CategoryRepository;
import com.spartanmarketplace.api.repository.ListingRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ListingService {

    private final ListingRepository listings;
    private final CategoryRepository categories;
    private final AccessService access;

    // public browse: only live listings are visible (US-2)
    @Transactional(readOnly = true)
    public List<ListingDto.Response> search(UUID categoryId, Integer minPrice, Integer maxPrice, String q,
                                            String sort) {
        Specification<Listing> spec = (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            p.add(cb.equal(root.get("status"), Listing.Status.LIVE));
            if (categoryId != null) {
                p.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (minPrice != null) {
                p.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                p.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim().toLowerCase() + "%";
                p.add(cb.or(cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(cb.coalesce(root.<String>get("description"), "")), like)));
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
        return listings.findAll(spec, sortFor(sort)).stream().map(ListingDto.Response::from).toList();
    }

    @Transactional(readOnly = true)
    public ListingDto.Response get(UUID id) {
        return ListingDto.Response.from(find(id));
    }

    @Transactional(readOnly = true)
    public List<ListingDto.Response> bySeller(UUID sellerId) {
        access.getUser(sellerId);
        return listings.findBySellerId(sellerId).stream().map(ListingDto.Response::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ListingDto.Response> pending(UUID adminId) {
        access.requireAdmin(adminId);
        return listings.findByStatus(Listing.Status.PENDING_REVIEW).stream().map(ListingDto.Response::from).toList();
    }

    // new listings wait in the admin review queue (US-5)
    public ListingDto.Response create(ListingDto.Request req) {
        User seller = access.requireVerified(req.sellerId());
        Listing listing = new Listing();
        listing.setSeller(seller);
        apply(listing, req);
        return ListingDto.Response.from(listings.save(listing));
    }

    // an edit sends the listing back through review
    public ListingDto.Response update(UUID id, ListingDto.Request req) {
        Listing listing = find(id);
        access.requireActive(req.sellerId());
        requireSeller(listing, req.sellerId());
        if (listing.getStatus() == Listing.Status.SOLD) {
            throw new ConflictException("A sold listing cannot be edited");
        }
        apply(listing, req);
        listing.setStatus(Listing.Status.PENDING_REVIEW);
        return ListingDto.Response.from(listing);
    }

    // US-6
    public ListingDto.Response markSold(UUID id, UUID userId) {
        Listing listing = find(id);
        access.requireActive(userId);
        requireSeller(listing, userId);
        if (listing.getStatus() != Listing.Status.LIVE) {
            throw new ConflictException("Only live listings can be marked sold");
        }
        listing.setStatus(Listing.Status.SOLD);
        return ListingDto.Response.from(listing);
    }

    // US-32
    public ListingDto.Response decide(UUID adminId, UUID id, boolean approve) {
        User admin = access.requireAdmin(adminId);
        Listing listing = find(id);
        if (listing.getStatus() != Listing.Status.PENDING_REVIEW) {
            throw new ConflictException("Listing is not awaiting review");
        }
        listing.setStatus(approve ? Listing.Status.LIVE : Listing.Status.REJECTED);
        listing.setReviewedBy(admin);
        return ListingDto.Response.from(listing);
    }

    // seller or admin
    public void delete(UUID id, UUID userId) {
        Listing listing = find(id);
        User user = access.getUser(userId);
        if (user.getRole() != User.Role.ADMIN) {
            access.requireActive(userId);
            requireSeller(listing, userId);
        }
        try {
            listings.delete(listing);
            listings.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Listing has transactions on file and cannot be deleted");
        }
    }

    public Listing find(UUID id) {
        return listings.findById(id).orElseThrow(() -> new NotFoundException("Listing not found: " + id));
    }

    private void requireSeller(Listing listing, UUID userId) {
        if (!listing.getSeller().getId().equals(userId)) {
            throw new ForbiddenException("Only the seller can do this");
        }
    }

    private void apply(Listing listing, ListingDto.Request req) {
        listing.setTitle(req.title());
        listing.setDescription(req.description());
        listing.setPrice(req.price());
        Category category = req.categoryId() == null ? null : categories.findById(req.categoryId())
                .orElseThrow(() -> new NotFoundException("Category not found: " + req.categoryId()));
        listing.setCategory(category);
        if (req.photoUrls() != null) {
            listing.getPhotos().clear();
            int order = 0;
            for (String url : req.photoUrls()) {
                ListingPhoto photo = new ListingPhoto();
                photo.setListing(listing);
                photo.setUrl(url);
                photo.setSortOrder(order++);
                listing.getPhotos().add(photo);
            }
        }
    }

    private Sort sortFor(String sort) {
        if (sort == null) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        return switch (sort) {
            case "priceAsc" -> Sort.by(Sort.Direction.ASC, "price");
            case "priceDesc" -> Sort.by(Sort.Direction.DESC, "price");
            case "newest" -> Sort.by(Sort.Direction.DESC, "createdAt");
            default -> throw new BadRequestException("sort must be newest, priceAsc or priceDesc");
        };
    }
}
