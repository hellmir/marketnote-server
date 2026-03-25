package com.personal.marketnote.common.saga;

import com.personal.marketnote.common.saga.exception.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("SagaStep 도메인 테스트")
class SagaStepTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-03-16T10:00:00Z"), ZoneId.of("Asia/Seoul")
    );

    private SagaStep createPendingStep() {
        SagaStepCreateState state = new SagaStepCreateState(
                1L, "DEDUCT_INVENTORY", 0, "{\"productId\":100}"
        );
        return SagaStep.from(state, FIXED_CLOCK);
    }

    private SagaStep createProcessingStep() {
        SagaStep step = createPendingStep();
        step.process();
        return step;
    }

    private SagaStep createSucceededStep() {
        SagaStep step = createProcessingStep();
        step.succeed("{\"result\":\"ok\"}");
        return step;
    }

    private SagaStep createFailedStep() {
        SagaStep step = createProcessingStep();
        step.fail("{\"error\":\"timeout\"}");
        return step;
    }

    private SagaStep createCompensatingStep() {
        SagaStep step = createFailedStep();
        step.compensate("{\"action\":\"restore\"}");
        return step;
    }

    @Nested
    @DisplayName("from(CreateState) 팩토리 메서드")
    class FromCreateState {

        @Test
        @DisplayName("생성 시 PENDING 상태로 초기화된다")
        void initializesPendingStatus() {
            // when
            SagaStep step = createPendingStep();

            // then
            assertThat(step.getStatus()).isEqualTo(SagaStepStatus.PENDING);
        }

        @Test
        @DisplayName("생성 시 Clock 기반 createdAt과 modifiedAt이 설정된다")
        void setsCreatedAtAndModifiedAtFromClock() {
            // when
            SagaStep step = createPendingStep();

            // then
            LocalDateTime expectedTime = LocalDateTime.now(FIXED_CLOCK);
            assertThat(step.getCreatedAt()).isEqualTo(expectedTime);
            assertThat(step.getModifiedAt()).isEqualTo(expectedTime);
        }

        @Test
        @DisplayName("생성 시 전달된 필드가 올바르게 설정된다")
        void setsAllFields() {
            // when
            SagaStep step = createPendingStep();

            // then
            assertThat(step.getSagaInstanceId()).isEqualTo(1L);
            assertThat(step.getStepName()).isEqualTo("DEDUCT_INVENTORY");
            assertThat(step.getStepIndex()).isZero();
            assertThat(step.getRequest()).isEqualTo("{\"productId\":100}");
            assertThat(step.getResponse()).isNull();
            assertThat(step.getCompensationRequest()).isNull();
            assertThat(step.getCompensationResponse()).isNull();
        }

        @Test
        @DisplayName("sagaInstanceId가 null이면 SagaInstanceIdNoValueException이 발생한다")
        void throwsExceptionWhenSagaInstanceIdIsNull() {
            // given
            SagaStepCreateState state = new SagaStepCreateState(
                    null, "DEDUCT_INVENTORY", 0, "{}"
            );

            // when & then
            assertThatThrownBy(() -> SagaStep.from(state, FIXED_CLOCK))
                    .isInstanceOf(SagaInstanceIdNoValueException.class);
        }

        @Test
        @DisplayName("stepName이 null이면 SagaStepNameNoValueException이 발생한다")
        void throwsExceptionWhenStepNameIsNull() {
            // given
            SagaStepCreateState state = new SagaStepCreateState(
                    1L, null, 0, "{}"
            );

            // when & then
            assertThatThrownBy(() -> SagaStep.from(state, FIXED_CLOCK))
                    .isInstanceOf(SagaStepNameNoValueException.class);
        }

        @Test
        @DisplayName("request가 null이면 SagaStepRequestNoValueException이 발생한다")
        void throwsExceptionWhenRequestIsNull() {
            // given
            SagaStepCreateState state = new SagaStepCreateState(
                    1L, "DEDUCT_INVENTORY", 0, null
            );

            // when & then
            assertThatThrownBy(() -> SagaStep.from(state, FIXED_CLOCK))
                    .isInstanceOf(SagaStepRequestNoValueException.class);
        }

        @Test
        @DisplayName("stepIndex가 음수이면 InvalidSagaStepIndexException이 발생한다")
        void throwsExceptionWhenStepIndexIsNegative() {
            // given
            SagaStepCreateState state = new SagaStepCreateState(
                    1L, "DEDUCT_INVENTORY", -1, "{}"
            );

            // when & then
            assertThatThrownBy(() -> SagaStep.from(state, FIXED_CLOCK))
                    .isInstanceOf(InvalidSagaStepIndexException.class);
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
            SagaStepSnapshotState state = new SagaStepSnapshotState(
                    1L, 10L, "DEDUCT_INVENTORY", 2, SagaStepStatus.PROCESSING,
                    "{\"productId\":100}", "{\"result\":\"ok\"}",
                    "{\"action\":\"restore\"}", "{\"restored\":true}",
                    now, now
            );

            // when
            SagaStep step = SagaStep.from(state);

            // then
            assertThat(step.getId()).isEqualTo(1L);
            assertThat(step.getSagaInstanceId()).isEqualTo(10L);
            assertThat(step.getStepName()).isEqualTo("DEDUCT_INVENTORY");
            assertThat(step.getStepIndex()).isEqualTo(2);
            assertThat(step.getStatus()).isEqualTo(SagaStepStatus.PROCESSING);
            assertThat(step.getRequest()).isEqualTo("{\"productId\":100}");
            assertThat(step.getResponse()).isEqualTo("{\"result\":\"ok\"}");
            assertThat(step.getCompensationRequest()).isEqualTo("{\"action\":\"restore\"}");
            assertThat(step.getCompensationResponse()).isEqualTo("{\"restored\":true}");
            assertThat(step.getCreatedAt()).isEqualTo(now);
            assertThat(step.getModifiedAt()).isEqualTo(now);
        }
    }

    @Nested
    @DisplayName("process() 상태 전이")
    class Process {

        @Test
        @DisplayName("PENDING 상태에서 호출 시 PROCESSING으로 전이된다")
        void transitionsFromPendingToProcessing() {
            // given
            SagaStep step = createPendingStep();

            // when
            step.process();

            // then
            assertThat(step.getStatus()).isEqualTo(SagaStepStatus.PROCESSING);
        }

        @Test
        @DisplayName("PROCESSING 상태에서 호출 시 InvalidSagaStepStatusTransitionException이 발생한다")
        void throwsExceptionWhenNotPending() {
            // given
            SagaStep step = createProcessingStep();

            // when & then
            assertThatThrownBy(step::process)
                    .isInstanceOf(InvalidSagaStepStatusTransitionException.class);
        }
    }

    @Nested
    @DisplayName("succeed() 상태 전이")
    class Succeed {

        @Test
        @DisplayName("PROCESSING 상태에서 호출 시 SUCCEEDED로 전이되고 응답이 기록된다")
        void transitionsToSucceededWithResponse() {
            // given
            SagaStep step = createProcessingStep();

            // when
            step.succeed("{\"result\":\"ok\"}");

            // then
            assertThat(step.getStatus()).isEqualTo(SagaStepStatus.SUCCEEDED);
            assertThat(step.getResponse()).isEqualTo("{\"result\":\"ok\"}");
        }

        @Test
        @DisplayName("PENDING 상태에서 호출 시 InvalidSagaStepStatusTransitionException이 발생한다")
        void throwsExceptionWhenNotProcessing() {
            // given
            SagaStep step = createPendingStep();

            // when & then
            assertThatThrownBy(() -> step.succeed("{\"result\":\"ok\"}"))
                    .isInstanceOf(InvalidSagaStepStatusTransitionException.class);
        }
    }

    @Nested
    @DisplayName("fail() 상태 전이")
    class Fail {

        @Test
        @DisplayName("PROCESSING 상태에서 호출 시 FAILED로 전이되고 응답이 기록된다")
        void transitionsToFailedWithResponse() {
            // given
            SagaStep step = createProcessingStep();

            // when
            step.fail("{\"error\":\"timeout\"}");

            // then
            assertThat(step.getStatus()).isEqualTo(SagaStepStatus.FAILED);
            assertThat(step.getResponse()).isEqualTo("{\"error\":\"timeout\"}");
        }

        @Test
        @DisplayName("PENDING 상태에서 호출 시 InvalidSagaStepStatusTransitionException이 발생한다")
        void throwsExceptionWhenNotProcessing() {
            // given
            SagaStep step = createPendingStep();

            // when & then
            assertThatThrownBy(() -> step.fail("{\"error\":\"timeout\"}"))
                    .isInstanceOf(InvalidSagaStepStatusTransitionException.class);
        }
    }

    @Nested
    @DisplayName("compensate() 상태 전이")
    class Compensate {

        @Test
        @DisplayName("FAILED 상태에서 호출 시 COMPENSATING으로 전이되고 보상 요청이 기록된다")
        void transitionsToCompensatingWithRequest() {
            // given
            SagaStep step = createFailedStep();

            // when
            step.compensate("{\"action\":\"restore\"}");

            // then
            assertThat(step.getStatus()).isEqualTo(SagaStepStatus.COMPENSATING);
            assertThat(step.getCompensationRequest()).isEqualTo("{\"action\":\"restore\"}");
        }

        @Test
        @DisplayName("SUCCEEDED 상태에서 호출 시 COMPENSATING으로 전이되고 보상 요청이 기록된다")
        void transitionsFromSucceededToCompensating() {
            // given
            SagaStep step = createSucceededStep();

            // when
            step.compensate("{\"action\":\"restore\"}");

            // then
            assertThat(step.getStatus()).isEqualTo(SagaStepStatus.COMPENSATING);
            assertThat(step.getCompensationRequest()).isEqualTo("{\"action\":\"restore\"}");
        }

        @Test
        @DisplayName("compensationRequest가 null이면 SagaStepCompensationRequestNoValueException이 발생한다")
        void throwsExceptionWhenCompensationRequestIsNull() {
            // given
            SagaStep step = createFailedStep();

            // when & then
            assertThatThrownBy(() -> step.compensate(null))
                    .isInstanceOf(SagaStepCompensationRequestNoValueException.class);
        }

        @Test
        @DisplayName("PROCESSING 상태에서 호출 시 InvalidSagaStepStatusTransitionException이 발생한다")
        void throwsExceptionWhenNotCompensatable() {
            // given
            SagaStep step = createProcessingStep();

            // when & then
            assertThatThrownBy(() -> step.compensate("{\"action\":\"restore\"}"))
                    .isInstanceOf(InvalidSagaStepStatusTransitionException.class);
        }
    }

    @Nested
    @DisplayName("completeCompensation() 상태 전이")
    class CompleteCompensation {

        @Test
        @DisplayName("COMPENSATING 상태에서 호출 시 COMPENSATED로 전이되고 보상 응답이 기록된다")
        void transitionsToCompensatedWithResponse() {
            // given
            SagaStep step = createCompensatingStep();

            // when
            step.completeCompensation("{\"restored\":true}");

            // then
            assertThat(step.getStatus()).isEqualTo(SagaStepStatus.COMPENSATED);
            assertThat(step.getCompensationResponse()).isEqualTo("{\"restored\":true}");
        }

        @Test
        @DisplayName("FAILED 상태에서 호출 시 InvalidSagaStepStatusTransitionException이 발생한다")
        void throwsExceptionWhenNotCompensating() {
            // given
            SagaStep step = createFailedStep();

            // when & then
            assertThatThrownBy(() -> step.completeCompensation("{\"restored\":true}"))
                    .isInstanceOf(InvalidSagaStepStatusTransitionException.class);
        }
    }

    @Nested
    @DisplayName("failCompensation() 상태 전이")
    class FailCompensation {

        @Test
        @DisplayName("COMPENSATING 상태에서 호출 시 FAILED로 전이되고 보상 응답이 기록된다")
        void transitionsToFailedWithResponse() {
            // given
            SagaStep step = createCompensatingStep();

            // when
            step.failCompensation("{\"error\":\"compensation failed\"}");

            // then
            assertThat(step.getStatus()).isEqualTo(SagaStepStatus.FAILED);
            assertThat(step.getCompensationResponse()).isEqualTo("{\"error\":\"compensation failed\"}");
        }

        @Test
        @DisplayName("FAILED 상태에서 호출 시 InvalidSagaStepStatusTransitionException이 발생한다")
        void throwsExceptionWhenNotCompensating() {
            // given
            SagaStep step = createFailedStep();

            // when & then
            assertThatThrownBy(() -> step.failCompensation("{\"error\":\"compensation failed\"}"))
                    .isInstanceOf(InvalidSagaStepStatusTransitionException.class);
        }
    }

    @Nested
    @DisplayName("술어 메서드")
    class PredicateMethods {

        @Test
        @DisplayName("PENDING 상태에서 isPending()은 true를 반환한다")
        void isPendingReturnsTrueForPending() {
            // given
            SagaStep step = createPendingStep();

            // when & then
            assertThat(step.isPending()).isTrue();
            assertThat(step.isProcessing()).isFalse();
        }

        @Test
        @DisplayName("SUCCEEDED 상태에서 isTerminal()은 true를 반환한다")
        void isTerminalReturnsTrueForSucceeded() {
            // given
            SagaStep step = createSucceededStep();

            // when & then
            assertThat(step.isTerminal()).isTrue();
        }

        @Test
        @DisplayName("COMPENSATED 상태에서 isTerminal()은 true를 반환한다")
        void isTerminalReturnsTrueForCompensated() {
            // given
            SagaStep step = createCompensatingStep();
            step.completeCompensation("{\"restored\":true}");

            // when & then
            assertThat(step.isTerminal()).isTrue();
        }

        @Test
        @DisplayName("PROCESSING 상태에서 isTerminal()은 false를 반환한다")
        void isTerminalReturnsFalseForProcessing() {
            // given
            SagaStep step = createProcessingStep();

            // when & then
            assertThat(step.isTerminal()).isFalse();
        }

        @Test
        @DisplayName("SUCCEEDED 상태에서 requiresCompensation()은 true를 반환한다")
        void requiresCompensationReturnsTrueForSucceeded() {
            // given
            SagaStep step = createSucceededStep();

            // when & then
            assertThat(step.requiresCompensation()).isTrue();
        }

        @Test
        @DisplayName("PENDING 상태에서 requiresCompensation()은 false를 반환한다")
        void requiresCompensationReturnsFalseForPending() {
            // given
            SagaStep step = createPendingStep();

            // when & then
            assertThat(step.requiresCompensation()).isFalse();
        }

        @Test
        @DisplayName("FAILED 상태에서 requiresCompensation()은 false를 반환한다")
        void requiresCompensationReturnsFalseForFailed() {
            // given
            SagaStep step = createFailedStep();

            // when & then
            assertThat(step.requiresCompensation()).isFalse();
        }
    }
}
