package com.personal.marketnote.reward.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DuplicateAttendanceExceptionTest {

    @Test
    @DisplayName("기본 생성자는 cause 없이 메시지를 보존한다")
    void preservesMessageWithoutCause() {
        DuplicateAttendanceException ex = new DuplicateAttendanceException();

        assertThat(ex.getMessage()).contains("ERR_ATTENDANCE_01");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    @DisplayName("cause 인자 생성자는 cause를 보존한다")
    void preservesCauseWhenProvided() {
        RuntimeException root = new RuntimeException("constraint violation");

        DuplicateAttendanceException ex = new DuplicateAttendanceException(root);

        assertThat(ex.getMessage()).contains("ERR_ATTENDANCE_01");
        assertThat(ex.getCause()).isSameAs(root);
    }
}
