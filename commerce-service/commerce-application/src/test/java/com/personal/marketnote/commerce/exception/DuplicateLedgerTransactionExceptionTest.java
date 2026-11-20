package com.personal.marketnote.commerce.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DuplicateLedgerTransactionExceptionTest {

    @Test
    @DisplayName("idempotencyKey 단일 인자 생성자는 cause 없이 메시지를 보존한다")
    void preservesMessageWithoutCause() {
        DuplicateLedgerTransactionException ex =
                new DuplicateLedgerTransactionException("PG_SETTLEMENT:1");

        assertThat(ex.getMessage()).contains("PG_SETTLEMENT:1");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    @DisplayName("idempotencyKey와 cause 인자 생성자는 cause를 보존한다")
    void preservesCauseWhenProvided() {
        RuntimeException root = new RuntimeException("DataIntegrityViolation");

        DuplicateLedgerTransactionException ex =
                new DuplicateLedgerTransactionException("PG_SETTLEMENT:1", root);

        assertThat(ex.getMessage()).contains("PG_SETTLEMENT:1");
        assertThat(ex.getCause()).isSameAs(root);
    }
}
