package com.personal.marketnote.user.adapter.in.web.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import static com.personal.marketnote.common.utility.RegularExpressionConstant.*;

@Getter
public class SignUpRequest {
    @Schema(
            name = "email",
            description = "이메일 주소",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "이메일 주소는 필수값입니다.")
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
    private String phoneNumber;

    @Schema(
            name = "fingerprint",
            description = "디바이스 핑거프린트",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Pattern(regexp = "^[a-zA-Z0-9\\-_]+$", message = "핑거프린트는 영숫자, 하이픈, 언더스코어만 허용합니다.")
    @Size(max = 255, message = "핑거프린트는 255자를 초과할 수 없습니다.")
    private String fingerprint;
}
