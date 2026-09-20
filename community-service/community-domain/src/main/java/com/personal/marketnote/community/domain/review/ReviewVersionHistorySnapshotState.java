package com.personal.marketnote.community.domain.review;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewVersionHistorySnapshotState {
    private final Long id;
    private final Long reviewId;
    private final Rating rating;
    private final String content;
    private final Boolean isPhoto;
    private final LocalDateTime createdAt;
}

