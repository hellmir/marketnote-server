package com.personal.marketnote.product.adapter.out.persistence.shipping.repository;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.product.adapter.out.persistence.shipping.entity.ShippingPolicyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShippingPolicyJpaRepository extends JpaRepository<ShippingPolicyJpaEntity, Long> {

    Optional<ShippingPolicyJpaEntity> findBySellerIdAndStatus(Long sellerId, EntityStatus status);

    List<ShippingPolicyJpaEntity> findAllBySellerIdInAndStatus(List<Long> sellerIds, EntityStatus status);
}
