package com.personal.marketnote.community.adapter.out.persistence.review.repository;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.adapter.out.persistence.like.entity.QLikeJpaEntity;
import com.personal.marketnote.community.adapter.out.persistence.review.entity.QReviewJpaEntity;
import com.personal.marketnote.community.domain.like.LikeTargetType;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;

final class ReviewQueryExpressions {

    static final String SORT_ID = "id";
    static final String SORT_ORDER_NUM = "orderNum";
    static final String SORT_LIKE_COUNT = "likeCount";
    static final String SORT_RATING = "rating";

    private ReviewQueryExpressions() {
    }

    static JPQLQuery<Long> likeCountSubQuery(QReviewJpaEntity review) {
        QLikeJpaEntity like = new QLikeJpaEntity("likeCountSub");
        return JPAExpressions.select(like.count())
                .from(like)
                .where(
                        like.id.targetType.eq(LikeTargetType.REVIEW),
                        like.id.targetId.eq(review.id)
                );
    }

    static JPQLQuery<Long> likeCountSubQueryForId(Long reviewId) {
        QLikeJpaEntity like = new QLikeJpaEntity("likeCountForId");
        return JPAExpressions.select(like.count())
                .from(like)
                .where(
                        like.id.targetType.eq(LikeTargetType.REVIEW),
                        like.id.targetId.eq(reviewId)
                );
    }

    static JPQLQuery<Long> orderNumSubQueryForId(Long reviewId) {
        QReviewJpaEntity cursorReview = new QReviewJpaEntity("cursorOrderNum");
        return JPAExpressions.select(cursorReview.orderNum)
                .from(cursorReview)
                .where(cursorReview.id.eq(reviewId));
    }

    static JPQLQuery<Float> ratingSubQueryForId(Long reviewId) {
        QReviewJpaEntity cursorReview = new QReviewJpaEntity("cursorRating");
        return JPAExpressions.select(cursorReview.rating)
                .from(cursorReview)
                .where(cursorReview.id.eq(reviewId));
    }

    static OrderSpecifier<?>[] orderSpecifiers(
            String sortProperty, Boolean isAsc, QReviewJpaEntity review
    ) {
        boolean ascending = Boolean.TRUE.equals(isAsc);
        if (SORT_ORDER_NUM.equals(sortProperty)) {
            return ascending
                    ? new OrderSpecifier<?>[]{review.orderNum.asc(), review.id.asc()}
                    : new OrderSpecifier<?>[]{review.orderNum.desc(), review.id.desc()};
        }
        if (SORT_LIKE_COUNT.equals(sortProperty)) {
            NumberExpression<Long> likeCount = wrapAsNumber(likeCountSubQuery(review));
            return ascending
                    ? new OrderSpecifier<?>[]{likeCount.asc(), review.rating.asc(), review.id.asc()}
                    : new OrderSpecifier<?>[]{likeCount.desc(), review.rating.desc(), review.id.desc()};
        }
        if (SORT_RATING.equals(sortProperty)) {
            return ascending
                    ? new OrderSpecifier<?>[]{review.rating.asc(), review.id.desc()}
                    : new OrderSpecifier<?>[]{review.rating.desc(), review.id.desc()};
        }
        return ascending
                ? new OrderSpecifier<?>[]{review.id.asc()}
                : new OrderSpecifier<?>[]{review.id.desc()};
    }

    static BooleanExpression cursorCondition(
            Long cursor, String sortProperty, Boolean isAsc, QReviewJpaEntity review
    ) {
        if (FormatValidator.hasNoValue(cursor)) {
            return null;
        }
        boolean ascending = Boolean.TRUE.equals(isAsc);
        if (SORT_ORDER_NUM.equals(sortProperty)) {
            return orderNumCursorCondition(cursor, ascending, review);
        }
        if (SORT_LIKE_COUNT.equals(sortProperty)) {
            return likeCountCursorCondition(cursor, ascending, review);
        }
        if (SORT_RATING.equals(sortProperty)) {
            return ratingCursorCondition(cursor, ascending, review);
        }
        return idCursorCondition(cursor, ascending, review);
    }

    private static BooleanExpression idCursorCondition(
            Long cursor, boolean ascending, QReviewJpaEntity review
    ) {
        if (ascending) {
            return review.id.gt(cursor);
        }
        return review.id.lt(cursor);
    }

    private static BooleanExpression orderNumCursorCondition(
            Long cursor, boolean ascending, QReviewJpaEntity review
    ) {
        JPQLQuery<Long> cursorOrderNum = orderNumSubQueryForId(cursor);
        if (ascending) {
            BooleanExpression greater = review.orderNum.gt(cursorOrderNum);
            BooleanExpression equalAndGreater = review.orderNum.eq(cursorOrderNum).and(review.id.gt(cursor));
            return greater.or(equalAndGreater);
        }
        BooleanExpression less = review.orderNum.lt(cursorOrderNum);
        BooleanExpression equalAndLess = review.orderNum.eq(cursorOrderNum).and(review.id.lt(cursor));
        return less.or(equalAndLess);
    }

    private static BooleanExpression likeCountCursorCondition(
            Long cursor, boolean ascending, QReviewJpaEntity review
    ) {
        NumberExpression<Long> rowLikeCount = wrapAsNumber(likeCountSubQuery(review));
        NumberExpression<Long> cursorLikeCount = wrapAsNumber(likeCountSubQueryForId(cursor));
        JPQLQuery<Float> cursorRating = ratingSubQueryForId(cursor);
        if (ascending) {
            BooleanExpression greater = rowLikeCount.gt(cursorLikeCount);
            BooleanExpression equalLikeAndGreaterRating = rowLikeCount.eq(cursorLikeCount)
                    .and(review.rating.gt(cursorRating));
            BooleanExpression equalLikeEqualRatingGreaterId = rowLikeCount.eq(cursorLikeCount)
                    .and(review.rating.eq(cursorRating))
                    .and(review.id.gt(cursor));
            return greater.or(equalLikeAndGreaterRating).or(equalLikeEqualRatingGreaterId);
        }
        BooleanExpression less = rowLikeCount.lt(cursorLikeCount);
        BooleanExpression equalLikeAndLessRating = rowLikeCount.eq(cursorLikeCount)
                .and(review.rating.lt(cursorRating));
        BooleanExpression equalLikeEqualRatingLessId = rowLikeCount.eq(cursorLikeCount)
                .and(review.rating.eq(cursorRating))
                .and(review.id.lt(cursor));
        return less.or(equalLikeAndLessRating).or(equalLikeEqualRatingLessId);
    }

    private static NumberExpression<Long> wrapAsNumber(JPQLQuery<Long> subQuery) {
        return Expressions.numberTemplate(Long.class, "({0})", subQuery);
    }

    private static BooleanExpression ratingCursorCondition(
            Long cursor, boolean ascending, QReviewJpaEntity review
    ) {
        JPQLQuery<Float> cursorRating = ratingSubQueryForId(cursor);
        if (ascending) {
            BooleanExpression greater = review.rating.gt(cursorRating);
            BooleanExpression equalAndLessId = review.rating.eq(cursorRating).and(review.id.lt(cursor));
            return greater.or(equalAndLessId);
        }
        BooleanExpression less = review.rating.lt(cursorRating);
        BooleanExpression equalAndLessId = review.rating.eq(cursorRating).and(review.id.lt(cursor));
        return less.or(equalAndLessId);
    }
}
