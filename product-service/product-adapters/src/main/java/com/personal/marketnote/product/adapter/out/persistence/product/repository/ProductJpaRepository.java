package com.personal.marketnote.product.adapter.out.persistence.product.repository;

import com.personal.marketnote.product.adapter.out.persistence.product.entity.ProductJpaEntity;
import org.springframework.data.domain.Pageable;
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
              LEFT JOIN PricePolicyJpaEntity pp
                ON pp.productJpaEntity = p
               AND pp.id = (
                    SELECT MAX(pp2.id)
                    FROM PricePolicyJpaEntity pp2
                    WHERE pp2.productJpaEntity = p
                      AND NOT EXISTS (
                        SELECT 1
                        FROM ProductOptionPricePolicyJpaEntity popp
                        WHERE popp.pricePolicyJpaEntity = pp2
                      )
               )
            WHERE 1 = 1
              AND p.status = com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus.ACTIVE
              AND (
                    :pattern IS NULL
                 OR (:searchTarget = 'name' AND p.name LIKE :pattern)
                 OR (:searchTarget = 'brandName' AND p.brandName LIKE :pattern)
                 OR (:searchTarget <> 'name' AND :searchTarget <> 'brandName' AND (p.name LIKE :pattern OR p.brandName LIKE :pattern))
              )
              AND (
                    :cursor IS NULL
                 OR NOT EXISTS (SELECT 1 FROM PricePolicyJpaEntity pp0 WHERE pp0.id = :cursor)
                 OR (
                        (:sortProperty = 'orderNum' AND (
                              p.orderNum > (
                                  SELECT pp2.productJpaEntity.orderNum
                                  FROM PricePolicyJpaEntity pp2
                                  WHERE pp2.id = :cursor
                              )
                           OR (p.orderNum = (
                                    SELECT pp2.productJpaEntity.orderNum
                                    FROM PricePolicyJpaEntity pp2
                                    WHERE pp2.id = :cursor
                                )
                               AND p.id > (
                                    SELECT pp2.productJpaEntity.id
                                    FROM PricePolicyJpaEntity pp2
                                    WHERE pp2.id = :cursor
                               ))
                        ))
                     OR (:sortProperty = 'popularity' AND (
                              p.sales > (
                                  SELECT pp2.productJpaEntity.sales
                                  FROM PricePolicyJpaEntity pp2
                                  WHERE pp2.id = :cursor
                              )
                           OR (p.sales = (
                                    SELECT pp2.productJpaEntity.sales
                                    FROM PricePolicyJpaEntity pp2
                                    WHERE pp2.id = :cursor
                                )
                               AND p.id > (
                                    SELECT pp2.productJpaEntity.id
                                    FROM PricePolicyJpaEntity pp2
                                    WHERE pp2.id = :cursor
                               ))
                        ))
                     OR (:sortProperty = 'discountPrice' AND (
                              COALESCE(pp.discountPrice, 0) > (
                                  SELECT COALESCE(pp3.discountPrice, 0)
                                  FROM PricePolicyJpaEntity pp3
                                  WHERE pp3.id = :cursor
                              )
                           OR (COALESCE(pp.discountPrice, 0) = (
                                    SELECT COALESCE(pp3.discountPrice, 0)
                                    FROM PricePolicyJpaEntity pp3
                                    WHERE pp3.id = :cursor
                                )
                               AND pp.id > :cursor)
                        ))
                     OR (:sortProperty = 'accumulatedPoint' AND (
                              COALESCE(pp.accumulatedPoint, 0) > (
                                  SELECT COALESCE(pp5.accumulatedPoint, 0)
                                  FROM PricePolicyJpaEntity pp5
                                  WHERE pp5.id = :cursor
                              )
                           OR (COALESCE(pp.accumulatedPoint, 0) = (
                                    SELECT COALESCE(pp5.accumulatedPoint, 0)
                                    FROM PricePolicyJpaEntity pp5
                                    WHERE pp5.id = :cursor
                                )
                               AND pp.id > :cursor)
                        ))
                 )
              )
            ORDER BY
                CASE WHEN :sortProperty = 'orderNum' THEN p.orderNum END ASC,
                CASE WHEN :sortProperty = 'popularity' THEN p.sales END ASC,
                CASE WHEN :sortProperty = 'discountPrice' THEN COALESCE(pp.discountPrice, 0) END ASC,
                CASE WHEN :sortProperty = 'accumulatedPoint' THEN COALESCE(pp.accumulatedPoint, 0) END ASC,
                p.id ASC
            """)
    List<ProductJpaEntity> findAllActiveByCursorAsc(
            @Param("cursor") Long cursor,
            Pageable pageable,
            @Param("sortProperty") String sortProperty,
            @Param("searchTarget") String searchTarget,
            @Param("pattern") String pattern);

    @Query("""
            SELECT p
            FROM ProductJpaEntity p
              LEFT JOIN PricePolicyJpaEntity pp
                ON pp.productJpaEntity = p
               AND pp.id = (
                    SELECT MAX(pp2.id)
                    FROM PricePolicyJpaEntity pp2
                    WHERE pp2.productJpaEntity = p
                      AND NOT EXISTS (
                        SELECT 1
                        FROM ProductOptionPricePolicyJpaEntity popp
                        WHERE popp.pricePolicyJpaEntity = pp2
                      )
               )
            WHERE 1 = 1
              AND p.status = com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus.ACTIVE
              AND (
                    :pattern IS NULL
                 OR (:searchTarget = 'name' AND p.name LIKE :pattern)
                 OR (:searchTarget = 'brandName' AND p.brandName LIKE :pattern)
                 OR (:searchTarget <> 'name' AND :searchTarget <> 'brandName' AND (p.name LIKE :pattern OR p.brandName LIKE :pattern))
              )
              AND (
                    :cursor IS NULL
                 OR NOT EXISTS (SELECT 1 FROM PricePolicyJpaEntity pp0 WHERE pp0.id = :cursor)
                 OR (
                        (:sortProperty = 'orderNum' AND (
                              p.orderNum < (
                                  SELECT pp2.productJpaEntity.orderNum
                                  FROM PricePolicyJpaEntity pp2
                                  WHERE pp2.id = :cursor
                              )
                           OR (p.orderNum = (
                                    SELECT pp2.productJpaEntity.orderNum
                                    FROM PricePolicyJpaEntity pp2
                                    WHERE pp2.id = :cursor
                                )
                               AND p.id < (
                                    SELECT pp2.productJpaEntity.id
                                    FROM PricePolicyJpaEntity pp2
                                    WHERE pp2.id = :cursor
                               ))
                        ))
                     OR (:sortProperty = 'popularity' AND (
                              p.sales < (
                                  SELECT pp2.productJpaEntity.sales
                                  FROM PricePolicyJpaEntity pp2
                                  WHERE pp2.id = :cursor
                              )
                           OR (p.sales = (
                                    SELECT pp2.productJpaEntity.sales
                                    FROM PricePolicyJpaEntity pp2
                                    WHERE pp2.id = :cursor
                                )
                               AND p.id < (
                                    SELECT pp2.productJpaEntity.id
                                    FROM PricePolicyJpaEntity pp2
                                    WHERE pp2.id = :cursor
                               ))
                        ))
                     OR (:sortProperty = 'discountPrice' AND (
                              COALESCE(pp.discountPrice, 0) < (
                                  SELECT COALESCE(pp3.discountPrice, 0)
                                  FROM PricePolicyJpaEntity pp3
                                  WHERE pp3.id = :cursor
                              )
                           OR (COALESCE(pp.discountPrice, 0) = (
                                    SELECT COALESCE(pp3.discountPrice, 0)
                                    FROM PricePolicyJpaEntity pp3
                                    WHERE pp3.id = :cursor
                                )
                               AND pp.id < :cursor)
                        ))
                     OR (:sortProperty = 'accumulatedPoint' AND (
                              COALESCE(pp.accumulatedPoint, 0) < (
                                  SELECT COALESCE(pp5.accumulatedPoint, 0)
                                  FROM PricePolicyJpaEntity pp5
                                  WHERE pp5.id = :cursor
                              )
                           OR (COALESCE(pp.accumulatedPoint, 0) = (
                                    SELECT COALESCE(pp5.accumulatedPoint, 0)
                                    FROM PricePolicyJpaEntity pp5
                                    WHERE pp5.id = :cursor
                                )
                               AND pp.id < :cursor)
                        ))
                 )
              )
            ORDER BY
                CASE WHEN :sortProperty = 'orderNum' THEN p.orderNum END DESC,
                CASE WHEN :sortProperty = 'popularity' THEN p.sales END DESC,
                CASE WHEN :sortProperty = 'discountPrice' THEN COALESCE(pp.discountPrice, 0) END DESC,
                CASE WHEN :sortProperty = 'accumulatedPoint' THEN COALESCE(pp.accumulatedPoint, 0) END DESC,
                p.id DESC
            """)
    List<ProductJpaEntity> findAllActiveByCursorDesc(
            @Param("cursor") Long cursor,
            Pageable pageable,
            @Param("sortProperty") String sortProperty,
            @Param("searchTarget") String searchTarget,
            @Param("pattern") String pattern);

    @Query("""
            SELECT p
            FROM ProductJpaEntity p
              LEFT JOIN PricePolicyJpaEntity pp
                ON pp.productJpaEntity = p
               AND pp.id = (
                    SELECT MAX(pp2.id)
                    FROM PricePolicyJpaEntity pp2
                    WHERE pp2.productJpaEntity = p
                      AND NOT EXISTS (
                        SELECT 1
                        FROM ProductOptionPricePolicyJpaEntity popp
                        WHERE popp.pricePolicyJpaEntity = pp2
                      )
               )
            WHERE 1 = 1
              AND p.status = com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus.ACTIVE
              AND EXISTS (
                SELECT 1
                FROM ProductCategoryJpaEntity pc
                WHERE 1 = 1
                    AND pc.productId = p.id
                    AND pc.categoryId = :categoryId
              )
              AND (
                    :pattern IS NULL
                 OR (:searchTarget = 'name' AND p.name LIKE :pattern)
                 OR (:searchTarget = 'brandName' AND p.brandName LIKE :pattern)
                 OR (:searchTarget <> 'name' AND :searchTarget <> 'brandName' AND (p.name LIKE :pattern OR p.brandName LIKE :pattern))
              )
              AND (
                    :cursor IS NULL
                 OR NOT EXISTS (SELECT 1 FROM PricePolicyJpaEntity pp0 WHERE pp0.id = :cursor)
                 OR (
                        (:sortProperty = 'orderNum' AND (
                              p.orderNum > (
                                  SELECT pp2.productJpaEntity.orderNum
                                  FROM PricePolicyJpaEntity pp2
                                  WHERE pp2.id = :cursor
                              )
                           OR (p.orderNum = (
                                    SELECT pp2.productJpaEntity.orderNum
                                    FROM PricePolicyJpaEntity pp2
                                    WHERE pp2.id = :cursor
                                )
                               AND p.id > (
                                    SELECT pp2.productJpaEntity.id
                                    FROM PricePolicyJpaEntity pp2
                                    WHERE pp2.id = :cursor
                               ))
                        ))
                     OR (:sortProperty = 'popularity' AND (
                              p.sales > (
                                  SELECT pp2.productJpaEntity.sales
                                  FROM PricePolicyJpaEntity pp2
                                  WHERE pp2.id = :cursor
                              )
                           OR (p.sales = (
                                    SELECT pp2.productJpaEntity.sales
                                    FROM PricePolicyJpaEntity pp2
                                    WHERE pp2.id = :cursor
                                )
                               AND p.id > (
                                    SELECT pp2.productJpaEntity.id
                                    FROM PricePolicyJpaEntity pp2
                                    WHERE pp2.id = :cursor
                               ))
                        ))
                     OR (:sortProperty = 'discountPrice' AND (
                              COALESCE(pp.discountPrice, 0) > (
                                  SELECT COALESCE(pp3.discountPrice, 0)
                                  FROM PricePolicyJpaEntity pp3
                                  WHERE pp3.id = :cursor
                              )
                           OR (COALESCE(pp.discountPrice, 0) = (
                                    SELECT COALESCE(pp3.discountPrice, 0)
                                    FROM PricePolicyJpaEntity pp3
                                    WHERE pp3.id = :cursor
                                )
                               AND pp.id > :cursor)
                        ))
                     OR (:sortProperty = 'accumulatedPoint' AND (
                              COALESCE(pp.accumulatedPoint, 0) > (
                                  SELECT COALESCE(pp5.accumulatedPoint, 0)
                                  FROM PricePolicyJpaEntity pp5
                                  WHERE pp5.id = :cursor
                              )
                           OR (COALESCE(pp.accumulatedPoint, 0) = (
                                    SELECT COALESCE(pp5.accumulatedPoint, 0)
                                    FROM PricePolicyJpaEntity pp5
                                    WHERE pp5.id = :cursor
                                )
                               AND pp.id > :cursor)
                        ))
                 )
              )
            ORDER BY
                CASE WHEN :sortProperty = 'orderNum' THEN p.orderNum END ASC,
                CASE WHEN :sortProperty = 'popularity' THEN p.sales END ASC,
                CASE WHEN :sortProperty = 'discountPrice' THEN COALESCE(pp.discountPrice, 0) END ASC,
                CASE WHEN :sortProperty = 'accumulatedPoint' THEN COALESCE(pp.accumulatedPoint, 0) END ASC,
                p.id ASC
            """)
    List<ProductJpaEntity> findAllActiveByCategoryIdCursorAsc(
            @Param("categoryId") Long categoryId,
            @Param("cursor") Long cursor,
            Pageable pageable,
            @Param("sortProperty") String sortProperty,
            @Param("searchTarget") String searchTarget,
            @Param("pattern") String pattern);

    @Query("""
            SELECT p
            FROM ProductJpaEntity p
              LEFT JOIN PricePolicyJpaEntity pp
                ON pp.productJpaEntity = p
               AND pp.id = (
                    SELECT MAX(pp2.id)
                    FROM PricePolicyJpaEntity pp2
                    WHERE pp2.productJpaEntity = p
                      AND NOT EXISTS (
                        SELECT 1
                        FROM ProductOptionPricePolicyJpaEntity popp
                        WHERE popp.pricePolicyJpaEntity = pp2
                      )
               )
            WHERE 1 = 1
              AND p.status = com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus.ACTIVE
              AND EXISTS (
                SELECT 1
                FROM ProductCategoryJpaEntity pc
                WHERE 1 = 1
                    AND pc.productId = p.id
                    AND pc.categoryId = :categoryId
              )
              AND (
                    :pattern IS NULL
                 OR (:searchTarget = 'name' AND p.name LIKE :pattern)
                 OR (:searchTarget = 'brandName' AND p.brandName LIKE :pattern)
                 OR (:searchTarget <> 'name' AND :searchTarget <> 'brandName' AND (p.name LIKE :pattern OR p.brandName LIKE :pattern))
              )
              AND (
                    :cursor IS NULL
                 OR NOT EXISTS (SELECT 1 FROM PricePolicyJpaEntity pp0 WHERE pp0.id = :cursor)
                 OR (
                        (:sortProperty = 'orderNum' AND (
                              p.orderNum < (
                                  SELECT pp2.productJpaEntity.orderNum
                                  FROM PricePolicyJpaEntity pp2
                                  WHERE pp2.id = :cursor
                              )
                           OR (p.orderNum = (
                                    SELECT pp2.productJpaEntity.orderNum
                                    FROM PricePolicyJpaEntity pp2
                                    WHERE pp2.id = :cursor
                                )
                               AND p.id < (
                                    SELECT pp2.productJpaEntity.id
                                    FROM PricePolicyJpaEntity pp2
                                    WHERE pp2.id = :cursor
                               ))
                        ))
                     OR (:sortProperty = 'popularity' AND (
                              p.sales < (
                                  SELECT pp2.productJpaEntity.sales
                                  FROM PricePolicyJpaEntity pp2
                                  WHERE pp2.id = :cursor
                              )
                           OR (p.sales = (
                                    SELECT pp2.productJpaEntity.sales
                                    FROM PricePolicyJpaEntity pp2
                                    WHERE pp2.id = :cursor
                                )
                               AND p.id < (
                                    SELECT pp2.productJpaEntity.id
                                    FROM PricePolicyJpaEntity pp2
                                    WHERE pp2.id = :cursor
                               ))
                        ))
                     OR (:sortProperty = 'discountPrice' AND (
                              COALESCE(pp.discountPrice, 0) < (
                                  SELECT COALESCE(pp3.discountPrice, 0)
                                  FROM PricePolicyJpaEntity pp3
                                  WHERE pp3.id = :cursor
                              )
                           OR (COALESCE(pp.discountPrice, 0) = (
                                    SELECT COALESCE(pp3.discountPrice, 0)
                                    FROM PricePolicyJpaEntity pp3
                                    WHERE pp3.id = :cursor
                                )
                               AND pp.id < :cursor)
                        ))
                     OR (:sortProperty = 'accumulatedPoint' AND (
                              COALESCE(pp.accumulatedPoint, 0) < (
                                  SELECT COALESCE(pp5.accumulatedPoint, 0)
                                  FROM PricePolicyJpaEntity pp5
                                  WHERE pp5.id = :cursor
                              )
                           OR (COALESCE(pp.accumulatedPoint, 0) = (
                                    SELECT COALESCE(pp5.accumulatedPoint, 0)
                                    FROM PricePolicyJpaEntity pp5
                                    WHERE pp5.id = :cursor
                                )
                               AND pp.id < :cursor)
                        ))
                 )
              )
            ORDER BY
                CASE WHEN :sortProperty = 'orderNum' THEN p.orderNum END DESC,
                CASE WHEN :sortProperty = 'popularity' THEN p.sales END DESC,
                CASE WHEN :sortProperty = 'discountPrice' THEN COALESCE(pp.discountPrice, 0) END DESC,
                CASE WHEN :sortProperty = 'accumulatedPoint' THEN COALESCE(pp.accumulatedPoint, 0) END DESC,
                p.id DESC
            """)
    List<ProductJpaEntity> findAllActiveByCategoryIdCursorDesc(
            @Param("categoryId") Long categoryId,
            @Param("cursor") Long cursor,
            Pageable pageable,
            @Param("sortProperty") String sortProperty,
            @Param("searchTarget") String searchTarget,
            @Param("pattern") String pattern);

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
