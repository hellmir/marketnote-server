package com.personal.marketnote.reward.adapter.in.event.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.saga.SagaResponsePublisher;
import com.personal.marketnote.common.saga.SagaStepMessage;
import com.personal.marketnote.reward.domain.point.PointAmount;
import com.personal.marketnote.reward.domain.point.UserPoint;
import com.personal.marketnote.reward.domain.point.UserPointSnapshotState;
import com.personal.marketnote.reward.port.in.command.point.CancelPendingPointCommand;
import com.personal.marketnote.reward.port.in.command.point.ModifyPendingSharedPointCommand;
import com.personal.marketnote.reward.port.in.usecase.point.CancelPendingPointUseCase;
import com.personal.marketnote.reward.port.in.usecase.point.ModifyPendingSharedPointUseCase;
import com.personal.marketnote.reward.port.out.point.FindUserPointPort;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderPaymentCompletedSharedPointSagaConsumer 테스트")
class OrderPaymentCompletedSharedPointSagaConsumerTest {

    private static final String SAGA_ID = "ORDER_PAYMENT_COMPLETED:1";
    private static final String SAGA_TYPE = "ORDER_PAYMENT_COMPLETED";
    private static final String STEP_NAME = "ACCRUE_SHARED_POINT";
    private static final UUID SHARER_KEY = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    @InjectMocks
    private OrderPaymentCompletedSharedPointSagaConsumer consumer;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ModifyPendingSharedPointUseCase modifyPendingSharedPointUseCase;

    @Mock
    private CancelPendingPointUseCase cancelPendingPointUseCase;

    @Mock
    private FindUserPointPort findUserPointPort;

    @Mock
    private SagaResponsePublisher sagaResponsePublisher;

    @Mock
    private Acknowledgment acknowledgment;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(consumer, "sharePointRate", new BigDecimal("0.1"));
    }

    @Nested
    @DisplayName("ACTION 처리")
    class ActionHandling {

        @Test
        @DisplayName("공유자 있을 때 적립 성공 응답을 발행한다")
        void shouldPublishSuccessOnAccrual() throws Exception {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "orderId", 1L, "buyerId", 100L, "totalAmount", 60000L,
                    "sharerKeys", List.of(SHARER_KEY.toString())
            ));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, payload);

            consumer.handleSharedPointStep(createRecord(stepMessage), acknowledgment);

            verify(modifyPendingSharedPointUseCase).modifyPending(any(ModifyPendingSharedPointCommand.class));
            verify(sagaResponsePublisher).publishSuccess(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, "{\"success\":true}"
            );
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("공유자가 없으면 skip 응답을 발행한다")
        void shouldSkipWhenNoSharer() throws Exception {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "orderId", 1L, "buyerId", 100L, "totalAmount", 60000L,
                    "sharerKeys", List.of()
            ));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, payload);

            consumer.handleSharedPointStep(createRecord(stepMessage), acknowledgment);

            verifyNoInteractions(modifyPendingSharedPointUseCase);
            verify(sagaResponsePublisher).publishSuccess(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.ACTION), contains("skipped")
            );
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("큰 금액(100억)에서도 BigDecimal 정밀 계산으로 오차 없이 정확한 적립 금액을 산출한다")
        void shouldCalculatePreciselyOnLargeAmount() throws Exception {
            // given
            long totalAmount = 10_000_000_000L;
            String payload = objectMapper.writeValueAsString(Map.of(
                    "orderId", 1L, "buyerId", 100L, "totalAmount", totalAmount,
                    "sharerKeys", List.of(SHARER_KEY.toString())
            ));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, payload);

            // when
            consumer.handleSharedPointStep(createRecord(stepMessage), acknowledgment);

            // then
            ArgumentCaptor<ModifyPendingSharedPointCommand> captor = ArgumentCaptor.forClass(
                    ModifyPendingSharedPointCommand.class
            );
            verify(modifyPendingSharedPointUseCase).modifyPending(captor.capture());
            // BigDecimal 기대값: 10_000_000_000 * 0.1 = 1_000_000_000 (정확)
            // float 버그 시: Math.round(1e10 * 0.1f) = 1_000_000_077 (오차)
            assertThat(captor.getValue().amount()).isEqualTo(1_000_000_000L);
            verify(sagaResponsePublisher).publishSuccess(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, "{\"success\":true}"
            );
            verify(acknowledgment).acknowledge();
        }
    }

    @Nested
    @DisplayName("COMPENSATION 처리")
    class CompensationHandling {

        @Test
        @DisplayName("공유자 각각에 cancelPending을 호출하고 보상 성공 응답을 발행한다")
        void shouldCancelPendingForEachSharer() throws Exception {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "orderId", 1L, "buyerId", 100L, "totalAmount", 60000L,
                    "sharerKeys", List.of(SHARER_KEY.toString())
            ));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, payload);

            UserPoint sharerPoint = UserPoint.from(
                    UserPointSnapshotState.builder()
                            .userId(999L)
                            .userKey(SHARER_KEY.toString())
                            .amount(PointAmount.of(0L))
                            .addExpectedAmount(PointAmount.of(0L))
                            .expireExpectedAmount(PointAmount.of(0L))
                            .build()
            );
            when(findUserPointPort.findByUserKey(anyString())).thenReturn(Optional.of(sharerPoint));

            consumer.handleSharedPointStep(createRecord(stepMessage), acknowledgment);

            verify(cancelPendingPointUseCase, times(1)).cancelPending(any(CancelPendingPointCommand.class));
            verify(sagaResponsePublisher).publishSuccess(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, "{\"compensated\":true}"
            );
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("공유자가 없으면 보상 skip 응답을 발행한다")
        void shouldSkipCompensationWhenNoSharer() throws Exception {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "orderId", 1L, "buyerId", 100L, "totalAmount", 60000L,
                    "sharerKeys", List.of()
            ));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, payload);

            consumer.handleSharedPointStep(createRecord(stepMessage), acknowledgment);

            verifyNoInteractions(cancelPendingPointUseCase);
            verify(sagaResponsePublisher).publishSuccess(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.COMPENSATION), contains("skipped")
            );
            verify(acknowledgment).acknowledge();
        }
    }

    @SuppressWarnings("unchecked")
    private ConsumerRecord<String, EventEnvelope<?>> createRecord(SagaStepMessage stepMessage) {
        Clock clock = Clock.fixed(Instant.parse("2026-10-17T01:00:00Z"), ZoneId.of("Asia/Seoul"));
        EventEnvelope<SagaStepMessage> envelope = EventEnvelope.of(
                "saga.ORDER_PAYMENT_COMPLETED.ACCRUE_SHARED_POINT.action",
                "saga-orchestrator", stepMessage, clock
        );
        return new ConsumerRecord<>(
                "saga.order-payment-downstream.shared-point", 0, 0, SAGA_ID,
                (EventEnvelope<?>) (EventEnvelope) envelope
        );
    }
}
