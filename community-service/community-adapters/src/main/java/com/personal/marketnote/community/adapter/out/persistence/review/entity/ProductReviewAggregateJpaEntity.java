package com.personal.marketnote.community.adapter.out.persistence.review.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseEntity;
import com.personal.marketnote.community.domain.review.ProductReviewAggregate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;

@Entity
@Table(name = "product_review_aggregate")
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ProductReviewAggregateJpaEntity extends BaseEntity {
    @Id
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "total_count", nullable = false, columnDefinition = "INT DEFAULT 1")
    private Integer totalCount;

    @Column(name = "five_point_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer fivePointCount;

    @Column(name = "four_point_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer fourPointCount;

    @Column(name = "three_point_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer threePointCount;

    @Column(name = "two_point_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer twoPointCount;

    @Column(name = "one_point_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer onePointCount;

    @Column(name = "total_rating")
    private Long totalRating;

    @Column(name = "average_rating", nullable = false, precision = 3, scale = 2)
    private BigDecimal averageRating;

    public static ProductReviewAggregateJpaEntity from(ProductReviewAggregate aggregate) {
        return ProductReviewAggregateJpaEntity.builder()
                .productId(aggregate.getProductId())
                .totalCount(aggregate.getTotalCount())
                .fivePointCount(aggregate.getFivePointCount())
                .fourPointCount(aggregate.getFourPointCount())
                .threePointCount(aggregate.getThreePointCount())
                .twoPointCount(aggregate.getTwoPointCount())
                .onePointCount(aggregate.getOnePointCount())
                .totalRating(aggregate.getTotalRating())
                .averageRating(aggregate.getAverageRating())
                .build();
    }

    public void updateFrom(ProductReviewAggregate aggregate) {
        totalCount = aggregate.getTotalCount();
        fivePointCount = aggregate.getFivePointCount();
        fourPointCount = aggregate.getFourPointCount();
        threePointCount = aggregate.getThreePointCount();
        twoPointCount = aggregate.getTwoPointCount();
        onePointCount = aggregate.getOnePointCount();
        totalRating = aggregate.getTotalRating();
        averageRating = aggregate.getAverageRating();
    }
}
