package com.personal.marketnote.commerce.exception;

import com.personal.marketnote.common.domain.exception.BusinessSecurityException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UnauthorizedOrderStatusChangeExceptionTest {

    @Nested
    @DisplayName("UnauthorizedOrderStatusChangeException 생성 검증")
    class CreateExceptionTest {

        @Test
        @DisplayName("예외 메시지에 에러 코드 ERR_ORDER_AUTH_02가 포함된다")
        void exceptionMessage_containsErrorCode() {
            UnauthorizedOrderStatusChangeException exception
                    = new UnauthorizedOrderStatusChangeException();

            assertThat(exception.getMessage()).contains("ERR_ORDER_AUTH_02");
        }

        @Test
        @DisplayName("예외 메시지에 권한 안내 문구가 포함된다")
        void exceptionMessage_containsGuideMessage() {
            UnauthorizedOrderStatusChangeException exception
                    = new UnauthorizedOrderStatusChangeException();

            assertThat(exception.getMessage()).contains("해당 상태로 변경할 권한이 없습니다");
        }

        @Test
        @DisplayName("예외 메시지에 역할 정보가 노출되지 않는다")
        void exceptionMessage_doesNotContainRole() {
            UnauthorizedOrderStatusChangeException exception
                    = new UnauthorizedOrderStatusChangeException();

            assertThat(exception.getMessage()).doesNotContain("BUYER");
            assertThat(exception.getMessage()).doesNotContain("ADMIN");
            assertThat(exception.getMessage()).doesNotContain("SELLER");
        }

        @Test
        @DisplayName("예외 메시지에 상태 설명이 노출되지 않는다")
        void exceptionMessage_doesNotContainStatusDescription() {
            UnauthorizedOrderStatusChangeException exception
                    = new UnauthorizedOrderStatusChangeException();

            assertThat(exception.getMessage()).doesNotContain("결제 완료");
            assertThat(exception.getMessage()).doesNotContain("상품 준비중");
        }

        @Test
        @DisplayName("BusinessSecurityException을 상속한다")
        void exception_isBusinessSecurityException() {
            UnauthorizedOrderStatusChangeException exception
                    = new UnauthorizedOrderStatusChangeException();

            assertThat(exception).isInstanceOf(BusinessSecurityException.class);
        }

        @Test
        @DisplayName("RuntimeException을 상속한다")
        void exception_isRuntimeException() {
            UnauthorizedOrderStatusChangeException exception
                    = new UnauthorizedOrderStatusChangeException();

            assertThat(exception).isInstanceOf(RuntimeException.class);
        }
    }
}
