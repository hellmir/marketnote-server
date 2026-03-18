package com.personal.marketnote.user.adapter.in.web.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

import static com.personal.marketnote.common.domain.exception.ExceptionMessage.INVALID_EMAIL_EXCEPTION_MESSAGE;
import static com.personal.marketnote.common.utility.RegularExpressionConstant.*;

@Getter
public class SignUpRequest {
    @Schema(
            name = "email",
            description = "이메일 주소",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "이메일 주소는 필수값입니다.")
    @Pattern(regexp = EMAIL_PATTERN, message = INVALID_EMAIL_EXCEPTION_MESSAGE)
    private String email;

    @Schema(
            name = "password",
            description = "비밀번호",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Pattern(regexp = PASSWORD_PATTERN, message = "비밀번호는 8자 이상, 대문자, 소문자, 숫자, 특수문자를 포함해야 합니다.")
    private String password;

    @Schema(
            name = "verificationCode",
            description = "인증 코드",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "인증 코드는 필수값입니다.")
    private String verificationCode;

    @Schema(
            name = "nickname",
            description = "닉네임",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Pattern(regexp = NICKNAME_PATTERN, message = "닉네임은 한글, 영어 대소문자, 숫자만 가능하며, 2~10글자여야 합니다.")
    private String nickname;

    @Schema(
            name = "fullName",
            description = "성명",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Pattern(regexp = FULL_NAME_PATTERN, message = "성명은 2글자 이상, 10글자 이하여야 합니다.")
    private String fullName;

    @Schema(
            name = "phoneNumber",
            description = "전화번호",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Pattern(regexp = PHONE_NUMBER_PATTERN, message = "전화번호 형식이 올바르지 않습니다.")
    private String phoneNumber;
}
