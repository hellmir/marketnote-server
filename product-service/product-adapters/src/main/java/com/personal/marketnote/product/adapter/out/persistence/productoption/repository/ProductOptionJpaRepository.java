package com.personal.marketnote.product.adapter.out.persistence.productoption.repository;

import com.personal.marketnote.product.adapter.out.persistence.productoption.entity.ProductOptionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductOptionJpaRepository extends JpaRepository<ProductOptionJpaEntity, Long> {
    @Query("""
            select o.id
            from ProductOptionJpaEntity o
            where o.productOptionCategoryJpaEntity.id = :categoryId
            """)
    List<Long> findIdsByCategoryId(@Param("categoryId") Long categoryId);

    @Modifying
    @Query("""
            delete
            from ProductOptionJpaEntity o
            where o.productOptionCategoryJpaEntity.id = :categoryId
            """)
    void deleteAllByCategoryId(@Param("categoryId") Long categoryId);

    @Query("""
            select o
            from ProductOptionJpaEntity o
            where o.productOptionCategoryJpaEntity.productJpaEntity.id = :productId
              and o.id in :ids
            """)
    List<ProductOptionJpaEntity> findByProductIdAndIds(
            @Param("productId") Long productId,
            @Param("ids") List<Long> ids
    );

    @Query("""
            select count(o)
            from ProductOptionJpaEntity o
            where o.productOptionCategoryJpaEntity.productJpaEntity.id = :productId
              and o.id in :ids
            """)
    long countByProductIdAndIds(
            @Param("productId") Long productId,
            @Param("ids") List<Long> ids
    );
}
