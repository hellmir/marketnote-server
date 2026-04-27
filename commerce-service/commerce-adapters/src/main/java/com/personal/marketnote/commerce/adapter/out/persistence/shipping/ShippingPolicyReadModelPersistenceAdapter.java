package com.personal.marketnote.commerce.adapter.out.persistence.shipping;

import com.personal.marketnote.commerce.adapter.out.persistence.shipping.entity.ShippingPolicyReadModelJpaEntity;
import com.personal.marketnote.commerce.adapter.out.persistence.shipping.repository.ShippingPolicyReadModelJpaRepository;
import com.personal.marketnote.commerce.port.out.result.shipping.ShippingPolicyInfoResult;
import com.personal.marketnote.commerce.port.out.shipping.FindShippingPolicyBySellerIdsPort;
import com.personal.marketnote.commerce.port.out.shipping.SaveShippingPolicyReadModelPort;
import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@PersistenceAdapter
@RequiredArgsConstructor
public class ShippingPolicyReadModelPersistenceAdapter implements FindShippingPolicyBySellerIdsPort, SaveShippingPolicyReadModelPort {

    private final ShippingPolicyReadModelJpaRepository shippingPolicyReadModelJpaRepository;

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true)
    public Map<Long, ShippingPolicyInfoResult> findBySellerIds(List<Long> sellerIds) {
        if (FormatValidator.hasNoValue(sellerIds)) {
            return Map.of();
        }

        List<ShippingPolicyReadModelJpaEntity> entities =
                shippingPolicyReadModelJpaRepository.findAllBySellerIdInAndStatus(sellerIds, EntityStatus.ACTIVE);

        Map<Long, ShippingPolicyInfoResult> result = new HashMap<>();
        for (ShippingPolicyReadModelJpaEntity entity : entities) {
            result.put(entity.getSellerId(), new ShippingPolicyInfoResult(
                    entity.getSellerId(),
                    entity.getShippingFee(),
                    entity.getFreeShippingThreshold(),
                    entity.getJejuSurcharge(),
                    entity.getIslandSurcharge()
            ));
        }

        return result;
    }

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void upsert(Long sellerId, Long shippingFee, Long freeShippingThreshold,
                       Long jejuSurcharge, Long islandSurcharge) {
        Optional<ShippingPolicyReadModelJpaEntity> existing = shippingPolicyReadModelJpaRepository.findBySellerId(sellerId);

        if (existing.isPresent()) {
            existing.get().updateFrom(shippingFee, freeShippingThreshold, jejuSurcharge, islandSurcharge);
            return;
        }

        try {
            ShippingPolicyReadModelJpaEntity entity = ShippingPolicyReadModelJpaEntity.of(
                    sellerId, shippingFee, freeShippingThreshold, jejuSurcharge, islandSurcharge
            );
            shippingPolicyReadModelJpaRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException e) {
            log.info("배송비 정책 Read Model 중복 저장 (멱등 처리). sellerId={}", sellerId);
            shippingPolicyReadModelJpaRepository.findBySellerId(sellerId)
                    .ifPresent(entity -> entity.updateFrom(shippingFee, freeShippingThreshold, jejuSurcharge, islandSurcharge));
        }
    }

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void deactivateBySellerId(Long sellerId) {
        shippingPolicyReadModelJpaRepository.findBySellerId(sellerId)
                .ifPresent(ShippingPolicyReadModelJpaEntity::markInactive);
    }
}
