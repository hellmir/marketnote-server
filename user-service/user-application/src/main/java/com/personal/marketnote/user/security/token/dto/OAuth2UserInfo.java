package com.personal.marketnote.user.security.token.dto;

import lombok.Builder;

@Builder
public record OAuth2UserInfo(
        String id,
        String name
) {
}
