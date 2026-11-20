package com.personal.marketnote.reward.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DuplicateUserPointHistoryExceptionTest {

    @Test
    @DisplayName("기본 생성자는 cause 없이 메시지를 보존한다")
    void preservesMessageWithoutCause() {
        DuplicateUserPointHistoryException ex = new DuplicateUserPointHistoryException();

        assertThat(ex.getMessage()).contains("ERR_POINT_HISTORY_03");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    @DisplayName("cause 인자 생성자는 cause를 보존한다")
    void preservesCauseWhenProvided() {
        RuntimeException root = new RuntimeException("constraint violation");

        DuplicateUserPointHistoryException ex = new DuplicateUserPointHistoryException(root);

        assertThat(ex.getMessage()).contains("ERR_POINT_HISTORY_03");
        assertThat(ex.getCause()).isSameAs(root);
    }
}
