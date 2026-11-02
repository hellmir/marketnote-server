package com.personal.marketnote.commerce.adapter.in.event.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.port.out.event.PublishOrderEventPort;
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
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderReturnCompletedSagaConsumer 테스트")
class OrderReturnCompletedSagaConsumerTest {

    private static final String SAGA_ID = "ORDER_RETURN:1";
    private static final String SAGA_TYPE = "ORDER_RETURN";
    private static final String STEP_NAME = "PUBLISH_RETURN_COMPLETED";

    @InjectMocks
    private OrderReturnCompletedSagaConsumer consumer;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private PublishOrderEventPort publishOrderEventPort;

    @Mock
    private SagaResponsePublisher sagaResponsePublisher;

    @Mock
    private Acknowledgment acknowledgment;

    @Nested
    @DisplayName("ACTION 처리")
    class ActionHandling {

        private String actionPayload;

        @BeforeEach
        void setUp() throws Exception {
            actionPayload = new ObjectMapper().writeValueAsString(
                    Map.ofEntries(
                            Map.entry("orderId", 1L),
                            Map.entry("orderKey", "order-key-1"),
                            Map.entry("buyerId", 100L),
                            Map.entry("returnAmount", 50000L),
                            Map.entry("paymentAmount", 60000L),
                            Map.entry("pointAmount", 5000L),
                            Map.entry("shippingFee", 3000L),
                            Map.entry("isFullReturn", true),
                            Map.entry("returnShippingFee", 0L),
                            Map.entry("returnProducts", List.of(
                                    Map.of("pricePolicyId", 10L, "quantity", 2, "unitAmount", 25000L)
                            )),
                            Map.entry("returnId", "RET-SAGA-9999")
                    )
            );
        }

        @Test
        @DisplayName("반품 완료 이벤트 발행 성공 시 성공 응답을 발행한다")
        void shouldPublishSuccessOnReturnCompletedEvent() {
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            consumer.handleReturnCompletedStep(record, acknowledgment);

            verify(publishOrderEventPort).publishOrderReturnedEvent(
                    eq(1L), eq("order-key-1"), eq(100L),
                    eq(50000L), eq(60000L), eq(5000L),
                    eq(3000L), eq(true), eq(0L),
                    anyList(), eq("RET-SAGA-9999")
            );
            verify(sagaResponsePublisher).publishSuccess(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, "{\"success\":true}");
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("반품 완료 이벤트 발행 실패 시 실패 응답을 발행한다")
        void shouldPublishFailureOnEventPublishError() {
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            doThrow(new RuntimeException("이벤트 발행 실패"))
                    .when(publishOrderEventPort).publishOrderReturnedEvent(
                            anyLong(), anyString(), anyLong(),
                            anyLong(), anyLong(), anyLong(),
                            anyLong(), anyBoolean(), anyLong(),
                            anyList(), any()
                    );

            consumer.handleReturnCompletedStep(record, acknowledgment);

            verify(sagaResponsePublisher).publishFailure(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, "반품 완료 이벤트 발행 실패");
            verify(acknowledgment).acknowledge();
        }
    }

    @Nested
    @DisplayName("COMPENSATION 처리")
    class CompensationHandling {

        @Test
        @DisplayName("보상 메시지 수신 시 보상 없음 로그만 남기고 acknowledge한다")
        void shouldLogWarningOnCompensationMessage() throws Exception {
            String payload = new ObjectMapper().writeValueAsString(
                    Map.of("orderId", 1L, "buyerId", 100L));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, payload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            consumer.handleReturnCompletedStep(record, acknowledgment);

            verifyNoInteractions(publishOrderEventPort);
            verifyNoInteractions(sagaResponsePublisher);
            verify(acknowledgment).acknowledge();
        }
    }

    @Nested
    @DisplayName("페이로드 검증 실패")
    class PayloadValidation {

        @Test
        @DisplayName("orderId 또는 buyerId가 누락되면 실패 응답을 발행한다")
        void shouldPublishFailureOnMissingOrderIdOrBuyerId() throws Exception {
            String invalidPayload = new ObjectMapper().writeValueAsString(
                    Map.of("orderKey", "order-key-1", "returnAmount", 50000L));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, invalidPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            consumer.handleReturnCompletedStep(record, acknowledgment);

            verifyNoInteractions(publishOrderEventPort);
            verify(sagaResponsePublisher).publishFailure(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.ACTION), contains("orderId"));
            verify(acknowledgment).acknowledge();
        }
    }

    private ConsumerRecord<String, EventEnvelope<?>> createRecord(SagaStepMessage stepMessage) {
        Clock clock = Clock.fixed(Instant.parse("2026-10-11T01:00:00Z"), ZoneId.of("Asia/Seoul"));
        EventEnvelope<SagaStepMessage> envelope = EventEnvelope.of(
                "saga.ORDER_RETURN.PUBLISH_RETURN_COMPLETED.action", "saga-orchestrator", stepMessage, clock);

        @SuppressWarnings("unchecked")
        ConsumerRecord<String, EventEnvelope<?>> record =
                new ConsumerRecord<>("saga.order-return.completed", 0, 0, SAGA_ID,
                        (EventEnvelope<?>) (EventEnvelope) envelope);
        return record;
    }
}
