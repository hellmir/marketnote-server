package com.personal.marketnote.commerce.adapter.in.event.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.exception.DuplicateInventoryRestorationException;
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
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderReturnInventorySagaConsumer 테스트")
class OrderReturnInventorySagaConsumerTest {

    private static final String SAGA_ID = "ORDER_RETURN:1";
    private static final String SAGA_TYPE = "ORDER_RETURN";
    private static final String STEP_NAME = "RESTORE_INVENTORY";

    @InjectMocks
    private OrderReturnInventorySagaConsumer consumer;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private RestoreProductInventoryUseCase restoreProductInventoryUseCase;

    @Mock
    private ReduceProductInventoryUseCase reduceProductInventoryUseCase;

    @Mock
    private SagaResponsePublisher sagaResponsePublisher;

    @Mock
    private Acknowledgment acknowledgment;

    private String actionPayload;

    @BeforeEach
    void setUp() throws Exception {
        actionPayload = objectMapper.writeValueAsString(
                Map.of(
                        "orderId", 1L,
                        "returnProducts", List.of(
                                Map.of("pricePolicyId", 10L, "quantity", 2, "unitAmount", 25000L)
                        ),
                        "isFullReturn", true
                )
        );
    }

    @Nested
    @DisplayName("ACTION 처리")
    class ActionHandling {

        @Test
        @DisplayName("재고 복구 성공 시 성공 응답을 발행한다")
        void shouldPublishSuccessOnInventoryRestoration() {
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            consumer.handleInventoryStep(record, acknowledgment);

            verify(restoreProductInventoryUseCase).restore(anyList(), eq(1L), anyString());
            verify(sagaResponsePublisher).publishSuccess(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, "{\"success\":true}");
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("중복 재고 복구 시 멱등 처리로 성공 응답을 발행한다")
        void shouldPublishSuccessOnDuplicateRestoration() {
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            doThrow(new DuplicateInventoryRestorationException(1L))
                    .when(restoreProductInventoryUseCase).restore(anyList(), eq(1L), anyString());

            consumer.handleInventoryStep(record, acknowledgment);

            verify(sagaResponsePublisher).publishSuccess(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.ACTION), contains("idempotent"));
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("재고 복구 처리 실패 시 실패 응답을 발행한다")
        void shouldPublishFailureOnRestorationError() {
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            doThrow(new RuntimeException("재고 복구 실패"))
                    .when(restoreProductInventoryUseCase).restore(anyList(), eq(1L), anyString());

            consumer.handleInventoryStep(record, acknowledgment);

            verify(sagaResponsePublisher).publishFailure(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, "반품 재고 복구 처리 실패");
            verify(acknowledgment).acknowledge();
        }
    }

    @Nested
    @DisplayName("COMPENSATION 처리")
    class CompensationHandling {

        @Test
        @DisplayName("보상 시 재고 재차감 후 성공 응답을 발행한다")
        void shouldPublishSuccessOnCompensation() {
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            consumer.handleInventoryStep(record, acknowledgment);

            verify(reduceProductInventoryUseCase).reduce(anyList(), eq(1L), anyString());
            verify(sagaResponsePublisher).publishSuccess(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, "{\"compensated\":true}");
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("보상 처리 실패 시 실패 응답을 발행한다")
        void shouldPublishFailureOnCompensationError() {
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            doThrow(new RuntimeException("재차감 실패"))
                    .when(reduceProductInventoryUseCase).reduce(anyList(), eq(1L), anyString());

            consumer.handleInventoryStep(record, acknowledgment);

            verify(sagaResponsePublisher).publishFailure(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, "반품 재고 복구 보상 처리 실패");
            verify(acknowledgment).acknowledge();
        }
    }

    @Nested
    @DisplayName("페이로드 검증 실패")
    class PayloadValidation {

        @Test
        @DisplayName("ACTION에서 orderId가 누락되면 실패 응답을 발행한다")
        void shouldPublishFailureOnMissingOrderIdInAction() throws Exception {
            String invalidPayload = new ObjectMapper().writeValueAsString(
                    Map.of("returnProducts", List.of(
                            Map.of("pricePolicyId", 10L, "quantity", 2, "unitAmount", 25000L)
                    ), "isFullReturn", true));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, invalidPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            consumer.handleInventoryStep(record, acknowledgment);

            verifyNoInteractions(restoreProductInventoryUseCase);
            verify(sagaResponsePublisher).publishFailure(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.ACTION), contains("orderId"));
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("ACTION에서 returnProducts가 누락되면 실패 응답을 발행한다")
        void shouldPublishFailureOnMissingReturnProductsInAction() throws Exception {
            String invalidPayload = new ObjectMapper().writeValueAsString(
                    Map.of("orderId", 1L, "isFullReturn", true));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, invalidPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            consumer.handleInventoryStep(record, acknowledgment);

            verifyNoInteractions(restoreProductInventoryUseCase);
            verify(sagaResponsePublisher).publishFailure(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.ACTION), contains("returnProducts"));
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("COMPENSATION에서 orderId가 누락되면 실패 응답을 발행한다")
        void shouldPublishFailureOnMissingOrderIdInCompensation() throws Exception {
            String invalidPayload = new ObjectMapper().writeValueAsString(
                    Map.of("returnProducts", List.of(
                            Map.of("pricePolicyId", 10L, "quantity", 2, "unitAmount", 25000L)
                    )));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, invalidPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            consumer.handleInventoryStep(record, acknowledgment);

            verifyNoInteractions(reduceProductInventoryUseCase);
            verify(sagaResponsePublisher).publishFailure(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.COMPENSATION), contains("orderId"));
            verify(acknowledgment).acknowledge();
        }
    }

    private ConsumerRecord<String, EventEnvelope<?>> createRecord(SagaStepMessage stepMessage) {
        Clock clock = Clock.fixed(Instant.parse("2026-10-11T01:00:00Z"), ZoneId.of("Asia/Seoul"));
        EventEnvelope<SagaStepMessage> envelope = EventEnvelope.of(
                "saga.ORDER_RETURN.RESTORE_INVENTORY.action", "saga-orchestrator", stepMessage, clock);

        @SuppressWarnings("unchecked")
        ConsumerRecord<String, EventEnvelope<?>> record =
                new ConsumerRecord<>("saga.order-return.inventory", 0, 0, SAGA_ID,
                        (EventEnvelope<?>) (EventEnvelope) envelope);
        return record;
    }
}
