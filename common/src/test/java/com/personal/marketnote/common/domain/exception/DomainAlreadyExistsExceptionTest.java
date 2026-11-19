package com.personal.marketnote.common.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DomainAlreadyExistsException 테스트")
class DomainAlreadyExistsExceptionTest {

    @Test
    @DisplayName("기본 생성자는 cause 없이 메시지를 보존한다")
    void preservesMessage_withoutCause() {
        // when
        DomainAlreadyExistsException ex = new DomainAlreadyExistsException("이미 존재합니다");

        // then
        assertThat(ex.getMessage()).isEqualTo("이미 존재합니다");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    @DisplayName("cause 인자 생성자는 cause 를 보존한다")
    void preservesCause() {
        // given
        Throwable cause = new IllegalStateException("DB 충돌");

        // when
        DomainAlreadyExistsException ex = new DomainAlreadyExistsException("이미 존재합니다", cause);

        // then
        assertThat(ex.getMessage()).isEqualTo("이미 존재합니다");
        assertThat(ex.getCause()).isSameAs(cause);
    }
}
