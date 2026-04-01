package com.personal.marketnote.community.port.in.command.post;

import com.personal.marketnote.community.domain.post.PostSortProperty;
import org.springframework.data.domain.Sort;

public record GetUserOneOnOneInquiryPostsCommand(
        Long userId,
        int page,
        int pageSize,
        Sort.Direction sortDirection,
        PostSortProperty sortProperty
) {
}
