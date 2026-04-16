package com.personal.marketnote.commerce.adapter.out.persistence.quickpayment;

import com.personal.marketnote.commerce.adapter.out.persistence.quickpayment.entity.QuickPaymentCardJpaEntity;
import com.personal.marketnote.commerce.adapter.out.persistence.quickpayment.mapper.QuickPaymentCardEntityToDomainMapper;
import com.personal.marketnote.commerce.adapter.out.persistence.quickpayment.repository.QuickPaymentCardJpaRepository;
import com.personal.marketnote.commerce.domain.quickpayment.QuickPaymentCard;
import com.personal.marketnote.commerce.port.out.quickpayment.FindQuickPaymentCardPort;
import com.personal.marketnote.commerce.port.out.quickpayment.SaveQuickPaymentCardPort;
import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class QuickPaymentCardPersistenceAdapter implements SaveQuickPaymentCardPort, FindQuickPaymentCardPort {
    private final QuickPaymentCardJpaRepository quickPaymentCardJpaRepository;

    @Override
    public QuickPaymentCard save(QuickPaymentCard quickPaymentCard) {
        QuickPaymentCardJpaEntity entity = QuickPaymentCardJpaEntity.from(quickPaymentCard);
        QuickPaymentCardJpaEntity savedEntity = quickPaymentCardJpaRepository.save(entity);
        return QuickPaymentCardEntityToDomainMapper.mapToDomain(savedEntity);
    }

    @Override
    public Optional<QuickPaymentCard> findActiveByIdAndUserId(Long id, Long userId) {
        return quickPaymentCardJpaRepository.findByIdAndUserIdAndStatus(id, userId, EntityStatus.ACTIVE)
                .map(QuickPaymentCardEntityToDomainMapper::mapToDomain);
    }
}
