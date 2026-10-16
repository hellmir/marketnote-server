package com.personal.marketnote.commerce.adapter.in.event.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocation;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocationSnapshotState;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocationTransactionType;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocationTargetType;
import com.personal.marketnote.commerce.domain.ledger.IdempotencyKey;
import com.personal.marketnote.commerce.port.out.settlement.DeletePaymentAllocationPort;
import com.personal.marketnote.commerce.port.out.settlement.FindPaymentAllocationPort;
import com.personal.marketnote.commerce.port.out.settlement.SavePaymentAllocationPort;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.support.Acknowledgment;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderReturnSettlementSagaConsumer 테스트")
class OrderReturnSettlementSagaConsumerTest {

    private static final String SAGA_ID = "ORDER_RETURN:1";
    private static final String SAGA_TYPE = "ORDER_RETURN";
    private static final String STEP_NAME = "CANCEL_SETTLEMENT_ALLOCATION";

    @InjectMocks
    private OrderReturnSettlementSagaConsumer consumer;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private FindPaymentAllocationPort findPaymentAllocationPort;

    @Mock
    private SavePaymentAllocationPort savePaymentAllocationPort;

    @Mock
    private DeletePaymentAllocationPort deletePaymentAllocationPort;

    @Mock
    private SagaResponsePublisher sagaResponsePublisher;

    @Mock
    private Acknowledgment acknowledgment;

    @Nested
    @DisplayName("ACTION 처리")
    class ActionHandling {

        @Test
        @DisplayName("정산 역배분 생성 성공 시 성공 응답을 발행한다")
        void shouldPublishSuccessOnSettlementCancellation() throws Exception {
            String actionPayload = new ObjectMapper().writeValueAsString(Map.of("orderId", 1L));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            PaymentAllocation originalAllocation = PaymentAllocation.from(
                    PaymentAllocationSnapshotState.builder()
                            .id(10L)
                            .orderId(1L)
                            .sellerId(200L)
                            .allocatedAmount(50000L)
                            .shippingFee(3000L)
                            .transactionType(PaymentAllocationTransactionType.ORDER_REGISTRATION)
                            .targetType(PaymentAllocationTargetType.ORDER)
                            .idempotencyKey(IdempotencyKey.of("ORIGINAL:1"))
                            .createdAt(LocalDateTime.of(2026, 4, 16, 10, 0))
                            .build()
            );
            when(findPaymentAllocationPort.findByOrderId(1L)).thenReturn(List.of(originalAllocation));

            consumer.handleSettlementStep(record, acknowledgment);

            verify(findPaymentAllocationPort).findByOrderId(1L);
            verify(savePaymentAllocationPort).saveAll(anyList());
            verify(sagaResponsePublisher).publishSuccess(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, "{\"success\":true}");
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("중복 역배분 시 멱등 처리로 성공 응답을 발행한다")
        void shouldPublishSuccessOnDuplicateSettlementCancellation() throws Exception {
            String actionPayload = new ObjectMapper().writeValueAsString(Map.of("orderId", 1L));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            PaymentAllocation originalAllocation = PaymentAllocation.from(
                    PaymentAllocationSnapshotState.builder()
                            .id(10L)
                            .orderId(1L)
                            .sellerId(200L)
                            .allocatedAmount(50000L)
                            .shippingFee(3000L)
                            .transactionType(PaymentAllocationTransactionType.ORDER_REGISTRATION)
                            .targetType(PaymentAllocationTargetType.ORDER)
                            .idempotencyKey(IdempotencyKey.of("ORIGINAL:1"))
                            .createdAt(LocalDateTime.of(2026, 4, 16, 10, 0))
                            .build()
            );
            when(findPaymentAllocationPort.findByOrderId(1L)).thenReturn(List.of(originalAllocation));
            doThrow(new DataIntegrityViolationException("duplicate key"))
                    .when(savePaymentAllocationPort).saveAll(anyList());

            consumer.handleSettlementStep(record, acknowledgment);

            verify(sagaResponsePublisher).publishSuccess(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.ACTION), contains("idempotent"));
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("정산 역배분 처리 실패 시 실패 응답을 발행한다")
        void shouldPublishFailureOnSettlementError() throws Exception {
            String actionPayload = new ObjectMapper().writeValueAsString(Map.of("orderId", 1L));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            when(findPaymentAllocationPort.findByOrderId(1L))
                    .thenThrow(new RuntimeException("DB 조회 실패"));

            consumer.handleSettlementStep(record, acknowledgment);

            verify(sagaResponsePublisher).publishFailure(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, "반품 정산 역배분 처리 실패");
            verify(acknowledgment).acknowledge();
        }
    }

    @Nested
    @DisplayName("COMPENSATION 처리")
    class CompensationHandling {

        @Test
        @DisplayName("보상 시 역배분 삭제 후 성공 응답을 발행한다")
        void shouldPublishSuccessOnCompensation() throws Exception {
            String compensationPayload = new ObjectMapper().writeValueAsString(Map.of("orderId", 1L));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, compensationPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            consumer.handleSettlementStep(record, acknowledgment);

            verify(deletePaymentAllocationPort).deleteReturnAllocationsByOrderId(1L);
            verify(sagaResponsePublisher).publishSuccess(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, "{\"compensated\":true}");
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("보상 처리 실패 시 실패 응답을 발행한다")
        void shouldPublishFailureOnCompensationError() throws Exception {
            String compensationPayload = new ObjectMapper().writeValueAsString(Map.of("orderId", 1L));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, compensationPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            doThrow(new RuntimeException("삭제 실패"))
                    .when(deletePaymentAllocationPort).deleteReturnAllocationsByOrderId(1L);

            consumer.handleSettlementStep(record, acknowledgment);

            verify(sagaResponsePublisher).publishFailure(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, "반품 정산 역배분 보상 처리 실패");
            verify(acknowledgment).acknowledge();
        }
    }

    @Nested
    @DisplayName("페이로드 검증 실패")
    class PayloadValidation {

        @Test
        @DisplayName("ACTION에서 orderId가 누락되면 실패 응답을 발행한다")
        void shouldPublishFailureOnMissingOrderIdInAction() throws Exception {
            String invalidPayload = new ObjectMapper().writeValueAsString(Map.of());
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, invalidPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            consumer.handleSettlementStep(record, acknowledgment);

            verifyNoInteractions(findPaymentAllocationPort);
            verify(sagaResponsePublisher).publishFailure(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.ACTION), contains("orderId"));
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("COMPENSATION에서 orderId가 누락되면 실패 응답을 발행한다")
        void shouldPublishFailureOnMissingOrderIdInCompensation() throws Exception {
            String invalidPayload = new ObjectMapper().writeValueAsString(Map.of());
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, invalidPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            consumer.handleSettlementStep(record, acknowledgment);

            verifyNoInteractions(deletePaymentAllocationPort);
            verify(sagaResponsePublisher).publishFailure(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.COMPENSATION), contains("orderId"));
            verify(acknowledgment).acknowledge();
        }
    }

    private ConsumerRecord<String, EventEnvelope<?>> createRecord(SagaStepMessage stepMessage) {
        Clock clock = Clock.fixed(Instant.parse("2026-10-11T01:00:00Z"), ZoneId.of("Asia/Seoul"));
        EventEnvelope<SagaStepMessage> envelope = EventEnvelope.of(
                "saga.ORDER_RETURN.CANCEL_SETTLEMENT_ALLOCATION.action", "saga-orchestrator", stepMessage, clock);

        @SuppressWarnings("unchecked")
        ConsumerRecord<String, EventEnvelope<?>> record =
                new ConsumerRecord<>("saga.order-return.settlement", 0, 0, SAGA_ID,
                        (EventEnvelope<?>) (EventEnvelope) envelope);
        return record;
    }
}
