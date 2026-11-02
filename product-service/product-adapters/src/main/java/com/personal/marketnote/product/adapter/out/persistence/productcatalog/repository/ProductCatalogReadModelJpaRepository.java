package com.personal.marketnote.product.adapter.out.persistence.productcatalog.repository;

import com.personal.marketnote.product.adapter.out.persistence.productcatalog.entity.ProductCatalogReadModelJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductCatalogReadModelJpaRepository
        extends JpaRepository<ProductCatalogReadModelJpaEntity, Long>,
        ProductCatalogReadModelQueryRepository {

    Optional<ProductCatalogReadModelJpaEntity> findByPricePolicyId(Long pricePolicyId);

    List<ProductCatalogReadModelJpaEntity> findAllByProductId(Long productId);

    void deleteByProductId(Long productId);
}
