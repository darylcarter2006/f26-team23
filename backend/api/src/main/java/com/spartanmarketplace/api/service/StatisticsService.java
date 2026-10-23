package com.spartanmarketplace.api.service;

import com.spartanmarketplace.api.dto.ReportDto;
import com.spartanmarketplace.api.dto.ReviewDto;
import com.spartanmarketplace.api.dto.StatisticsDto;
import com.spartanmarketplace.api.dto.TransactionDto;
import com.spartanmarketplace.api.entity.AdminTask;
import com.spartanmarketplace.api.entity.Listing;
import com.spartanmarketplace.api.entity.Transaction;
import com.spartanmarketplace.api.entity.Vendor;
import com.spartanmarketplace.api.entity.VendorApplication;
import com.spartanmarketplace.api.repository.AdminTaskRepository;
import com.spartanmarketplace.api.repository.ListingRepository;
import com.spartanmarketplace.api.repository.MessageRepository;
import com.spartanmarketplace.api.repository.ProductRepository;
import com.spartanmarketplace.api.repository.ReportRepository;
import com.spartanmarketplace.api.repository.ReviewRepository;
import com.spartanmarketplace.api.repository.TransactionRepository;
import com.spartanmarketplace.api.repository.UserRepository;
import com.spartanmarketplace.api.repository.VendorApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

    private static final Duration WINDOW = Duration.ofDays(30);
    private static final int TOP_N = 5;

    private final UserRepository users;
    private final MessageRepository messages;
    private final ListingRepository listings;
    private final ProductRepository products;
    private final VendorApplicationRepository applications;
    private final ReportRepository reports;
    private final AdminTaskRepository adminTasks;
    private final TransactionRepository transactions;
    private final ReviewRepository reviews;
    private final VendorService vendorService;
    private final AccessService access;

    // US-37
    public StatisticsDto.Platform platform(UUID adminId) {
        access.requireAdmin(adminId);
        Instant since = Instant.now().minus(WINDOW);

        List<StatisticsDto.DayCount> signups = users.signupsPerDaySince(since).stream()
                .map(r -> new StatisticsDto.DayCount(toLocalDate(r[0]), ((Number) r[1]).longValue()))
                .toList();

        Map<String, Long> reportsByStatus = new TreeMap<>();
        for (Object[] row : reports.countByStatus()) {
            reportsByStatus.put(row[0].toString(), ((Number) row[1]).longValue());
        }

        // category activity combines vendor products and customer listings
        Map<UUID, StatisticsDto.NamedCount> categories = new HashMap<>();
        for (Object[] row : products.countPerCategory()) {
            merge(categories, row);
        }
        for (Object[] row : listings.countPerCategory()) {
            merge(categories, row);
        }

        List<StatisticsDto.NamedCount> topVendors = transactions.transactionsPerVendor().stream()
                .limit(TOP_N)
                .map(r -> new StatisticsDto.NamedCount((UUID) r[1], (String) r[0], ((Number) r[2]).longValue()))
                .toList();

        return new StatisticsDto.Platform(
                users.count(),
                messages.countActiveSendersSince(since),
                users.countByCreatedAtAfter(since),
                signups,
                listings.countByStatus(Listing.Status.PENDING_REVIEW),
                applications.countByStatus(VendorApplication.Status.PENDING),
                reportsByStatus,
                adminTasks.findByStatusOrderByCreatedAtDesc(AdminTask.Status.OPEN).size(),
                categories.values().stream()
                        .sorted(Comparator.comparingLong(StatisticsDto.NamedCount::count).reversed())
                        .limit(TOP_N).toList(),
                topVendors);
    }

    // US-26: the vendor's own analytics page
    public StatisticsDto.VendorStats vendor(UUID vendorId, UUID actorId) {
        Vendor vendor = vendorService.find(vendorId);
        access.requireTeamMember(vendor, actorId);
        return new StatisticsDto.VendorStats(
                vendorId,
                vendor.getBusinessName(),
                transactions.countByProductVendorIdAndStatus(vendorId, Transaction.Status.ACTIVE),
                transactions.countByProductVendorIdAndStatus(vendorId, Transaction.Status.COMPLETED),
                transactions.countByProductVendorIdAndStatus(vendorId, Transaction.Status.CANCELLED),
                reviews.countByVendorId(vendorId),
                reviews.averageRatingForVendor(vendorId),
                transactions.findByProductVendorIdOrderByCreatedAtDesc(vendorId).stream()
                        .map(TransactionDto.Response::from).toList(),
                reviews.findByVendorIdOrderByCreatedAtDesc(vendorId).stream()
                        .map(ReviewDto.Response::from).toList(),
                reports.historyForUser(vendor.getUser().getId()).stream()
                        .map(ReportDto.Response::from).toList());
    }

    private void merge(Map<UUID, StatisticsDto.NamedCount> target, Object[] row) {
        UUID id = (UUID) row[0];
        long count = ((Number) row[2]).longValue();
        target.merge(id, new StatisticsDto.NamedCount(id, (String) row[1], count),
                (a, b) -> new StatisticsDto.NamedCount(id, a.name(), a.count() + b.count()));
    }

    private LocalDate toLocalDate(Object value) {
        return value instanceof Date d ? d.toLocalDate() : (LocalDate) value;
    }
}
