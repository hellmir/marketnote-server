package com.personal.marketnote.common.security.hmac.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HmacTimestampExpiredExceptionTest {

    @Test
    @DisplayName("timestamp 단일 인자 생성자는 cause 없이 메시지를 보존한다")
    void preservesMessageWithoutCause() {
        HmacTimestampExpiredException ex = new HmacTimestampExpiredException("not-a-number");

        assertThat(ex.getMessage()).contains("not-a-number");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    @DisplayName("timestamp와 cause 인자 생성자는 cause를 보존한다")
    void preservesCauseWhenProvided() {
        NumberFormatException root = new NumberFormatException("For input string: not-a-number");

        HmacTimestampExpiredException ex = new HmacTimestampExpiredException("not-a-number", root);

        assertThat(ex.getMessage()).contains("not-a-number");
        assertThat(ex.getCause()).isSameAs(root);
    }
}
