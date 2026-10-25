package com.personal.marketnote.product.port.out.review;

import java.math.BigDecimal;

public interface SaveReviewAggregateReadModelPort {

    void upsert(Long productId, Integer totalCount, BigDecimal averageRating);
}
