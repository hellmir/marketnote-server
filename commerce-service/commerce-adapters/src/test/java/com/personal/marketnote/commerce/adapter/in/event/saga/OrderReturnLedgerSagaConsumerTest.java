package com.personal.marketnote.commerce.adapter.in.event.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.exception.DuplicateLedgerTransactionException;
import com.personal.marketnote.commerce.port.in.usecase.ledger.RecordLedgerEntryUseCase;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.saga.SagaResponsePublisher;
import com.personal.marketnote.common.saga.SagaStepMessage;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderReturnLedgerSagaConsumer 테스트")
class OrderReturnLedgerSagaConsumerTest {

    private static final String SAGA_ID = "ORDER_RETURN:1";
    private static final String SAGA_TYPE = "ORDER_RETURN";
    private static final String STEP_NAME = "RECORD_REVERSE_LEDGER";

    @InjectMocks
    private OrderReturnLedgerSagaConsumer consumer;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private RecordLedgerEntryUseCase recordLedgerEntryUseCase;

    @Mock
    private SagaResponsePublisher sagaResponsePublisher;

    @Mock
    private Acknowledgment acknowledgment;

    @Nested
    @DisplayName("ACTION 처리")
    class ActionHandling {

        @Test
        @DisplayName("역분개 성공 시 성공 응답을 발행한다")
        void shouldPublishSuccessOnReverseLedger() throws Exception {
            String actionPayload = new ObjectMapper().writeValueAsString(
                    Map.of("orderId", 1L, "returnAmount", 50000L, "idempotencyKey", "SAGA_ORDER_RETURN:1"));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            consumer.handleLedgerStep(record, acknowledgment);

            verify(recordLedgerEntryUseCase).recordPaymentCancellation(1L, 50000L, "SAGA_ORDER_RETURN:1");
            verify(sagaResponsePublisher).publishSuccess(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, "{\"success\":true}");
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("중복 역분개 시 멱등 처리로 성공 응답을 발행한다")
        void shouldPublishSuccessOnDuplicateLedgerEntry() throws Exception {
            String actionPayload = new ObjectMapper().writeValueAsString(
                    Map.of("orderId", 1L, "returnAmount", 50000L, "idempotencyKey", "SAGA_ORDER_RETURN:1"));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            doThrow(new DuplicateLedgerTransactionException("SAGA_ORDER_RETURN:1"))
                    .when(recordLedgerEntryUseCase).recordPaymentCancellation(1L, 50000L, "SAGA_ORDER_RETURN:1");

            consumer.handleLedgerStep(record, acknowledgment);

            verify(sagaResponsePublisher).publishSuccess(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.ACTION), contains("idempotent"));
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("역분개 처리 실패 시 실패 응답을 발행한다")
        void shouldPublishFailureOnLedgerError() throws Exception {
            String actionPayload = new ObjectMapper().writeValueAsString(
                    Map.of("orderId", 1L, "returnAmount", 50000L, "idempotencyKey", "SAGA_ORDER_RETURN:1"));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            doThrow(new RuntimeException("분개 실패"))
                    .when(recordLedgerEntryUseCase).recordPaymentCancellation(1L, 50000L, "SAGA_ORDER_RETURN:1");

            consumer.handleLedgerStep(record, acknowledgment);

            verify(sagaResponsePublisher).publishFailure(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, "반품 역분개 처리 실패");
            verify(acknowledgment).acknowledge();
        }
    }

    @Nested
    @DisplayName("COMPENSATION 처리")
    class CompensationHandling {

        @Test
        @DisplayName("보상 시 원래 분개를 복구하고 성공 응답을 발행한다")
        void shouldPublishSuccessOnCompensation() throws Exception {
            String compensationPayload = new ObjectMapper().writeValueAsString(
                    Map.of("orderId", 1L, "paymentAmount", 50000L, "idempotencyKey", "SAGA_ORDER_RETURN_COMP:1"));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, compensationPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            consumer.handleLedgerStep(record, acknowledgment);

            verify(recordLedgerEntryUseCase).recordPaymentApproval(1L, 50000L);
            verify(sagaResponsePublisher).publishSuccess(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, "{\"compensated\":true}");
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("중복 보상 시 멱등 처리로 성공 응답을 발행한다")
        void shouldPublishSuccessOnDuplicateCompensation() throws Exception {
            String compensationPayload = new ObjectMapper().writeValueAsString(
                    Map.of("orderId", 1L, "paymentAmount", 50000L, "idempotencyKey", "SAGA_ORDER_RETURN_COMP:1"));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, compensationPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            doThrow(new DuplicateLedgerTransactionException("SAGA_ORDER_RETURN_COMP:1"))
                    .when(recordLedgerEntryUseCase).recordPaymentApproval(1L, 50000L);

            consumer.handleLedgerStep(record, acknowledgment);

            verify(sagaResponsePublisher).publishSuccess(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.COMPENSATION), contains("idempotent"));
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("보상 처리 실패 시 실패 응답을 발행한다")
        void shouldPublishFailureOnCompensationError() throws Exception {
            String compensationPayload = new ObjectMapper().writeValueAsString(
                    Map.of("orderId", 1L, "paymentAmount", 50000L, "idempotencyKey", "SAGA_ORDER_RETURN_COMP:1"));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, compensationPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            doThrow(new RuntimeException("복구 실패"))
                    .when(recordLedgerEntryUseCase).recordPaymentApproval(1L, 50000L);

            consumer.handleLedgerStep(record, acknowledgment);

            verify(sagaResponsePublisher).publishFailure(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, "반품 역분개 보상 처리 실패");
            verify(acknowledgment).acknowledge();
        }
    }

    @Nested
    @DisplayName("페이로드 검증 실패")
    class PayloadValidation {

        @Test
        @DisplayName("ACTION에서 orderId가 누락되면 실패 응답을 발행한다")
        void shouldPublishFailureOnMissingOrderIdInAction() throws Exception {
            String invalidPayload = new ObjectMapper().writeValueAsString(
                    Map.of("returnAmount", 50000L, "idempotencyKey", "SAGA_ORDER_RETURN:1"));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, invalidPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            consumer.handleLedgerStep(record, acknowledgment);

            verifyNoInteractions(recordLedgerEntryUseCase);
            verify(sagaResponsePublisher).publishFailure(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.ACTION), contains("orderId"));
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("COMPENSATION에서 orderId가 누락되면 실패 응답을 발행한다")
        void shouldPublishFailureOnMissingOrderIdInCompensation() throws Exception {
            String invalidPayload = new ObjectMapper().writeValueAsString(
                    Map.of("paymentAmount", 50000L, "idempotencyKey", "SAGA_ORDER_RETURN_COMP:1"));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, invalidPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            consumer.handleLedgerStep(record, acknowledgment);

            verifyNoInteractions(recordLedgerEntryUseCase);
            verify(sagaResponsePublisher).publishFailure(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.COMPENSATION), contains("orderId"));
            verify(acknowledgment).acknowledge();
        }
    }

    private ConsumerRecord<String, EventEnvelope<?>> createRecord(SagaStepMessage stepMessage) {
        Clock clock = Clock.fixed(Instant.parse("2026-10-11T01:00:00Z"), ZoneId.of("Asia/Seoul"));
        EventEnvelope<SagaStepMessage> envelope = EventEnvelope.of(
                "saga.ORDER_RETURN.RECORD_REVERSE_LEDGER.action", "saga-orchestrator", stepMessage, clock);

        @SuppressWarnings("unchecked")
        ConsumerRecord<String, EventEnvelope<?>> record =
                new ConsumerRecord<>("saga.order-return.ledger", 0, 0, SAGA_ID,
                        (EventEnvelope<?>) (EventEnvelope) envelope);
        return record;
    }
}
