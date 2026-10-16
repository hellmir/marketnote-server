package com.personal.marketnote.product.adapter.in.event.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.saga.SagaResponsePublisher;
import com.personal.marketnote.common.saga.SagaStepMessage;
import com.personal.marketnote.product.port.in.command.DeleteCartProductCommand;
import com.personal.marketnote.product.port.in.usecase.cart.DeleteCartProductUseCase;
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
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderPaymentCompletedCartSagaConsumer 테스트")
class OrderPaymentCompletedCartSagaConsumerTest {

    private static final String SAGA_ID = "ORDER_PAYMENT_COMPLETED:1";
    private static final String SAGA_TYPE = "ORDER_PAYMENT_COMPLETED";
    private static final String STEP_NAME = "DELETE_CART";

    @InjectMocks
    private OrderPaymentCompletedCartSagaConsumer consumer;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private DeleteCartProductUseCase deleteCartProductUseCase;

    @Mock
    private SagaResponsePublisher sagaResponsePublisher;

    @Mock
    private Acknowledgment acknowledgment;

    @Nested
    @DisplayName("ACTION 처리")
    class ActionHandling {

        @Test
        @DisplayName("장바구니 삭제 성공 시 성공 응답을 발행한다")
        void shouldPublishSuccessOnCartDeletion() throws Exception {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "orderId", 1L, "buyerId", 100L, "pricePolicyIds", List.of(10L, 20L)
            ));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, payload);

            consumer.handleCartStep(createRecord(stepMessage), acknowledgment);

            verify(deleteCartProductUseCase).deleteCartProducts(any(DeleteCartProductCommand.class));
            verify(sagaResponsePublisher).publishSuccess(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, "{\"success\":true}"
            );
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("pricePolicyIds가 누락되면 실패 응답을 발행한다")
        void shouldPublishFailureOnMissingPricePolicyIds() throws Exception {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "orderId", 1L, "buyerId", 100L, "pricePolicyIds", List.of()
            ));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, payload);

            consumer.handleCartStep(createRecord(stepMessage), acknowledgment);

            verifyNoInteractions(deleteCartProductUseCase);
            verify(sagaResponsePublisher).publishFailure(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.ACTION), contains("pricePolicyIds")
            );
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("장바구니 삭제 실패 시 실패 응답을 발행한다")
        void shouldPublishFailureOnError() throws Exception {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "orderId", 1L, "buyerId", 100L, "pricePolicyIds", List.of(10L)
            ));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, payload);

            doThrow(new RuntimeException("DB 오류"))
                    .when(deleteCartProductUseCase).deleteCartProducts(any());

            consumer.handleCartStep(createRecord(stepMessage), acknowledgment);

            verify(sagaResponsePublisher).publishFailure(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, "장바구니 삭제 처리 실패"
            );
            verify(acknowledgment).acknowledge();
        }
    }

    @Nested
    @DisplayName("COMPENSATION 미지원")
    class CompensationHandling {

        @Test
        @DisplayName("COMPENSATION 메시지 수신 시 아무 작업도 하지 않고 acknowledge 한다")
        void shouldNotProcessCompensation() throws Exception {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "orderId", 1L, "buyerId", 100L, "pricePolicyIds", List.of(10L)
            ));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, payload);

            consumer.handleCartStep(createRecord(stepMessage), acknowledgment);

            verifyNoInteractions(deleteCartProductUseCase);
            verifyNoInteractions(sagaResponsePublisher);
            verify(acknowledgment).acknowledge();
        }
    }

    @SuppressWarnings("unchecked")
    private ConsumerRecord<String, EventEnvelope<?>> createRecord(SagaStepMessage stepMessage) {
        Clock clock = Clock.fixed(Instant.parse("2026-10-17T01:00:00Z"), ZoneId.of("Asia/Seoul"));
        EventEnvelope<SagaStepMessage> envelope = EventEnvelope.of(
                "saga.ORDER_PAYMENT_COMPLETED.DELETE_CART.action",
                "saga-orchestrator", stepMessage, clock
        );
        return new ConsumerRecord<>(
                "saga.order-payment-downstream.cart", 0, 0, SAGA_ID,
                (EventEnvelope<?>) (EventEnvelope) envelope
        );
    }
}
