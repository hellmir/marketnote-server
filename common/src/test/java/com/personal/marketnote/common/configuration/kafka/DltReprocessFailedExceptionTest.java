package com.personal.marketnote.common.configuration.kafka;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DltReprocessFailedExceptionTest {

    @Test
    @DisplayName("originalTopic 단일 인자 생성자는 cause 없이 메시지를 보존한다")
    void preservesMessageWithoutCause() {
        DltReprocessFailedException ex = new DltReprocessFailedException("order-events");

        assertThat(ex.getMessage()).contains("order-events");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    @DisplayName("originalTopic과 cause 인자 생성자는 cause를 보존한다")
    void preservesCauseWhenProvided() {
        RuntimeException root = new RuntimeException("kafka send failed");

        DltReprocessFailedException ex = new DltReprocessFailedException("order-events", root);

        assertThat(ex.getMessage()).contains("order-events");
        assertThat(ex.getCause()).isSameAs(root);
    }
}
