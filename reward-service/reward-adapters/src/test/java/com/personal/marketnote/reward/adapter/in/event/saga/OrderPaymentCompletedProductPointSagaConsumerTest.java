package com.personal.marketnote.reward.adapter.in.event.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.saga.SagaResponsePublisher;
import com.personal.marketnote.common.saga.SagaStepMessage;
import com.personal.marketnote.reward.port.in.command.point.CancelPendingPointCommand;
import com.personal.marketnote.reward.port.in.command.point.ModifyPendingPointCommand;
import com.personal.marketnote.reward.port.in.usecase.point.CancelPendingPointUseCase;
import com.personal.marketnote.reward.port.in.usecase.point.ModifyPendingPointUseCase;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderPaymentCompletedProductPointSagaConsumer 테스트")
class OrderPaymentCompletedProductPointSagaConsumerTest {

    private static final String SAGA_ID = "ORDER_PAYMENT_COMPLETED:1";
    private static final String SAGA_TYPE = "ORDER_PAYMENT_COMPLETED";
    private static final String STEP_NAME = "ACCRUE_PRODUCT_POINT";

    @InjectMocks
    private OrderPaymentCompletedProductPointSagaConsumer consumer;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ModifyPendingPointUseCase modifyPendingPointUseCase;

    @Mock
    private CancelPendingPointUseCase cancelPendingPointUseCase;

    @Mock
    private SagaResponsePublisher sagaResponsePublisher;

    @Mock
    private Acknowledgment acknowledgment;

    @Nested
    @DisplayName("ACTION 처리")
    class ActionHandling {

        @Test
        @DisplayName("적립 성공 시 성공 응답을 발행한다")
        void shouldPublishSuccessOnAccrual() throws Exception {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "orderId", 1L, "buyerId", 100L,
                    "totalAmount", 60000L, "totalAccumulatedPoint", 500L
            ));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, payload);

            consumer.handleProductPointStep(createRecord(stepMessage), acknowledgment);

            verify(modifyPendingPointUseCase).modifyPending(any(ModifyPendingPointCommand.class));
            verify(sagaResponsePublisher).publishSuccess(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, "{\"success\":true}"
            );
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("totalAccumulatedPoint가 0이면 skip 응답을 발행한다")
        void shouldSkipOnZeroAccumulatedPoint() throws Exception {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "orderId", 1L, "buyerId", 100L,
                    "totalAmount", 60000L, "totalAccumulatedPoint", 0L
            ));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, payload);

            consumer.handleProductPointStep(createRecord(stepMessage), acknowledgment);

            verifyNoInteractions(modifyPendingPointUseCase);
            verify(sagaResponsePublisher).publishSuccess(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.ACTION), contains("skipped")
            );
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("적립 처리 실패 시 실패 응답을 발행한다")
        void shouldPublishFailureOnError() throws Exception {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "orderId", 1L, "buyerId", 100L,
                    "totalAmount", 60000L, "totalAccumulatedPoint", 500L
            ));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, payload);

            doThrow(new RuntimeException("DB 오류"))
                    .when(modifyPendingPointUseCase).modifyPending(any());

            consumer.handleProductPointStep(createRecord(stepMessage), acknowledgment);

            verify(sagaResponsePublisher).publishFailure(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, "상품 적립 예정 포인트 처리 실패"
            );
            verify(acknowledgment).acknowledge();
        }
    }

    @Nested
    @DisplayName("COMPENSATION 처리")
    class CompensationHandling {

        @Test
        @DisplayName("cancelPending 호출 후 보상 성공 응답을 발행한다")
        void shouldPublishSuccessOnCompensation() throws Exception {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "orderId", 1L, "buyerId", 100L
            ));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, payload);

            consumer.handleProductPointStep(createRecord(stepMessage), acknowledgment);

            verify(cancelPendingPointUseCase).cancelPending(any(CancelPendingPointCommand.class));
            verify(sagaResponsePublisher).publishSuccess(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, "{\"compensated\":true}"
            );
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("보상 처리 실패 시 실패 응답을 발행한다")
        void shouldPublishFailureOnCompensationError() throws Exception {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "orderId", 1L, "buyerId", 100L
            ));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, payload);

            doThrow(new RuntimeException("DB 오류"))
                    .when(cancelPendingPointUseCase).cancelPending(any());

            consumer.handleProductPointStep(createRecord(stepMessage), acknowledgment);

            verify(sagaResponsePublisher).publishFailure(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION,
                    "상품 적립 예정 포인트 보상 처리 실패"
            );
            verify(acknowledgment).acknowledge();
        }
    }

    @SuppressWarnings("unchecked")
    private ConsumerRecord<String, EventEnvelope<?>> createRecord(SagaStepMessage stepMessage) {
        Clock clock = Clock.fixed(Instant.parse("2026-10-17T01:00:00Z"), ZoneId.of("Asia/Seoul"));
        EventEnvelope<SagaStepMessage> envelope = EventEnvelope.of(
                "saga.ORDER_PAYMENT_COMPLETED.ACCRUE_PRODUCT_POINT.action",
                "saga-orchestrator", stepMessage, clock
        );
        return new ConsumerRecord<>(
                "saga.order-payment-downstream.product-point", 0, 0, SAGA_ID,
                (EventEnvelope<?>) (EventEnvelope) envelope
        );
    }
}
