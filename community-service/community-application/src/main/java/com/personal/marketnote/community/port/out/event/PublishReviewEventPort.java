package com.personal.marketnote.community.port.out.event;

public interface PublishReviewEventPort {

    void publishReviewRegisteredEvent(Long orderId, Long pricePolicyId);
}
