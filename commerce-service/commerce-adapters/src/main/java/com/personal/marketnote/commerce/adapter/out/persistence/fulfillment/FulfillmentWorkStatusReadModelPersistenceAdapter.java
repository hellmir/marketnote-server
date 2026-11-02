package com.personal.marketnote.commerce.adapter.out.persistence.fulfillment;

import com.personal.marketnote.commerce.adapter.out.persistence.fulfillment.entity.FulfillmentWorkStatusReadModelJpaEntity;
import com.personal.marketnote.commerce.adapter.out.persistence.fulfillment.repository.FulfillmentWorkStatusReadModelJpaRepository;
import com.personal.marketnote.commerce.port.out.fulfillment.GetFulfillmentWorkStatusPort;
import com.personal.marketnote.commerce.port.out.fulfillment.SaveFulfillmentWorkStatusReadModelPort;
import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@PersistenceAdapter
@RequiredArgsConstructor
public class FulfillmentWorkStatusReadModelPersistenceAdapter implements
        SaveFulfillmentWorkStatusReadModelPort,
        GetFulfillmentWorkStatusPort {

    private final FulfillmentWorkStatusReadModelJpaRepository repository;

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void upsert(Long orderId, String workStatus) {
        Optional<FulfillmentWorkStatusReadModelJpaEntity> existing =
                repository.findByOrderId(orderId);

        if (existing.isPresent()) {
            existing.get().updateFrom(workStatus);
            return;
        }

        try {
            FulfillmentWorkStatusReadModelJpaEntity entity =
                    FulfillmentWorkStatusReadModelJpaEntity.of(orderId, workStatus);
            repository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException e) {
            log.info("풀필먼트 작업 상태 Read Model 중복 저장 (멱등 처리). orderId={}", orderId);
            repository.findByOrderId(orderId)
                    .ifPresent(entity -> entity.updateFrom(workStatus));
        }
    }

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true)
    public String getWorkStatus(Long orderId) {
        return repository.findByOrderId(orderId)
                .filter(entity -> entity.getStatus().isActive())
                .map(FulfillmentWorkStatusReadModelJpaEntity::getWorkStatus)
                .orElse("NOT_REGISTERED");
    }
}
