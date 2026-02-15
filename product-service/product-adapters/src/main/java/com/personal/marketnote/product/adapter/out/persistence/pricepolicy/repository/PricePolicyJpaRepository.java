package com.personal.marketnote.product.adapter.out.persistence.pricepolicy.repository;

import com.personal.marketnote.product.adapter.out.persistence.pricepolicy.entity.PricePolicyJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PricePolicyJpaRepository extends JpaRepository<PricePolicyJpaEntity, Long> {
    List<PricePolicyJpaEntity> findAllByProductJpaEntity_IdOrderByIdDesc(Long productId);

    @Query("""
            select p
            from PricePolicyJpaEntity p
            where p.id in (
                select popp.id.pricePolicyId
                from ProductOptionPricePolicyJpaEntity popp
                where popp.id.productOptionId in :optionIds
                group by popp.id.pricePolicyId
                having count(distinct popp.id.productOptionId) = :#{#optionIds.size()}
            )
            """)
    List<PricePolicyJpaEntity> findByOptionIds(List<Long> optionIds);

    @Query("""
            select p
            from PricePolicyJpaEntity p
            where p.id = (
                select max(matched.id.pricePolicyId)
                from ProductOptionPricePolicyJpaEntity matched
                where matched.id.pricePolicyId in (
                    select popp.id.pricePolicyId
                    from ProductOptionPricePolicyJpaEntity popp
                    where popp.id.productOptionId in :optionIds
                    group by popp.id.pricePolicyId
                    having count(distinct popp.id.productOptionId) = :#{#optionIds.size()}
                )
            )
            """)
    Optional<PricePolicyJpaEntity> findOneByOptionIds(List<Long> optionIds);

    @Query("""
            SELECT pp
            FROM PricePolicyJpaEntity pp
              JOIN pp.productJpaEntity p
            WHERE 1 = 1
              AND p.status = com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus.ACTIVE
              AND (
                    :pattern IS NULL
                 OR (:searchTarget = 'name' AND p.name LIKE :pattern)
                 OR (:searchTarget = 'brandName' AND p.brandName LIKE :pattern)
                 OR (:searchTarget <> 'name' AND :searchTarget <> 'brandName' AND (p.name LIKE :pattern OR p.brandName LIKE :pattern))
              )
              AND (
                    :categoryId IS NULL
                 OR EXISTS (
                        SELECT 1
                        FROM ProductCategoryJpaEntity pc
                        WHERE pc.productId = p.id
                          AND pc.categoryId = :categoryId
                 )
              )
              AND (
                    :pricePolicyIds IS NULL
                    OR pp.id IN (:pricePolicyIds)
              )
              AND (
                    :cursor IS NULL
                 OR NOT EXISTS (SELECT 1 FROM PricePolicyJpaEntity pp0 WHERE pp0.id = :cursor)
                 OR (
                        (:sortProperty = 'orderNum' AND (
                              pp.orderNum > (SELECT pp2.orderNum FROM PricePolicyJpaEntity pp2 WHERE pp2.id = :cursor)
                           OR (pp.orderNum = (SELECT pp2.orderNum FROM PricePolicyJpaEntity pp2 WHERE pp2.id = :cursor)
                               AND pp.id > :cursor)
                        ))
                     OR (:sortProperty = 'popularity' AND (
                              pp.popularity > (SELECT pp3.popularity FROM PricePolicyJpaEntity pp3 WHERE pp3.id = :cursor)
                           OR (pp.popularity = (SELECT pp3.popularity FROM PricePolicyJpaEntity pp3 WHERE pp3.id = :cursor)
                               AND pp.id > :cursor)
                        ))
                     OR (:sortProperty = 'discountPrice' AND (
                              pp.discountPrice > (SELECT pp4.discountPrice FROM PricePolicyJpaEntity pp4 WHERE pp4.id = :cursor)
                           OR (pp.discountPrice = (SELECT pp4.discountPrice FROM PricePolicyJpaEntity pp4 WHERE pp4.id = :cursor)
                               AND pp.id > :cursor)
                        ))
                     OR (:sortProperty = 'accumulatedPoint' AND (
                              pp.accumulatedPoint > (SELECT pp5.accumulatedPoint FROM PricePolicyJpaEntity pp5 WHERE pp5.id = :cursor)
                           OR (pp.accumulatedPoint = (SELECT pp5.accumulatedPoint FROM PricePolicyJpaEntity pp5 WHERE pp5.id = :cursor)
                               AND pp.id > :cursor)
                        ))
                 )
              )
            ORDER BY
                CASE WHEN :sortProperty = 'orderNum' THEN pp.orderNum END ASC,
                CASE WHEN :sortProperty = 'popularity' THEN pp.popularity END ASC,
                CASE WHEN :sortProperty = 'discountPrice' THEN pp.discountPrice END ASC,
                CASE WHEN :sortProperty = 'accumulatedPoint' THEN pp.accumulatedPoint END ASC,
                pp.id ASC
            """)
    List<PricePolicyJpaEntity> findAllActiveByCursorAsc(
            @Param("pricePolicyIds") List<Long> pricePolicyIds,
            @Param("cursor") Long cursor,
            Pageable pageable,
            @Param("sortProperty") String sortProperty,
            @Param("searchTarget") String searchTarget,
            @Param("pattern") String pattern,
            @Param("categoryId") Long categoryId
    );

    @Query("""
            SELECT pp
            FROM PricePolicyJpaEntity pp
              JOIN pp.productJpaEntity p
            WHERE 1 = 1
              AND p.status = com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus.ACTIVE
              AND (
                    :pattern IS NULL
                 OR (:searchTarget = 'name' AND p.name LIKE :pattern)
                 OR (:searchTarget = 'brandName' AND p.brandName LIKE :pattern)
                 OR (:searchTarget <> 'name' AND :searchTarget <> 'brandName' AND (p.name LIKE :pattern OR p.brandName LIKE :pattern))
              )
              AND (
                    :categoryId IS NULL
                 OR EXISTS (
                        SELECT 1
                        FROM ProductCategoryJpaEntity pc
                        WHERE pc.productId = p.id
                          AND pc.categoryId = :categoryId
                 )
              )
              AND (
                    :pricePolicyIds IS NULL
                 OR pp.id IN :pricePolicyIds
              )
              AND (
                    :cursor IS NULL
                 OR NOT EXISTS (SELECT 1 FROM PricePolicyJpaEntity pp0 WHERE pp0.id = :cursor)
                 OR (
                        (:sortProperty = 'orderNum' AND (
                              pp.orderNum < (
                                  SELECT pp2.orderNum
                                  FROM PricePolicyJpaEntity pp2
                                  WHERE pp2.id = :cursor
                              )
                           OR (pp.orderNum = (
                                    SELECT pp2.orderNum
                                    FROM PricePolicyJpaEntity pp2
                                    WHERE pp2.id = :cursor
                                )
                               AND pp.id < :cursor)
                        ))
                     OR (:sortProperty = 'popularity' AND (
                              pp.popularity < (
                                  SELECT pp3.popularity
                                  FROM PricePolicyJpaEntity pp3
                                  WHERE pp3.id = :cursor
                              )
                           OR (pp.popularity = (
                                    SELECT pp3.popularity
                                    FROM PricePolicyJpaEntity pp3
                                    WHERE pp3.id = :cursor
                                )
                               AND pp.id < :cursor)
                        ))
                     OR (:sortProperty = 'discountPrice' AND (
                              pp.discountPrice < (
                                  SELECT pp4.discountPrice
                                  FROM PricePolicyJpaEntity pp4
                                  WHERE pp4.id = :cursor
                              )
                           OR (pp.discountPrice = (
                                    SELECT pp4.discountPrice
                                    FROM PricePolicyJpaEntity pp4
                                    WHERE pp4.id = :cursor
                                )
                               AND pp.id < :cursor)
                        ))
                     OR (:sortProperty = 'accumulatedPoint' AND (
                              pp.accumulatedPoint < (
                                  SELECT pp5.accumulatedPoint
                                  FROM PricePolicyJpaEntity pp5
                                  WHERE pp5.id = :cursor
                              )
                           OR (pp.accumulatedPoint = (
                                    SELECT pp5.accumulatedPoint
                                    FROM PricePolicyJpaEntity pp5
                                    WHERE pp5.id = :cursor
                                )
                               AND pp.id < :cursor)
                        ))
                 )
              )
            ORDER BY
                CASE WHEN :sortProperty = 'orderNum' THEN pp.orderNum END DESC,
                CASE WHEN :sortProperty = 'popularity' THEN pp.popularity END DESC,
                CASE WHEN :sortProperty = 'discountPrice' THEN pp.discountPrice END DESC,
                CASE WHEN :sortProperty = 'accumulatedPoint' THEN pp.accumulatedPoint END DESC,
                pp.id DESC
            """)
    List<PricePolicyJpaEntity> findAllActiveByCursorDesc(
            @Param("pricePolicyIds") List<Long> pricePolicyIds,
            @Param("cursor") Long cursor,
            Pageable pageable,
            @Param("sortProperty") String sortProperty,
            @Param("searchTarget") String searchTarget,
            @Param("pattern") String pattern,
            @Param("categoryId") Long categoryId
    );

    @Query("""
            SELECT COUNT(pp)
            FROM PricePolicyJpaEntity pp
              JOIN pp.productJpaEntity p
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
            """)
    long countActiveByCategoryId(
            @Param("categoryId") Long categoryId,
            @Param("searchTarget") String searchTarget,
            @Param("pattern") String pattern
    );

    @Query("""
            SELECT COUNT(pp)
            FROM PricePolicyJpaEntity pp
              JOIN pp.productJpaEntity p
            WHERE 1 = 1
              AND p.status = com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus.ACTIVE
              AND (
                    :pattern IS NULL
                 OR (:searchTarget = 'name' AND p.name LIKE :pattern)
                 OR (:searchTarget = 'brandName' AND p.brandName LIKE :pattern)
                 OR (:searchTarget <> 'name' AND :searchTarget <> 'brandName' AND (p.name LIKE :pattern OR p.brandName LIKE :pattern))
              )
            """)
    long countActive(@Param("searchTarget") String searchTarget, @Param("pattern") String pattern);

    @Query("""
            SELECT DISTINCT pp
            FROM PricePolicyJpaEntity pp
              LEFT JOIN FETCH pp.productJpaEntity p
              LEFT JOIN FETCH pp.productOptionPricePolicyJpaEntities popp
              LEFT JOIN FETCH popp.productOptionJpaEntity po
            WHERE pp.id IN :pricePolicyIds
            """)
    List<PricePolicyJpaEntity> findAllWithProductAndOptionMappingsByIdIn(
            @Param("pricePolicyIds") List<Long> pricePolicyIds
    );

    @Query("""
            DELETE FROM PricePolicyJpaEntity pp
            WHERE pp.productJpaEntity.id = :productId
            """)
    void deleteByProductId(Long productId);
}
