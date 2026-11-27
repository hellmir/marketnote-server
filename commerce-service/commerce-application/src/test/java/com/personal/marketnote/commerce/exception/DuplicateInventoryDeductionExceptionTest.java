package com.personal.marketnote.commerce.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DuplicateInventoryDeductionExceptionTest {

    @Test
    @DisplayName("orderId 단일 인자 생성자는 cause 없이 메시지를 보존한다")
    void preservesMessageWithoutCause() {
        DuplicateInventoryDeductionException ex = new DuplicateInventoryDeductionException(100L);

        assertThat(ex.getMessage()).contains("100");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    @DisplayName("orderId와 cause 인자 생성자는 cause를 보존한다")
    void preservesCauseWhenProvided() {
        RuntimeException root = new RuntimeException("DataIntegrityViolation");

        DuplicateInventoryDeductionException ex =
            new DuplicateInventoryDeductionException(100L, root);

        assertThat(ex.getMessage()).contains("100");
        assertThat(ex.getCause()).isSameAs(root);
    }
}
