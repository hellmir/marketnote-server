package com.personal.marketnote.community.adapter.out.persistence.review.repository;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.community.adapter.out.persistence.review.entity.QReviewJpaEntity;
import com.personal.marketnote.community.adapter.out.persistence.review.entity.ReviewJpaEntity;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
public class ReviewProductQueryRepositoryImpl implements ReviewProductQueryRepository {

    private static final QReviewJpaEntity review = QReviewJpaEntity.reviewJpaEntity;

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ReviewJpaEntity> findProductReviewsByCursor(
            Long productId,
            Long cursor,
            Pageable pageable,
            String sortProperty,
            Boolean isAsc
    ) {
        return queryFactory
                .selectFrom(review)
                .where(
                        review.productId.eq(productId),
                        review.status.eq(EntityStatus.ACTIVE),
                        ReviewQueryExpressions.cursorCondition(cursor, sortProperty, isAsc, review)
                )
                .orderBy(ReviewQueryExpressions.orderSpecifiers(sortProperty, isAsc, review))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public List<ReviewJpaEntity> findProductPhotoReviewsByCursor(
            Long productId,
            Long cursor,
            Pageable pageable,
            String sortProperty,
            Boolean isAsc
    ) {
        return queryFactory
                .selectFrom(review)
                .where(
                        review.productId.eq(productId),
                        photoOnly(),
                        review.status.eq(EntityStatus.ACTIVE),
                        ReviewQueryExpressions.cursorCondition(cursor, sortProperty, isAsc, review)
                )
                .orderBy(ReviewQueryExpressions.orderSpecifiers(sortProperty, isAsc, review))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private BooleanExpression photoOnly() {
        return review.isPhoto.isTrue();
    }
}
