package com.personal.marketnote.product.adapter.out.persistence.product.repository;

import com.personal.marketnote.product.adapter.out.persistence.product.entity.ProductTagJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductTagJpaRepository extends JpaRepository<ProductTagJpaEntity, Long> {
    List<ProductTagJpaEntity> findAllByProductJpaEntityIdAndIdIn(Long productId, List<Long> tagIds);
}
