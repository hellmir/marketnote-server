package com.personal.marketnote.commerce.adapter.in.event.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.exception.PaymentAlreadyRefundedException;
import com.personal.marketnote.commerce.port.in.command.returntracker.CompleteReturnRefundCommand;
import com.personal.marketnote.commerce.port.in.usecase.returntracker.CompleteReturnRefundUseCase;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.saga.SagaResponsePublisher;
import com.personal.marketnote.common.saga.SagaStepMessage;
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
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderReturnRefundSagaConsumer 테스트")
class OrderReturnRefundSagaConsumerTest {

    private static final String SAGA_ID = "ORDER_RETURN:1";
    private static final String SAGA_TYPE = "ORDER_RETURN";
    private static final String STEP_NAME = "REFUND_PAYMENT";

    @InjectMocks
    private OrderReturnRefundSagaConsumer consumer;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CompleteReturnRefundUseCase completeReturnRefundUseCase;

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
                    Map.of(
                            "orderId", 1L,
                            "orderKey", "order-key-1",
                            "buyerId", 100L,
                            "returnAmount", 50000L,
                            "paymentAmount", 60000L,
                            "isFullReturn", true,
                            "returnShippingFee", 0L
                    )
            );
        }

        @Test
        @DisplayName("PG 환불 성공 시 성공 응답을 발행한다")
        void shouldPublishSuccessOnRefundCompleted() {
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            consumer.handleRefundStep(record, acknowledgment);

            verify(completeReturnRefundUseCase).completeReturnRefund(any(CompleteReturnRefundCommand.class));
            verify(sagaResponsePublisher).publishSuccess(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, "{\"success\":true}");
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("PG 환불 성공 시 올바른 Command가 전달된다")
        void shouldPassCorrectCommandOnRefund() {
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            consumer.handleRefundStep(record, acknowledgment);

            ArgumentCaptor<CompleteReturnRefundCommand> captor =
                    ArgumentCaptor.forClass(CompleteReturnRefundCommand.class);
            verify(completeReturnRefundUseCase).completeReturnRefund(captor.capture());
            CompleteReturnRefundCommand captured = captor.getValue();
            assertThat(captured.orderId()).isEqualTo(1L);
            assertThat(captured.orderKey()).isEqualTo("order-key-1");
            assertThat(captured.buyerId()).isEqualTo(100L);
            assertThat(captured.returnAmount()).isEqualTo(50000L);
            assertThat(captured.isFullReturn()).isTrue();
        }

        @Test
        @DisplayName("이미 환불된 결제는 멱등 처리로 성공 응답을 발행한다")
        void shouldPublishSuccessOnAlreadyRefunded() {
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            doThrow(new PaymentAlreadyRefundedException("order-key-1"))
                    .when(completeReturnRefundUseCase).completeReturnRefund(any(CompleteReturnRefundCommand.class));

            consumer.handleRefundStep(record, acknowledgment);

            verify(sagaResponsePublisher).publishSuccess(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.ACTION), contains("idempotent"));
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("PG 환불 처리 실패 시 실패 응답을 발행한다")
        void shouldPublishFailureOnRefundError() {
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            doThrow(new RuntimeException("PG 통신 오류"))
                    .when(completeReturnRefundUseCase).completeReturnRefund(any(CompleteReturnRefundCommand.class));

            consumer.handleRefundStep(record, acknowledgment);

            verify(sagaResponsePublisher).publishFailure(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.ACTION), contains("반품 PG 환불 처리 실패"));
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
                    Map.of("orderId", 1L, "orderKey", "order-key-1"));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, payload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            consumer.handleRefundStep(record, acknowledgment);

            verifyNoInteractions(completeReturnRefundUseCase);
            verifyNoInteractions(sagaResponsePublisher);
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
                    Map.of("orderKey", "order-key-1", "buyerId", 100L));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, invalidPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            consumer.handleRefundStep(record, acknowledgment);

            verifyNoInteractions(completeReturnRefundUseCase);
            verify(sagaResponsePublisher).publishFailure(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.ACTION), contains("orderId"));
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("orderKey가 누락되면 실패 응답을 발행한다")
        void shouldPublishFailureOnMissingOrderKey() throws Exception {
            String invalidPayload = new ObjectMapper().writeValueAsString(
                    Map.of("orderId", 1L, "buyerId", 100L));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, invalidPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            consumer.handleRefundStep(record, acknowledgment);

            verifyNoInteractions(completeReturnRefundUseCase);
            verify(sagaResponsePublisher).publishFailure(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.ACTION), contains("orderKey"));
            verify(acknowledgment).acknowledge();
        }
    }

    private ConsumerRecord<String, EventEnvelope<?>> createRecord(SagaStepMessage stepMessage) {
        Clock clock = Clock.fixed(Instant.parse("2026-10-11T01:00:00Z"), ZoneId.of("Asia/Seoul"));
        EventEnvelope<SagaStepMessage> envelope = EventEnvelope.of(
                "saga.ORDER_RETURN.REFUND_PAYMENT.action", "saga-orchestrator", stepMessage, clock);

        @SuppressWarnings("unchecked")
        ConsumerRecord<String, EventEnvelope<?>> record =
                new ConsumerRecord<>("saga.order-return.refund", 0, 0, SAGA_ID,
                        (EventEnvelope<?>) (EventEnvelope) envelope);
        return record;
    }
}
