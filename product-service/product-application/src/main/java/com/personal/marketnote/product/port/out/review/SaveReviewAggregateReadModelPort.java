package com.personal.marketnote.product.port.out.review;

public interface SaveReviewAggregateReadModelPort {

    void upsert(Long productId, Integer totalCount, Float averageRating);
}
