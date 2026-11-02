package com.personal.marketnote.product.adapter.out.persistence.productoption.repository;

import com.personal.marketnote.product.adapter.out.persistence.productoption.entity.ProductOptionCategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductOptionCategoryJpaRepository extends JpaRepository<ProductOptionCategoryJpaEntity, Long> {
    @Query("""
            SELECT DISTINCT c
            FROM ProductOptionCategoryJpaEntity c
              JOIN FETCH c.productJpaEntity p
              LEFT JOIN FETCH c.productOptionJpaEntities o
            WHERE 1 = 1
              AND p.id = :productId
              AND c.status = com.personal.marketnote.common.domain.EntityStatus.ACTIVE
              AND (o IS NULL OR o.status = com.personal.marketnote.common.domain.EntityStatus.ACTIVE)
            ORDER BY c.orderNum ASC, o.orderNum ASC
            """)
    List<ProductOptionCategoryJpaEntity> findActiveWithOptionsByProductId(
            @Param("productId") Long productId
    );

    @Query("""
            select c.productJpaEntity.id
            from ProductOptionCategoryJpaEntity c
            where c.id = :categoryId
            """)
    Long findProductIdByCategoryId(@Param("categoryId") Long categoryId);
}


