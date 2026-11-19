package com.personal.marketnote.file.domain.file.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InvalidSvgFileExceptionTest {

    @Test
    @DisplayName("기본 생성자는 cause 없이 메시지를 보존한다")
    void preservesMessageWithoutCause() {
        InvalidSvgFileException ex = new InvalidSvgFileException();

        assertThat(ex.getMessage()).contains("ERR_FILE_SVG_01");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    @DisplayName("cause 인자 생성자는 stack trace 보존을 위해 cause를 전파한다")
    void preservesCauseWhenProvided() {
        RuntimeException root = new RuntimeException("SVG 파싱 실패");

        InvalidSvgFileException ex = new InvalidSvgFileException(root);

        assertThat(ex.getMessage()).contains("ERR_FILE_SVG_01");
        assertThat(ex.getCause()).isSameAs(root);
    }
}
