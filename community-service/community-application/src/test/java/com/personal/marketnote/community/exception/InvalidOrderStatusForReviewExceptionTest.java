package com.personal.marketnote.community.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InvalidOrderStatusForReviewExceptionTest {

    @Test
    @DisplayName("기본 생성자는 cause 없이 메시지를 보존한다")
    void preservesMessageWithoutCause() {
        InvalidOrderStatusForReviewException ex = new InvalidOrderStatusForReviewException();

        assertThat(ex.getMessage()).contains("ERR_REVIEW_02");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    @DisplayName("cause 인자 생성자는 cause를 보존한다")
    void preservesCauseWhenProvided() {
        RuntimeException root = new RuntimeException("409 Conflict");

        InvalidOrderStatusForReviewException ex = new InvalidOrderStatusForReviewException(root);

        assertThat(ex.getMessage()).contains("ERR_REVIEW_02");
        assertThat(ex.getCause()).isSameAs(root);
    }
}
