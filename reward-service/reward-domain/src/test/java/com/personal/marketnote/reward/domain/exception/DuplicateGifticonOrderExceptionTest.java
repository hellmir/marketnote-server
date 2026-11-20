package com.personal.marketnote.reward.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DuplicateGifticonOrderExceptionTest {

    @Test
    @DisplayName("기본 생성자는 cause 없이 메시지를 보존한다")
    void preservesMessageWithoutCause() {
        DuplicateGifticonOrderException ex = new DuplicateGifticonOrderException();

        assertThat(ex.getMessage()).contains("ERR_GIFTICON_ORDER_02");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    @DisplayName("cause 인자 생성자는 cause를 보존한다")
    void preservesCauseWhenProvided() {
        RuntimeException root = new RuntimeException("trId unique constraint violation");

        DuplicateGifticonOrderException ex = new DuplicateGifticonOrderException(root);

        assertThat(ex.getMessage()).contains("ERR_GIFTICON_ORDER_02");
        assertThat(ex.getCause()).isSameAs(root);
    }
}
