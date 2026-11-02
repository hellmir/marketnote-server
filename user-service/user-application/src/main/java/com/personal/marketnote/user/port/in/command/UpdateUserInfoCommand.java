package com.personal.marketnote.user.port.in.command;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.Builder;

@Builder
public record UpdateUserInfoCommand(
        Boolean isActive,
        String email,
        String nickname,
        String phoneNumber,
        String password
) {

    public boolean hasIsActive() {
        return FormatValidator.hasValue(isActive);
    }

    public boolean hasPassword() {
        return FormatValidator.hasValue(password);
    }

    public boolean hasEmail() {
        return FormatValidator.hasValue(email);
    }

    public boolean hasNickname() {
        return FormatValidator.hasValue(nickname);
    }

    public boolean hasPhoneNumber() {
        return FormatValidator.hasValue(phoneNumber);
    }
}
