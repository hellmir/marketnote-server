package com.personal.marketnote.user.port.in.command;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.Builder;

@Builder
public record SignUpCommand(
        String email,
        String password,
        String verificationCode,
        String nickname,
        String fullName,
        String phoneNumber,
        String fingerprint
) {
    public boolean hasPassword() {
        return FormatValidator.hasValue(password);
    }

    public boolean hasPhoneNumber() {
        return FormatValidator.hasValue(phoneNumber);
    }

    public boolean hasFingerprint() {
        return FormatValidator.hasValue(fingerprint);
    }
}
