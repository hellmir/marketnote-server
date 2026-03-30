package com.personal.marketnote.fulfillment.adapter.out.persistence.goods;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.fulfillment.adapter.out.persistence.goods.entity.FulfillmentGoodsRegistrationJpaEntity;
import com.personal.marketnote.fulfillment.adapter.out.persistence.goods.repository.FulfillmentGoodsRegistrationJpaRepository;
import com.personal.marketnote.fulfillment.domain.goods.FulfillmentGoodsRegistration;
import com.personal.marketnote.fulfillment.exception.FulfillmentGoodsAlreadyRegisteredException;
import com.personal.marketnote.fulfillment.port.out.goods.FindFulfillmentGoodsRegistrationPort;
import com.personal.marketnote.fulfillment.port.out.goods.SaveFulfillmentGoodsRegistrationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;

@PersistenceAdapter
@RequiredArgsConstructor
public class FulfillmentGoodsRegistrationPersistenceAdapter
        implements FindFulfillmentGoodsRegistrationPort, SaveFulfillmentGoodsRegistrationPort {
    private final FulfillmentGoodsRegistrationJpaRepository repository;

    @Override
    public boolean existsByProductId(Long productId) {
        return repository.existsByProductId(productId);
    }

    @Override
    public void save(FulfillmentGoodsRegistration registration) {
        try {
            repository.saveAndFlush(FulfillmentGoodsRegistrationJpaEntity.from(registration));
        } catch (DataIntegrityViolationException e) {
            throw new FulfillmentGoodsAlreadyRegisteredException(registration.getProductId());
        }
    }
}
