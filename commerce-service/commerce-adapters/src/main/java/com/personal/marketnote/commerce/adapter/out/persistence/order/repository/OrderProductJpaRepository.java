package com.personal.marketnote.commerce.adapter.out.persistence.order.repository;

import com.personal.marketnote.commerce.adapter.out.persistence.order.entity.OrderProductId;
import com.personal.marketnote.commerce.adapter.out.persistence.order.entity.OrderProductJpaEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface OrderProductJpaRepository extends JpaRepository<OrderProductJpaEntity, OrderProductId> {
    @Query("""
            SELECT op
            FROM OrderProductJpaEntity op
            WHERE 1 = 1
                AND op.id.orderId = :orderId
                AND op.id.pricePolicyId = :pricePolicyId
            """)
    Optional<OrderProductJpaEntity> findByOrderIdAndPricePolicyId(
            @Param("orderId") Long orderId, @Param("pricePolicyId") Long pricePolicyId
    );
}
