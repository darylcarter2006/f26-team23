package com.spartanmarketplace.api.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class StatisticsDto {

    private StatisticsDto() {
    }

    public record DayCount(LocalDate date, long count) {
    }

    public record NamedCount(UUID id, String name, long count) {
    }

    public record Platform(long totalUsers, long activeUsersLast30Days, long newSignupsLast30Days,
                           List<DayCount> signupsPerDay, long pendingListings, long pendingVendorApplications,
                           Map<String, Long> reportsByStatus, long openAdminTasks,
                           List<NamedCount> mostActiveCategories, List<NamedCount> mostActiveVendors) {
    }

    public record VendorStats(UUID vendorId, String businessName, long activeTransactions,
                              long completedTransactions, long cancelledTransactions, long reviewCount,
                              Double averageRating, List<TransactionDto.Response> transactionHistory,
                              List<ReviewDto.Response> customerFeedback, List<ReportDto.Response> reportHistory) {
    }
}
