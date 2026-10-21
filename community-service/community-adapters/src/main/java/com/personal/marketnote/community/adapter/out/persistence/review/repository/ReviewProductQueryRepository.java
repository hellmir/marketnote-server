package com.personal.marketnote.community.adapter.out.persistence.review.repository;

import com.personal.marketnote.community.adapter.out.persistence.review.entity.ReviewJpaEntity;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReviewProductQueryRepository {

    List<ReviewJpaEntity> findProductReviewsByCursor(
            Long productId,
            Long cursor,
            Pageable pageable,
            String sortProperty,
            Boolean isAsc
    );

    List<ReviewJpaEntity> findProductPhotoReviewsByCursor(
            Long productId,
            Long cursor,
            Pageable pageable,
            String sortProperty,
            Boolean isAsc
    );
}
