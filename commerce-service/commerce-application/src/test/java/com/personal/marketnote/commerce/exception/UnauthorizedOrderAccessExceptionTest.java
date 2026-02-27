package com.personal.marketnote.commerce.exception;

import com.personal.marketnote.common.domain.exception.BusinessSecurityException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UnauthorizedOrderAccessExceptionTest {

    @Nested
    @DisplayName("UnauthorizedOrderAccessException 생성 검증")
    class CreateExceptionTest {

        @Test
        @DisplayName("예외 메시지에 에러 코드 ERR_ORDER_AUTH_01이 포함된다")
        void exceptionMessage_containsErrorCode() {
            UnauthorizedOrderAccessException exception = new UnauthorizedOrderAccessException();

            assertThat(exception.getMessage()).contains("ERR_ORDER_AUTH_01");
        }

        @Test
        @DisplayName("예외 메시지에 접근 권한 안내 문구가 포함된다")
        void exceptionMessage_containsGuideMessage() {
            UnauthorizedOrderAccessException exception = new UnauthorizedOrderAccessException();

            assertThat(exception.getMessage()).contains("해당 주문에 대한 접근 권한이 없습니다");
        }

        @Test
        @DisplayName("예외 메시지에 내부 ID가 노출되지 않는다")
        void exceptionMessage_doesNotContainInternalId() {
            UnauthorizedOrderAccessException exception = new UnauthorizedOrderAccessException();

            assertThat(exception.getMessage()).doesNotContain("123");
            assertThat(exception.getMessage()).doesNotContain("456");
            assertThat(exception.getMessage()).doesNotContain("주문 ID");
            assertThat(exception.getMessage()).doesNotContain("구매자 ID");
        }

        @Test
        @DisplayName("BusinessSecurityException을 상속한다")
        void exception_isBusinessSecurityException() {
            UnauthorizedOrderAccessException exception = new UnauthorizedOrderAccessException();

            assertThat(exception).isInstanceOf(BusinessSecurityException.class);
        }

        @Test
        @DisplayName("RuntimeException을 상속한다")
        void exception_isRuntimeException() {
            UnauthorizedOrderAccessException exception = new UnauthorizedOrderAccessException();

            assertThat(exception).isInstanceOf(RuntimeException.class);
        }
    }
}
