package com.personal.marketnote.product.adapter.out.persistence.pricepolicy.repository;

import com.personal.marketnote.product.adapter.out.persistence.pricepolicy.entity.PricePolicyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PricePolicyJpaRepository extends JpaRepository<PricePolicyJpaEntity, Long>, PricePolicySearchQueryRepository {
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
            SELECT COUNT(pp)
            FROM PricePolicyJpaEntity pp
              JOIN pp.productJpaEntity p
            WHERE 1 = 1
              AND p.status = com.personal.marketnote.common.domain.EntityStatus.ACTIVE
              AND pp.status = com.personal.marketnote.common.domain.EntityStatus.ACTIVE
              AND EXISTS (
                SELECT 1
                FROM ProductCategoryJpaEntity pc
                WHERE 1 = 1
                    AND pc.productId = p.id
                    AND pc.categoryId = :categoryId
              )
              AND (
                    :pattern = ''
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
              AND p.status = com.personal.marketnote.common.domain.EntityStatus.ACTIVE
              AND pp.status = com.personal.marketnote.common.domain.EntityStatus.ACTIVE
              AND (
                    :pattern = ''
                 OR (:searchTarget = 'name' AND p.name LIKE :pattern)
                 OR (:searchTarget = 'brandName' AND p.brandName LIKE :pattern)
                 OR (:searchTarget <> 'name' AND :searchTarget <> 'brandName' AND (p.name LIKE :pattern OR p.brandName LIKE :pattern))
              )
            """)
    long countActive(@Param("searchTarget") String searchTarget, @Param("pattern") String pattern);

    @Query("""
            SELECT pp
            FROM PricePolicyJpaEntity pp
              LEFT JOIN FETCH pp.productJpaEntity p
            WHERE pp.id IN :pricePolicyIds
            """)
    List<PricePolicyJpaEntity> findAllWithProductAndOptionMappingsByIdIn(
            @Param("pricePolicyIds") List<Long> pricePolicyIds
    );

    @Query("""
            UPDATE PricePolicyJpaEntity pp
            SET pp.status = com.personal.marketnote.common.domain.EntityStatus.INACTIVE
            WHERE pp.productJpaEntity.id = :productId
            """)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    void deactivateByProductId(@Param("productId") Long productId);

    @Query(value = """
            UPDATE price_policy
            SET popularity = COALESCE(
                (SELECT SUM(op.quantity)
                 FROM order_product op
                 WHERE op.price_policy_id = price_policy.id
                   AND op.confirmed_at >= :since
                   AND op.order_status = 'CONFIRMED'),
                0)
            """, nativeQuery = true)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    void updateWeeklyPopularity(@Param("since") LocalDateTime since);
}
