package com.personal.marketnote.product.adapter.out.persistence.review.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseGeneralEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "review_aggregate_read_models",
        uniqueConstraints = @UniqueConstraint(name = "uk_review_aggregate_read_model_product_id", columnNames = "productId")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ReviewAggregateReadModelJpaEntity extends BaseGeneralEntity {

    @Column(nullable = false, unique = true)
    private Long productId;

    @Column(nullable = false)
    private Integer totalCount;

    @Column(nullable = false)
    private Float averageRating;

    public static ReviewAggregateReadModelJpaEntity of(Long productId, Integer totalCount, Float averageRating) {
        return ReviewAggregateReadModelJpaEntity.builder()
                .productId(productId)
                .totalCount(totalCount)
                .averageRating(averageRating)
                .build();
    }

    public void updateFrom(Integer totalCount, Float averageRating) {
        this.totalCount = totalCount;
        this.averageRating = averageRating;
        activate();
    }

    public void markInactive() {
        deactivate();
    }
}
