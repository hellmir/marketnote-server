package com.personal.marketnote.commerce.adapter.out.persistence.quickpayment;

import com.personal.marketnote.commerce.adapter.out.persistence.quickpayment.entity.QuickPaymentCardJpaEntity;
import com.personal.marketnote.commerce.adapter.out.persistence.quickpayment.mapper.QuickPaymentCardEntityToDomainMapper;
import com.personal.marketnote.commerce.adapter.out.persistence.quickpayment.repository.QuickPaymentCardJpaRepository;
import com.personal.marketnote.commerce.domain.quickpayment.QuickPaymentCard;
import com.personal.marketnote.commerce.port.out.quickpayment.SaveQuickPaymentCardPort;
import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import lombok.RequiredArgsConstructor;

@PersistenceAdapter
@RequiredArgsConstructor
public class QuickPaymentCardPersistenceAdapter implements SaveQuickPaymentCardPort {
    private final QuickPaymentCardJpaRepository quickPaymentCardJpaRepository;

    @Override
    public QuickPaymentCard save(QuickPaymentCard quickPaymentCard) {
        QuickPaymentCardJpaEntity entity = QuickPaymentCardJpaEntity.from(quickPaymentCard);
        QuickPaymentCardJpaEntity savedEntity = quickPaymentCardJpaRepository.save(entity);
        return QuickPaymentCardEntityToDomainMapper.mapToDomain(savedEntity);
    }
}
