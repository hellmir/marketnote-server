package com.personal.marketnote.commerce.adapter.out.persistence.shipping.repository;

import com.personal.marketnote.commerce.adapter.out.persistence.shipping.entity.ShippingAddressReadModelJpaEntity;
import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShippingAddressReadModelJpaRepository extends JpaRepository<ShippingAddressReadModelJpaEntity, Long> {

    Optional<ShippingAddressReadModelJpaEntity> findByShippingAddressId(Long shippingAddressId);

    Optional<ShippingAddressReadModelJpaEntity> findByShippingAddressIdAndUserIdAndStatus(
            Long shippingAddressId, Long userId, EntityStatus status
    );
}
