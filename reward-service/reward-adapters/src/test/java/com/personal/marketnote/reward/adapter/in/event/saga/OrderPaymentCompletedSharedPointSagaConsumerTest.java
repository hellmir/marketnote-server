package com.personal.marketnote.reward.adapter.in.event.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.saga.SagaResponsePublisher;
import com.personal.marketnote.common.saga.SagaStepMessage;
import com.personal.marketnote.reward.configuration.RewardProperties;
import com.personal.marketnote.reward.domain.point.PointAmount;
import com.personal.marketnote.reward.domain.point.UserPoint;
import com.personal.marketnote.reward.domain.point.UserPointSnapshotState;
import com.personal.marketnote.reward.exception.DuplicateUserPointHistoryException;
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

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

    @Spy
    private RewardProperties rewardProperties = new RewardProperties();

    @Mock
    private Acknowledgment acknowledgment;

    @BeforeEach
    void setUp() {
        rewardProperties.setSharePointRate(new BigDecimal("0.1"));
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
    @DisplayName("ACTION 페이로드 검증/스킵")
    class ActionValidationAndSkip {

        @Test
        @DisplayName("orderId가 없으면 검증 실패 응답을 발행한다")
        void shouldPublishFailureWhenOrderIdMissing() throws Exception {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "buyerId", 100L, "totalAmount", 60000L,
                    "sharerKeys", List.of(SHARER_KEY.toString())
            ));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, payload);

            consumer.handleSharedPointStep(createRecord(stepMessage), acknowledgment);

            verifyNoInteractions(modifyPendingSharedPointUseCase);
            verify(sagaResponsePublisher).publishFailure(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.ACTION), contains("페이로드 검증 실패")
            );
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("buyerId가 없으면 검증 실패 응답을 발행한다")
        void shouldPublishFailureWhenBuyerIdMissing() throws Exception {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "orderId", 1L, "totalAmount", 60000L,
                    "sharerKeys", List.of(SHARER_KEY.toString())
            ));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, payload);

            consumer.handleSharedPointStep(createRecord(stepMessage), acknowledgment);

            verifyNoInteractions(modifyPendingSharedPointUseCase);
            verify(sagaResponsePublisher).publishFailure(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.ACTION), contains("페이로드 검증 실패")
            );
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("totalAmount가 0 이하이면 skip 응답을 발행한다")
        void shouldSkipWhenTotalAmountIsZero() throws Exception {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "orderId", 1L, "buyerId", 100L, "totalAmount", 0L,
                    "sharerKeys", List.of(SHARER_KEY.toString())
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
        @DisplayName("계산된 공유 포인트 금액이 0이면 skip 응답을 발행한다")
        void shouldSkipWhenCalculatedAmountIsZero() throws Exception {
            rewardProperties.setSharePointRate(BigDecimal.ZERO);
            String payload = objectMapper.writeValueAsString(Map.of(
                    "orderId", 1L, "buyerId", 100L, "totalAmount", 60000L,
                    "sharerKeys", List.of(SHARER_KEY.toString())
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
        @DisplayName("이미 처리된 공유 적립이면 멱등 처리하고 성공 응답을 발행한다")
        void shouldHandleDuplicateAsIdempotent() throws Exception {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "orderId", 1L, "buyerId", 100L, "totalAmount", 60000L,
                    "sharerKeys", List.of(SHARER_KEY.toString())
            ));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, payload);

            doThrow(new DuplicateUserPointHistoryException())
                    .when(modifyPendingSharedPointUseCase).modifyPending(any(ModifyPendingSharedPointCommand.class));

            consumer.handleSharedPointStep(createRecord(stepMessage), acknowledgment);

            verify(sagaResponsePublisher).publishSuccess(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, "{\"success\":true}"
            );
            verify(sagaResponsePublisher, never()).publishFailure(
                    anyString(), anyString(), anyString(), anyString(), anyString()
            );
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("ACTION 처리 중 예외가 발생하면 실패 응답을 발행한다")
        void shouldPublishFailureWhenActionThrows() throws Exception {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "orderId", 1L, "buyerId", 100L, "totalAmount", 60000L,
                    "sharerKeys", List.of(SHARER_KEY.toString())
            ));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, payload);

            doThrow(new RuntimeException("DB 장애"))
                    .when(modifyPendingSharedPointUseCase).modifyPending(any(ModifyPendingSharedPointCommand.class));

            consumer.handleSharedPointStep(createRecord(stepMessage), acknowledgment);

            verify(sagaResponsePublisher).publishFailure(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.ACTION), contains("공유 적립 예정 포인트 처리 실패")
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

    @Nested
    @DisplayName("COMPENSATION 페이로드 검증/예외")
    class CompensationValidationAndError {

        @Test
        @DisplayName("orderId가 없으면 검증 실패 응답을 발행한다")
        void shouldPublishFailureWhenOrderIdMissingOnCompensation() throws Exception {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "buyerId", 100L, "totalAmount", 60000L,
                    "sharerKeys", List.of(SHARER_KEY.toString())
            ));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, payload);

            consumer.handleSharedPointStep(createRecord(stepMessage), acknowledgment);

            verifyNoInteractions(cancelPendingPointUseCase);
            verify(sagaResponsePublisher).publishFailure(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.COMPENSATION), contains("페이로드 검증 실패")
            );
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("회원 포인트가 존재하지 않으면 보상 실패 응답을 발행한다")
        void shouldPublishFailureWhenUserPointNotFound() throws Exception {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "orderId", 1L, "buyerId", 100L, "totalAmount", 60000L,
                    "sharerKeys", List.of(SHARER_KEY.toString())
            ));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, payload);

            when(findUserPointPort.findByUserKey(anyString())).thenReturn(Optional.empty());

            consumer.handleSharedPointStep(createRecord(stepMessage), acknowledgment);

            verifyNoInteractions(cancelPendingPointUseCase);
            verify(sagaResponsePublisher).publishFailure(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.COMPENSATION), contains("공유 적립 예정 포인트 보상 처리 실패")
            );
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("COMPENSATION 처리 중 예외가 발생하면 실패 응답을 발행한다")
        void shouldPublishFailureWhenCompensationThrows() throws Exception {
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
            doThrow(new RuntimeException("DB 장애"))
                    .when(cancelPendingPointUseCase).cancelPending(any(CancelPendingPointCommand.class));

            consumer.handleSharedPointStep(createRecord(stepMessage), acknowledgment);

            verify(sagaResponsePublisher).publishFailure(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.COMPENSATION), contains("공유 적립 예정 포인트 보상 처리 실패")
            );
            verify(acknowledgment).acknowledge();
        }
    }

    @Nested
    @DisplayName("기타 처리")
    class MiscHandling {

        @Test
        @DisplayName("envelope이 null이면 처리하지 않고 ack만 한다")
        void shouldSkipWhenEnvelopeIsNull() {
            ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                    "saga.order-payment-downstream.shared-point", 0, 0, SAGA_ID, null
            );

            consumer.handleSharedPointStep(record, acknowledgment);

            verifyNoInteractions(modifyPendingSharedPointUseCase);
            verifyNoInteractions(cancelPendingPointUseCase);
            verifyNoInteractions(sagaResponsePublisher);
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("알 수 없는 messageType이면 처리하지 않고 ack만 한다")
        void shouldSkipUnknownMessageType() {
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, "UNKNOWN", "{}");

            consumer.handleSharedPointStep(createRecord(stepMessage), acknowledgment);

            verifyNoInteractions(modifyPendingSharedPointUseCase);
            verifyNoInteractions(cancelPendingPointUseCase);
            verifyNoInteractions(sagaResponsePublisher);
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
