package com.personal.marketnote.commerce.adapter.out.persistence.shipping.repository;

import com.personal.marketnote.commerce.adapter.out.persistence.shipping.entity.ShippingPolicyReadModelJpaEntity;
import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShippingPolicyReadModelJpaRepository extends JpaRepository<ShippingPolicyReadModelJpaEntity, Long> {

    Optional<ShippingPolicyReadModelJpaEntity> findBySellerId(Long sellerId);

    Optional<ShippingPolicyReadModelJpaEntity> findBySellerIdAndStatus(Long sellerId, EntityStatus status);

    List<ShippingPolicyReadModelJpaEntity> findAllBySellerIdInAndStatus(List<Long> sellerIds, EntityStatus status);
}
