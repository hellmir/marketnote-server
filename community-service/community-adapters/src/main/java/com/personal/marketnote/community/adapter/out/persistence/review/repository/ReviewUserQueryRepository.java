package com.personal.marketnote.community.adapter.out.persistence.review.repository;

import com.personal.marketnote.community.adapter.out.persistence.review.entity.ReviewJpaEntity;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReviewUserQueryRepository {

    List<ReviewJpaEntity> findUserReviewsByCursor(
            Long userId,
            Long cursor,
            Pageable pageable,
            String sortProperty,
            Boolean isAsc
    );

    List<ReviewJpaEntity> findUserReviewsByOffset(
            Long reviewerId,
            Pageable pageable
    );
}
