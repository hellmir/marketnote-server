package com.personal.marketnote.common.saga.log;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SagaContextTest {

    @Test
    @DisplayName("restore()는 주어진 sagaId/sagaType/sagaStep을 그대로 사용한다")
    void shouldRestoreWithGivenValues() {
        SagaContext context = SagaContext.restore("saga-123", "ORDER_CANCEL", "STEP_B");

        assertThat(context.sagaId()).isEqualTo("saga-123");
        assertThat(context.sagaType()).isEqualTo("ORDER_CANCEL");
        assertThat(context.sagaStep()).isEqualTo("STEP_B");
    }
}
