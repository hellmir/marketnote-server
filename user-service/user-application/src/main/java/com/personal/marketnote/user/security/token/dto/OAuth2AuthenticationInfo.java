package com.personal.marketnote.user.security.token.dto;

import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import lombok.Builder;

@Builder
public record OAuth2AuthenticationInfo(
        String id, AuthVendor authVendor, Long userId
) {
}
