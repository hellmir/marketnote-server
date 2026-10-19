package com.personal.marketnote.product.adapter.out.persistence.productoption.repository;

import com.personal.marketnote.product.adapter.out.persistence.productoption.entity.ProductOptionPricePolicyId;
import com.personal.marketnote.product.adapter.out.persistence.productoption.entity.ProductOptionPricePolicyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductOptionPricePolicyJpaRepository extends JpaRepository<ProductOptionPricePolicyJpaEntity, ProductOptionPricePolicyId> {
    @Query("""
            select popp.id.pricePolicyId
            from com.personal.marketnote.product.adapter.out.persistence.productoption.entity.ProductOptionPricePolicyJpaEntity popp
            where popp.id.pricePolicyId in (
                select popp2.id.pricePolicyId
                from com.personal.marketnote.product.adapter.out.persistence.productoption.entity.ProductOptionPricePolicyJpaEntity popp2
                where popp2.id.productOptionId in :optionIds
                group by popp2.id.pricePolicyId
                having count(distinct popp2.id.productOptionId) = :size
            )
            group by popp.id.pricePolicyId
            having count(popp.id.productOptionId) = :size
            """)
    List<Long> findExactMatchingPricePolicyIds(@Param("optionIds") List<Long> optionIds, @Param("size") long size);

    long countByPricePolicyJpaEntity_Id(Long pricePolicyId);

    @Query("""
            select popp.id.pricePolicyId as pricePolicyId, popp.id.productOptionId as productOptionId
            from com.personal.marketnote.product.adapter.out.persistence.productoption.entity.ProductOptionPricePolicyJpaEntity popp
            where popp.id.pricePolicyId in :pricePolicyIds
            order by popp.id.pricePolicyId, popp.id.productOptionId
            """)
    List<PricePolicyOptionMapping> findAllOptionIdsByPricePolicyIds(@Param("pricePolicyIds") List<Long> pricePolicyIds);

    interface PricePolicyOptionMapping {
        Long getPricePolicyId();
        Long getProductOptionId();
    }

    @Modifying
    @Query("""
            delete
            from com.personal.marketnote.product.adapter.out.persistence.productoption.entity.ProductOptionPricePolicyJpaEntity popp
            where popp.productOptionJpaEntity.id in :optionIds
            """)
    void deleteByOptionIds(@Param("optionIds") List<Long> optionIds);

    @Query("""
            select popp.id.productOptionId
            from com.personal.marketnote.product.adapter.out.persistence.productoption.entity.ProductOptionPricePolicyJpaEntity popp
            where popp.id.pricePolicyId = :pricePolicyId
            """)
    List<Long> findOptionIdsByPricePolicyId(@Param("pricePolicyId") Long pricePolicyId);

    @Modifying
    @Query("""
            delete
            from com.personal.marketnote.product.adapter.out.persistence.productoption.entity.ProductOptionPricePolicyJpaEntity popp
            where popp.id.pricePolicyId = :pricePolicyId
            """)
    void deleteByPricePolicyId(@Param("pricePolicyId") Long pricePolicyId);

    @Modifying
    @Query("""
            delete
            from com.personal.marketnote.product.adapter.out.persistence.productoption.entity.ProductOptionPricePolicyJpaEntity popp
            where popp.id.pricePolicyId in :pricePolicyIds
            """)
    void deleteByPricePolicyIds(@Param("pricePolicyIds") List<Long> pricePolicyIds);
}

