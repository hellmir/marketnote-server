package com.personal.marketnote.commerce.adapter.out.persistence.shipping;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.commerce.adapter.out.persistence.shipping.entity.ShippingAddressReadModelJpaEntity;
import com.personal.marketnote.commerce.adapter.out.persistence.shipping.repository.ShippingAddressReadModelJpaRepository;
import com.personal.marketnote.commerce.exception.ShippingAddressNotFoundException;
import com.personal.marketnote.commerce.port.out.result.user.ShippingAddressInfoResult;
import com.personal.marketnote.commerce.port.out.user.FindUserShippingAddressPort;
import com.personal.marketnote.commerce.port.out.user.SaveShippingAddressReadModelPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@PersistenceAdapter
@RequiredArgsConstructor
public class ShippingAddressReadModelPersistenceAdapter implements FindUserShippingAddressPort, SaveShippingAddressReadModelPort {

    private final ShippingAddressReadModelJpaRepository shippingAddressReadModelJpaRepository;

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true)
    public ShippingAddressInfoResult findByIdAndUserId(Long shippingAddressId, Long userId) {
        ShippingAddressReadModelJpaEntity entity = shippingAddressReadModelJpaRepository
                .findByShippingAddressIdAndUserIdAndStatus(shippingAddressId, userId, EntityStatus.ACTIVE)
                .orElseThrow(() -> new ShippingAddressNotFoundException(shippingAddressId));

        return new ShippingAddressInfoResult(
                entity.getRecipientName(),
                entity.getRecipientPhoneNumber(),
                entity.getAddress(),
                entity.getAddressDetail()
        );
    }

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void upsert(Long shippingAddressId, Long userId, String recipientName, String recipientPhoneNumber, String address, String addressDetail) {
        Optional<ShippingAddressReadModelJpaEntity> existing =
                shippingAddressReadModelJpaRepository.findByShippingAddressId(shippingAddressId);

        if (existing.isPresent()) {
            existing.get().updateFrom(recipientName, recipientPhoneNumber, address, addressDetail);
            return;
        }

        try {
            ShippingAddressReadModelJpaEntity entity = ShippingAddressReadModelJpaEntity.of(
                    shippingAddressId, userId, recipientName, recipientPhoneNumber, address, addressDetail
            );
            shippingAddressReadModelJpaRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException e) {
            log.info("배송지 Read Model 중복 저장 (멱등 처리). shippingAddressId={}", shippingAddressId);
            shippingAddressReadModelJpaRepository.findByShippingAddressId(shippingAddressId)
                    .ifPresent(entity -> entity.updateFrom(recipientName, recipientPhoneNumber, address, addressDetail));
        }
    }

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void deactivateByShippingAddressId(Long shippingAddressId) {
        shippingAddressReadModelJpaRepository.findByShippingAddressId(shippingAddressId)
                .ifPresent(ShippingAddressReadModelJpaEntity::markInactive);
    }
}
