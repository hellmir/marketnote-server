package com.personal.marketnote.reward.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InvalidGifticonOrderStatusFilterExceptionTest {

    @Test
    @DisplayName("value 생성자는 cause 없이 메시지를 보존한다")
    void preservesMessageWithoutCause() {
        InvalidGifticonOrderStatusFilterException ex = new InvalidGifticonOrderStatusFilterException("UNKNOWN");

        assertThat(ex.getMessage()).contains("UNKNOWN");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    @DisplayName("cause 인자 생성자는 cause를 보존한다")
    void preservesCauseWhenProvided() {
        IllegalArgumentException root = new IllegalArgumentException("No enum constant");

        InvalidGifticonOrderStatusFilterException ex = new InvalidGifticonOrderStatusFilterException("UNKNOWN", root);

        assertThat(ex.getMessage()).contains("UNKNOWN");
        assertThat(ex.getCause()).isSameAs(root);
    }
}
