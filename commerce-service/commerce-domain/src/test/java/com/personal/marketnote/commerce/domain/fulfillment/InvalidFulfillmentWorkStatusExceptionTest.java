package com.personal.marketnote.commerce.domain.fulfillment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InvalidFulfillmentWorkStatusExceptionTest {

    @Test
    @DisplayName("workStatusCode 단일 인자 생성자는 cause 없이 메시지를 보존한다")
    void preservesMessageWithoutCause() {
        InvalidFulfillmentWorkStatusException ex = new InvalidFulfillmentWorkStatusException("UNKNOWN_CODE");

        assertThat(ex.getMessage()).contains("UNKNOWN_CODE");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    @DisplayName("workStatusCode와 cause 인자 생성자는 cause를 보존한다")
    void preservesCauseWhenProvided() {
        IllegalArgumentException root = new IllegalArgumentException("invalid enum");

        InvalidFulfillmentWorkStatusException ex =
            new InvalidFulfillmentWorkStatusException("UNKNOWN_CODE", root);

        assertThat(ex.getMessage()).contains("UNKNOWN_CODE");
        assertThat(ex.getCause()).isSameAs(root);
    }
}
