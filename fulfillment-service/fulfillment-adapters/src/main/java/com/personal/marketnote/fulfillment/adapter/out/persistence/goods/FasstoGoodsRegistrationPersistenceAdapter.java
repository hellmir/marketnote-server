package com.personal.marketnote.fulfillment.adapter.out.persistence.goods;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.fulfillment.adapter.out.persistence.goods.entity.FasstoGoodsRegistrationJpaEntity;
import com.personal.marketnote.fulfillment.adapter.out.persistence.goods.repository.FasstoGoodsRegistrationJpaRepository;
import com.personal.marketnote.fulfillment.domain.goods.FasstoGoodsRegistration;
import com.personal.marketnote.fulfillment.exception.FasstoGoodsAlreadyRegisteredException;
import com.personal.marketnote.fulfillment.port.out.goods.FindFasstoGoodsRegistrationPort;
import com.personal.marketnote.fulfillment.port.out.goods.SaveFasstoGoodsRegistrationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;

@PersistenceAdapter
@RequiredArgsConstructor
public class FasstoGoodsRegistrationPersistenceAdapter
        implements FindFasstoGoodsRegistrationPort, SaveFasstoGoodsRegistrationPort {
    private final FasstoGoodsRegistrationJpaRepository repository;

    @Override
    public boolean existsByProductId(Long productId) {
        return repository.existsByProductId(productId);
    }

    @Override
    public void save(FasstoGoodsRegistration registration) {
        try {
            repository.saveAndFlush(FasstoGoodsRegistrationJpaEntity.from(registration));
        } catch (DataIntegrityViolationException e) {
            throw new FasstoGoodsAlreadyRegisteredException(registration.getProductId());
        }
    }
}
