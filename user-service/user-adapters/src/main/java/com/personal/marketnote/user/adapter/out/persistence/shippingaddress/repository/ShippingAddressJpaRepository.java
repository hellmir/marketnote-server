package com.personal.marketnote.user.adapter.out.persistence.shippingaddress.repository;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.user.adapter.out.persistence.shippingaddress.entity.ShippingAddressJpaEntity;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShippingAddressJpaRepository extends JpaRepository<ShippingAddressJpaEntity, Long> {

    boolean existsByUserIdAndAddressTypeAndStatus(Long userId, ShippingAddressType addressType, EntityStatus status);

    long countByUserIdAndAddressTypeAndStatus(Long userId, ShippingAddressType addressType, EntityStatus status);

    boolean existsByUserIdAndStatus(Long userId, EntityStatus status);

    List<ShippingAddressJpaEntity> findAllByUserIdAndStatus(Long userId, EntityStatus status);

    Optional<ShippingAddressJpaEntity> findByIdAndUserIdAndStatus(Long id, Long userId, EntityStatus status);

    List<ShippingAddressJpaEntity> findAllByUserIdAndIsDefaultAndStatus(Long userId, Boolean isDefault, EntityStatus status);
}
