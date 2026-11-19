package com.personal.marketnote.common.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DomainNotFoundException 테스트")
class DomainNotFoundExceptionTest {

    @Test
    @DisplayName("메시지 생성자는 cause 없이 메시지를 보존한다")
    void preservesMessage_withoutCause() {
        // when
        DomainNotFoundException ex = new DomainNotFoundException("찾을 수 없습니다");

        // then
        assertThat(ex.getMessage()).isEqualTo("찾을 수 없습니다");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    @DisplayName("cause 인자 생성자는 cause 를 보존한다")
    void preservesCause() {
        // given
        Throwable cause = new IllegalStateException("외부 조회 실패");

        // when
        DomainNotFoundException ex = new DomainNotFoundException("찾을 수 없습니다", cause);

        // then
        assertThat(ex.getMessage()).isEqualTo("찾을 수 없습니다");
        assertThat(ex.getCause()).isSameAs(cause);
    }
}
