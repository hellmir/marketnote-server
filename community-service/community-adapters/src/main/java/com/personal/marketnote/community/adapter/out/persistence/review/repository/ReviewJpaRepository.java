package com.personal.marketnote.community.adapter.out.persistence.review.repository;

import com.personal.marketnote.community.adapter.out.persistence.review.entity.ReviewJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewJpaRepository extends JpaRepository<ReviewJpaEntity, Long>,
        ReviewProductQueryRepository, ReviewUserQueryRepository, ReviewStatsQueryRepository {

    boolean existsByOrderIdAndPricePolicyId(Long orderId, Long pricePolicyId);

    boolean existsByIdAndReviewerId(Long id, Long reviewerId);
}
