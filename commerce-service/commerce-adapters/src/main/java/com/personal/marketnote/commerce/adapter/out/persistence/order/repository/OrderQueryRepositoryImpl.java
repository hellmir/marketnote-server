package com.personal.marketnote.commerce.adapter.out.persistence.order.repository;

import com.personal.marketnote.commerce.adapter.out.persistence.order.entity.OrderJpaEntity;
import com.personal.marketnote.commerce.adapter.out.persistence.order.entity.QOrderJpaEntity;
import com.personal.marketnote.commerce.adapter.out.persistence.order.entity.QOrderProductJpaEntity;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.common.utility.FormatValidator;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class OrderQueryRepositoryImpl implements OrderQueryRepository {

    private static final QOrderJpaEntity order = QOrderJpaEntity.orderJpaEntity;
    private static final QOrderProductJpaEntity orderProduct = QOrderProductJpaEntity.orderProductJpaEntity;
    private static final long ADMIN_QUERY_LIMIT = 1000L;

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Long> findIdsByBuyerIdWithFilters(
            Long buyerId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            List<String> statuses,
            int statusSize
    ) {
        return queryFactory
                .select(order.id)
                .distinct()
                .from(order)
                .where(
                        order.buyerId.eq(buyerId),
                        order.orderStatus.notIn(OrderStatus.PAYMENT_PENDING, OrderStatus.FAILED),
                        createdAtOnOrAfter(startDate),
                        createdAtBefore(endDate),
                        statusesIn(statuses, statusSize)
                )
                .orderBy(order.id.desc())
                .fetch();
    }

    @Override
    public List<Long> findIdsByAdminFilters(
            Long sellerId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String orderStatus
    ) {
        return queryFactory
                .select(order.id)
                .distinct()
                .from(order)
                .leftJoin(order.orderProductJpaEntities, orderProduct)
                .where(
                        order.orderStatus.ne(OrderStatus.PAYMENT_PENDING),
                        sellerIdEq(sellerId),
                        createdAtOnOrAfter(startDate),
                        createdAtBefore(endDate),
                        orderStatusEq(orderStatus)
                )
                .orderBy(order.id.desc())
                .limit(ADMIN_QUERY_LIMIT)
                .fetch();
    }

    @Override
    public List<OrderJpaEntity> findWithProductsByIds(List<Long> ids) {
        if (FormatValidator.hasNoValue(ids) || ids.isEmpty()) {
            return List.of();
        }
        return queryFactory
                .selectFrom(order)
                .distinct()
                .leftJoin(order.orderProductJpaEntities, orderProduct).fetchJoin()
                .where(order.id.in(ids))
                .fetch();
    }

    @Override
    public List<Long> findOrderIdsEligibleForAutoConfirm(LocalDateTime deliveredBefore) {
        return queryFactory
                .select(order.id)
                .distinct()
                .from(order)
                .innerJoin(order.orderProductJpaEntities, orderProduct)
                .where(
                        order.orderStatus.eq(OrderStatus.DELIVERED),
                        orderProduct.orderStatus.eq(OrderStatus.DELIVERED),
                        orderProduct.deliveredAt.isNotNull(),
                        orderProduct.deliveredAt.loe(deliveredBefore)
                )
                .orderBy(order.id.asc())
                .fetch();
    }

    private BooleanExpression createdAtOnOrAfter(LocalDateTime startDate) {
        if (FormatValidator.hasNoValue(startDate)) {
            return null;
        }
        return order.createdAt.goe(startDate);
    }

    private BooleanExpression createdAtBefore(LocalDateTime endDate) {
        if (FormatValidator.hasNoValue(endDate)) {
            return null;
        }
        return order.createdAt.lt(endDate);
    }

    private BooleanExpression statusesIn(List<String> statuses, int statusSize) {
        if (statusSize == 0) {
            return null;
        }
        if (FormatValidator.hasNoValue(statuses) || statuses.isEmpty()) {
            return null;
        }
        List<OrderStatus> statusEnums = statuses.stream()
                .map(OrderStatus::valueOf)
                .toList();
        return order.orderStatus.in(statusEnums);
    }

    private BooleanExpression sellerIdEq(Long sellerId) {
        if (FormatValidator.hasNoValue(sellerId)) {
            return null;
        }
        return orderProduct.sellerId.eq(sellerId);
    }

    private BooleanExpression orderStatusEq(String orderStatus) {
        if (FormatValidator.hasNoValue(orderStatus) || orderStatus.isEmpty()) {
            return null;
        }
        return order.orderStatus.eq(OrderStatus.valueOf(orderStatus));
    }
}
