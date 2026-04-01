package com.personal.marketnote.community.domain.review;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewSnapshotState {
    private final Long id;
    private final Long reviewerId;
    private final Long orderId;
    private final Long productId;
    private final Long pricePolicyId;
    private final String productImageUrl;
    private final String selectedOptions;
    private final Integer quantity;
    private final String reviewerName;
    private final String reviewerMaskedName;
    private final Float rating;
    private final String content;
    private final Boolean isPhoto;
    private final Boolean isEdited;
    private final Integer likeCount;
    private final EntityStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;
    private final Long orderNum;
}
