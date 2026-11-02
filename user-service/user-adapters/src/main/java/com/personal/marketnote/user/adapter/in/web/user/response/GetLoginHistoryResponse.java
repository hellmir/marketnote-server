package com.personal.marketnote.user.adapter.in.web.user.response;

import com.personal.marketnote.user.port.in.result.GetLoginHistoryResult;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import lombok.AccessLevel;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder(access = AccessLevel.PRIVATE)
public record GetLoginHistoryResponse(
        Long id,
        Long userId,
        AuthVendor authVendor,
        String ipAddress,
        LocalDateTime loggedInAt
) {
    public static GetLoginHistoryResponse from(GetLoginHistoryResult result) {
        return GetLoginHistoryResponse.builder()
                .id(result.id())
                .userId(result.userId())
                .authVendor(result.authVendor())
                .ipAddress(result.ipAddress())
                .loggedInAt(result.loggedInAt())
                .build();
    }
}
