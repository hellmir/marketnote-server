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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderPaymentCompletedSagaConsumer 테스트")
class OrderPaymentCompletedSagaConsumerTest {

    private static final String SAGA_ID = "ORDER_PAYMENT:1";
    private static final String SAGA_TYPE = "ORDER_PAYMENT";
    private static final String STEP_NAME = "PUBLISH_PAYMENT_COMPLETED";

    @InjectMocks
    private OrderPaymentCompletedSagaConsumer consumer;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private PublishOrderEventPort publishOrderEventPort;

    @Mock
    private SagaResponsePublisher sagaResponsePublisher;

    @Mock
    private Acknowledgment acknowledgment;

    private String actionPayload;

    @BeforeEach
    void setUp() throws Exception {
        actionPayload = objectMapper.writeValueAsString(
                java.util.Map.of(
                        "orderId", 1L,
                        "buyerId", 100L,
                        "totalAmount", 60000L,
                        "pointAmount", 10000L,
                        "totalAccumulatedPoint", 500L,
                        "orderProducts", List.of(
                                java.util.Map.of(
                                        "pricePolicyId", 10L,
                                        "quantity", 2,
                                        "unitAmount", 25000L
                                )
                        )
                )
        );
    }

    @Nested
    @DisplayName("ACTION 처리")
    class ActionHandling {

        @Test
        @DisplayName("결제 완료 이벤트 발행 성공 시 성공 응답을 발행한다")
        void shouldPublishSuccessOnEventPublish() {
            // given
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            // when
            consumer.handlePaymentCompletedStep(record, acknowledgment);

            // then
            verify(publishOrderEventPort).publishOrderPaymentCompletedEvent(
                    eq(1L), eq(100L), eq(60000L), eq(10000L), anyList(), eq(500L));
            verify(sagaResponsePublisher).publishSuccess(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, "{\"success\":true}");
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("이벤트 발행 실패 시 실패 응답을 발행한다")
        void shouldPublishFailureOnEventPublishError() {
            // given
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            doThrow(new RuntimeException("이벤트 발행 실패"))
                    .when(publishOrderEventPort).publishOrderPaymentCompletedEvent(
                            anyLong(), anyLong(), anyLong(), anyLong(), anyList(), anyLong());

            // when
            consumer.handlePaymentCompletedStep(record, acknowledgment);

            // then
            verify(sagaResponsePublisher).publishFailure(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, "결제 완료 이벤트 발행 실패");
            verify(acknowledgment).acknowledge();
        }
    }

    @Nested
    @DisplayName("COMPENSATION 처리")
    class CompensationHandling {

        @Test
        @DisplayName("보상 메시지 수신 시 경고 로그만 출력하고 처리하지 않는다")
        void shouldLogWarningOnCompensationMessage() {
            // given
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, "{}");
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            // when
            consumer.handlePaymentCompletedStep(record, acknowledgment);

            // then
            verifyNoInteractions(publishOrderEventPort);
            verifyNoInteractions(sagaResponsePublisher);
            verify(acknowledgment).acknowledge();
        }
    }

    private ConsumerRecord<String, EventEnvelope<?>> createRecord(SagaStepMessage stepMessage) {
        Clock clock = Clock.fixed(Instant.parse("2026-03-17T01:00:00Z"), ZoneId.of("Asia/Seoul"));
        EventEnvelope<SagaStepMessage> envelope = EventEnvelope.of(
                "saga.ORDER_PAYMENT.PUBLISH_PAYMENT_COMPLETED.action", "saga-orchestrator", stepMessage, clock);

        @SuppressWarnings("unchecked")
        ConsumerRecord<String, EventEnvelope<?>> record =
                new ConsumerRecord<>("saga.order-payment.completed", 0, 0, SAGA_ID,
                        (EventEnvelope<?>) (EventEnvelope) envelope);
        return record;
    }
}
