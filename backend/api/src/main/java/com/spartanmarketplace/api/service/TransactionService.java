package com.spartanmarketplace.api.service;

import com.spartanmarketplace.api.dto.TransactionDto;
import com.spartanmarketplace.api.entity.BuyRequest;
import com.spartanmarketplace.api.entity.Conversation;
import com.spartanmarketplace.api.entity.Listing;
import com.spartanmarketplace.api.entity.MeetupLocation;
import com.spartanmarketplace.api.entity.Transaction;
import com.spartanmarketplace.api.entity.User;
import com.spartanmarketplace.api.exception.BadRequestException;
import com.spartanmarketplace.api.exception.ConflictException;
import com.spartanmarketplace.api.exception.ForbiddenException;
import com.spartanmarketplace.api.exception.NotFoundException;
import com.spartanmarketplace.api.repository.BuyRequestRepository;
import com.spartanmarketplace.api.repository.ProductRepository;
import com.spartanmarketplace.api.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

// two-stage lifecycle: both sides confirm the meetup, then both confirm the sale
@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {

    private final TransactionRepository transactions;
    private final BuyRequestRepository buyRequests;
    private final ProductRepository products;
    private final ListingService listingService;
    private final ConversationService conversationService;
    private final MeetupLocationService meetupLocations;
    private final AccessService access;

    @Transactional(readOnly = true)
    public TransactionDto.Response get(UUID id, UUID actorId) {
        Transaction transaction = find(id);
        requireParticipantOrAdmin(transaction, actorId);
        return TransactionDto.Response.from(transaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionDto.Response> forUser(UUID userId) {
        access.getUser(userId);
        return transactions.findByBuyerIdOrSellerIdOrderByCreatedAtDesc(userId, userId).stream()
                .map(TransactionDto.Response::from).toList();
    }

    // a buyer starts a purchase on a customer listing; vendor products go through buy requests
    public TransactionDto.Response startListingPurchase(UUID listingId, TransactionDto.PurchaseRequest req) {
        User buyer = access.requireVerified(req.buyerId());
        Listing listing = listingService.find(listingId);
        if (listing.getStatus() != Listing.Status.LIVE) {
            throw new ConflictException("Listing is not available");
        }
        if (listing.getSeller().getId().equals(buyer.getId())) {
            throw new ConflictException("You cannot buy your own listing");
        }
        Conversation conversation = conversationService.findOrCreate(buyer, listing.getSeller());
        Transaction transaction = new Transaction();
        transaction.setListing(listing);
        transaction.setBuyer(buyer);
        transaction.setSeller(listing.getSeller());
        transaction.setConversation(conversation);
        return TransactionDto.Response.from(transactions.save(transaction));
    }

    // US-8: pick an Admin-managed spot, or "Other" with a liability acknowledgment
    public TransactionDto.Response chooseMeetup(UUID id, TransactionDto.MeetupRequest req) {
        Transaction transaction = findActive(id);
        requireParticipant(transaction, req.actorId());
        if (req.meetupLocationId() != null) {
            MeetupLocation location = meetupLocations.get(req.meetupLocationId());
            if (!location.isActive()) {
                throw new BadRequestException("Meetup location is not active");
            }
            transaction.setMeetupLocation(location);
            transaction.setLiabilityAcknowledgedAt(null);
        } else {
            if (!req.liabilityAcknowledged()) {
                throw new BadRequestException("Choosing \"Other\" requires acknowledging the liability warning");
            }
            transaction.setMeetupLocation(null);
            transaction.setLiabilityAcknowledgedAt(Instant.now());
        }
        // changing the plan resets stage-1 agreement
        transaction.setBuyerIntentConfirmedAt(null);
        transaction.setSellerIntentConfirmedAt(null);
        return TransactionDto.Response.from(transaction);
    }

    // stage 1
    public TransactionDto.Response confirmIntent(UUID id, UUID actorId) {
        Transaction transaction = findActive(id);
        boolean buyer = isBuyer(transaction, actorId);
        requireParticipant(transaction, actorId);
        if (transaction.getMeetupLocation() == null && transaction.getLiabilityAcknowledgedAt() == null) {
            throw new ConflictException("Choose a meetup location first");
        }
        if (buyer) {
            transaction.setBuyerIntentConfirmedAt(Instant.now());
        } else {
            transaction.setSellerIntentConfirmedAt(Instant.now());
        }
        return TransactionDto.Response.from(transaction);
    }

    // stage 2: when both sides confirm, the transaction completes and a sold listing comes down (US-25)
    public TransactionDto.Response confirmSale(UUID id, UUID actorId) {
        Transaction transaction = findActive(id);
        boolean buyer = isBuyer(transaction, actorId);
        requireParticipant(transaction, actorId);
        if (transaction.getBuyerIntentConfirmedAt() == null || transaction.getSellerIntentConfirmedAt() == null) {
            throw new ConflictException("Both sides must confirm the meetup before the sale");
        }
        if (buyer) {
            transaction.setBuyerSaleConfirmedAt(Instant.now());
        } else {
            transaction.setSellerSaleConfirmedAt(Instant.now());
        }
        if (transaction.getBuyerSaleConfirmedAt() != null && transaction.getSellerSaleConfirmedAt() != null) {
            transaction.setStatus(Transaction.Status.COMPLETED);
            if (transaction.getListing() != null) {
                transaction.getListing().setStatus(Listing.Status.SOLD);
            }
        }
        return TransactionDto.Response.from(transaction);
    }

    // a cancelled vendor sale puts its stock back
    public TransactionDto.Response cancel(UUID id, UUID actorId) {
        Transaction transaction = findActive(id);
        requireParticipant(transaction, actorId);
        transaction.setStatus(Transaction.Status.CANCELLED);
        buyRequests.findByTransactionId(id).ifPresent(request -> {
            UUID productId = request.getProduct().getId();
            int quantity = request.getQuantity();
            request.setStatus(BuyRequest.Status.REJECTED);
            products.releaseStock(productId, quantity);
        });
        return TransactionDto.Response.from(transactions.findById(id).orElseThrow());
    }

    public Transaction find(UUID id) {
        return transactions.findById(id).orElseThrow(() -> new NotFoundException("Transaction not found: " + id));
    }

    private Transaction findActive(UUID id) {
        Transaction transaction = find(id);
        if (transaction.getStatus() != Transaction.Status.ACTIVE) {
            throw new ConflictException("Transaction is " + transaction.getStatus().name().toLowerCase());
        }
        return transaction;
    }

    private boolean isBuyer(Transaction transaction, UUID actorId) {
        return transaction.getBuyer().getId().equals(actorId);
    }

    // the seller side includes members of the vendor's team
    private boolean isSeller(Transaction transaction, UUID actorId) {
        return transaction.getSeller().getId().equals(actorId)
                || (transaction.getProduct() != null
                && access.isTeamMember(transaction.getProduct().getVendor(), actorId));
    }

    private void requireParticipant(Transaction transaction, UUID actorId) {
        access.requireActive(actorId);
        if (!isBuyer(transaction, actorId) && !isSeller(transaction, actorId)) {
            throw new ForbiddenException("Not a participant in this transaction");
        }
    }

    private void requireParticipantOrAdmin(Transaction transaction, UUID actorId) {
        User actor = access.getUser(actorId);
        if (actor.getRole() != User.Role.ADMIN) {
            requireParticipant(transaction, actorId);
        }
    }
}
