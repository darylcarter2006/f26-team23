package com.spartanmarketplace.api.service;

import com.spartanmarketplace.api.dto.BuyRequestDto;
import com.spartanmarketplace.api.entity.BuyRequest;
import com.spartanmarketplace.api.entity.Conversation;
import com.spartanmarketplace.api.entity.Product;
import com.spartanmarketplace.api.entity.Transaction;
import com.spartanmarketplace.api.entity.User;
import com.spartanmarketplace.api.exception.ConflictException;
import com.spartanmarketplace.api.exception.NotFoundException;
import com.spartanmarketplace.api.repository.BuyRequestRepository;
import com.spartanmarketplace.api.repository.ProductRepository;
import com.spartanmarketplace.api.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

// Request to Buy: stock is held (decremented) immediately, then released if the vendor rejects or never answers
@Service
@RequiredArgsConstructor
@Transactional
public class BuyRequestService {

    private static final Duration HOLD_TIMEOUT = Duration.ofHours(24);

    private final BuyRequestRepository buyRequests;
    private final ProductRepository products;
    private final TransactionRepository transactions;
    private final ProductService productService;
    private final VendorService vendorService;
    private final ConversationService conversationService;
    private final AccessService access;

    // US-4
    public BuyRequestDto.Response create(UUID productId, BuyRequestDto.CreateRequest req) {
        User buyer = access.requireVerified(req.buyerId());
        Product product = productService.find(productId);
        if (access.isTeamMember(product.getVendor(), buyer.getId())) {
            throw new ConflictException("You cannot buy from your own vendor account");
        }
        if (product.getVendor().getUser().getStatus() != User.Status.ACTIVE) {
            throw new ConflictException("Vendor is not currently accepting requests");
        }
        int quantity = req.quantity() == null ? 1 : req.quantity();
        // atomic: fails if the product is hidden or stock is short
        if (products.holdStock(productId, quantity) == 0) {
            throw new ConflictException("Product is unavailable or has insufficient stock");
        }
        Product held = productService.find(productId);
        BuyRequest request = new BuyRequest();
        request.setProduct(held);
        request.setBuyer(buyer);
        request.setQuantity(quantity);
        request.setExpiresAt(Instant.now().plus(HOLD_TIMEOUT));
        return BuyRequestDto.Response.from(buyRequests.save(request));
    }

    @Transactional(readOnly = true)
    public List<BuyRequestDto.Response> forVendor(UUID vendorId, UUID actorId) {
        access.requireTeamMember(vendorService.find(vendorId), actorId);
        return buyRequests.findByProductVendorIdOrderByRequestedAtDesc(vendorId).stream()
                .map(BuyRequestDto.Response::from).toList();
    }

    @Transactional(readOnly = true)
    public List<BuyRequestDto.Response> forBuyer(UUID buyerId) {
        access.getUser(buyerId);
        return buyRequests.findByBuyerIdOrderByRequestedAtDesc(buyerId).stream()
                .map(BuyRequestDto.Response::from).toList();
    }

    // US-24: accepting opens in-site messaging and a transaction for the pickup
    public BuyRequestDto.Response accept(UUID id, UUID actorId) {
        BuyRequest request = findPending(id);
        Product product = request.getProduct();
        access.requireTeamMember(product.getVendor(), actorId);
        if (request.getExpiresAt() != null && request.getExpiresAt().isBefore(Instant.now())) {
            throw new ConflictException("Request has expired");
        }
        User seller = product.getVendor().getUser();
        Conversation conversation = conversationService.findOrCreate(request.getBuyer(), seller);
        Transaction transaction = new Transaction();
        transaction.setProduct(product);
        transaction.setBuyer(request.getBuyer());
        transaction.setSeller(seller);
        transaction.setConversation(conversation);
        transactions.save(transaction);
        request.setStatus(BuyRequest.Status.ACCEPTED);
        request.setRespondedAt(Instant.now());
        request.setTransaction(transaction);
        return BuyRequestDto.Response.from(request);
    }

    // US-24: rejecting releases the held stock
    public BuyRequestDto.Response reject(UUID id, UUID actorId) {
        BuyRequest request = findPending(id);
        access.requireTeamMember(request.getProduct().getVendor(), actorId);
        request.setStatus(BuyRequest.Status.REJECTED);
        request.setRespondedAt(Instant.now());
        // the status is flushed before the bulk update, which then clears the persistence context
        products.releaseStock(request.getProduct().getId(), request.getQuantity());
        return BuyRequestDto.Response.from(buyRequests.findById(id).orElseThrow());
    }

    // vendor never responded: free the held stock
    @Scheduled(fixedDelay = 300_000, initialDelay = 60_000)
    public void expireStaleRequests() {
        List<UUID> stale = buyRequests.findByStatusAndExpiresAtBefore(BuyRequest.Status.PENDING, Instant.now())
                .stream().map(BuyRequest::getId).toList();
        // reload each one: the stock release clears the persistence context
        for (UUID id : stale) {
            BuyRequest request = buyRequests.findById(id).orElseThrow();
            request.setStatus(BuyRequest.Status.EXPIRED);
            request.setRespondedAt(Instant.now());
            products.releaseStock(request.getProduct().getId(), request.getQuantity());
        }
    }

    private BuyRequest findPending(UUID id) {
        BuyRequest request = buyRequests.findById(id)
                .orElseThrow(() -> new NotFoundException("Buy request not found: " + id));
        if (request.getStatus() != BuyRequest.Status.PENDING) {
            throw new ConflictException("Buy request already " + request.getStatus().name().toLowerCase());
        }
        return request;
    }
}
