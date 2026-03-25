package com.personal.marketnote.common.saga;

import com.personal.marketnote.common.saga.exception.InvalidSagaStatusTransitionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("SagaInstance 도메인 테스트")
class SagaInstanceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-03-16T10:00:00Z"), ZoneId.of("Asia/Seoul")
    );

    private SagaInstance createStartedInstance() {
        SagaInstanceCreateState state = new SagaInstanceCreateState(
                "saga-id-1", "OrderPayment", "{\"orderId\":1}"
        );
        return SagaInstance.from(state, FIXED_CLOCK);
    }

    private SagaInstance createProcessingInstance() {
        SagaInstance instance = createStartedInstance();
        instance.process();
        return instance;
    }

    private SagaInstance createFailedInstance() {
        SagaInstance instance = createProcessingInstance();
        instance.fail();
        return instance;
    }

    private SagaInstance createCompensatingInstance() {
        SagaInstance instance = createFailedInstance();
        instance.rollback();
        return instance;
    }

    @Nested
    @DisplayName("from(CreateState) 팩토리 메서드")
    class FromCreateState {

        @Test
        @DisplayName("생성 시 STARTED 상태와 currentStepIndex 0으로 초기화된다")
        void initializesStartedStatusAndZeroStepIndex() {
            // when
            SagaInstance instance = createStartedInstance();

            // then
            assertThat(instance.getStatus()).isEqualTo(SagaStatus.STARTED);
            assertThat(instance.getCurrentStepIndex()).isZero();
        }

        @Test
        @DisplayName("생성 시 Clock 기반 createdAt과 modifiedAt이 설정된다")
        void setsCreatedAtAndModifiedAtFromClock() {
            // when
            SagaInstance instance = createStartedInstance();

            // then
            LocalDateTime expectedTime = LocalDateTime.now(FIXED_CLOCK);
            assertThat(instance.getCreatedAt()).isEqualTo(expectedTime);
            assertThat(instance.getModifiedAt()).isEqualTo(expectedTime);
            assertThat(instance.getCompletedAt()).isNull();
        }

        @Test
        @DisplayName("생성 시 전달된 필드가 올바르게 설정된다")
        void setsAllFields() {
            // when
            SagaInstance instance = createStartedInstance();

            // then
            assertThat(instance.getSagaId()).isEqualTo("saga-id-1");
            assertThat(instance.getSagaType()).isEqualTo("OrderPayment");
            assertThat(instance.getPayload()).isEqualTo("{\"orderId\":1}");
        }

        @Test
        @DisplayName("sagaId가 null이면 IllegalArgumentException이 발생한다")
        void throwsExceptionWhenSagaIdIsNull() {
            // given
            SagaInstanceCreateState state = new SagaInstanceCreateState(null, "OrderPayment", "{}");

            // when & then
            assertThatThrownBy(() -> SagaInstance.from(state, FIXED_CLOCK))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("sagaType이 null이면 IllegalArgumentException이 발생한다")
        void throwsExceptionWhenSagaTypeIsNull() {
            // given
            SagaInstanceCreateState state = new SagaInstanceCreateState("saga-id-1", null, "{}");

            // when & then
            assertThatThrownBy(() -> SagaInstance.from(state, FIXED_CLOCK))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("from(SnapshotState) 팩토리 메서드")
    class FromSnapshotState {

        @Test
        @DisplayName("SnapshotState의 모든 필드가 그대로 매핑된다")
        void mapsAllFieldsFromSnapshot() {
            // given
            LocalDateTime now = LocalDateTime.now(FIXED_CLOCK);
            SagaInstanceSnapshotState state = new SagaInstanceSnapshotState(
                    1L, "saga-id-1", "OrderPayment", SagaStatus.PROCESSING,
                    2, "{\"orderId\":1}", now, now, null
            );

            // when
            SagaInstance instance = SagaInstance.from(state);

            // then
            assertThat(instance.getId()).isEqualTo(1L);
            assertThat(instance.getSagaId()).isEqualTo("saga-id-1");
            assertThat(instance.getSagaType()).isEqualTo("OrderPayment");
            assertThat(instance.getStatus()).isEqualTo(SagaStatus.PROCESSING);
            assertThat(instance.getCurrentStepIndex()).isEqualTo(2);
            assertThat(instance.getPayload()).isEqualTo("{\"orderId\":1}");
            assertThat(instance.getCreatedAt()).isEqualTo(now);
            assertThat(instance.getModifiedAt()).isEqualTo(now);
            assertThat(instance.getCompletedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("process() 상태 전이")
    class Process {

        @Test
        @DisplayName("STARTED 상태에서 호출 시 PROCESSING으로 전이된다")
        void transitionsFromStartedToProcessing() {
            // given
            SagaInstance instance = createStartedInstance();

            // when
            instance.process();

            // then
            assertThat(instance.getStatus()).isEqualTo(SagaStatus.PROCESSING);
        }

        @Test
        @DisplayName("PROCESSING 상태에서 호출 시 InvalidSagaStatusTransitionException이 발생한다")
        void throwsExceptionWhenNotStarted() {
            // given
            SagaInstance instance = createProcessingInstance();

            // when & then
            assertThatThrownBy(instance::process)
                    .isInstanceOf(InvalidSagaStatusTransitionException.class);
        }
    }

    @Nested
    @DisplayName("advanceStep() 상태 전이")
    class AdvanceStep {

        @Test
        @DisplayName("PROCESSING 상태에서 호출 시 currentStepIndex가 1 증가한다")
        void incrementsStepIndex() {
            // given
            SagaInstance instance = createProcessingInstance();

            // when
            instance.advanceStep();

            // then
            assertThat(instance.getCurrentStepIndex()).isEqualTo(1);
        }

        @Test
        @DisplayName("STARTED 상태에서 호출 시 InvalidSagaStatusTransitionException이 발생한다")
        void throwsExceptionWhenNotProcessing() {
            // given
            SagaInstance instance = createStartedInstance();

            // when & then
            assertThatThrownBy(instance::advanceStep)
                    .isInstanceOf(InvalidSagaStatusTransitionException.class);
        }
    }

    @Nested
    @DisplayName("complete() 상태 전이")
    class Complete {

        @Test
        @DisplayName("PROCESSING 상태에서 호출 시 SUCCEEDED로 전이되고 completedAt이 설정된다")
        void transitionsToSucceededWithCompletedAt() {
            // given
            SagaInstance instance = createProcessingInstance();

            // when
            instance.complete(FIXED_CLOCK);

            // then
            assertThat(instance.getStatus()).isEqualTo(SagaStatus.SUCCEEDED);
            assertThat(instance.getCompletedAt()).isEqualTo(LocalDateTime.now(FIXED_CLOCK));
        }

        @Test
        @DisplayName("STARTED 상태에서 호출 시 InvalidSagaStatusTransitionException이 발생한다")
        void throwsExceptionWhenNotProcessing() {
            // given
            SagaInstance instance = createStartedInstance();

            // when & then
            assertThatThrownBy(() -> instance.complete(FIXED_CLOCK))
                    .isInstanceOf(InvalidSagaStatusTransitionException.class);
        }
    }

    @Nested
    @DisplayName("fail() 상태 전이")
    class Fail {

        @Test
        @DisplayName("PROCESSING 상태에서 호출 시 FAILED로 전이된다")
        void transitionsToFailed() {
            // given
            SagaInstance instance = createProcessingInstance();

            // when
            instance.fail();

            // then
            assertThat(instance.getStatus()).isEqualTo(SagaStatus.FAILED);
        }

        @Test
        @DisplayName("STARTED 상태에서 호출 시 InvalidSagaStatusTransitionException이 발생한다")
        void throwsExceptionWhenNotProcessing() {
            // given
            SagaInstance instance = createStartedInstance();

            // when & then
            assertThatThrownBy(instance::fail)
                    .isInstanceOf(InvalidSagaStatusTransitionException.class);
        }
    }

    @Nested
    @DisplayName("rollback() 상태 전이")
    class Rollback {

        @Test
        @DisplayName("FAILED 상태에서 호출 시 COMPENSATING으로 전이된다")
        void transitionsToCompensating() {
            // given
            SagaInstance instance = createFailedInstance();

            // when
            instance.rollback();

            // then
            assertThat(instance.getStatus()).isEqualTo(SagaStatus.COMPENSATING);
        }

        @Test
        @DisplayName("PROCESSING 상태에서 호출 시 InvalidSagaStatusTransitionException이 발생한다")
        void throwsExceptionWhenNotFailed() {
            // given
            SagaInstance instance = createProcessingInstance();

            // when & then
            assertThatThrownBy(instance::rollback)
                    .isInstanceOf(InvalidSagaStatusTransitionException.class);
        }
    }

    @Nested
    @DisplayName("compensate() 상태 전이")
    class Compensate {

        @Test
        @DisplayName("COMPENSATING 상태에서 호출 시 COMPENSATED로 전이되고 completedAt이 설정된다")
        void transitionsToCompensatedWithCompletedAt() {
            // given
            SagaInstance instance = createCompensatingInstance();

            // when
            instance.compensate(FIXED_CLOCK);

            // then
            assertThat(instance.getStatus()).isEqualTo(SagaStatus.COMPENSATED);
            assertThat(instance.getCompletedAt()).isEqualTo(LocalDateTime.now(FIXED_CLOCK));
        }

        @Test
        @DisplayName("FAILED 상태에서 호출 시 InvalidSagaStatusTransitionException이 발생한다")
        void throwsExceptionWhenNotCompensating() {
            // given
            SagaInstance instance = createFailedInstance();

            // when & then
            assertThatThrownBy(() -> instance.compensate(FIXED_CLOCK))
                    .isInstanceOf(InvalidSagaStatusTransitionException.class);
        }
    }

    @Nested
    @DisplayName("failCompensation() 상태 전이")
    class FailCompensation {

        @Test
        @DisplayName("COMPENSATING 상태에서 호출 시 FAILED로 전이된다")
        void transitionsToFailed() {
            // given
            SagaInstance instance = createCompensatingInstance();

            // when
            instance.failCompensation();

            // then
            assertThat(instance.getStatus()).isEqualTo(SagaStatus.FAILED);
        }

        @Test
        @DisplayName("FAILED 상태에서 호출 시 InvalidSagaStatusTransitionException이 발생한다")
        void throwsExceptionWhenNotCompensating() {
            // given
            SagaInstance instance = createFailedInstance();

            // when & then
            assertThatThrownBy(instance::failCompensation)
                    .isInstanceOf(InvalidSagaStatusTransitionException.class);
        }
    }

    @Nested
    @DisplayName("isTerminal() 술어 메서드")
    class IsTerminal {

        @Test
        @DisplayName("SUCCEEDED 상태에서 true를 반환한다")
        void returnsTrueForSucceeded() {
            // given
            SagaInstance instance = createProcessingInstance();
            instance.complete(FIXED_CLOCK);

            // when & then
            assertThat(instance.isTerminal()).isTrue();
        }

        @Test
        @DisplayName("COMPENSATED 상태에서 true를 반환한다")
        void returnsTrueForCompensated() {
            // given
            SagaInstance instance = createCompensatingInstance();
            instance.compensate(FIXED_CLOCK);

            // when & then
            assertThat(instance.isTerminal()).isTrue();
        }

        @Test
        @DisplayName("PROCESSING 상태에서 false를 반환한다")
        void returnsFalseForProcessing() {
            // given
            SagaInstance instance = createProcessingInstance();

            // when & then
            assertThat(instance.isTerminal()).isFalse();
        }
    }
}
