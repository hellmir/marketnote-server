package com.personal.marketnote.commerce.adapter.out.persistence.order.repository;

import com.personal.marketnote.commerce.adapter.out.persistence.order.entity.OrderJpaEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderQueryRepository {

    List<Long> findIdsByBuyerIdWithFilters(
            Long buyerId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            List<String> statuses,
            int statusSize
    );

    List<Long> findIdsByAdminFilters(
            Long sellerId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String orderStatus
    );

    List<OrderJpaEntity> findWithProductsByIds(List<Long> ids);

    List<Long> findOrderIdsScheduledForAutoConfirm(LocalDateTime now, Long lastId, int limit);
}
