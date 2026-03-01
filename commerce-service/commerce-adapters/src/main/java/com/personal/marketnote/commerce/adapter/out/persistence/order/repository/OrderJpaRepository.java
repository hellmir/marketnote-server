package com.personal.marketnote.commerce.adapter.out.persistence.order.repository;

import com.personal.marketnote.commerce.adapter.out.persistence.order.entity.OrderJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, Long> {
    @Query(value = """
            SELECT DISTINCT o.id
            FROM orders o
            WHERE o.buyer_id = :buyerId
              AND o.order_status <> 'PAYMENT_PENDING'
              AND (CAST(:startDate AS timestamp) IS NULL OR o.created_at >= CAST(:startDate AS timestamp))
              AND (CAST(:endDate   AS timestamp) IS NULL OR o.created_at <  CAST(:endDate   AS timestamp))
              AND (:statusSize = 0 OR o.order_status IN (:statuses))
            ORDER BY o.id DESC
            """, nativeQuery = true)
    List<Long> findIdsByBuyerIdWithFilters(
            @Param("buyerId") Long buyerId,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate,
            @Param("statuses") List<String> statuses,
            @Param("statusSize") int statusSize
    );

    @Query(value = """
            SELECT DISTINCT o.id
            FROM orders o
            LEFT JOIN order_product op ON o.id = op.order_id
            WHERE o.order_status <> 'PAYMENT_PENDING'
              AND (CAST(:sellerId AS BIGINT) IS NULL OR op.seller_id = :sellerId)
              AND (CAST(:startDate AS timestamp) IS NULL OR o.created_at >= CAST(:startDate AS timestamp))
              AND (CAST(:endDate   AS timestamp) IS NULL OR o.created_at <  CAST(:endDate   AS timestamp))
              AND (:orderStatus = '' OR o.order_status = :orderStatus)
            ORDER BY o.id DESC
            LIMIT 1000
            """, nativeQuery = true)
    List<Long> findIdsByAdminFilters(
            @Param("sellerId") Long sellerId,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate,
            @Param("orderStatus") String orderStatus
    );

    @Query("""
            SELECT DISTINCT o
            FROM OrderJpaEntity o
            LEFT JOIN FETCH o.orderProductJpaEntities
            WHERE o.id IN :ids
            """)
    List<OrderJpaEntity> findWithProductsByIds(@Param("ids") List<Long> ids);
}
