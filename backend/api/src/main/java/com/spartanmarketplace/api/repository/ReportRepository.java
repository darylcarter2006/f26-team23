package com.spartanmarketplace.api.repository;

import com.spartanmarketplace.api.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ReportRepository extends JpaRepository<Report, UUID> {

    List<Report> findByStatusOrderByCreatedAtDesc(Report.Status status);

    List<Report> findAllByOrderByCreatedAtDesc();

    // everything on file for a user: reports they filed plus reports about chats/transactions they are part of
    @Query("select distinct r from Report r left join r.conversation c left join r.transaction t "
            + "where r.reporter.id = :userId or c.userA.id = :userId or c.userB.id = :userId "
            + "or t.buyer.id = :userId or t.seller.id = :userId order by r.createdAt desc")
    List<Report> historyForUser(UUID userId);

    @Query("select r.status, count(r) from Report r group by r.status")
    List<Object[]> countByStatus();
}
