package com.personal.marketnote.community.adapter.out.persistence.review.repository;

import com.personal.marketnote.community.adapter.out.persistence.review.entity.ReviewVersionHistoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewVersionHistoryJpaRepository extends JpaRepository<ReviewVersionHistoryJpaEntity, Long> {
}
