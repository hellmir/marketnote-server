package com.personal.marketnote.community.port.in.command.review;

import com.personal.marketnote.community.domain.review.ReviewSortProperty;
import org.springframework.data.domain.Sort;

public record GetUserReviewsCommand(
        Long userId,
        int page,
        int pageSize,
        Sort.Direction sortDirection,
        ReviewSortProperty sortProperty
) {
}
