package com.personal.marketnote.user.security.token.dto;

import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record GrantedTokenInfo(
        @NotNull String accessToken,
        @Nullable String refreshToken,
        @NotNull String id,
        @NotNull AuthVendor authVendor,
        String name,
        String profileImageUrl,
        OAuth2UserInfo userInfo
) {
}
