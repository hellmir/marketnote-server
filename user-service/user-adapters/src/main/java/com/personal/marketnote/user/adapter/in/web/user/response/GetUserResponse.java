package com.personal.marketnote.user.adapter.in.web.user.response;

import com.personal.marketnote.user.port.in.result.AccountInfoResult;
import com.personal.marketnote.user.port.in.result.GetUserResult;
import lombok.AccessLevel;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder(access = AccessLevel.PRIVATE)
public record GetUserResponse(
        Long id,
        AccountInfoResult accountInfo,
        String nickname,
        String email,
        String fullName,
        String phoneNumber,
        String referenceCode,
        String roleId,
        LocalDateTime signedUpAt,
        LocalDateTime lastLoggedInAt,
        String status,
        boolean isWithdrawn,
        Long orderNum,
        int penaltyCount
) {
    public static GetUserResponse from(GetUserResult getUserResult) {
        return GetUserResponse.builder()
                .id(getUserResult.id())
                .accountInfo(getUserResult.accountInfo())
                .nickname(getUserResult.nickname())
                .email(getUserResult.email())
                .fullName(getUserResult.fullName())
                .phoneNumber(getUserResult.phoneNumber())
                .referenceCode(getUserResult.referenceCode())
                .roleId(getUserResult.roleId())
                .signedUpAt(getUserResult.signedUpAt())
                .lastLoggedInAt(getUserResult.lastLoggedInAt())
                .status(getUserResult.status())
                .isWithdrawn(getUserResult.isWithdrawn())
                .orderNum(getUserResult.orderNum())
                .penaltyCount(getUserResult.penaltyCount())
                .build();
    }
}
