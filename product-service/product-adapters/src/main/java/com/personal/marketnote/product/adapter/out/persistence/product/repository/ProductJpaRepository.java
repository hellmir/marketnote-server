package com.personal.marketnote.product.adapter.out.persistence.product.repository;

import com.personal.marketnote.product.adapter.out.persistence.product.entity.ProductJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, Long> {
    boolean existsByIdAndSellerId(Long productId, Long sellerId);

    @Query("""
            SELECT p
            FROM ProductJpaEntity p
            WHERE 1 = 1
              AND EXISTS (
                SELECT 1
                FROM ProductCategoryJpaEntity pc
                WHERE 1 = 1
                    AND pc.productId = p.id
                    AND pc.categoryId = :categoryId
              )
            ORDER BY p.orderNum ASC
            """)
    List<ProductJpaEntity> findAllByCategoryIdOrderByOrderNumAsc(@Param("categoryId") Long categoryId);

    @Query("""
            SELECT p
            FROM ProductJpaEntity p
            WHERE 1 = 1
              AND p.status = com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus.ACTIVE
              AND EXISTS (
                SELECT 1
                FROM PricePolicyJpaEntity pp
                WHERE 1 = 1
                    AND pp.productJpaEntity = p
                    AND pp.id IN :pricePolicyIds
              )
            """)
    List<ProductJpaEntity> findByPricePolicyIds(@Param("pricePolicyIds") List<Long> pricePolicyIds);

    @Query("""
            SELECT DISTINCT p
            FROM ProductJpaEntity p
              LEFT JOIN FETCH p.productTagJpaEntities tags
              LEFT JOIN FETCH p.pricePolicyJpaEntities pricePolicies
            WHERE p.id IN :productIds
            """)
    List<ProductJpaEntity> findAllWithTagsAndPoliciesByIdIn(@Param("productIds") List<Long> productIds);
}
