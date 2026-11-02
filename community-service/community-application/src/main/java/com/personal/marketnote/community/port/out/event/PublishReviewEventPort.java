package com.personal.marketnote.community.port.out.event;

import java.math.BigDecimal;

public interface PublishReviewEventPort {

    void publishReviewRegisteredEvent(
            Long orderId, Long pricePolicyId, Long productId,
            Integer totalCount, BigDecimal averageRating,
            Long reviewerId, Long reviewId
    );

    void publishReviewUpdatedEvent(Long reviewId, Long productId, Integer totalCount, BigDecimal averageRating);

    void publishReviewDeletedEvent(Long reviewId, Long productId, Integer totalCount, BigDecimal averageRating, Long reviewerId);
}
