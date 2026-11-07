package com.personal.marketnote.product.adapter.out.persistence.shipping;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.product.adapter.out.mapper.ShippingPolicyJpaEntityToDomainMapper;
import com.personal.marketnote.product.adapter.out.persistence.shipping.entity.ShippingPolicyJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.shipping.repository.ShippingPolicyJpaRepository;
import com.personal.marketnote.product.domain.shipping.ShippingPolicy;
import com.personal.marketnote.product.exception.ShippingPolicyAlreadyExistsException;
import com.personal.marketnote.product.exception.ShippingPolicyNotFoundException;
import com.personal.marketnote.product.port.out.shipping.FindShippingPolicyPort;
import com.personal.marketnote.product.port.out.shipping.SaveShippingPolicyPort;
import com.personal.marketnote.product.port.out.shipping.UpdateShippingPolicyPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

@Slf4j
@PersistenceAdapter
@RequiredArgsConstructor
public class ShippingPolicyPersistenceAdapter implements SaveShippingPolicyPort, FindShippingPolicyPort, UpdateShippingPolicyPort {

    private final ShippingPolicyJpaRepository shippingPolicyJpaRepository;

    @Override
    public Long save(ShippingPolicy shippingPolicy) {
        try {
            ShippingPolicyJpaEntity entity = ShippingPolicyJpaEntity.from(shippingPolicy);
            ShippingPolicyJpaEntity saved = shippingPolicyJpaRepository.save(entity);
            return saved.getId();
        } catch (DataIntegrityViolationException dive) {
            log.warn("해당 판매자의 배송비 정책이 이미 존재합니다. sellerId={}", shippingPolicy.getSellerId());
            throw new ShippingPolicyAlreadyExistsException();
        }
    }

    @Override
    public Optional<ShippingPolicy> findActiveBySellerId(Long sellerId) {
        return shippingPolicyJpaRepository.findBySellerIdAndStatus(sellerId, EntityStatus.ACTIVE)
                .flatMap(ShippingPolicyJpaEntityToDomainMapper::mapToDomain);
    }

    @Override
    public List<ShippingPolicy> findActiveBySellerIds(List<Long> sellerIds) {
        return shippingPolicyJpaRepository.findAllBySellerIdInAndStatus(sellerIds, EntityStatus.ACTIVE)
                .stream()
                .map(ShippingPolicyJpaEntityToDomainMapper::mapToDomain)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    @Override
    public void update(ShippingPolicy shippingPolicy) {
        ShippingPolicyJpaEntity entity = shippingPolicyJpaRepository
                .findById(shippingPolicy.getId())
                .orElseThrow(() -> {
                    log.warn("해당 판매자의 배송비 정책을 찾을 수 없습니다. sellerId={}", shippingPolicy.getSellerId());
                    return new ShippingPolicyNotFoundException();
                });

        entity.updateFrom(shippingPolicy);
    }
}
