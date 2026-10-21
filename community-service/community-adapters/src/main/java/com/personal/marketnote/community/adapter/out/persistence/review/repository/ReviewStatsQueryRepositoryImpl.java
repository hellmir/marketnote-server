package com.personal.marketnote.community.adapter.out.persistence.review.repository;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.adapter.out.persistence.review.entity.QReviewJpaEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReviewStatsQueryRepositoryImpl implements ReviewStatsQueryRepository {

    private static final QReviewJpaEntity review = QReviewJpaEntity.reviewJpaEntity;

    private final JPAQueryFactory queryFactory;

    @Override
    public long countByProductId(Long productId) {
        Long count = queryFactory
                .select(review.count())
                .from(review)
                .where(
                        review.productId.eq(productId),
                        review.status.eq(EntityStatus.ACTIVE)
                )
                .fetchOne();
        return FormatValidator.hasValue(count) ? count : 0L;
    }

    @Override
    public long countByProductIdAndIsPhoto(Long productId, Boolean isPhoto) {
        Long count = queryFactory
                .select(review.count())
                .from(review)
                .where(
                        review.productId.eq(productId),
                        review.isPhoto.eq(isPhoto),
                        review.status.eq(EntityStatus.ACTIVE)
                )
                .fetchOne();
        return FormatValidator.hasValue(count) ? count : 0L;
    }

    @Override
    public long countByReviewerId(Long reviewerId) {
        Long count = queryFactory
                .select(review.count())
                .from(review)
                .where(review.reviewerId.eq(reviewerId))
                .fetchOne();
        return FormatValidator.hasValue(count) ? count : 0L;
    }
}
