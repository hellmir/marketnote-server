package com.personal.marketnote.common.domain.exception;

import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.InvalidIdException;
import com.personal.marketnote.common.logging.PiiMaskingConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.personal.marketnote.common.domain.exception.ExceptionMessage.INVALID_EMAIL_EXCEPTION_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;

class ExceptionMessageTest {

    @Test
    @DisplayName("INVALID_EMAIL_EXCEPTION_MESSAGE 는 %s 리터럴을 포함하지 않는다")
    void invalidEmailMessageDoesNotContainFormatSpecifier() {
        assertThat(INVALID_EMAIL_EXCEPTION_MESSAGE).doesNotContain("%s");
    }

    @Test
    @DisplayName("INVALID_EMAIL_EXCEPTION_MESSAGE 는 줄바꿈을 포함하지 않는다")
    void invalidEmailMessageDoesNotContainLineBreak() {
        assertThat(INVALID_EMAIL_EXCEPTION_MESSAGE)
                .doesNotContain("\n")
                .doesNotContain("\r");
    }

    @Test
    @DisplayName("INVALID_EMAIL_EXCEPTION_MESSAGE 는 PiiMaskingConverter 통과 후 원문과 동일하다 (마스킹 대상 이메일 토큰 미포함)")
    void invalidEmailMessageUnchangedAfterPiiMasking() {
        // 현재 PiiMaskingConverter 는 "email=" prefix 토큰만 마스킹. 메시지 내 예시 이메일(abcd@abc.com)은 원문 보존.
        String masked = PiiMaskingConverter.mask(INVALID_EMAIL_EXCEPTION_MESSAGE);

        assertThat(masked).isEqualTo(INVALID_EMAIL_EXCEPTION_MESSAGE);
    }

    @Nested
    @DisplayName("InvalidIdException")
    class InvalidIdExceptionTest {

        @Test
        @DisplayName("InvalidIdException 은 전송된 ID 값을 message 에 치환한다")
        void invalidIdExceptionMessageContainsValue() {
            InvalidIdException exception = new InvalidIdException("123");

            assertThat(exception.getMessage())
                    .isEqualTo("ID는 양의 정수(1 이상의 숫자값)여야 합니다. 전송된 ID: 123");
        }

        @Test
        @DisplayName("InvalidIdException message 는 %s 리터럴을 포함하지 않는다")
        void invalidIdExceptionMessageDoesNotContainFormatSpecifier() {
            InvalidIdException exception = new InvalidIdException("123");

            assertThat(exception.getMessage()).doesNotContain("%s");
        }

        @Test
        @DisplayName("InvalidIdException message 는 잘못된 값도 그대로 치환한다")
        void invalidIdExceptionMessageContainsInvalidValueAsIs() {
            InvalidIdException exception = new InvalidIdException("abc");

            assertThat(exception.getMessage())
                    .isEqualTo("ID는 양의 정수(1 이상의 숫자값)여야 합니다. 전송된 ID: abc");
        }
    }
}
