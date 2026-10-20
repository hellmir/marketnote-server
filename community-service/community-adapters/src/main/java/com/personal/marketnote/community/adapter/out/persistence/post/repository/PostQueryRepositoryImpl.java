package com.personal.marketnote.community.adapter.out.persistence.post.repository;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.adapter.out.persistence.post.entity.PostJpaEntity;
import com.personal.marketnote.community.adapter.out.persistence.post.entity.QPostJpaEntity;
import com.personal.marketnote.community.domain.post.Board;
import com.personal.marketnote.community.domain.post.PostTargetGroupType;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class PostQueryRepositoryImpl implements PostQueryRepository {

    private static final QPostJpaEntity post = QPostJpaEntity.postJpaEntity;

    private final JPAQueryFactory queryFactory;

    @Override
    public List<PostJpaEntity> findByBoardAndFilters(
            Board board,
            String category,
            PostTargetGroupType targetGroupType,
            Long targetGroupId,
            Long cursor,
            boolean isDesc,
            Boolean isPublicOnly,
            Long filterUserId,
            boolean isAnsweredOnly,
            boolean searchInTitle,
            boolean searchInContent,
            String searchKeywordPattern,
            EntityStatus status,
            Pageable pageable
    ) {
        JPAQuery<PostJpaEntity> query = queryFactory
                .selectFrom(post)
                .where(
                        post.status.eq(status),
                        post.board.eq(board),
                        post.parentId.isNull(),
                        categoryEq(category),
                        targetGroupTypeEq(targetGroupType),
                        targetGroupIdEq(targetGroupId),
                        publicOnlyCondition(isPublicOnly),
                        userIdEq(filterUserId),
                        answeredOnlyCondition(isAnsweredOnly),
                        searchKeywordCondition(searchInTitle, searchInContent, searchKeywordPattern),
                        cursorCondition(cursor, isDesc)
                )
                .orderBy(idOrder(isDesc))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());
        return query.fetch();
    }

    @Override
    public long countByBoardAndFilters(
            Board board,
            String category,
            PostTargetGroupType targetGroupType,
            Long targetGroupId,
            Boolean isPublicOnly,
            Long filterUserId,
            boolean isAnsweredOnly,
            boolean searchInTitle,
            boolean searchInContent,
            String searchKeywordPattern,
            EntityStatus status
    ) {
        Long count = queryFactory
                .select(post.count())
                .from(post)
                .where(
                        post.status.eq(status),
                        post.board.eq(board),
                        post.parentId.isNull(),
                        categoryEq(category),
                        targetGroupTypeEq(targetGroupType),
                        targetGroupIdEq(targetGroupId),
                        publicOnlyCondition(isPublicOnly),
                        userIdEq(filterUserId),
                        answeredOnlyCondition(isAnsweredOnly),
                        searchKeywordCondition(searchInTitle, searchInContent, searchKeywordPattern)
                )
                .fetchOne();
        return FormatValidator.hasValue(count) ? count : 0L;
    }

    @Override
    public long countByUserIdAndBoard(
            Long userId,
            Board board,
            boolean isAnsweredOnly,
            boolean searchInTitle,
            boolean searchInContent,
            String searchKeywordPattern,
            EntityStatus status
    ) {
        Long count = queryFactory
                .select(post.count())
                .from(post)
                .where(
                        post.status.eq(status),
                        post.board.eq(board),
                        post.userId.eq(userId),
                        post.parentId.isNull(),
                        answeredOnlyCondition(isAnsweredOnly),
                        searchKeywordCondition(searchInTitle, searchInContent, searchKeywordPattern)
                )
                .fetchOne();
        return FormatValidator.hasValue(count) ? count : 0L;
    }

    @Override
    public List<PostJpaEntity> findByUserIdAndBoard(
            Long userId,
            Board board,
            Long cursor,
            boolean isDesc,
            boolean isAnsweredOnly,
            boolean searchInTitle,
            boolean searchInContent,
            String searchKeywordPattern,
            EntityStatus status,
            Pageable pageable
    ) {
        return queryFactory
                .selectFrom(post)
                .where(
                        post.status.eq(status),
                        post.board.eq(board),
                        post.userId.eq(userId),
                        post.parentId.isNull(),
                        answeredOnlyCondition(isAnsweredOnly),
                        searchKeywordCondition(searchInTitle, searchInContent, searchKeywordPattern),
                        cursorCondition(cursor, isDesc)
                )
                .orderBy(idOrder(isDesc))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public List<PostJpaEntity> findByBoardAndFiltersOrderByAnswered(
            Board board,
            String category,
            PostTargetGroupType targetGroupType,
            Long targetGroupId,
            Long cursor,
            boolean isDesc,
            Boolean isPublicOnly,
            Long filterUserId,
            boolean isAnsweredOnly,
            boolean searchInTitle,
            boolean searchInContent,
            String searchKeywordPattern,
            EntityStatus status,
            Pageable pageable
    ) {
        return queryFactory
                .selectFrom(post)
                .where(
                        post.status.eq(status),
                        post.board.eq(board),
                        post.parentId.isNull(),
                        categoryEq(category),
                        targetGroupTypeEq(targetGroupType),
                        targetGroupIdEq(targetGroupId),
                        publicOnlyCondition(isPublicOnly),
                        userIdEq(filterUserId),
                        answeredOnlyCondition(isAnsweredOnly),
                        searchKeywordCondition(searchInTitle, searchInContent, searchKeywordPattern),
                        cursorCondition(cursor, isDesc)
                )
                .orderBy(hasReplyOrder(isDesc), idOrder(isDesc))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public List<PostJpaEntity> findByUserIdAndBoardOrderByAnswered(
            Long userId,
            Board board,
            Long cursor,
            boolean isDesc,
            boolean isAnsweredOnly,
            boolean searchInTitle,
            boolean searchInContent,
            String searchKeywordPattern,
            EntityStatus status,
            Pageable pageable
    ) {
        return queryFactory
                .selectFrom(post)
                .where(
                        post.status.eq(status),
                        post.board.eq(board),
                        post.userId.eq(userId),
                        post.parentId.isNull(),
                        answeredOnlyCondition(isAnsweredOnly),
                        searchKeywordCondition(searchInTitle, searchInContent, searchKeywordPattern),
                        cursorCondition(cursor, isDesc)
                )
                .orderBy(hasReplyOrder(isDesc), idOrder(isDesc))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public List<PostJpaEntity> findByUserIdAndBoardByOffset(
            Long userId,
            Board board,
            EntityStatus status,
            Pageable pageable
    ) {
        JPAQuery<PostJpaEntity> query = queryFactory
                .selectFrom(post)
                .where(
                        post.status.eq(status),
                        post.board.eq(board),
                        post.userId.eq(userId),
                        post.parentId.isNull()
                );
        applyPageableSort(query, pageable);
        return query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public List<PostJpaEntity> findRepliesByParentIds(List<Long> parentIds, EntityStatus status) {
        QPostJpaEntity child = new QPostJpaEntity("child");
        return queryFactory
                .selectFrom(post)
                .where(post.id.in(
                        JPAExpressions.select(child.id.max())
                                .from(child)
                                .where(
                                        child.parentId.in(parentIds),
                                        child.status.eq(status)
                                )
                                .groupBy(child.parentId)
                ))
                .orderBy(post.id.desc())
                .fetch();
    }

    private BooleanExpression categoryEq(String category) {
        return FormatValidator.hasNoValue(category) ? null : post.category.eq(category);
    }

    private BooleanExpression targetGroupTypeEq(PostTargetGroupType targetGroupType) {
        return FormatValidator.hasNoValue(targetGroupType) ? null : post.targetGroupType.eq(targetGroupType);
    }

    private BooleanExpression targetGroupIdEq(Long targetGroupId) {
        return FormatValidator.hasNoValue(targetGroupId) ? null : post.targetGroupId.eq(targetGroupId);
    }

    private BooleanExpression userIdEq(Long filterUserId) {
        return FormatValidator.hasNoValue(filterUserId) ? null : post.userId.eq(filterUserId);
    }

    private BooleanExpression publicOnlyCondition(Boolean isPublicOnly) {
        if (FormatValidator.hasNoValue(isPublicOnly)) {
            return null;
        }
        if (Boolean.TRUE.equals(isPublicOnly)) {
            return post.isPrivate.isFalse();
        }
        return null;
    }

    private BooleanExpression answeredOnlyCondition(boolean isAnsweredOnly) {
        if (!isAnsweredOnly) {
            return null;
        }
        return post.hasReply.isTrue();
    }

    private BooleanExpression searchKeywordCondition(
            boolean searchInTitle,
            boolean searchInContent,
            String searchKeywordPattern
    ) {
        if (FormatValidator.hasNoValue(searchKeywordPattern)) {
            return null;
        }
        BooleanExpression condition = null;
        if (searchInTitle) {
            condition = post.title.lower().like(searchKeywordPattern);
        }
        if (searchInContent) {
            BooleanExpression contentCondition = post.content.lower().like(searchKeywordPattern);
            condition = FormatValidator.hasNoValue(condition) ? contentCondition : condition.or(contentCondition);
        }
        return condition;
    }

    private BooleanExpression cursorCondition(Long cursor, boolean isDesc) {
        if (FormatValidator.hasNoValue(cursor)) {
            return null;
        }
        if (isDesc) {
            return post.id.lt(cursor);
        }
        return post.id.gt(cursor);
    }

    private OrderSpecifier<Long> idOrder(boolean isDesc) {
        return isDesc ? post.id.desc() : post.id.asc();
    }

    private OrderSpecifier<Integer> hasReplyOrder(boolean isDesc) {
        NumberExpression<Integer> hasReplyAsNumber = new CaseBuilder()
                .when(post.hasReply.isTrue()).then(1)
                .otherwise(0);
        return isDesc ? hasReplyAsNumber.desc() : hasReplyAsNumber.asc();
    }

    private void applyPageableSort(JPAQuery<PostJpaEntity> query, Pageable pageable) {
        Sort sort = pageable.getSort();
        if (sort.isUnsorted()) {
            return;
        }
        PathBuilder<PostJpaEntity> pathBuilder = new PathBuilder<>(PostJpaEntity.class, post.getMetadata());
        List<OrderSpecifier<?>> specifiers = new ArrayList<>();
        for (Sort.Order order : sort) {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            ComparableExpressionBase<?> path = pathBuilder.getComparable(order.getProperty(), Comparable.class);
            specifiers.add(new OrderSpecifier<>(direction, path));
        }
        query.orderBy(specifiers.toArray(OrderSpecifier[]::new));
    }
}
