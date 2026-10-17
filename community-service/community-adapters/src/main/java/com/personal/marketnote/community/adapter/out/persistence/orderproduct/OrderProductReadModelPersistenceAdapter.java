package com.personal.marketnote.community.adapter.out.persistence.orderproduct;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.community.adapter.out.persistence.orderproduct.entity.OrderProductReadModelJpaEntity;
import com.personal.marketnote.community.adapter.out.persistence.orderproduct.repository.OrderProductReadModelJpaRepository;
import com.personal.marketnote.community.port.out.order.FindOrderProductPort;
import com.personal.marketnote.community.port.out.order.SaveOrderProductReadModelPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@PersistenceAdapter
@RequiredArgsConstructor
public class OrderProductReadModelPersistenceAdapter
        implements FindOrderProductPort, SaveOrderProductReadModelPort {

    private final OrderProductReadModelJpaRepository orderProductReadModelJpaRepository;

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true)
    public Optional<Long> findUnitAmountByOrderIdAndPricePolicyId(Long orderId, Long pricePolicyId) {
        return orderProductReadModelJpaRepository
                .findByOrderIdAndPricePolicyIdAndStatus(orderId, pricePolicyId, EntityStatus.ACTIVE)
                .map(OrderProductReadModelJpaEntity::getUnitAmount);
    }

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void upsert(Long orderId, Long pricePolicyId, Long buyerId, Long unitAmount) {
        Optional<OrderProductReadModelJpaEntity> existing =
                orderProductReadModelJpaRepository.findByOrderIdAndPricePolicyId(orderId, pricePolicyId);

        if (existing.isPresent()) {
            existing.get().updateFrom(buyerId, unitAmount);
            return;
        }

        try {
            OrderProductReadModelJpaEntity entity =
                    OrderProductReadModelJpaEntity.of(orderId, pricePolicyId, buyerId, unitAmount);
            orderProductReadModelJpaRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException dive) {
            log.info("주문 상품 Read Model 중복 저장 (멱등 처리). orderId={}, pricePolicyId={}",
                    orderId, pricePolicyId
            );
            orderProductReadModelJpaRepository.findByOrderIdAndPricePolicyId(orderId, pricePolicyId)
                    .ifPresent(entity -> entity.updateFrom(buyerId, unitAmount));
        }
    }
}
