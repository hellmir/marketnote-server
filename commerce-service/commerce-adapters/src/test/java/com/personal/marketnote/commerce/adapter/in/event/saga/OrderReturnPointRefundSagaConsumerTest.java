package com.personal.marketnote.commerce.adapter.in.event.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.port.out.reward.ModifyUserPointPort;
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
@DisplayName("OrderReturnPointRefundSagaConsumer 테스트")
class OrderReturnPointRefundSagaConsumerTest {

    private static final String SAGA_ID = "ORDER_RETURN:1";
    private static final String SAGA_TYPE = "ORDER_RETURN";
    private static final String STEP_NAME = "REFUND_POINT";

    @InjectMocks
    private OrderReturnPointRefundSagaConsumer consumer;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ModifyUserPointPort modifyUserPointPort;

    @Mock
    private SagaResponsePublisher sagaResponsePublisher;

    @Mock
    private Acknowledgment acknowledgment;

    @Nested
    @DisplayName("ACTION 처리")
    class ActionHandling {

        @Test
        @DisplayName("포인트 환불 성공 시 성공 응답을 발행한다")
        void shouldPublishSuccessOnPointRefund() throws Exception {
            String actionPayload = new ObjectMapper().writeValueAsString(
                    Map.of("orderId", 1L, "buyerId", 100L, "refundPointAmount", 5000L));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            consumer.handlePointRefundStep(record, acknowledgment);

            verify(modifyUserPointPort).refundOrderPoints(100L, 5000L, 1L);
            verify(sagaResponsePublisher).publishSuccess(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, "{\"success\":true}");
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("포인트 미사용 주문이면 환불 생략하고 성공 응답을 발행한다")
        void shouldSkipRefundAndPublishSuccessOnZeroPoints() throws Exception {
            String actionPayload = new ObjectMapper().writeValueAsString(
                    Map.of("orderId", 1L, "buyerId", 100L, "refundPointAmount", 0L));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            consumer.handlePointRefundStep(record, acknowledgment);

            verifyNoInteractions(modifyUserPointPort);
            verify(sagaResponsePublisher).publishSuccess(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.ACTION), contains("skipped"));
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("포인트 환불 처리 실패 시 실패 응답을 발행한다")
        void shouldPublishFailureOnPointRefundError() throws Exception {
            String actionPayload = new ObjectMapper().writeValueAsString(
                    Map.of("orderId", 1L, "buyerId", 100L, "refundPointAmount", 5000L));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            doThrow(new RuntimeException("포인트 서비스 통신 오류"))
                    .when(modifyUserPointPort).refundOrderPoints(100L, 5000L, 1L);

            consumer.handlePointRefundStep(record, acknowledgment);

            verify(sagaResponsePublisher).publishFailure(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, "반품 포인트 환불 처리 실패");
            verify(acknowledgment).acknowledge();
        }
    }

    @Nested
    @DisplayName("COMPENSATION 처리")
    class CompensationHandling {

        @Test
        @DisplayName("보상 시 포인트 재차감 후 성공 응답을 발행한다")
        void shouldPublishSuccessOnCompensation() throws Exception {
            String compensationPayload = new ObjectMapper().writeValueAsString(
                    Map.of("orderId", 1L, "buyerId", 100L, "refundPointAmount", 5000L));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, compensationPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            consumer.handlePointRefundStep(record, acknowledgment);

            verify(modifyUserPointPort).deductOrderPoints(100L, 5000L, 1L);
            verify(sagaResponsePublisher).publishSuccess(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, "{\"compensated\":true}");
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("포인트 미사용 주문의 보상은 생략하고 성공 응답을 발행한다")
        void shouldSkipCompensationOnZeroPoints() throws Exception {
            String compensationPayload = new ObjectMapper().writeValueAsString(
                    Map.of("orderId", 1L, "buyerId", 100L, "refundPointAmount", 0L));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, compensationPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            consumer.handlePointRefundStep(record, acknowledgment);

            verifyNoInteractions(modifyUserPointPort);
            verify(sagaResponsePublisher).publishSuccess(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.COMPENSATION), contains("skipped"));
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("보상 처리 실패 시 실패 응답을 발행한다")
        void shouldPublishFailureOnCompensationError() throws Exception {
            String compensationPayload = new ObjectMapper().writeValueAsString(
                    Map.of("orderId", 1L, "buyerId", 100L, "refundPointAmount", 5000L));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, compensationPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            doThrow(new RuntimeException("차감 실패"))
                    .when(modifyUserPointPort).deductOrderPoints(100L, 5000L, 1L);

            consumer.handlePointRefundStep(record, acknowledgment);

            verify(sagaResponsePublisher).publishFailure(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, "반품 포인트 환불 보상 처리 실패");
            verify(acknowledgment).acknowledge();
        }
    }

    @Nested
    @DisplayName("페이로드 검증 실패")
    class PayloadValidation {

        @Test
        @DisplayName("ACTION에서 orderId 또는 buyerId가 누락되면 실패 응답을 발행한다")
        void shouldPublishFailureOnMissingOrderIdOrBuyerIdInAction() throws Exception {
            String invalidPayload = new ObjectMapper().writeValueAsString(
                    Map.of("refundPointAmount", 5000L));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, invalidPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            consumer.handlePointRefundStep(record, acknowledgment);

            verifyNoInteractions(modifyUserPointPort);
            verify(sagaResponsePublisher).publishFailure(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.ACTION), contains("orderId"));
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("COMPENSATION에서 orderId 또는 buyerId가 누락되면 실패 응답을 발행한다")
        void shouldPublishFailureOnMissingOrderIdOrBuyerIdInCompensation() throws Exception {
            String invalidPayload = new ObjectMapper().writeValueAsString(
                    Map.of("refundPointAmount", 5000L));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, invalidPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            consumer.handlePointRefundStep(record, acknowledgment);

            verifyNoInteractions(modifyUserPointPort);
            verify(sagaResponsePublisher).publishFailure(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.COMPENSATION), contains("orderId"));
            verify(acknowledgment).acknowledge();
        }
    }

    private ConsumerRecord<String, EventEnvelope<?>> createRecord(SagaStepMessage stepMessage) {
        Clock clock = Clock.fixed(Instant.parse("2026-10-11T01:00:00Z"), ZoneId.of("Asia/Seoul"));
        EventEnvelope<SagaStepMessage> envelope = EventEnvelope.of(
                "saga.ORDER_RETURN.REFUND_POINT.action", "saga-orchestrator", stepMessage, clock);

        @SuppressWarnings("unchecked")
        ConsumerRecord<String, EventEnvelope<?>> record =
                new ConsumerRecord<>("saga.order-return.point-refund", 0, 0, SAGA_ID,
                        (EventEnvelope<?>) (EventEnvelope) envelope);
        return record;
    }
}
