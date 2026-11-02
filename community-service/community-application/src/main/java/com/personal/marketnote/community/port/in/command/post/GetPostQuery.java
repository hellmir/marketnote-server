package com.personal.marketnote.community.port.in.command.post;

import com.personal.marketnote.community.domain.post.Board;
import com.personal.marketnote.community.domain.post.PostTargetType;
import lombok.Builder;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;

@Builder
public record GetPostQuery(
        OAuth2AuthenticatedPrincipal principal,
        Long userId,
        Board board,
        PostTargetType targetType,
        Long id
) {
}
