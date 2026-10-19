package com.personal.marketnote.fulfillment.adapter.out.persistence.goods.repository;

import com.personal.marketnote.fulfillment.adapter.out.persistence.goods.entity.FulfillmentGoodsRegistrationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FulfillmentGoodsRegistrationJpaRepository extends JpaRepository<FulfillmentGoodsRegistrationJpaEntity, Long> {
    boolean existsByProductKey(UUID productKey);
}
