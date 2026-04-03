package com.personal.marketnote.community.port.out.event;

public interface PublishReviewEventPort {

    void publishReviewRegisteredEvent(Long orderId, Long pricePolicyId, Long productId, Integer totalCount, Float averageRating);

    void publishReviewUpdatedEvent(Long reviewId, Long productId, Integer totalCount, Float averageRating);

    void publishReviewDeletedEvent(Long reviewId, Long productId, Integer totalCount, Float averageRating);
}
