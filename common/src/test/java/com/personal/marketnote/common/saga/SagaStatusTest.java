package com.personal.marketnote.common.saga;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SagaStatus Enum 테스트")
class SagaStatusTest {

    @Test
    @DisplayName("isTerminal은 SUCCEEDED와 COMPENSATED만 true를 반환한다")
    void isTerminal_returnsTrueOnlyForSucceededAndCompensated() {
        assertThat(SagaStatus.SUCCEEDED.isTerminal()).isTrue();
        assertThat(SagaStatus.COMPENSATED.isTerminal()).isTrue();

        assertThat(SagaStatus.STARTED.isTerminal()).isFalse();
        assertThat(SagaStatus.PROCESSING.isTerminal()).isFalse();
        assertThat(SagaStatus.FAILED.isTerminal()).isFalse();
        assertThat(SagaStatus.COMPENSATING.isTerminal()).isFalse();
    }

    @Test
    @DisplayName("canProcess는 STARTED만 true를 반환한다")
    void canProcess_returnsTrueOnlyForStarted() {
        assertThat(SagaStatus.STARTED.canProcess()).isTrue();

        assertThat(SagaStatus.PROCESSING.canProcess()).isFalse();
        assertThat(SagaStatus.SUCCEEDED.canProcess()).isFalse();
        assertThat(SagaStatus.FAILED.canProcess()).isFalse();
        assertThat(SagaStatus.COMPENSATING.canProcess()).isFalse();
        assertThat(SagaStatus.COMPENSATED.canProcess()).isFalse();
    }

    @Test
    @DisplayName("canCompensate는 FAILED만 true를 반환한다")
    void canCompensate_returnsTrueOnlyForFailed() {
        assertThat(SagaStatus.FAILED.canCompensate()).isTrue();

        assertThat(SagaStatus.STARTED.canCompensate()).isFalse();
        assertThat(SagaStatus.PROCESSING.canCompensate()).isFalse();
        assertThat(SagaStatus.SUCCEEDED.canCompensate()).isFalse();
        assertThat(SagaStatus.COMPENSATING.canCompensate()).isFalse();
        assertThat(SagaStatus.COMPENSATED.canCompensate()).isFalse();
    }

    @Test
    @DisplayName("각 상태의 is 술어 메서드가 올바른 값을 반환한다")
    void predicateMethods_returnCorrectValues() {
        assertThat(SagaStatus.STARTED.isStarted()).isTrue();
        assertThat(SagaStatus.PROCESSING.isProcessing()).isTrue();
        assertThat(SagaStatus.SUCCEEDED.isSucceeded()).isTrue();
        assertThat(SagaStatus.FAILED.isFailed()).isTrue();
        assertThat(SagaStatus.COMPENSATING.isCompensating()).isTrue();
        assertThat(SagaStatus.COMPENSATED.isCompensated()).isTrue();

        assertThat(SagaStatus.STARTED.isProcessing()).isFalse();
        assertThat(SagaStatus.PROCESSING.isStarted()).isFalse();
    }
}
