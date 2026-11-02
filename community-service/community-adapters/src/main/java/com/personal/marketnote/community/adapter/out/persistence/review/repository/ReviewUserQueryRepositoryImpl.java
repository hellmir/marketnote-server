package com.personal.marketnote.community.adapter.out.persistence.review.repository;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.community.adapter.out.persistence.review.entity.QReviewJpaEntity;
import com.personal.marketnote.community.adapter.out.persistence.review.entity.ReviewJpaEntity;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class ReviewUserQueryRepositoryImpl implements ReviewUserQueryRepository {

    private static final QReviewJpaEntity review = QReviewJpaEntity.reviewJpaEntity;

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ReviewJpaEntity> findUserReviewsByCursor(
            Long userId,
            Long cursor,
            Pageable pageable,
            String sortProperty,
            Boolean isAsc
    ) {
        return queryFactory
                .selectFrom(review)
                .where(
                        review.reviewerId.eq(userId),
                        review.status.eq(EntityStatus.ACTIVE),
                        ReviewQueryExpressions.cursorCondition(cursor, sortProperty, isAsc, review)
                )
                .orderBy(ReviewQueryExpressions.orderSpecifiers(sortProperty, isAsc, review))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public List<ReviewJpaEntity> findUserReviewsByOffset(Long reviewerId, Pageable pageable) {
        JPAQuery<ReviewJpaEntity> query = queryFactory
                .selectFrom(review)
                .where(
                        review.reviewerId.eq(reviewerId),
                        review.status.eq(EntityStatus.ACTIVE)
                );
        applyPageableSort(query, pageable);
        return query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private void applyPageableSort(JPAQuery<ReviewJpaEntity> query, Pageable pageable) {
        Sort sort = pageable.getSort();
        if (sort.isUnsorted()) {
            return;
        }
        PathBuilder<ReviewJpaEntity> pathBuilder = new PathBuilder<>(ReviewJpaEntity.class, review.getMetadata());
        List<OrderSpecifier<?>> specifiers = new ArrayList<>();
        for (Sort.Order order : sort) {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            ComparableExpressionBase<?> path = pathBuilder.getComparable(order.getProperty(), Comparable.class);
            specifiers.add(new OrderSpecifier<>(direction, path));
        }
        query.orderBy(specifiers.toArray(OrderSpecifier[]::new));
    }
}
