package com.personal.marketnote.commerce.adapter.in.event.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.exception.InvalidOrderStatusTransitionException;
import com.personal.marketnote.commerce.port.in.command.order.CompleteCancelOrderCommand;
import com.personal.marketnote.commerce.port.in.usecase.order.CompleteCancelOrderUseCase;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderCancelCompleteSagaConsumer 테스트")
class OrderCancelCompleteSagaConsumerTest {

    private static final String SAGA_ID = "ORDER_CANCEL:1";
    private static final String SAGA_TYPE = "ORDER_CANCEL";
    private static final String STEP_NAME = "COMPLETE_CANCELLATION";
    private static final ObjectMapper TEST_OBJECT_MAPPER = new ObjectMapper();

    @InjectMocks
    private OrderCancelCompleteSagaConsumer consumer;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CompleteCancelOrderUseCase completeCancelOrderUseCase;

    @Mock
    private SagaResponsePublisher sagaResponsePublisher;

    @Mock
    private Acknowledgment acknowledgment;

    @Nested
    @DisplayName("ACTION 처리 — 주문 취소 완료")
    class ActionCompleteCancellation {

        @Test
        @DisplayName("주문 취소 완료 성공 시 성공 응답을 발행한다")
        void shouldPublishSuccessOnCompleteCancellation() throws Exception {
            String actionPayload = buildFullPayload();
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            consumer.handleCompleteCancellationStep(record, acknowledgment);

            verify(completeCancelOrderUseCase).completeCancellation(argThat(command ->
                    command.orderId().equals(1L)
                            && command.orderKey().equals("ORD-001")
                            && command.buyerId().equals(100L)
                            && command.isFullCancel()
                            && "CANCEL_ORDER".equals(command.reasonCategory())
                            && "구매 의사 취소".equals(command.reason())
            ));
            verify(sagaResponsePublisher).publishSuccess(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, "{\"success\":true}");
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("주문 상태 전이 실패 시 실패 응답을 발행한다")
        void shouldPublishFailureOnInvalidStatusTransition() throws Exception {
            String actionPayload = buildFullPayload();
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            doThrow(new InvalidOrderStatusTransitionException(
                    com.personal.marketnote.commerce.domain.order.OrderStatus.PAID,
                    com.personal.marketnote.commerce.domain.order.OrderStatus.CANCELLED))
                    .when(completeCancelOrderUseCase).completeCancellation(any(CompleteCancelOrderCommand.class));

            consumer.handleCompleteCancellationStep(record, acknowledgment);

            verify(sagaResponsePublisher).publishFailure(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.ACTION), contains("주문 취소 완료 처리 실패"));
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("예기치 않은 예외 발생 시 실패 응답을 발행한다")
        void shouldPublishFailureOnUnexpectedException() throws Exception {
            String actionPayload = buildFullPayload();
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            doThrow(new RuntimeException("DB 연결 실패"))
                    .when(completeCancelOrderUseCase).completeCancellation(any(CompleteCancelOrderCommand.class));

            consumer.handleCompleteCancellationStep(record, acknowledgment);

            verify(sagaResponsePublisher).publishFailure(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.ACTION), contains("주문 취소 완료 처리 실패"));
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
                    "isFullCancel", true
            ));
            String invalidPayload = TEST_OBJECT_MAPPER.writeValueAsString(payloadMap);
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, invalidPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            consumer.handleCompleteCancellationStep(record, acknowledgment);

            verifyNoInteractions(completeCancelOrderUseCase);
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
                    "isFullCancel", true
            ));
            String invalidPayload = TEST_OBJECT_MAPPER.writeValueAsString(payloadMap);
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, invalidPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            consumer.handleCompleteCancellationStep(record, acknowledgment);

            verifyNoInteractions(completeCancelOrderUseCase);
            verify(sagaResponsePublisher).publishFailure(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.ACTION), contains("orderKey"));
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
                    new ConsumerRecord<>("saga.order-cancel.completed", 0, 0, SAGA_ID, null);

            consumer.handleCompleteCancellationStep(record, acknowledgment);

            verifyNoInteractions(completeCancelOrderUseCase);
            verifyNoInteractions(sagaResponsePublisher);
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("SagaStepMessage 역직렬화 실패 시 acknowledge하고 poison pill을 방지한다")
        void shouldAcknowledgeOnDeserializationFailure() {
            EventEnvelope<String> envelope = EventEnvelope.of(
                    "saga.ORDER_CANCEL.COMPLETE_CANCELLATION.action", "saga-orchestrator",
                    "invalid-json",
                    Clock.fixed(Instant.parse("2026-04-26T01:00:00Z"), ZoneId.of("Asia/Seoul")));

            @SuppressWarnings("unchecked")
            ConsumerRecord<String, EventEnvelope<?>> record =
                    new ConsumerRecord<>("saga.order-cancel.completed", 0, 0, SAGA_ID,
                            (EventEnvelope<?>) (EventEnvelope) envelope);

            consumer.handleCompleteCancellationStep(record, acknowledgment);

            verifyNoInteractions(completeCancelOrderUseCase);
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("COMPENSATION 메시지이면 경고 로그 후 acknowledge한다")
        void shouldAcknowledgeOnCompensationMessage() throws Exception {
            String payload = TEST_OBJECT_MAPPER.writeValueAsString(Map.of("orderId", 1L));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, payload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            consumer.handleCompleteCancellationStep(record, acknowledgment);

            verifyNoInteractions(completeCancelOrderUseCase);
            verifyNoMoreInteractions(sagaResponsePublisher);
            verify(acknowledgment).acknowledge();
        }
    }

    private static String buildFullPayload() throws Exception {
        Map<String, Object> orderProduct = Map.of(
                "pricePolicyId", 10L,
                "sharerKey", UUID.randomUUID().toString(),
                "quantity", 2,
                "unitAmount", 25000L
        );
        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("orderId", 1L);
        payloadMap.put("orderKey", "ORD-001");
        payloadMap.put("buyerId", 100L);
        payloadMap.put("cancelAmount", 50000L);
        payloadMap.put("paymentAmount", 50000L);
        payloadMap.put("pointAmount", 0L);
        payloadMap.put("shippingFee", 3000L);
        payloadMap.put("isFullCancel", true);
        payloadMap.put("alreadyRefunded", 0L);
        payloadMap.put("reasonCategory", "CANCEL_ORDER");
        payloadMap.put("reason", "구매 의사 취소");
        payloadMap.put("orderProducts", List.of(orderProduct));
        return TEST_OBJECT_MAPPER.writeValueAsString(payloadMap);
    }

    private ConsumerRecord<String, EventEnvelope<?>> createRecord(SagaStepMessage stepMessage) {
        Clock clock = Clock.fixed(Instant.parse("2026-04-26T01:00:00Z"), ZoneId.of("Asia/Seoul"));
        EventEnvelope<SagaStepMessage> envelope = EventEnvelope.of(
                "saga.ORDER_CANCEL.COMPLETE_CANCELLATION.action", "saga-orchestrator", stepMessage, clock);

        @SuppressWarnings("unchecked")
        ConsumerRecord<String, EventEnvelope<?>> record =
                new ConsumerRecord<>("saga.order-cancel.completed", 0, 0, SAGA_ID,
                        (EventEnvelope<?>) (EventEnvelope) envelope);
        return record;
    }
}
