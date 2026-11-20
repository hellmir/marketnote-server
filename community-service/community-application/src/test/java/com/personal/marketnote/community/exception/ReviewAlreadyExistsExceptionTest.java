package com.personal.marketnote.community.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewAlreadyExistsExceptionTest {

    @Test
    @DisplayName("기본 생성자는 cause 없이 메시지를 보존한다")
    void preservesMessageWithoutCause() {
        ReviewAlreadyExistsException ex = new ReviewAlreadyExistsException();

        assertThat(ex.getMessage()).contains("이미 해당 주문에 대한 리뷰가 존재합니다.");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    @DisplayName("cause 인자 생성자는 cause를 보존한다")
    void preservesCauseWhenProvided() {
        RuntimeException root = new RuntimeException("constraint violation");

        ReviewAlreadyExistsException ex = new ReviewAlreadyExistsException(root);

        assertThat(ex.getMessage()).contains("이미 해당 주문에 대한 리뷰가 존재합니다.");
        assertThat(ex.getCause()).isSameAs(root);
    }
}
