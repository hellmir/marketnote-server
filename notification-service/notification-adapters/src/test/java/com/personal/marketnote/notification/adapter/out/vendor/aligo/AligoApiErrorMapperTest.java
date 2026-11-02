package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import com.personal.marketnote.notification.domain.aligo.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AligoApiErrorMapperTest {

    @Test
    @DisplayName("result_code가 0이면 예외를 던지지 않는다")
    void shouldNotThrowWhenResultCodeIsZero() {
        assertThatCode(() -> AligoApiErrorMapper.throwIfError(0, "성공"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("result_code가 -101이면 AligoAuthenticationException을 던진다")
    void shouldThrowAuthenticationExceptionWhenCodeIsMinus101() {
        assertThatThrownBy(() -> AligoApiErrorMapper.throwIfError(-101, "인증 오류"))
                .isInstanceOf(AligoAuthenticationException.class);
    }

    @Test
    @DisplayName("result_code가 -99이고 메시지에 '포인트'가 포함되면 AligoInsufficientPointException을 던진다")
    void shouldThrowInsufficientPointExceptionWhenCodeMinus99AndMessageContainsPoint() {
        assertThatThrownBy(() -> AligoApiErrorMapper.throwIfError(-99, "포인트가 부족합니다"))
                .isInstanceOf(AligoInsufficientPointException.class);
    }

    @Test
    @DisplayName("result_code가 -99이고 메시지에 '파라미터'가 포함되면 AligoParameterMissingException을 던진다")
    void shouldThrowParameterMissingExceptionWhenCodeMinus99AndMessageContainsParameter() {
        assertThatThrownBy(() -> AligoApiErrorMapper.throwIfError(-99, "필수 파라미터가 누락되었습니다"))
                .isInstanceOf(AligoParameterMissingException.class);
    }

    @Test
    @DisplayName("result_code가 -99이고 메시지에 '발송 5분전'이 포함되면 AligoCancelDeadlineExceededException을 던진다")
    void shouldThrowCancelDeadlineExceededExceptionWhenCodeMinus99AndMessageContainsDeadline() {
        assertThatThrownBy(() -> AligoApiErrorMapper.throwIfError(-99, "발송 5분전까지만 취소 가능합니다"))
                .isInstanceOf(AligoCancelDeadlineExceededException.class);
    }

    @Test
    @DisplayName("result_code가 509이면 AligoTemplateNotModifiableException을 던진다")
    void shouldThrowTemplateNotModifiableExceptionWhenCodeIs509() {
        assertThatThrownBy(() -> AligoApiErrorMapper.throwIfError(509, "수정 가능 상태가 아닙니다"))
                .isInstanceOf(AligoTemplateNotModifiableException.class);
    }

    @Test
    @DisplayName("알 수 없는 에러 코드면 AligoApiRequestFailedException을 던진다")
    void shouldThrowApiRequestFailedExceptionWhenCodeIsUnknown() {
        assertThatThrownBy(() -> AligoApiErrorMapper.throwIfError(-999, "알 수 없는 오류"))
                .isInstanceOf(AligoApiRequestFailedException.class);
    }
}
