package com.personal.marketnote.community.adapter.out.persistence.review.repository;

import com.personal.marketnote.community.adapter.out.persistence.review.entity.ProductReviewAggregateJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductReviewAggregateJpaRepository extends JpaRepository<ProductReviewAggregateJpaEntity, Long> {
    @Query("""
            SELECT p
            FROM ProductReviewAggregateJpaEntity p
            WHERE p.productId = :productId
            """)
    Optional<ProductReviewAggregateJpaEntity> findByProductId(@Param("productId") Long productId);

    @Query("""
            SELECT p
            FROM ProductReviewAggregateJpaEntity p
            WHERE p.productId IN :productIds
            """)
    List<ProductReviewAggregateJpaEntity> findByProductIdIn(@Param("productIds") List<Long> productIds);
}
