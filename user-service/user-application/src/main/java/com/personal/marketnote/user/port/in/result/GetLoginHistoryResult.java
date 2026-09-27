package com.personal.marketnote.user.port.in.result;

import com.personal.marketnote.user.domain.user.LoginHistory;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import lombok.AccessLevel;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder(access = AccessLevel.PRIVATE)
public record GetLoginHistoryResult(
        Long id,
        Long userId,
        AuthVendor authVendor,
        String ipAddress,
        LocalDateTime loggedInAt
) {
    public static GetLoginHistoryResult from(LoginHistory loginHistory) {
        return GetLoginHistoryResult.builder()
                .id(loginHistory.getId())
                .userId(loginHistory.getUser().getId())
                .authVendor(loginHistory.getAuthVendor())
                .ipAddress(loginHistory.getIpAddress().getValue())
                .loggedInAt(loginHistory.getCreatedAt())
                .build();
    }
}
