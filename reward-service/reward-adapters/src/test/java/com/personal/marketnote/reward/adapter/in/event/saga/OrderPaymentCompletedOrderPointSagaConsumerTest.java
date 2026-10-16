package com.personal.marketnote.reward.adapter.in.event.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.saga.SagaResponsePublisher;
import com.personal.marketnote.common.saga.SagaStepMessage;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import com.personal.marketnote.reward.exception.DuplicateUserPointHistoryException;
import com.personal.marketnote.reward.port.in.command.point.ModifyUserPointCommand;
import com.personal.marketnote.reward.port.in.usecase.point.ModifyUserPointUseCase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderPaymentCompletedOrderPointSagaConsumer 테스트")
class OrderPaymentCompletedOrderPointSagaConsumerTest {

    private static final String SAGA_ID = "ORDER_PAYMENT_COMPLETED:1";
    private static final String SAGA_TYPE = "ORDER_PAYMENT_COMPLETED";
    private static final String STEP_NAME = "DEDUCT_ORDER_POINT";

    @InjectMocks
    private OrderPaymentCompletedOrderPointSagaConsumer consumer;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ModifyUserPointUseCase modifyUserPointUseCase;

    @Mock
    private SagaResponsePublisher sagaResponsePublisher;

    @Mock
    private Acknowledgment acknowledgment;

    private String actionPayload;

    @BeforeEach
    void setUp() throws Exception {
        actionPayload = objectMapper.writeValueAsString(Map.of(
                "orderId", 1L, "buyerId", 100L, "pointAmount", 5000L
        ));
    }

    @Nested
    @DisplayName("ACTION 처리")
    class ActionHandling {

        @Test
        @DisplayName("포인트 차감 성공 시 성공 응답을 발행한다")
        void shouldPublishSuccessOnDeduction() {
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, actionPayload);

            consumer.handleOrderPointStep(createRecord(stepMessage), acknowledgment);

            ArgumentCaptor<ModifyUserPointCommand> captor = ArgumentCaptor.forClass(ModifyUserPointCommand.class);
            verify(modifyUserPointUseCase).modify(captor.capture());
            assertThat(captor.getValue().isAccrual()).isFalse();
            assertThat(captor.getValue().amount()).isEqualTo(5000L);
            verify(sagaResponsePublisher).publishSuccess(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, "{\"success\":true}"
            );
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("이미 처리된 차감이면 멱등 처리로 성공 응답을 발행한다")
        void shouldPublishSuccessOnDuplicateDeduction() {
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, actionPayload);

            doThrow(new DuplicateUserPointHistoryException(100L, UserPointSourceType.ORDER, 1L, "중복"))
                    .when(modifyUserPointUseCase).modify(any());

            consumer.handleOrderPointStep(createRecord(stepMessage), acknowledgment);

            verify(sagaResponsePublisher).publishSuccess(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, "{\"success\":true}"
            );
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("pointAmount가 0이면 skip 응답을 발행한다")
        void shouldSkipOnZeroPoint() throws Exception {
            String zeroPayload = objectMapper.writeValueAsString(Map.of(
                    "orderId", 1L, "buyerId", 100L, "pointAmount", 0L
            ));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, zeroPayload);

            consumer.handleOrderPointStep(createRecord(stepMessage), acknowledgment);

            verifyNoInteractions(modifyUserPointUseCase);
            verify(sagaResponsePublisher).publishSuccess(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.ACTION), contains("skipped")
            );
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("차감 처리 실패 시 실패 응답을 발행한다")
        void shouldPublishFailureOnError() {
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, actionPayload);

            doThrow(new RuntimeException("DB 오류"))
                    .when(modifyUserPointUseCase).modify(any());

            consumer.handleOrderPointStep(createRecord(stepMessage), acknowledgment);

            verify(sagaResponsePublisher).publishFailure(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, "주문 포인트 차감 처리 실패"
            );
            verify(acknowledgment).acknowledge();
        }
    }

    @Nested
    @DisplayName("COMPENSATION 처리")
    class CompensationHandling {

        @Test
        @DisplayName("환원 성공 시 보상 성공 응답을 발행한다")
        void shouldPublishSuccessOnRestoration() throws Exception {
            String compensationPayload = objectMapper.writeValueAsString(Map.of(
                    "orderId", 1L, "buyerId", 100L, "pointAmount", 5000L
            ));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, compensationPayload);

            consumer.handleOrderPointStep(createRecord(stepMessage), acknowledgment);

            ArgumentCaptor<ModifyUserPointCommand> captor = ArgumentCaptor.forClass(ModifyUserPointCommand.class);
            verify(modifyUserPointUseCase).modify(captor.capture());
            assertThat(captor.getValue().isAccrual()).isTrue();
            assertThat(captor.getValue().reason()).contains("SAGA 보상");
            verify(sagaResponsePublisher).publishSuccess(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, "{\"compensated\":true}"
            );
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("환원 실패 시 실패 응답을 발행한다")
        void shouldPublishFailureOnError() throws Exception {
            String compensationPayload = objectMapper.writeValueAsString(Map.of(
                    "orderId", 1L, "buyerId", 100L, "pointAmount", 5000L
            ));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, compensationPayload);

            doThrow(new RuntimeException("DB 오류"))
                    .when(modifyUserPointUseCase).modify(any());

            consumer.handleOrderPointStep(createRecord(stepMessage), acknowledgment);

            verify(sagaResponsePublisher).publishFailure(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION,
                    "주문 포인트 환원 보상 처리 실패"
            );
            verify(acknowledgment).acknowledge();
        }
    }

    @SuppressWarnings("unchecked")
    private ConsumerRecord<String, EventEnvelope<?>> createRecord(SagaStepMessage stepMessage) {
        Clock clock = Clock.fixed(Instant.parse("2026-10-17T01:00:00Z"), ZoneId.of("Asia/Seoul"));
        EventEnvelope<SagaStepMessage> envelope = EventEnvelope.of(
                "saga.ORDER_PAYMENT_COMPLETED.DEDUCT_ORDER_POINT.action",
                "saga-orchestrator", stepMessage, clock
        );
        return new ConsumerRecord<>(
                "saga.order-payment-downstream.order-point", 0, 0, SAGA_ID,
                (EventEnvelope<?>) (EventEnvelope) envelope
        );
    }
}
