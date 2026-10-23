package com.spartanmarketplace.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

// two-stage lifecycle for both products and listings; exactly one of product/listing is set
@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
public class Transaction {

    public enum Status { ACTIVE, COMPLETED, CANCELLED, EXPIRED }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id")
    private Listing listing;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    // null = "Other"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meetup_location_id")
    private MeetupLocation meetupLocation;

    // set only for "Other"
    private Instant liabilityAcknowledgedAt;

    private Instant buyerIntentConfirmedAt;
    private Instant sellerIntentConfirmedAt;
    private Instant buyerSaleConfirmedAt;
    private Instant sellerSaleConfirmedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
