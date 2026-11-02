package com.personal.marketnote.community.domain.review;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ReviewVersionHistory {
    private Long id;
    private Long reviewId;
    private Rating rating;
    private String content;
    private Boolean isPhoto;
    private LocalDateTime createdAt;

    public static ReviewVersionHistory from(ReviewVersionHistoryCreateState state) {
        return ReviewVersionHistory.builder()
                .reviewId(state.getReviewId())
                .rating(state.getRating())
                .content(state.getContent())
                .isPhoto(state.getIsPhoto())
                .build();
    }

    public static ReviewVersionHistory from(ReviewVersionHistorySnapshotState state) {
        return ReviewVersionHistory.builder()
                .id(state.getId())
                .reviewId(state.getReviewId())
                .rating(state.getRating())
                .content(state.getContent())
                .isPhoto(state.getIsPhoto())
                .createdAt(state.getCreatedAt())
                .build();
    }
}

