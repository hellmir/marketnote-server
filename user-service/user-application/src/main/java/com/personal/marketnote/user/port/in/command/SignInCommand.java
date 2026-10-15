package com.personal.marketnote.user.port.in.command;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static lombok.AccessLevel.PRIVATE;

@AllArgsConstructor(access = PRIVATE)
@Getter
public class SignInCommand {
    private final String email;
    private final String password;
    private final String fingerprint;

    public static SignInCommand of(String email, String password) {
        return new SignInCommand(email, password, null);
    }

    public static SignInCommand of(String email, String password, String fingerprint) {
        return new SignInCommand(email, password, fingerprint);
    }

    public boolean hasFingerprint() {
        return FormatValidator.hasValue(fingerprint);
    }
}
