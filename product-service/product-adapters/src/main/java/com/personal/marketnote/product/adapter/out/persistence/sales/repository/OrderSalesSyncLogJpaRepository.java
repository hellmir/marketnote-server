package com.personal.marketnote.product.adapter.out.persistence.sales.repository;

import com.personal.marketnote.product.adapter.out.persistence.sales.entity.OrderSalesSyncLogJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderSalesSyncLogJpaRepository extends JpaRepository<OrderSalesSyncLogJpaEntity, Long> {

    boolean existsByOrderId(Long orderId);
}
