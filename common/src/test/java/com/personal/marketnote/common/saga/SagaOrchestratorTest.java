package com.personal.marketnote.common.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.outbox.OutboxEvent;
import com.personal.marketnote.common.outbox.SaveOutboxEventPort;
import com.personal.marketnote.common.saga.exception.SagaInstanceNotFoundException;
import com.personal.marketnote.common.saga.port.FindSagaPort;
import com.personal.marketnote.common.saga.port.SaveSagaPort;
import com.personal.marketnote.common.saga.port.UpdateSagaPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SagaOrchestrator 테스트")
class SagaOrchestratorTest {

    private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2026, 3, 16, 10, 0, 0);
    private static final String SAGA_ID = "saga-001";
    private static final String SAGA_TYPE = "ORDER_PAYMENT";
    private static final String STEP_1_NAME = "DEDUCT_INVENTORY";
    private static final String STEP_2_NAME = "PROCESS_PAYMENT";
    private static final String STEP_3_NAME = "SEND_NOTIFICATION";
    private static final String TOPIC_1 = "saga.inventory";
    private static final String TOPIC_2 = "saga.payment";
    private static final String TOPIC_3 = "saga.notification";
    private static final String PAYLOAD_JSON = "{\"orderId\":1,\"productId\":10,\"quantity\":5}";

    @Mock
    private SaveSagaPort saveSagaPort;

    @Mock
    private FindSagaPort findSagaPort;

    @Mock
    private UpdateSagaPort updateSagaPort;

    @Mock
    private SaveOutboxEventPort saveOutboxEventPort;

    @Mock
    private ObjectMapper objectMapper;

    private Clock clock;
    private SagaOrchestrator sagaOrchestrator;
    private TestSagaDefinition testDefinition;
    private TestContext testContext;

    @BeforeEach
    void setUp() throws Exception {
        clock = Clock.fixed(
                Instant.parse("2026-03-16T01:00:00Z"),
                ZoneId.of("Asia/Seoul")
        );
        testDefinition = new TestSagaDefinition();
        testContext = new TestContext(1L, 10L, 5);
        sagaOrchestrator = new SagaOrchestrator(
                saveSagaPort, findSagaPort, updateSagaPort,
                saveOutboxEventPort, objectMapper, clock,
                List.of(testDefinition)
        );

        lenient().when(objectMapper.writeValueAsString(any())).thenReturn(PAYLOAD_JSON);
        lenient().when(objectMapper.readValue(anyString(), eq(TestContext.class))).thenReturn(testContext);
    }

    record TestContext(Long orderId, Long productId, int quantity) {
    }

    static class TestSagaDefinition implements SagaDefinition<TestContext> {

        @Override
        public String getSagaType() {
            return SAGA_TYPE;
        }

        @Override
        public Class<TestContext> getContextType() {
            return TestContext.class;
        }

        @Override
        public List<SagaStepDefinition<TestContext>> getSteps() {
            return List.of(
                    new SagaStepDefinition<>(STEP_1_NAME, TOPIC_1,
                            ctx -> "{\"productId\":" + ctx.productId() + ",\"quantity\":" + ctx.quantity() + "}",
                            ctx -> "{\"productId\":" + ctx.productId() + ",\"quantity\":" + ctx.quantity() + "}"),
                    new SagaStepDefinition<>(STEP_2_NAME, TOPIC_2,
                            ctx -> "{\"orderId\":" + ctx.orderId() + ",\"amount\":5000}",
                            ctx -> "{\"orderId\":" + ctx.orderId() + ",\"refundAmount\":5000}"),
                    new SagaStepDefinition<>(STEP_3_NAME, TOPIC_3,
                            ctx -> "{\"orderId\":" + ctx.orderId() + ",\"message\":\"completed\"}",
                            null)
            );
        }
    }

    @Nested
    @DisplayName("start")
    class Start {

        @Test
        @DisplayName("SAGA를 시작하면 SagaInstance가 PROCESSING 상태로 저장된다")
        void shouldCreateInstanceInProcessingState() throws Exception {
            // given
            setupSaveSagaPort();
            setupSaveStepPort();

            // when
            SagaInstance result = sagaOrchestrator.start(testDefinition, SAGA_ID, testContext);

            // then
            assertThat(result.getStatus()).isEqualTo(SagaStatus.PROCESSING);
            assertThat(result.getSagaId()).isEqualTo(SAGA_ID);
            assertThat(result.getSagaType()).isEqualTo(SAGA_TYPE);

            ArgumentCaptor<SagaInstance> saveCaptor = ArgumentCaptor.forClass(SagaInstance.class);
            verify(saveSagaPort).save(saveCaptor.capture());
            assertThat(saveCaptor.getValue().getStatus()).isEqualTo(SagaStatus.STARTED);

            ArgumentCaptor<SagaInstance> updateCaptor = ArgumentCaptor.forClass(SagaInstance.class);
            verify(updateSagaPort).update(updateCaptor.capture());
            assertThat(updateCaptor.getValue().getStatus()).isEqualTo(SagaStatus.PROCESSING);
        }

        @Test
        @DisplayName("SAGA를 시작하면 첫 번째 스텝이 PROCESSING 상태로 생성되고 액션 메시지가 Outbox에 발행된다")
        void shouldCreateFirstStepAndPublishToOutbox() throws Exception {
            // given
            setupSaveSagaPort();
            setupSaveStepPort();

            // when
            sagaOrchestrator.start(testDefinition, SAGA_ID, testContext);

            // then
            ArgumentCaptor<SagaStep> stepCaptor = ArgumentCaptor.forClass(SagaStep.class);
            verify(saveSagaPort).saveStep(stepCaptor.capture());
            SagaStep savedStep = stepCaptor.getValue();
            assertThat(savedStep.getStepName()).isEqualTo(STEP_1_NAME);
            assertThat(savedStep.getStepIndex()).isZero();
            assertThat(savedStep.getStatus()).isEqualTo(SagaStepStatus.PROCESSING);

            ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(saveOutboxEventPort).save(outboxCaptor.capture());
            OutboxEvent outboxEvent = outboxCaptor.getValue();
            assertThat(outboxEvent.getTopic()).isEqualTo(TOPIC_1);
            assertThat(outboxEvent.getPartitionKey()).isEqualTo(SAGA_ID);
            assertThat(outboxEvent.getEventType()).contains(STEP_1_NAME);
            assertThat(outboxEvent.getSource()).isEqualTo("saga-orchestrator");
        }
    }

    @Nested
    @DisplayName("handleStepResponse - 성공")
    class HandleStepResponseSuccess {

        @Test
        @DisplayName("중간 스텝 응답 성공 시 현재 스텝이 SUCCEEDED가 되고 다음 스텝이 생성되어 Outbox에 발행된다")
        void shouldAdvanceToNextStepOnIntermediateStepSuccess() throws Exception {
            // given
            SagaInstance instance = createProcessingInstance(0);
            SagaStep step0 = createSagaStep(10L, 1L, STEP_1_NAME, 0,
                    SagaStepStatus.PROCESSING, "{\"productId\":10}", null, null, null);

            when(findSagaPort.findBySagaId(SAGA_ID)).thenReturn(Optional.of(instance));
            when(findSagaPort.findStepsBySagaInstanceId(1L)).thenReturn(List.of(step0));
            setupSaveStepPort();

            // when
            sagaOrchestrator.handleStepResponse(SAGA_ID, STEP_1_NAME, true, "{\"success\":true}");

            // then
            assertThat(step0.getStatus()).isEqualTo(SagaStepStatus.SUCCEEDED);
            verify(updateSagaPort).updateStep(step0);

            assertThat(instance.getCurrentStepIndex()).isEqualTo(1);
            verify(updateSagaPort).update(instance);

            ArgumentCaptor<SagaStep> stepCaptor = ArgumentCaptor.forClass(SagaStep.class);
            verify(saveSagaPort).saveStep(stepCaptor.capture());
            SagaStep nextStep = stepCaptor.getValue();
            assertThat(nextStep.getStepName()).isEqualTo(STEP_2_NAME);
            assertThat(nextStep.getStepIndex()).isEqualTo(1);
            assertThat(nextStep.getStatus()).isEqualTo(SagaStepStatus.PROCESSING);

            ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(saveOutboxEventPort).save(outboxCaptor.capture());
            assertThat(outboxCaptor.getValue().getTopic()).isEqualTo(TOPIC_2);
        }

        @Test
        @DisplayName("마지막 스텝 응답 성공 시 SagaInstance가 SUCCEEDED 상태가 된다")
        void shouldCompleteSagaOnLastStepSuccess() throws Exception {
            // given
            SagaInstance instance = createProcessingInstance(2);
            SagaStep step2 = createSagaStep(30L, 1L, STEP_3_NAME, 2,
                    SagaStepStatus.PROCESSING, "{\"message\":\"test\"}", null, null, null);

            when(findSagaPort.findBySagaId(SAGA_ID)).thenReturn(Optional.of(instance));
            when(findSagaPort.findStepsBySagaInstanceId(1L)).thenReturn(List.of(step2));

            // when
            sagaOrchestrator.handleStepResponse(SAGA_ID, STEP_3_NAME, true, "{\"sent\":true}");

            // then
            assertThat(instance.getStatus()).isEqualTo(SagaStatus.SUCCEEDED);
            assertThat(instance.getCompletedAt()).isNotNull();
            verify(updateSagaPort).update(instance);
            verify(saveSagaPort, never()).saveStep(any());
            verify(saveOutboxEventPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("handleStepResponse - 실패")
    class HandleStepResponseFailure {

        @Test
        @DisplayName("스텝 실패 시 SagaInstance가 COMPENSATING 상태가 되고 이전 성공 스텝의 보상 메시지가 역순으로 발행된다")
        void shouldStartCompensationOnStepFailure() throws Exception {
            // given
            SagaInstance instance = createProcessingInstance(1);
            SagaStep step0 = createSagaStep(10L, 1L, STEP_1_NAME, 0,
                    SagaStepStatus.SUCCEEDED, "{\"productId\":10}", "{\"success\":true}", null, null);
            SagaStep step1 = createSagaStep(20L, 1L, STEP_2_NAME, 1,
                    SagaStepStatus.PROCESSING, "{\"amount\":5000}", null, null, null);

            when(findSagaPort.findBySagaId(SAGA_ID)).thenReturn(Optional.of(instance));
            when(findSagaPort.findStepsBySagaInstanceId(1L)).thenReturn(List.of(step0, step1));

            // when
            sagaOrchestrator.handleStepResponse(SAGA_ID, STEP_2_NAME, false, "{\"error\":\"insufficient funds\"}");

            // then
            assertThat(step1.getStatus()).isEqualTo(SagaStepStatus.FAILED);
            assertThat(instance.getStatus()).isEqualTo(SagaStatus.COMPENSATING);

            assertThat(step0.getStatus()).isEqualTo(SagaStepStatus.COMPENSATING);
            assertThat(step0.getCompensationRequest()).isNotNull();

            ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(saveOutboxEventPort).save(outboxCaptor.capture());
            OutboxEvent compensationEvent = outboxCaptor.getValue();
            assertThat(compensationEvent.getTopic()).isEqualTo(TOPIC_1);
            assertThat(compensationEvent.getEventType()).contains("compensation");
        }

        @Test
        @DisplayName("첫 스텝 실패 시 보상 대상이 없으면 즉시 COMPENSATED 상태가 된다")
        void shouldCompensateImmediatelyWhenNoStepsToCompensate() throws Exception {
            // given
            SagaInstance instance = createProcessingInstance(0);
            SagaStep step0 = createSagaStep(10L, 1L, STEP_1_NAME, 0,
                    SagaStepStatus.PROCESSING, "{\"productId\":10}", null, null, null);

            when(findSagaPort.findBySagaId(SAGA_ID)).thenReturn(Optional.of(instance));
            when(findSagaPort.findStepsBySagaInstanceId(1L)).thenReturn(List.of(step0));

            // when
            sagaOrchestrator.handleStepResponse(SAGA_ID, STEP_1_NAME, false, "{\"error\":\"out of stock\"}");

            // then
            assertThat(step0.getStatus()).isEqualTo(SagaStepStatus.FAILED);
            assertThat(instance.getStatus()).isEqualTo(SagaStatus.COMPENSATED);
            assertThat(instance.getCompletedAt()).isNotNull();

            verify(saveOutboxEventPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("handleCompensationResponse")
    class HandleCompensationResponse {

        @Test
        @DisplayName("보상 응답 성공 처리 후 모든 보상이 완료되면 SagaInstance가 COMPENSATED 상태가 된다")
        void shouldCompleteSagaWhenAllCompensationsDone() throws Exception {
            // given
            SagaInstance instance = createCompensatingInstance();
            SagaStep step0 = createSagaStep(10L, 1L, STEP_1_NAME, 0,
                    SagaStepStatus.COMPENSATING, "{\"productId\":10}", "{\"success\":true}",
                    "{\"rollback\":true}", null);
            SagaStep step1 = createSagaStep(20L, 1L, STEP_2_NAME, 1,
                    SagaStepStatus.FAILED, "{\"amount\":5000}", "{\"error\":\"fail\"}", null, null);

            when(findSagaPort.findBySagaId(SAGA_ID)).thenReturn(Optional.of(instance));
            when(findSagaPort.findStepsBySagaInstanceId(1L)).thenReturn(List.of(step0, step1));

            // when
            sagaOrchestrator.handleCompensationResponse(SAGA_ID, STEP_1_NAME, true, "{\"rollbackSuccess\":true}");

            // then
            assertThat(step0.getStatus()).isEqualTo(SagaStepStatus.COMPENSATED);
            assertThat(instance.getStatus()).isEqualTo(SagaStatus.COMPENSATED);
            assertThat(instance.getCompletedAt()).isNotNull();

            verify(updateSagaPort).updateStep(step0);
            verify(updateSagaPort).update(instance);
        }

        @Test
        @DisplayName("보상 실패 시 SagaInstance가 FAILED 상태로 전환된다")
        void shouldFailSagaOnCompensationFailure() throws Exception {
            // given
            SagaInstance instance = createCompensatingInstance();
            SagaStep step0 = createSagaStep(10L, 1L, STEP_1_NAME, 0,
                    SagaStepStatus.COMPENSATING, "{\"productId\":10}", "{\"success\":true}",
                    "{\"rollback\":true}", null);

            when(findSagaPort.findBySagaId(SAGA_ID)).thenReturn(Optional.of(instance));
            when(findSagaPort.findStepsBySagaInstanceId(1L)).thenReturn(List.of(step0));

            // when
            sagaOrchestrator.handleCompensationResponse(SAGA_ID, STEP_1_NAME, false, "{\"error\":\"rollback failed\"}");

            // then
            assertThat(step0.getStatus()).isEqualTo(SagaStepStatus.FAILED);
            assertThat(instance.getStatus()).isEqualTo(SagaStatus.FAILED);

            verify(updateSagaPort).updateStep(step0);
            verify(updateSagaPort).update(instance);
        }
    }

    @Nested
    @DisplayName("예외 케이스")
    class ExceptionCases {

        @Test
        @DisplayName("존재하지 않는 sagaId로 응답 처리 시 SagaInstanceNotFoundException이 발생한다")
        void shouldThrowWhenSagaNotFound() {
            // given
            when(findSagaPort.findBySagaId("non-existent")).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                    sagaOrchestrator.handleStepResponse("non-existent", STEP_1_NAME, true, "{}"))
                    .isInstanceOf(SagaInstanceNotFoundException.class);

            verifyNoInteractions(updateSagaPort);
            verifyNoInteractions(saveOutboxEventPort);
        }

        @Test
        @DisplayName("이미 완료된 SAGA에 응답 도착 시 무시한다")
        void shouldIgnoreResponseForTerminalSaga() throws Exception {
            // given
            SagaInstance instance = createSagaInstance(1L, SAGA_ID, SAGA_TYPE,
                    SagaStatus.SUCCEEDED, 2, PAYLOAD_JSON, FIXED_TIME);

            when(findSagaPort.findBySagaId(SAGA_ID)).thenReturn(Optional.of(instance));

            // when
            sagaOrchestrator.handleStepResponse(SAGA_ID, STEP_1_NAME, true, "{}");

            // then
            verifyNoInteractions(updateSagaPort);
            verifyNoInteractions(saveOutboxEventPort);
            verify(findSagaPort, never()).findStepsBySagaInstanceId(any());
        }
    }

    // --- Helper Methods ---

    private void setupSaveSagaPort() {
        when(saveSagaPort.save(any(SagaInstance.class))).thenAnswer(invocation -> {
            SagaInstance instance = invocation.getArgument(0);
            return SagaInstance.from(new SagaInstanceSnapshotState(
                    1L, instance.getSagaId(), instance.getSagaType(), instance.getStatus(),
                    instance.getCurrentStepIndex(), instance.getPayload(),
                    instance.getCreatedAt(), instance.getModifiedAt(), instance.getCompletedAt()
            ));
        });
    }

    private void setupSaveStepPort() {
        when(saveSagaPort.saveStep(any(SagaStep.class))).thenAnswer(invocation -> {
            SagaStep step = invocation.getArgument(0);
            return SagaStep.from(new SagaStepSnapshotState(
                    100L, step.getSagaInstanceId(), step.getStepName(), step.getStepIndex(),
                    step.getStatus(), step.getRequest(), step.getResponse(),
                    step.getCompensationRequest(), step.getCompensationResponse(),
                    step.getCreatedAt(), step.getModifiedAt()
            ));
        });
    }

    private SagaInstance createProcessingInstance(int currentStepIndex) {
        return createSagaInstance(1L, SAGA_ID, SAGA_TYPE,
                SagaStatus.PROCESSING, currentStepIndex, PAYLOAD_JSON, null);
    }

    private SagaInstance createCompensatingInstance() {
        return createSagaInstance(1L, SAGA_ID, SAGA_TYPE,
                SagaStatus.COMPENSATING, 1, PAYLOAD_JSON, null);
    }

    private SagaInstance createSagaInstance(Long id, String sagaId, String sagaType,
                                            SagaStatus status, int currentStepIndex,
                                            String payload, LocalDateTime completedAt) {
        return SagaInstance.from(new SagaInstanceSnapshotState(
                id, sagaId, sagaType, status, currentStepIndex, payload,
                FIXED_TIME, FIXED_TIME, completedAt
        ));
    }

    private SagaStep createSagaStep(Long id, Long sagaInstanceId, String stepName,
                                     int stepIndex, SagaStepStatus status, String request,
                                     String response, String compensationRequest,
                                     String compensationResponse) {
        return SagaStep.from(new SagaStepSnapshotState(
                id, sagaInstanceId, stepName, stepIndex, status, request,
                response, compensationRequest, compensationResponse,
                FIXED_TIME, FIXED_TIME
        ));
    }
}
