package com.personal.marketnote.common.configuration.kafka;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DltResolveFailedExceptionTest {

    @Test
    @DisplayName("topic/partition/offset 인자 생성자는 cause 없이 메시지를 보존한다")
    void preservesMessageWithoutCause() {
        DltResolveFailedException ex = new DltResolveFailedException("order-events", 0, 100L);

        assertThat(ex.getMessage()).contains("order-events").contains("partition=0").contains("offset=100");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    @DisplayName("cause 인자 생성자는 cause를 보존한다")
    void preservesCauseWhenProvided() {
        RuntimeException root = new RuntimeException("DLT save failed");

        DltResolveFailedException ex = new DltResolveFailedException("order-events", 0, 100L, root);

        assertThat(ex.getMessage()).contains("order-events");
        assertThat(ex.getCause()).isSameAs(root);
    }
}
