package com.personal.marketnote.product.adapter.out.persistence.shipping;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.product.adapter.out.mapper.ShippingPolicyJpaEntityToDomainMapper;
import com.personal.marketnote.product.adapter.out.persistence.shipping.entity.ShippingPolicyJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.shipping.repository.ShippingPolicyJpaRepository;
import com.personal.marketnote.product.domain.shipping.ShippingPolicy;
import com.personal.marketnote.product.exception.ShippingPolicyAlreadyExistsException;
import com.personal.marketnote.product.port.out.shipping.FindShippingPolicyPort;
import com.personal.marketnote.product.port.out.shipping.SaveShippingPolicyPort;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class ShippingPolicyPersistenceAdapter implements SaveShippingPolicyPort, FindShippingPolicyPort {

    private final ShippingPolicyJpaRepository shippingPolicyJpaRepository;

    @Override
    public Long save(ShippingPolicy shippingPolicy) {
        try {
            ShippingPolicyJpaEntity entity = ShippingPolicyJpaEntity.from(shippingPolicy);
            ShippingPolicyJpaEntity saved = shippingPolicyJpaRepository.save(entity);
            return saved.getId();
        } catch (DataIntegrityViolationException dive) {
            throw new ShippingPolicyAlreadyExistsException(shippingPolicy.getSellerId());
        }
    }

    @Override
    public Optional<ShippingPolicy> findActiveBySellerId(Long sellerId) {
        return shippingPolicyJpaRepository.findBySellerIdAndStatus(sellerId, EntityStatus.ACTIVE)
                .flatMap(ShippingPolicyJpaEntityToDomainMapper::mapToDomain);
    }
}
