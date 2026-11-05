package com.personal.marketnote.user.adapter.in.web.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

import static com.personal.marketnote.common.utility.RegularExpressionConstant.NICKNAME_PATTERN;
import static com.personal.marketnote.common.utility.RegularExpressionConstant.PHONE_NUMBER_INPUT_PATTERN;

// FIXME: password 정규식 재적용 (별도 이슈)
@Getter
public class UpdateUserInfoRequest {
    @Schema(
            name = "isActive",
            description = "계정 활성화 여부 (true: 활성화, false: 비활성화)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Boolean isActive;

    @Schema(
            name = "email",
            description = "이메일 주소",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String email;

    @Schema(
            name = "nickname",
            description = "닉네임",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Pattern(regexp = NICKNAME_PATTERN, message = "닉네임은 한글, 영어 대소문자, 숫자만 가능하며, 2~10글자여야 합니다.")
    private String nickname;

    @Schema(
            name = "phoneNumber",
            description = "전화번호 (예: 01012345678 또는 010-1234-5678, 서버 내부 canonical 저장)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Pattern(
            regexp = PHONE_NUMBER_INPUT_PATTERN,
            message = "전화번호 형식이 올바르지 않습니다. (예: 01012345678, 010-1234-5678)"
    )
    private String phoneNumber;

    @Schema(
            name = "password",
            description = "비밀번호",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
//    @Pattern(regexp = PASSWORD_PATTERN, message = "비밀번호는 8자 이상, 대문자, 소문자, 숫자, 특수문자를 포함해야 합니다.")
    private String password;
}
