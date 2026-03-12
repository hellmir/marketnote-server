package com.personal.marketnote.fulfillment.adapter.out.persistence.goods.repository;

import com.personal.marketnote.fulfillment.adapter.out.persistence.goods.entity.FasstoGoodsRegistrationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FasstoGoodsRegistrationJpaRepository extends JpaRepository<FasstoGoodsRegistrationJpaEntity, Long> {
    boolean existsByProductId(Long productId);
}
