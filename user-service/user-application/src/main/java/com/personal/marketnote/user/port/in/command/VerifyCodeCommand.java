package com.personal.marketnote.user.port.in.command;

import lombok.Getter;

@Getter
public class VerifyCodeCommand {
    private final String email;
    private final String verificationCode;

    private VerifyCodeCommand(String email, String verificationCode) {
        this.email = email;
        this.verificationCode = verificationCode;
    }

    public static VerifyCodeCommand of(String email, String verificationCode) {
        return new VerifyCodeCommand(email, verificationCode);
    }
}
