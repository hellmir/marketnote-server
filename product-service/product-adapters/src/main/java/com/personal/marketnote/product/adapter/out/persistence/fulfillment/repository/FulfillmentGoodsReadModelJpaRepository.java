package com.personal.marketnote.product.adapter.out.persistence.fulfillment.repository;

import com.personal.marketnote.product.adapter.out.persistence.fulfillment.entity.FulfillmentGoodsReadModelJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FulfillmentGoodsReadModelJpaRepository extends JpaRepository<FulfillmentGoodsReadModelJpaEntity, Long> {
    Optional<FulfillmentGoodsReadModelJpaEntity> findByCustomerGoodsCode(String customerGoodsCode);
}
