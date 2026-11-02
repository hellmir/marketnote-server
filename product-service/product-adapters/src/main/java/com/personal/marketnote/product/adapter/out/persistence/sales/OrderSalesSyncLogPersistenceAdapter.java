package com.personal.marketnote.product.adapter.out.persistence.sales;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.product.adapter.out.persistence.sales.entity.OrderSalesSyncLogJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.sales.repository.OrderSalesSyncLogJpaRepository;
import com.personal.marketnote.product.port.out.sales.OrderSalesSyncLogPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@PersistenceAdapter
@RequiredArgsConstructor
public class OrderSalesSyncLogPersistenceAdapter implements OrderSalesSyncLogPort {

    private final OrderSalesSyncLogJpaRepository repository;

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true)
    public boolean isProcessed(Long orderId) {
        return repository.existsByOrderId(orderId);
    }

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void markProcessed(Long orderId) {
        repository.saveAndFlush(OrderSalesSyncLogJpaEntity.of(orderId));
    }
}
