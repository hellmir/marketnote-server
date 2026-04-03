package com.personal.marketnote.product.adapter.out.persistence.review;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.adapter.out.persistence.review.entity.ReviewAggregateReadModelJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.review.repository.ReviewAggregateReadModelJpaRepository;
import com.personal.marketnote.product.port.out.result.ProductReviewAggregateResult;
import com.personal.marketnote.product.port.out.review.FindProductReviewAggregatesPort;
import com.personal.marketnote.product.port.out.review.SaveReviewAggregateReadModelPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@PersistenceAdapter
@RequiredArgsConstructor
public class ReviewAggregateReadModelPersistenceAdapter implements FindProductReviewAggregatesPort, SaveReviewAggregateReadModelPort {
    private final ReviewAggregateReadModelJpaRepository reviewAggregateReadModelJpaRepository;

    @Override
    public Map<Long, ProductReviewAggregateResult> findByProductIds(List<Long> productIds) {
        if (FormatValidator.hasNoValue(productIds)) {
            return Map.of();
        }

        List<ReviewAggregateReadModelJpaEntity> entities =
                reviewAggregateReadModelJpaRepository.findByProductIdInAndStatus(productIds, EntityStatus.ACTIVE);

        return entities.stream()
                .collect(Collectors.toMap(
                        ReviewAggregateReadModelJpaEntity::getProductId,
                        entity -> new ProductReviewAggregateResult(
                                entity.getProductId(),
                                entity.getTotalCount(),
                                entity.getAverageRating()
                        ),
                        (existing, replacement) -> existing
                ));
    }

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void upsert(Long productId, Integer totalCount, Float averageRating) {
        Optional<ReviewAggregateReadModelJpaEntity> existing =
                reviewAggregateReadModelJpaRepository.findByProductId(productId);

        if (existing.isPresent()) {
            existing.get().updateFrom(totalCount, averageRating);
            return;
        }

        try {
            ReviewAggregateReadModelJpaEntity entity =
                    ReviewAggregateReadModelJpaEntity.of(productId, totalCount, averageRating);
            reviewAggregateReadModelJpaRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException e) {
            log.info("리뷰 집계 Read Model 중복 저장 (멱등 처리). productId={}", productId);
            reviewAggregateReadModelJpaRepository.findByProductId(productId)
                    .ifPresent(entity -> entity.updateFrom(totalCount, averageRating));
        }
    }
}
