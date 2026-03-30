package com.personal.marketnote.fulfillment.adapter.out.persistence.goods.repository;

import com.personal.marketnote.fulfillment.adapter.out.persistence.goods.entity.FulfillmentGoodsRegistrationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FulfillmentGoodsRegistrationJpaRepository extends JpaRepository<FulfillmentGoodsRegistrationJpaEntity, Long> {
    boolean existsByProductId(Long productId);
}
