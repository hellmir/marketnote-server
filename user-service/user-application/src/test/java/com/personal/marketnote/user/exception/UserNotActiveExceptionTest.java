package com.personal.marketnote.user.exception;

import com.personal.marketnote.common.logging.PiiMaskingConverter;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserNotActiveExceptionTest {

    private static final String ERROR_CODE = "ERR_USER_01";
    private static final String EMAIL = "foo@bar.com";

    @Test
    @DisplayName("이메일 생성자는 email={email} prefix 포맷을 따르고 자연어 '전송된 이메일 주소' 문구를 포함하지 않는다")
    void shouldUseEmailPrefixFormatAndNotContainNaturalLanguage() {
        // given
        UserNotActiveException exception = new UserNotActiveException(ERROR_CODE, EMAIL);

        // when
        String message = exception.getMessage();

        // then
        assertThat(message).contains("email=" + EMAIL);
        assertThat(message).doesNotContain("전송된 이메일 주소");
        assertThat(message).contains(ERROR_CODE);
        assertThat(message).contains("비활성화된 계정입니다");
    }

    @Test
    @DisplayName("PiiMaskingConverter.mask로 예외 메시지 마스킹 시 email 평문이 제거되고 마스킹 토큰이 포함된다")
    void shouldMaskEmailWhenMessagePassedThroughPiiMaskingConverter() {
        // given
        UserNotActiveException exception = new UserNotActiveException(ERROR_CODE, EMAIL);
        String rawMessage = exception.getMessage();

        // when
        String masked = PiiMaskingConverter.mask(rawMessage);

        // then
        assertThat(masked).doesNotContain(EMAIL);
        assertThat(masked).contains("f***@bar.com");
        assertThat(masked).contains(ERROR_CODE);
    }

    @Test
    @DisplayName("AuthVendor 생성자는 벤더명을 그대로 포함하며 PII 마스킹 대상이 아니다")
    void shouldKeepAuthVendorNameAsIs() {
        // given
        UserNotActiveException exception = new UserNotActiveException(ERROR_CODE, AuthVendor.KAKAO);

        // when
        String message = exception.getMessage();
        String masked = PiiMaskingConverter.mask(message);

        // then
        assertThat(message).contains("인증 벤더: KAKAO");
        assertThat(masked).isEqualTo(message);
    }
}
