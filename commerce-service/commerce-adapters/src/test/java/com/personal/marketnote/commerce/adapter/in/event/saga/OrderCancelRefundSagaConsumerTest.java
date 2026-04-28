package com.personal.marketnote.commerce.adapter.in.event.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.exception.PaymentAlreadyRefundedException;
import com.personal.marketnote.commerce.exception.PaymentNotFoundException;
import com.personal.marketnote.commerce.port.in.command.payment.RefundPaymentCommand;
import com.personal.marketnote.commerce.port.in.usecase.payment.RefundPaymentUseCase;
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
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderCancelRefundSagaConsumer 테스트")
class OrderCancelRefundSagaConsumerTest {

    private static final String SAGA_ID = "ORDER_CANCEL:1";
    private static final String SAGA_TYPE = "ORDER_CANCEL";
    private static final String STEP_NAME = "REFUND_PAYMENT";
    private static final ObjectMapper TEST_OBJECT_MAPPER = new ObjectMapper();

    @InjectMocks
    private OrderCancelRefundSagaConsumer consumer;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private RefundPaymentUseCase refundPaymentUseCase;

    @Mock
    private SagaResponsePublisher sagaResponsePublisher;

    @Mock
    private Acknowledgment acknowledgment;

    @Nested
    @DisplayName("ACTION 처리 — PG 환불")
    class ActionRefundPayment {

        @Test
        @DisplayName("PG 환불 성공 시 성공 응답을 발행한다")
        void shouldPublishSuccessOnRefundCompleted() throws Exception {
            String actionPayload = buildFullPayload();
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            consumer.handleRefundPaymentStep(record, acknowledgment);

            verify(refundPaymentUseCase).refund(argThat(command ->
                    command.orderId().equals(1L)
                            && command.orderKey().equals("ORD-001")
                            && command.cancelAmount().equals(50000L)
                            && command.paymentAmount().equals(50000L)
                            && command.isFullCancel()
                            && command.alreadyRefunded().equals(0L)
            ));
            verify(sagaResponsePublisher).publishSuccess(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, "{\"success\":true}");
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("이미 환불된 결제이면 멱등 처리로 성공 응답을 발행한다")
        void shouldPublishSuccessOnAlreadyRefunded() throws Exception {
            String actionPayload = buildFullPayload();
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            doThrow(new PaymentAlreadyRefundedException("ORD-001"))
                    .when(refundPaymentUseCase).refund(any(RefundPaymentCommand.class));

            consumer.handleRefundPaymentStep(record, acknowledgment);

            verify(sagaResponsePublisher).publishSuccess(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.ACTION), contains("skipped"));
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("PG 환불 처리 실패 시 실패 응답을 발행한다")
        void shouldPublishFailureOnRefundError() throws Exception {
            String actionPayload = buildFullPayload();
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            doThrow(new RuntimeException("PG 통신 실패"))
                    .when(refundPaymentUseCase).refund(any(RefundPaymentCommand.class));

            consumer.handleRefundPaymentStep(record, acknowledgment);

            verify(sagaResponsePublisher).publishFailure(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.ACTION), contains("PG 환불 처리 실패"));
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("결제 정보를 찾을 수 없으면 실패 응답을 발행한다")
        void shouldPublishFailureOnPaymentNotFound() throws Exception {
            String actionPayload = buildFullPayload();
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            doThrow(new PaymentNotFoundException(1L))
                    .when(refundPaymentUseCase).refund(any(RefundPaymentCommand.class));

            consumer.handleRefundPaymentStep(record, acknowledgment);

            verify(sagaResponsePublisher).publishFailure(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.ACTION), contains("PG 환불 처리 실패"));
            verify(acknowledgment).acknowledge();
        }
    }

    @Nested
    @DisplayName("페이로드 검증 실패")
    class PayloadValidation {

        @Test
        @DisplayName("orderId가 누락되면 실패 응답을 발행한다")
        void shouldPublishFailureOnMissingOrderId() throws Exception {
            Map<String, Object> payloadMap = new HashMap<>(Map.of(
                    "orderKey", "ORD-001",
                    "buyerId", 100L,
                    "cancelAmount", 50000L,
                    "paymentAmount", 50000L,
                    "pointAmount", 0L,
                    "shippingFee", 3000L,
                    "isFullCancel", true,
                    "alreadyRefunded", 0L
            ));
            String invalidPayload = TEST_OBJECT_MAPPER.writeValueAsString(payloadMap);
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, invalidPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            consumer.handleRefundPaymentStep(record, acknowledgment);

            verifyNoInteractions(refundPaymentUseCase);
            verify(sagaResponsePublisher).publishFailure(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.ACTION), contains("orderId"));
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("orderKey가 누락되면 실패 응답을 발행한다")
        void shouldPublishFailureOnMissingOrderKey() throws Exception {
            Map<String, Object> payloadMap = new HashMap<>(Map.of(
                    "orderId", 1L,
                    "buyerId", 100L,
                    "cancelAmount", 50000L,
                    "paymentAmount", 50000L,
                    "pointAmount", 0L,
                    "shippingFee", 3000L,
                    "isFullCancel", true,
                    "alreadyRefunded", 0L
            ));
            String invalidPayload = TEST_OBJECT_MAPPER.writeValueAsString(payloadMap);
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, invalidPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            consumer.handleRefundPaymentStep(record, acknowledgment);

            verifyNoInteractions(refundPaymentUseCase);
            verify(sagaResponsePublisher).publishFailure(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.ACTION), contains("orderKey"));
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("paymentAmount가 누락되면 실패 응답을 발행한다")
        void shouldPublishFailureOnMissingPaymentAmount() throws Exception {
            Map<String, Object> payloadMap = new HashMap<>(Map.of(
                    "orderId", 1L,
                    "orderKey", "ORD-001",
                    "buyerId", 100L,
                    "cancelAmount", 50000L,
                    "pointAmount", 0L,
                    "shippingFee", 3000L,
                    "isFullCancel", true,
                    "alreadyRefunded", 0L
            ));
            String invalidPayload = TEST_OBJECT_MAPPER.writeValueAsString(payloadMap);
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, invalidPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            consumer.handleRefundPaymentStep(record, acknowledgment);

            verifyNoInteractions(refundPaymentUseCase);
            verify(sagaResponsePublisher).publishFailure(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.ACTION), contains("paymentAmount"));
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("부분 취소 시 cancelAmount가 누락되면 실패 응답을 발행한다")
        void shouldPublishFailureOnMissingCancelAmountForPartialCancel() throws Exception {
            Map<String, Object> payloadMap = new HashMap<>(Map.of(
                    "orderId", 1L,
                    "orderKey", "ORD-001",
                    "buyerId", 100L,
                    "paymentAmount", 50000L,
                    "pointAmount", 0L,
                    "shippingFee", 3000L,
                    "isFullCancel", false,
                    "alreadyRefunded", 0L
            ));
            String invalidPayload = TEST_OBJECT_MAPPER.writeValueAsString(payloadMap);
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, invalidPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            consumer.handleRefundPaymentStep(record, acknowledgment);

            verifyNoInteractions(refundPaymentUseCase);
            verify(sagaResponsePublisher).publishFailure(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.ACTION), contains("cancelAmount"));
            verify(acknowledgment).acknowledge();
        }
    }

    @Nested
    @DisplayName("봉투/메시지 타입 검증")
    class EnvelopeAndMessageType {

        @Test
        @DisplayName("envelope이 null이면 acknowledge만 수행한다")
        void shouldAcknowledgeOnNullEnvelope() {
            @SuppressWarnings("unchecked")
            ConsumerRecord<String, EventEnvelope<?>> record =
                    new ConsumerRecord<>("saga.order-cancel.refund", 0, 0, SAGA_ID, null);

            consumer.handleRefundPaymentStep(record, acknowledgment);

            verifyNoInteractions(refundPaymentUseCase);
            verifyNoInteractions(sagaResponsePublisher);
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("SagaStepMessage 역직렬화 실패 시 acknowledge하고 poison pill을 방지한다")
        void shouldAcknowledgeOnDeserializationFailure() {
            EventEnvelope<String> envelope = EventEnvelope.of(
                    "saga.ORDER_CANCEL.REFUND_PAYMENT.action", "saga-orchestrator",
                    "invalid-json-not-saga-step-message",
                    Clock.fixed(Instant.parse("2026-04-26T01:00:00Z"), ZoneId.of("Asia/Seoul")));

            @SuppressWarnings("unchecked")
            ConsumerRecord<String, EventEnvelope<?>> record =
                    new ConsumerRecord<>("saga.order-cancel.refund", 0, 0, SAGA_ID,
                            (EventEnvelope<?>) (EventEnvelope) envelope);

            consumer.handleRefundPaymentStep(record, acknowledgment);

            verifyNoInteractions(refundPaymentUseCase);
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("COMPENSATION 메시지이면 경고 로그 후 acknowledge한다")
        void shouldAcknowledgeOnCompensationMessage() throws Exception {
            String payload = TEST_OBJECT_MAPPER.writeValueAsString(Map.of("orderId", 1L));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, payload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            consumer.handleRefundPaymentStep(record, acknowledgment);

            verifyNoInteractions(refundPaymentUseCase);
            verifyNoMoreInteractions(sagaResponsePublisher);
            verify(acknowledgment).acknowledge();
        }
    }

    private static String buildFullPayload() throws Exception {
        return TEST_OBJECT_MAPPER.writeValueAsString(Map.of(
                "orderId", 1L,
                "orderKey", "ORD-001",
                "buyerId", 100L,
                "cancelAmount", 50000L,
                "paymentAmount", 50000L,
                "pointAmount", 0L,
                "shippingFee", 3000L,
                "isFullCancel", true,
                "alreadyRefunded", 0L
        ));
    }

    private ConsumerRecord<String, EventEnvelope<?>> createRecord(SagaStepMessage stepMessage) {
        Clock clock = Clock.fixed(Instant.parse("2026-04-26T01:00:00Z"), ZoneId.of("Asia/Seoul"));
        EventEnvelope<SagaStepMessage> envelope = EventEnvelope.of(
                "saga.ORDER_CANCEL.REFUND_PAYMENT.action", "saga-orchestrator", stepMessage, clock);

        @SuppressWarnings("unchecked")
        ConsumerRecord<String, EventEnvelope<?>> record =
                new ConsumerRecord<>("saga.order-cancel.refund", 0, 0, SAGA_ID,
                        (EventEnvelope<?>) (EventEnvelope) envelope);
        return record;
    }
}
