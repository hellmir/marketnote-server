package com.personal.marketnote.user.adapter.in.web.user.mapper;

import com.personal.marketnote.user.adapter.in.web.user.request.SignInRequest;
import com.personal.marketnote.user.adapter.in.web.user.request.SignUpRequest;
import com.personal.marketnote.user.adapter.in.web.user.request.UpdateUserInfoRequest;
import com.personal.marketnote.user.adapter.in.web.user.request.VerifyCodeRequest;
import com.personal.marketnote.user.port.in.command.SignInCommand;
import com.personal.marketnote.user.port.in.command.SignUpCommand;
import com.personal.marketnote.user.port.in.command.UpdateUserInfoCommand;
import com.personal.marketnote.user.port.in.command.VerifyCodeCommand;

public class UserRequestToCommandMapper {
    public static SignUpCommand mapToCommand(SignUpRequest signUpRequest) {
        return SignUpCommand.builder()
                .email(signUpRequest.getEmail())
                .password(signUpRequest.getPassword())
                .verificationCode(signUpRequest.getVerificationCode())
                .nickname(signUpRequest.getNickname())
                .fullName(signUpRequest.getFullName())
                .phoneNumber(signUpRequest.getPhoneNumber())
                .build();
    }

    public static SignInCommand mapToCommand(SignInRequest signInRequest) {
        return SignInCommand.of(signInRequest.getEmail(), signInRequest.getPassword());
    }

    public static UpdateUserInfoCommand mapToCommand(UpdateUserInfoRequest updateUserInfoRequest) {
        return UpdateUserInfoCommand.builder()
                .isActive(updateUserInfoRequest.getIsActive())
                .email(updateUserInfoRequest.getEmail())
                .nickname(updateUserInfoRequest.getNickname())
                .phoneNumber(updateUserInfoRequest.getPhoneNumber())
                .password(updateUserInfoRequest.getPassword())
                .build();
    }

    public static VerifyCodeCommand mapToCommand(VerifyCodeRequest verifyCodeRequest) {
        return VerifyCodeCommand.of(
                verifyCodeRequest.getEmail(),
                verifyCodeRequest.getVerificationCode()
        );
    }
}
