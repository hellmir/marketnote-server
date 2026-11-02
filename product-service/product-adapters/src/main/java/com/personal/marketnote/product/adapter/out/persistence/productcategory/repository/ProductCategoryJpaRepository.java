package com.personal.marketnote.product.adapter.out.persistence.productcategory.repository;

import com.personal.marketnote.product.adapter.out.persistence.productcategory.entity.ProductCategoryId;
import com.personal.marketnote.product.adapter.out.persistence.productcategory.entity.ProductCategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCategoryJpaRepository extends JpaRepository<ProductCategoryJpaEntity, ProductCategoryId> {
    void deleteByProductId(Long productId);

    boolean existsByCategoryId(Long categoryId);
}
