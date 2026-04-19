package com.personal.marketnote.community.adapter.out.persistence.product.repository;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.community.adapter.out.persistence.product.entity.ProductReadModelJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductReadModelJpaRepository extends JpaRepository<ProductReadModelJpaEntity, Long> {

    Optional<ProductReadModelJpaEntity> findByPricePolicyId(Long pricePolicyId);

    List<ProductReadModelJpaEntity> findByPricePolicyIdInAndStatus(List<Long> pricePolicyIds, EntityStatus status);

    List<ProductReadModelJpaEntity> findByProductId(Long productId);
}
