package com.personal.marketnote.user.exception;

public final class ExceptionMessage {
    public static final String USER_ID_NOT_FOUND_EXCEPTION_MESSAGE = "회원을 찾을 수 없습니다.";
    public static final String USER_OIDC_ID_NOT_FOUND_EXCEPTION_MESSAGE = "회원을 찾을 수 없습니다.";
    public static final String USER_EMAIL_NOT_FOUND_EXCEPTION_MESSAGE = "회원을 찾을 수 없습니다.";
    public static final String USER_PHONE_NUMBER_NOT_FOUND_EXCEPTION_MESSAGE = "회원을 찾을 수 없습니다.";
    public static final String USER_REFERENCE_CODE_NOT_FOUND_EXCEPTION_MESSAGE = "회원을 찾을 수 없습니다.";

    public static final String OIDC_ID_ALREADY_EXISTS_EXCEPTION_MESSAGE = "%s:: 이미 가입된 회원입니다.";
    public static final String NICKNAME_ALREADY_EXISTS_EXCEPTION_MESSAGE = "%s:: 이미 가입된 닉네임입니다.";
    public static final String PHONE_NUMBER_ALREADY_EXISTS_EXCEPTION_MESSAGE = "%s:: 이미 가입된 전화번호입니다.";
    public static final String EMAIL_ALREADY_EXISTS_EXCEPTION_MESSAGE = "%s:: 이미 가입된 이메일 주소입니다.";
    public static final String LOGIN_INFO_NO_VALUE_EXCEPTION_MESSAGE = "%s:: 회원 전화번호 또는 authVendor 및 oidcId 중 하나는 필수입니다.";
    public static final String LOGIN_FAILED_EXCEPTION_MESSAGE = "%s:: 아이디(이메일) 혹은 비밀번호가 올바르지 않습니다. 입력한 내용을 다시 확인해주세요.";
    public static final String NICKNAME_CONTAINS_PROFANITY_EXCEPTION_MESSAGE = "%s:: 닉네임에 부적절한 단어가 포함되어 있습니다.";

    private ExceptionMessage() {
    }
}
