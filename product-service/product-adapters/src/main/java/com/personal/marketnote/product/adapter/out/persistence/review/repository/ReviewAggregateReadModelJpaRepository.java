package com.personal.marketnote.product.adapter.out.persistence.review.repository;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.product.adapter.out.persistence.review.entity.ReviewAggregateReadModelJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewAggregateReadModelJpaRepository extends JpaRepository<ReviewAggregateReadModelJpaEntity, Long> {

    Optional<ReviewAggregateReadModelJpaEntity> findByProductId(Long productId);

    List<ReviewAggregateReadModelJpaEntity> findByProductIdInAndStatus(List<Long> productIds, EntityStatus status);
}
