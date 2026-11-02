package com.personal.marketnote.community.port.in.command.post;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.domain.post.*;
import lombok.Builder;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;

@Builder
public record GetPostsQuery(
        OAuth2AuthenticatedPrincipal principal,
        Long userId,
        Board board,
        String category,
        PostTargetGroupType targetGroupType,
        Long targetGroupId,
        PostTargetType targetType,
        Long targetId,
        Long cursor,
        int pageSize,
        Sort.Direction sortDirection,
        PostSortProperty sortProperty,
        PostSearchTarget searchTarget,
        String searchKeyword,
        PostFilterCategory filter,
        PostFilterValue filterValue
) {
    public boolean isPublicPosts() {
        return board.isNonMemberViewBoard() || FormatValidator.hasValue(targetType);
    }
}
