package com.personal.marketnote.community.adapter.out.persistence.orderproduct.repository;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.community.adapter.out.persistence.orderproduct.entity.OrderProductReadModelJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderProductReadModelJpaRepository extends JpaRepository<OrderProductReadModelJpaEntity, Long> {

    Optional<OrderProductReadModelJpaEntity> findByOrderIdAndPricePolicyId(Long orderId, Long pricePolicyId);

    Optional<OrderProductReadModelJpaEntity> findByOrderIdAndPricePolicyIdAndStatus(
            Long orderId, Long pricePolicyId, EntityStatus status
    );
}
