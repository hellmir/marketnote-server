package com.personal.marketnote.community.adapter.out.persistence.review.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseOrderedGeneralEntity;
import com.personal.marketnote.community.domain.review.Review;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Formula;

import static com.personal.marketnote.common.utility.EntityConstant.BOOLEAN_DEFAULT_FALSE;

@Entity
@Table(name = "review")
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ReviewJpaEntity extends BaseOrderedGeneralEntity {
    @Column(name = "reviewer_id", nullable = false)
    private Long reviewerId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "price_policy_id", nullable = false)
    private Long pricePolicyId;

    @Column(name = "product_image_url", length = 2047)
    private String productImageUrl;

    @Column(name = "selected_options", length = 127)
    private String selectedOptions;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "reviewer_name", nullable = false, length = 15)
    private String reviewerName;

    @Column(name = "masked_reviewer_name", nullable = false, length = 15)
    private String maskedReviewerName;

    @Column(name = "rating", nullable = false)
    private Float rating;

    @Column(name = "content", nullable = false, length = 8191)
    private String content;

    @Column(name = "photo_yn", nullable = false)
    private Boolean isPhoto;

    @Column(name = "edited_yn", nullable = false, columnDefinition = BOOLEAN_DEFAULT_FALSE)
    private Boolean isEdited;

    @Formula("""
            (
                SELECT count(l.user_id)
                FROM likes l
                WHERE l.target_type = 'REVIEW'
                  AND l.target_id = id
                  AND l.status = 'ACTIVE'
            )
            """)
    private Integer likeCount;

    public static ReviewJpaEntity from(Review review) {
        return ReviewJpaEntity.builder()
                .reviewerId(review.getReviewerId())
                .orderId(review.getOrderId())
                .productId(review.getProductId())
                .pricePolicyId(review.getPricePolicyId())
                .productImageUrl(review.getProductImageUrl())
                .selectedOptions(review.getSelectedOptions())
                .quantity(review.getQuantity())
                .reviewerName(review.getReviewerName())
                .maskedReviewerName(review.getMaskedReviewerName())
                .rating(review.getRating())
                .content(review.getContent())
                .isPhoto(review.getIsPhoto())
                .build();
    }

    public void updateFrom(Review review) {
        updateActivation(review);
        rating = review.getRating();
        content = review.getContent();
        isPhoto = review.getIsPhoto();
        isEdited = true;
    }

    private void updateActivation(Review review) {
        if (review.isActive()) {
            activate();
            return;
        }

        if (review.isInactive()) {
            deactivate();
            return;
        }

        hide();
    }
}
