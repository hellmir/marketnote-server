package com.personal.marketnote.commerce.adapter.in.event.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.port.out.fulfillment.CancelFulfillmentReleasePort;
import com.personal.marketnote.commerce.port.out.fulfillment.CancelFulfillmentReleaseResult;
import com.personal.marketnote.common.exception.FulfillmentServiceRequestFailedException;
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

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderCancelFulfillmentSagaConsumer 테스트")
class OrderCancelFulfillmentSagaConsumerTest {

    private static final String SAGA_ID = "ORDER_CANCEL:1";
    private static final String SAGA_TYPE = "ORDER_CANCEL";
    private static final String STEP_NAME = "CANCEL_FULFILLMENT_RELEASE";

    @InjectMocks
    private OrderCancelFulfillmentSagaConsumer consumer;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CancelFulfillmentReleasePort cancelFulfillmentReleasePort;

    @Mock
    private SagaResponsePublisher sagaResponsePublisher;

    @Mock
    private Acknowledgment acknowledgment;

    @Nested
    @DisplayName("ACTION 처리 — PREPARING 상태")
    class ActionPreparingStatus {

        private String actionPayload;

        @BeforeEach
        void setUp() throws Exception {
            actionPayload = new ObjectMapper().writeValueAsString(
                    Map.of("orderId", 1L, "originalStatus", "PREPARING"));
        }

        @Test
        @DisplayName("풀필먼트 출고 취소 성공 시 성공 응답을 발행한다")
        void shouldPublishSuccessOnFulfillmentCancelApproved() {
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            when(cancelFulfillmentReleasePort.cancelRelease(1L))
                    .thenReturn(new CancelFulfillmentReleaseResult(1L, true, "취소 성공"));

            consumer.handleFulfillmentCancelStep(record, acknowledgment);

            verify(cancelFulfillmentReleasePort).cancelRelease(1L);
            verify(sagaResponsePublisher).publishSuccess(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, "{\"success\":true}");
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("풀필먼트가 출고 취소를 거부하면 실패 응답을 발행한다")
        void shouldPublishFailureOnFulfillmentCancelRejected() {
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            when(cancelFulfillmentReleasePort.cancelRelease(1L))
                    .thenReturn(new CancelFulfillmentReleaseResult(1L, false, "피킹 완료"));

            consumer.handleFulfillmentCancelStep(record, acknowledgment);

            verify(sagaResponsePublisher).publishFailure(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.ACTION), contains("풀필먼트 출고 취소 거부"));
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("풀필먼트 서비스 통신 실패 시 실패 응답을 발행한다")
        void shouldPublishFailureOnFulfillmentCommunicationError() {
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            when(cancelFulfillmentReleasePort.cancelRelease(1L))
                    .thenThrow(new FulfillmentServiceRequestFailedException(new IOException("connection refused")));

            consumer.handleFulfillmentCancelStep(record, acknowledgment);

            verify(sagaResponsePublisher).publishFailure(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.ACTION), contains("풀필먼트 출고 취소 처리 실패"));
            verify(acknowledgment).acknowledge();
        }
    }

    @Nested
    @DisplayName("ACTION 처리 — PAID 상태 (풀필먼트 취소 skip)")
    class ActionPaidStatus {

        private String actionPayload;

        @BeforeEach
        void setUp() throws Exception {
            actionPayload = new ObjectMapper().writeValueAsString(
                    Map.of("orderId", 2L, "originalStatus", "PAID"));
        }

        @Test
        @DisplayName("PAID 상태이면 풀필먼트 취소 없이 즉시 성공 응답을 발행한다")
        void shouldSkipFulfillmentCancelAndPublishSuccess() {
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            consumer.handleFulfillmentCancelStep(record, acknowledgment);

            verifyNoInteractions(cancelFulfillmentReleasePort);
            verify(sagaResponsePublisher).publishSuccess(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.ACTION), contains("skipped"));
            verify(acknowledgment).acknowledge();
        }
    }

    @Nested
    @DisplayName("페이로드 검증 실패")
    class PayloadValidation {

        @Test
        @DisplayName("orderId가 누락되면 실패 응답을 발행한다")
        void shouldPublishFailureOnMissingOrderId() throws Exception {
            String invalidPayload = new ObjectMapper().writeValueAsString(
                    Map.of("originalStatus", "PREPARING"));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, invalidPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            consumer.handleFulfillmentCancelStep(record, acknowledgment);

            verifyNoInteractions(cancelFulfillmentReleasePort);
            verify(sagaResponsePublisher).publishFailure(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.ACTION), contains("orderId"));
            verify(acknowledgment).acknowledge();
        }
    }

    private ConsumerRecord<String, EventEnvelope<?>> createRecord(SagaStepMessage stepMessage) {
        Clock clock = Clock.fixed(Instant.parse("2026-04-26T01:00:00Z"), ZoneId.of("Asia/Seoul"));
        EventEnvelope<SagaStepMessage> envelope = EventEnvelope.of(
                "saga.ORDER_CANCEL.CANCEL_FULFILLMENT_RELEASE.action", "saga-orchestrator", stepMessage, clock);

        @SuppressWarnings("unchecked")
        ConsumerRecord<String, EventEnvelope<?>> record =
                new ConsumerRecord<>("saga.order-cancel.fulfillment", 0, 0, SAGA_ID,
                        (EventEnvelope<?>) (EventEnvelope) envelope);
        return record;
    }
}
