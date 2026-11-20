package com.personal.marketnote.commerce.exception;

import com.personal.marketnote.common.domain.exception.DomainAlreadyExistsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DuplicatePaymentReadyExceptionTest {

    @Test
    @DisplayName("기본 생성자는 cause 없이 메시지를 보존한다")
    void preservesMessageWithoutCause() {
        DuplicatePaymentReadyException ex = new DuplicatePaymentReadyException();

        assertThat(ex.getMessage()).contains("거래 등록이 진행 중");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    @DisplayName("cause 인자 생성자는 cause를 보존한다")
    void preservesCauseWhenProvided() {
        RuntimeException root = new RuntimeException("DataIntegrityViolation");

        DuplicatePaymentReadyException ex = new DuplicatePaymentReadyException(root);

        assertThat(ex.getMessage()).contains("거래 등록이 진행 중");
        assertThat(ex.getCause()).isSameAs(root);
    }

    @Test
    @DisplayName("DomainAlreadyExistsException을 상속한다")
    void exception_isDomainAlreadyExistsException() {
        DuplicatePaymentReadyException ex = new DuplicatePaymentReadyException();

        assertThat(ex).isInstanceOf(DomainAlreadyExistsException.class);
    }
}
