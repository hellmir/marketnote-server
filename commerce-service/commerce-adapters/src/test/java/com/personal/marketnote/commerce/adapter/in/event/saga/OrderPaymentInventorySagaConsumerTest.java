package com.personal.marketnote.commerce.adapter.in.event.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.exception.DuplicateInventoryDeductionException;
import com.personal.marketnote.commerce.port.in.usecase.inventory.ReduceProductInventoryUseCase;
import com.personal.marketnote.commerce.port.in.usecase.inventory.RestoreProductInventoryUseCase;
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
@DisplayName("OrderPaymentInventorySagaConsumer 테스트")
class OrderPaymentInventorySagaConsumerTest {

    private static final String SAGA_ID = "ORDER_PAYMENT:1";
    private static final String SAGA_TYPE = "ORDER_PAYMENT";
    private static final String STEP_NAME = "DEDUCT_INVENTORY";

    @InjectMocks
    private OrderPaymentInventorySagaConsumer consumer;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ReduceProductInventoryUseCase reduceProductInventoryUseCase;

    @Mock
    private RestoreProductInventoryUseCase restoreProductInventoryUseCase;

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
                        "orderProducts", List.of(
                                java.util.Map.of("pricePolicyId", 10L, "quantity", 2, "unitAmount", 25000L)
                        )
                )
        );
    }

    @Nested
    @DisplayName("ACTION 처리")
    class ActionHandling {

        @Test
        @DisplayName("재고 차감 성공 시 성공 응답을 발행한다")
        void shouldPublishSuccessOnInventoryDeduction() {
            // given
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            // when
            consumer.handleInventoryStep(record, acknowledgment);

            // then
            verify(reduceProductInventoryUseCase).reduce(anyList(), eq(1L), anyString());
            verify(sagaResponsePublisher).publishSuccess(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, "{\"success\":true}");
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("중복 재고 차감 시 멱등 처리로 성공 응답을 발행한다")
        void shouldPublishSuccessOnDuplicateDeduction() {
            // given
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            doThrow(new DuplicateInventoryDeductionException(1L))
                    .when(reduceProductInventoryUseCase).reduce(anyList(), eq(1L), anyString());

            // when
            consumer.handleInventoryStep(record, acknowledgment);

            // then
            verify(sagaResponsePublisher).publishSuccess(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.ACTION), contains("idempotent"));
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("재고 차감 실패 시 실패 응답을 발행한다")
        void shouldPublishFailureOnDeductionError() {
            // given
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            doThrow(new RuntimeException("재고 부족"))
                    .when(reduceProductInventoryUseCase).reduce(anyList(), eq(1L), anyString());

            // when
            consumer.handleInventoryStep(record, acknowledgment);

            // then
            verify(sagaResponsePublisher).publishFailure(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, "재고 차감 처리 실패");
            verify(acknowledgment).acknowledge();
        }
    }

    @Nested
    @DisplayName("COMPENSATION 처리")
    class CompensationHandling {

        @Test
        @DisplayName("재고 복구 성공 시 성공 응답을 발행한다")
        void shouldPublishSuccessOnInventoryRestoration() {
            // given
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            // when
            consumer.handleInventoryStep(record, acknowledgment);

            // then
            verify(restoreProductInventoryUseCase).restore(anyList(), eq(1L), anyString());
            verify(sagaResponsePublisher).publishSuccess(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, "{\"compensated\":true}");
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("재고 복구 실패 시 실패 응답을 발행한다")
        void shouldPublishFailureOnRestorationError() {
            // given
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            doThrow(new RuntimeException("복구 실패"))
                    .when(restoreProductInventoryUseCase).restore(anyList(), eq(1L), anyString());

            // when
            consumer.handleInventoryStep(record, acknowledgment);

            // then
            verify(sagaResponsePublisher).publishFailure(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, "재고 복구 처리 실패");
            verify(acknowledgment).acknowledge();
        }
    }

    private ConsumerRecord<String, EventEnvelope<?>> createRecord(SagaStepMessage stepMessage) {
        Clock clock = Clock.fixed(Instant.parse("2026-03-27T01:00:00Z"), ZoneId.of("Asia/Seoul"));
        EventEnvelope<SagaStepMessage> envelope = EventEnvelope.of(
                "saga.ORDER_PAYMENT.DEDUCT_INVENTORY.action", "saga-orchestrator", stepMessage, clock);

        @SuppressWarnings("unchecked")
        ConsumerRecord<String, EventEnvelope<?>> record =
                new ConsumerRecord<>("saga.order-payment.inventory", 0, 0, SAGA_ID,
                        (EventEnvelope<?>) (EventEnvelope) envelope);
        return record;
    }
}
