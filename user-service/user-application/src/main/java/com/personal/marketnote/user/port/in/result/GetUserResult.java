package com.personal.marketnote.user.port.in.result;

import com.personal.marketnote.common.domain.email.Email;
import com.personal.marketnote.common.domain.phonenumber.PhoneNumber;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.user.domain.user.User;
import lombok.AccessLevel;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder(access = AccessLevel.PRIVATE)
public record GetUserResult(
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
    public static GetUserResult from(User user) {
        return GetUserResult.builder()
                .id(user.getId())
                .accountInfo(AccountInfoResult.from(user.getUserAuthProviders()))
                .nickname(user.getNickname())
                .email(toEmailValue(user.getEmail()))
                .fullName(user.getFullName())
                .phoneNumber(toPhoneNumberValue(user.getPhoneNumber()))
                .referenceCode(user.getReferenceCode())
                .roleId(user.getRole().getId())
                .signedUpAt(user.getSignedUpAt())
                .lastLoggedInAt(user.getLastLoggedInAt())
                .status(user.getStatus().name())
                .isWithdrawn(user.isWithdrawn())
                .orderNum(user.getOrderNum())
                .build();
    }

    private static String toPhoneNumberValue(PhoneNumber phoneNumber) {
        if (FormatValidator.hasNoValue(phoneNumber)) {
            return null;
        }
        return phoneNumber.getValue();
    }

    private static String toEmailValue(Email email) {
        if (FormatValidator.hasNoValue(email)) {
            return null;
        }
        return email.getValue();
    }
}
