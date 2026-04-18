package com.personal.marketnote.user.port.in.result;

import com.personal.marketnote.user.domain.user.User;
import lombok.AccessLevel;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder(access = AccessLevel.PRIVATE)
public record GetUserInfoResult(
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
        Long orderNum
) {
    public static GetUserInfoResult from(User user) {
        return GetUserInfoResult.builder()
                .id(user.getId())
                .accountInfo(AccountInfoResult.from(user.getUserAuthProviders()))
                .nickname(user.getNickname())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .referenceCode(user.getReferenceCode())
                .roleId(user.getRole().getId())
                .signedUpAt(user.getSignedUpAt())
                .lastLoggedInAt(user.getLastLoggedInAt())
                .status(user.getStatus().name())
                .isWithdrawn(user.isWithdrawn())
                .orderNum(user.getOrderNum())
                .build();
    }
}
