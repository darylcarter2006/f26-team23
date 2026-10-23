package com.spartanmarketplace.api.service;

import com.spartanmarketplace.api.dto.ReviewDto;
import com.spartanmarketplace.api.entity.AdminTask;
import com.spartanmarketplace.api.entity.Review;
import com.spartanmarketplace.api.entity.ReviewReply;
import com.spartanmarketplace.api.entity.Transaction;
import com.spartanmarketplace.api.entity.User;
import com.spartanmarketplace.api.exception.ConflictException;
import com.spartanmarketplace.api.exception.ForbiddenException;
import com.spartanmarketplace.api.exception.NotFoundException;
import com.spartanmarketplace.api.repository.AdminTaskRepository;
import com.spartanmarketplace.api.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private static final int BAD_RATING_MAX = 2;
    private static final long REVIEW_BOMBING_LIMIT = 10;
    private static final Duration REVIEW_BOMBING_WINDOW = Duration.ofHours(24);
    private static final long SERIAL_REVIEWER_LIMIT = 3;
    private static final Duration SERIAL_REVIEWER_WINDOW = Duration.ofDays(7);

    private final ReviewRepository reviews;
    private final AdminTaskRepository adminTasks;
    private final TransactionService transactionService;
    private final ProductService productService;
    private final VendorService vendorService;
    private final AccessService access;

    // US-7: only the buyer of a completed vendor-product transaction can review it
    public ReviewDto.Response create(UUID transactionId, ReviewDto.Request req) {
        User reviewer = access.requireVerified(req.reviewerId());
        Transaction transaction = transactionService.find(transactionId);
        if (!transaction.getBuyer().getId().equals(reviewer.getId())) {
            throw new ForbiddenException("Only the buyer can review this transaction");
        }
        if (transaction.getProduct() == null) {
            throw new ConflictException("Customer listings are not reviewable");
        }
        if (transaction.getStatus() != Transaction.Status.COMPLETED) {
            throw new ConflictException("Reviews unlock once both sides confirm the sale");
        }
        if (reviews.existsByTransactionId(transactionId)) {
            throw new ConflictException("This transaction has already been reviewed");
        }
        Review review = new Review();
        review.setTransaction(transaction);
        review.setReviewer(reviewer);
        review.setProduct(transaction.getProduct());
        review.setVendor(transaction.getProduct().getVendor());
        review.setRating(req.rating());
        review.setComment(req.comment());
        reviews.save(review);
        flagAbusePatterns(review);
        return ReviewDto.Response.from(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewDto.Response> listAll(UUID adminId) {
        access.requireAdmin(adminId);
        return reviews.findAllByOrderByCreatedAtDesc().stream().map(ReviewDto.Response::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewDto.Response> forVendor(UUID vendorId) {
        vendorService.find(vendorId);
        return reviews.findByVendorIdOrderByCreatedAtDesc(vendorId).stream().map(ReviewDto.Response::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewDto.Response> forProduct(UUID productId) {
        productService.find(productId);
        return reviews.findByProductIdOrderByCreatedAtDesc(productId).stream().map(ReviewDto.Response::from).toList();
    }

    @Transactional(readOnly = true)
    public ReviewDto.Response get(UUID id) {
        return ReviewDto.Response.from(find(id));
    }

    // US-26 gap: a reply directly under a review, written by the vendor's team
    public ReviewDto.Response reply(UUID reviewId, ReviewDto.ReplyRequest req) {
        Review review = find(reviewId);
        User author = access.requireTeamMember(review.getVendor(), req.authorId());
        if (review.getReply() != null) {
            throw new ConflictException("This review already has a reply");
        }
        ReviewReply reply = new ReviewReply();
        reply.setReview(review);
        reply.setAuthor(author);
        reply.setBody(req.body());
        review.setReply(reply);
        reviews.flush();
        return ReviewDto.Response.from(review);
    }

    // US-34 moderation
    public void delete(UUID adminId, UUID id) {
        access.requireAdmin(adminId);
        reviews.delete(find(id));
    }

    private Review find(UUID id) {
        return reviews.findById(id).orElseThrow(() -> new NotFoundException("Review not found: " + id));
    }

    // US-35 / US-36: more than 10 bad reviews on a vendor in 24h, or more than 3 from one user in 7 days
    private void flagAbusePatterns(Review review) {
        if (review.getRating() > BAD_RATING_MAX) {
            return;
        }
        UUID vendorId = review.getVendor().getId();
        long vendorBad = reviews.countByVendorIdAndRatingLessThanEqualAndCreatedAtAfter(
                vendorId, BAD_RATING_MAX, Instant.now().minus(REVIEW_BOMBING_WINDOW));
        if (vendorBad > REVIEW_BOMBING_LIMIT && !adminTasks.existsByTypeAndVendorIdAndStatus(
                AdminTask.Type.REVIEW_BOMBING, vendorId, AdminTask.Status.OPEN)) {
            AdminTask task = new AdminTask();
            task.setType(AdminTask.Type.REVIEW_BOMBING);
            task.setVendor(review.getVendor());
            task.setNote(vendorBad + " bad reviews in the last 24 hours");
            adminTasks.save(task);
        }
        UUID reviewerId = review.getReviewer().getId();
        long reviewerBad = reviews.countByReviewerIdAndRatingLessThanEqualAndCreatedAtAfter(
                reviewerId, BAD_RATING_MAX, Instant.now().minus(SERIAL_REVIEWER_WINDOW));
        if (reviewerBad > SERIAL_REVIEWER_LIMIT && !adminTasks.existsByTypeAndUserIdAndStatus(
                AdminTask.Type.SERIAL_BAD_REVIEWER, reviewerId, AdminTask.Status.OPEN)) {
            AdminTask task = new AdminTask();
            task.setType(AdminTask.Type.SERIAL_BAD_REVIEWER);
            task.setUser(review.getReviewer());
            task.setNote(reviewerBad + " bad reviews in the last 7 days");
            adminTasks.save(task);
        }
    }
}
