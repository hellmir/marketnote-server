package com.personal.marketnote.user.adapter.in.web.user.response;

import com.personal.marketnote.user.port.in.result.WithdrawResult;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;

import java.util.Map;

public record WithdrawResponse(
        boolean isKakaoDisconnected,
        boolean isGoogleDisconnected,
        boolean isAppleDisconnected
) {
    public static WithdrawResponse from(WithdrawResult result) {
        Map<AuthVendor, Boolean> disconnectResults = result.disconnectResults();
        return new WithdrawResponse(
                disconnectResults.getOrDefault(AuthVendor.KAKAO, true),
                disconnectResults.getOrDefault(AuthVendor.GOOGLE, true),
                disconnectResults.getOrDefault(AuthVendor.APPLE, true)
        );
    }
}
