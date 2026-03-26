package com.personal.marketnote.commerce.adapter.in.event.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.exception.DuplicateLedgerTransactionException;
import com.personal.marketnote.commerce.port.in.usecase.ledger.RecordLedgerEntryUseCase;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.saga.SagaResponsePublisher;
import com.personal.marketnote.common.saga.SagaStepMessage;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderPaymentLedgerSagaConsumer 테스트")
class OrderPaymentLedgerSagaConsumerTest {

    private static final String SAGA_ID = "ORDER_PAYMENT:1";
    private static final String SAGA_TYPE = "ORDER_PAYMENT";
    private static final String STEP_NAME = "RECORD_LEDGER";

    @InjectMocks
    private OrderPaymentLedgerSagaConsumer consumer;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private RecordLedgerEntryUseCase recordLedgerEntryUseCase;

    @Mock
    private SagaResponsePublisher sagaResponsePublisher;

    @Mock
    private Acknowledgment acknowledgment;

    private String actionPayload;
    private String compensationPayload;

    @BeforeEach
    void setUp() throws Exception {
        actionPayload = objectMapper.writeValueAsString(
                java.util.Map.of("orderId", 1L, "paymentAmount", 50000L));
        compensationPayload = objectMapper.writeValueAsString(
                java.util.Map.of("orderId", 1L, "cancelAmount", 50000L, "idempotencyKey", "SAGA_COMP:1"));
    }

    @Nested
    @DisplayName("ACTION 처리")
    class ActionHandling {

        @Test
        @DisplayName("분개 성공 시 성공 응답을 발행한다")
        void shouldPublishSuccessOnLedgerRecord() {
            // given
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            // when
            consumer.handleLedgerStep(record, acknowledgment);

            // then
            verify(recordLedgerEntryUseCase).recordPaymentApproval(1L, 50000L);
            verify(sagaResponsePublisher).publishSuccess(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, "{\"success\":true}");
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("중복 분개 시 멱등 처리로 성공 응답을 발행한다")
        void shouldPublishSuccessOnDuplicateLedger() {
            // given
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            doThrow(new DuplicateLedgerTransactionException("PG_SETTLEMENT:1"))
                    .when(recordLedgerEntryUseCase).recordPaymentApproval(1L, 50000L);

            // when
            consumer.handleLedgerStep(record, acknowledgment);

            // then
            verify(sagaResponsePublisher).publishSuccess(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.ACTION), contains("idempotent"));
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("분개 실패 시 실패 응답을 발행한다")
        void shouldPublishFailureOnLedgerError() {
            // given
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            doThrow(new RuntimeException("분개 처리 실패"))
                    .when(recordLedgerEntryUseCase).recordPaymentApproval(1L, 50000L);

            // when
            consumer.handleLedgerStep(record, acknowledgment);

            // then
            verify(sagaResponsePublisher).publishFailure(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, "분개 처리 실패");
            // 도메인 수준 메시지 검증 (e.getMessage() 대신 고정 메시지)
            verify(acknowledgment).acknowledge();
        }
    }

    @Nested
    @DisplayName("COMPENSATION 처리")
    class CompensationHandling {

        @Test
        @DisplayName("역분개 성공 시 성공 응답을 발행한다")
        void shouldPublishSuccessOnLedgerReverse() {
            // given
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, compensationPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            // when
            consumer.handleLedgerStep(record, acknowledgment);

            // then
            verify(recordLedgerEntryUseCase).recordPaymentCancellation(1L, 50000L, "SAGA_COMP:1");
            verify(sagaResponsePublisher).publishSuccess(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, "{\"compensated\":true}");
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("중복 역분개 시 멱등 처리로 성공 응답을 발행한다")
        void shouldPublishSuccessOnDuplicateReverse() {
            // given
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, compensationPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            doThrow(new DuplicateLedgerTransactionException("SAGA_COMP:1"))
                    .when(recordLedgerEntryUseCase).recordPaymentCancellation(1L, 50000L, "SAGA_COMP:1");

            // when
            consumer.handleLedgerStep(record, acknowledgment);

            // then
            verify(sagaResponsePublisher).publishSuccess(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.COMPENSATION), contains("idempotent"));
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("역분개 실패 시 실패 응답을 발행한다")
        void shouldPublishFailureOnReverseError() {
            // given
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, compensationPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            doThrow(new RuntimeException("역분개 실패"))
                    .when(recordLedgerEntryUseCase).recordPaymentCancellation(1L, 50000L, "SAGA_COMP:1");

            // when
            consumer.handleLedgerStep(record, acknowledgment);

            // then
            verify(sagaResponsePublisher).publishFailure(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, "역분개 처리 실패");
            verify(acknowledgment).acknowledge();
        }
    }

    private ConsumerRecord<String, EventEnvelope<?>> createRecord(SagaStepMessage stepMessage) {
        Clock clock = Clock.fixed(Instant.parse("2026-03-17T01:00:00Z"), ZoneId.of("Asia/Seoul"));
        EventEnvelope<SagaStepMessage> envelope = EventEnvelope.of(
                "saga.ORDER_PAYMENT.RECORD_LEDGER.action", "saga-orchestrator", stepMessage, clock);

        @SuppressWarnings("unchecked")
        ConsumerRecord<String, EventEnvelope<?>> record =
                new ConsumerRecord<>("saga.order-payment.ledger", 0, 0, SAGA_ID,
                        (EventEnvelope<?>) (EventEnvelope) envelope);
        return record;
    }
}
