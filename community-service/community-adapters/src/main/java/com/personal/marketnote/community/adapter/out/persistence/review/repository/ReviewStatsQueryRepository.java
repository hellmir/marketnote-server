package com.personal.marketnote.community.adapter.out.persistence.review.repository;

public interface ReviewStatsQueryRepository {

    long countByProductId(Long productId);

    long countByProductIdAndIsPhoto(Long productId, Boolean isPhoto);

    long countByReviewerId(Long reviewerId);
}
