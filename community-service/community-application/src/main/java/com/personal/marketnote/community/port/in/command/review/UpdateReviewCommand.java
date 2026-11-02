package com.personal.marketnote.community.port.in.command.review;

import lombok.Builder;

@Builder
public record UpdateReviewCommand(
        Long id,
        Long reviewerId,
        Float rating,
        String content,
        Boolean isPhoto
) {
}
