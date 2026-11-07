package com.personal.marketnote.user.adapter.out.persistence.shippingaddress;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.user.adapter.out.mapper.ShippingAddressJpaEntityToDomainMapper;
import com.personal.marketnote.user.adapter.out.persistence.shippingaddress.entity.ShippingAddressJpaEntity;
import com.personal.marketnote.user.adapter.out.persistence.shippingaddress.repository.ShippingAddressJpaRepository;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddress;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressType;
import com.personal.marketnote.user.exception.ShippingAddressNotFoundException;
import com.personal.marketnote.user.port.out.shippingaddress.FindShippingAddressPort;
import com.personal.marketnote.user.port.out.shippingaddress.SaveShippingAddressPort;
import com.personal.marketnote.user.port.out.shippingaddress.UpdateShippingAddressPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Slf4j
@PersistenceAdapter
@RequiredArgsConstructor
public class ShippingAddressPersistenceAdapter implements FindShippingAddressPort, SaveShippingAddressPort, UpdateShippingAddressPort {
    private final ShippingAddressJpaRepository shippingAddressJpaRepository;

    @Override
    public ShippingAddress save(ShippingAddress shippingAddress) {
        ShippingAddressJpaEntity entity = ShippingAddressJpaEntity.from(shippingAddress);
        ShippingAddressJpaEntity savedEntity = shippingAddressJpaRepository.save(entity);
        return ShippingAddressJpaEntityToDomainMapper.mapToDomain(savedEntity);
    }

    @Override
    public boolean existsByUserIdAndAddressType(Long userId, ShippingAddressType addressType) {
        return shippingAddressJpaRepository.existsByUserIdAndAddressTypeAndStatus(
                userId, addressType, EntityStatus.ACTIVE
        );
    }

    @Override
    public long countByUserIdAndAddressType(Long userId, ShippingAddressType addressType) {
        return shippingAddressJpaRepository.countByUserIdAndAddressTypeAndStatus(
                userId, addressType, EntityStatus.ACTIVE
        );
    }

    @Override
    public boolean existsByUserId(Long userId) {
        return shippingAddressJpaRepository.existsByUserIdAndStatus(userId, EntityStatus.ACTIVE);
    }

    @Override
    public List<ShippingAddress> findAllByUserId(Long userId) {
        return shippingAddressJpaRepository.findAllByUserIdAndStatus(userId, EntityStatus.ACTIVE)
                .stream()
                .map(ShippingAddressJpaEntityToDomainMapper::mapToDomain)
                .toList();
    }

    @Override
    public Optional<ShippingAddress> findByIdAndUserId(Long id, Long userId) {
        return shippingAddressJpaRepository.findByIdAndUserIdAndStatus(id, userId, EntityStatus.ACTIVE)
                .map(ShippingAddressJpaEntityToDomainMapper::mapToDomain);
    }

    @Override
    public List<ShippingAddress> findDefaultsByUserId(Long userId) {
        return shippingAddressJpaRepository.findAllByUserIdAndIsDefaultAndStatus(userId, true, EntityStatus.ACTIVE)
                .stream()
                .map(ShippingAddressJpaEntityToDomainMapper::mapToDomain)
                .toList();
    }

    @Override
    public void update(ShippingAddress shippingAddress) {
        ShippingAddressJpaEntity entity = shippingAddressJpaRepository.findById(shippingAddress.getId())
                .orElseThrow(() -> {
                    log.warn("배송지를 찾을 수 없습니다. shippingAddressId={}", shippingAddress.getId());
                    return new ShippingAddressNotFoundException();
                });

        entity.updateFrom(shippingAddress);
    }
}
