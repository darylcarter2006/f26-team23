package com.spartanmarketplace.api.service;

import com.spartanmarketplace.api.dto.ReportDto;
import com.spartanmarketplace.api.entity.Conversation;
import com.spartanmarketplace.api.entity.Report;
import com.spartanmarketplace.api.entity.Transaction;
import com.spartanmarketplace.api.entity.User;
import com.spartanmarketplace.api.exception.BadRequestException;
import com.spartanmarketplace.api.exception.ForbiddenException;
import com.spartanmarketplace.api.exception.NotFoundException;
import com.spartanmarketplace.api.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {

    private final ReportRepository reports;
    private final ConversationService conversationService;
    private final TransactionService transactionService;
    private final AccessService access;

    // US-9: a participant reports a chat or transaction
    public ReportDto.Response create(ReportDto.Request req) {
        User reporter = access.requireVerified(req.reporterId());
        if (req.transactionId() == null && req.conversationId() == null) {
            throw new BadRequestException("transactionId or conversationId is required");
        }
        Report report = new Report();
        report.setReporter(reporter);
        report.setReason(req.reason());
        if (req.conversationId() != null) {
            Conversation conversation = conversationService.find(req.conversationId());
            if (!conversation.getUserA().getId().equals(reporter.getId())
                    && !conversation.getUserB().getId().equals(reporter.getId())) {
                throw new ForbiddenException("You can only report conversations you are part of");
            }
            report.setConversation(conversation);
        }
        if (req.transactionId() != null) {
            Transaction transaction = transactionService.find(req.transactionId());
            if (!transaction.getBuyer().getId().equals(reporter.getId())
                    && !transaction.getSeller().getId().equals(reporter.getId())) {
                throw new ForbiddenException("You can only report transactions you are part of");
            }
            report.setTransaction(transaction);
        }
        return ReportDto.Response.from(reports.save(report));
    }

    // US-34: moderation queue
    @Transactional(readOnly = true)
    public List<ReportDto.Response> list(UUID adminId, Report.Status status) {
        access.requireAdmin(adminId);
        List<Report> found = status == null ? reports.findAllByOrderByCreatedAtDesc()
                : reports.findByStatusOrderByCreatedAtDesc(status);
        return found.stream().map(ReportDto.Response::from).toList();
    }

    @Transactional(readOnly = true)
    public ReportDto.Response get(UUID adminId, UUID id) {
        access.requireAdmin(adminId);
        return ReportDto.Response.from(find(id));
    }

    public ReportDto.Response updateStatus(UUID adminId, UUID id, Report.Status status) {
        User admin = access.requireAdmin(adminId);
        Report report = find(id);
        report.setStatus(status);
        report.setReviewedBy(admin);
        return ReportDto.Response.from(report);
    }

    // full report history for a user, not just isolated reports
    @Transactional(readOnly = true)
    public List<ReportDto.Response> historyForUser(UUID adminId, UUID userId) {
        access.requireAdmin(adminId);
        access.getUser(userId);
        return reports.historyForUser(userId).stream().map(ReportDto.Response::from).toList();
    }

    private Report find(UUID id) {
        return reports.findById(id).orElseThrow(() -> new NotFoundException("Report not found: " + id));
    }
}
