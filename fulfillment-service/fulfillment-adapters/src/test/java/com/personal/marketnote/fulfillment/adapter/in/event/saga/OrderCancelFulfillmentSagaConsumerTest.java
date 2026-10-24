package com.personal.marketnote.fulfillment.adapter.in.event.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.saga.SagaResponsePublisher;
import com.personal.marketnote.common.saga.SagaStepMessage;
import com.personal.marketnote.fulfillment.adapter.in.event.saga.payload.OrderCancelFulfillmentSagaPayload;
import com.personal.marketnote.fulfillment.domain.delivery.FulfillmentWorkStatus;
import com.personal.marketnote.fulfillment.exception.FulfillmentDeliveryCancellationNotAllowedException;
import com.personal.marketnote.fulfillment.exception.FulfillmentDeliveryRegistrationNotFoundException;
import com.personal.marketnote.fulfillment.port.in.command.CancelInternalFulfillmentDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.result.CancelInternalFulfillmentDeliveryResult;
import com.personal.marketnote.fulfillment.port.in.usecase.CancelInternalFulfillmentDeliveryUseCase;
import com.personal.marketnote.fulfillment.port.out.event.PublishOrderCancelFailedEventPort;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderCancelFulfillmentSagaConsumer 테스트")
class OrderCancelFulfillmentSagaConsumerTest {

    private static final String SAGA_ID = "ORDER_CANCEL:1";
    private static final String SAGA_TYPE = "ORDER_CANCEL";
    private static final String STEP_NAME = "CANCEL_FULFILLMENT";
    private static final long BUYER_ID = 100L;

    @InjectMocks
    private OrderCancelFulfillmentSagaConsumer consumer;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CancelInternalFulfillmentDeliveryUseCase cancelInternalFulfillmentDeliveryUseCase;

    @Mock
    private SagaResponsePublisher sagaResponsePublisher;

    @Mock
    private PublishOrderCancelFailedEventPort publishOrderCancelFailedEventPort;

    @Mock
    private Acknowledgment acknowledgment;

    private String preparingPayload;
    private String paidPayload;

    @BeforeEach
    void setUp() throws Exception {
        preparingPayload = objectMapper.writeValueAsString(new OrderCancelFulfillmentSagaPayload(
                1L, BUYER_ID, "PREPARING", List.of("PK-1", "PK-2")
        ));
        paidPayload = objectMapper.writeValueAsString(new OrderCancelFulfillmentSagaPayload(
                1L, BUYER_ID, "PAID", List.of("PK-1")
        ));
    }

    @Test
    @DisplayName("ACTION 타입 메시지 수신 시 originalStatus=PREPARING이면 cancelDelivery 호출 후 성공 응답을 발행한다")
    void shouldCallCancelDeliveryAndPublishSuccessWhenOriginalStatusIsPreparing() {
        SagaStepMessage stepMessage = new SagaStepMessage(
                SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, preparingPayload
        );
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

        CancelInternalFulfillmentDeliveryCommand expectedCommand = new CancelInternalFulfillmentDeliveryCommand(
                1L, List.of("PK-1", "PK-2")
        );
        when(cancelInternalFulfillmentDeliveryUseCase.cancelDelivery(expectedCommand))
                .thenReturn(new CancelInternalFulfillmentDeliveryResult(1L, true, "출고 취소 성공"));

        consumer.handleOrderCancelFulfillmentStep(record, acknowledgment);

        verify(cancelInternalFulfillmentDeliveryUseCase).cancelDelivery(expectedCommand);
        verify(sagaResponsePublisher).publishSuccess(
                SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, "{\"compensated\":true}"
        );
        verify(acknowledgment).acknowledge();
        verifyNoInteractions(publishOrderCancelFailedEventPort);
        verifyNoMoreInteractions(sagaResponsePublisher, cancelInternalFulfillmentDeliveryUseCase);
    }

    @Test
    @DisplayName("ACTION 타입 메시지 수신 시 originalStatus=PAID면 cancelDelivery를 호출하지 않고 skipped 성공을 발행한다")
    void shouldSkipWhenOriginalStatusIsPaid() {
        SagaStepMessage stepMessage = new SagaStepMessage(
                SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, paidPayload
        );
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

        consumer.handleOrderCancelFulfillmentStep(record, acknowledgment);

        verifyNoInteractions(cancelInternalFulfillmentDeliveryUseCase);
        verifyNoInteractions(publishOrderCancelFailedEventPort);
        verify(sagaResponsePublisher).publishSuccess(
                eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                eq(SagaStepMessage.ACTION), contains("\"reason\":\"fulfillment-cancel-not-required\"")
        );
        verify(sagaResponsePublisher).publishSuccess(
                eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                eq(SagaStepMessage.ACTION), contains("\"originalStatus\":\"PAID\"")
        );
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("FulfillmentDeliveryRegistrationNotFoundException은 멱등 성공이므로 OrderCancelFailedEvent를 발행하지 않는다")
    void shouldNotPublishOrderCancelFailedWhenRegistrationNotFound() {
        SagaStepMessage stepMessage = new SagaStepMessage(
                SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, preparingPayload
        );
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

        CancelInternalFulfillmentDeliveryCommand expectedCommand = new CancelInternalFulfillmentDeliveryCommand(
                1L, List.of("PK-1", "PK-2")
        );
        doThrow(new FulfillmentDeliveryRegistrationNotFoundException(1L))
                .when(cancelInternalFulfillmentDeliveryUseCase).cancelDelivery(expectedCommand);

        consumer.handleOrderCancelFulfillmentStep(record, acknowledgment);

        verify(sagaResponsePublisher).publishSuccess(
                eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                eq(SagaStepMessage.ACTION), contains("idempotent")
        );
        verifyNoInteractions(publishOrderCancelFailedEventPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("FulfillmentDeliveryCancellationNotAllowedException은 멱등 성공이므로 OrderCancelFailedEvent를 발행하지 않는다")
    void shouldNotPublishOrderCancelFailedWhenCancellationNotAllowed() {
        SagaStepMessage stepMessage = new SagaStepMessage(
                SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, preparingPayload
        );
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

        CancelInternalFulfillmentDeliveryCommand expectedCommand = new CancelInternalFulfillmentDeliveryCommand(
                1L, List.of("PK-1", "PK-2")
        );
        doThrow(new FulfillmentDeliveryCancellationNotAllowedException(FulfillmentWorkStatus.RELEASED))
                .when(cancelInternalFulfillmentDeliveryUseCase).cancelDelivery(expectedCommand);

        consumer.handleOrderCancelFulfillmentStep(record, acknowledgment);

        verify(sagaResponsePublisher).publishSuccess(
                eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                eq(SagaStepMessage.ACTION), contains("alreadyCancelled")
        );
        verifyNoInteractions(publishOrderCancelFailedEventPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("ACTION 타입 메시지 수신 시 일반 예외 발생하면 OrderCancelFailedEvent를 발행한다")
    void shouldPublishOrderCancelFailedWhenUnexpectedExceptionOccurs() {
        SagaStepMessage stepMessage = new SagaStepMessage(
                SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, preparingPayload
        );
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

        CancelInternalFulfillmentDeliveryCommand expectedCommand = new CancelInternalFulfillmentDeliveryCommand(
                1L, List.of("PK-1", "PK-2")
        );
        doThrow(new RuntimeException("외부 API 실패"))
                .when(cancelInternalFulfillmentDeliveryUseCase).cancelDelivery(expectedCommand);

        consumer.handleOrderCancelFulfillmentStep(record, acknowledgment);

        verify(sagaResponsePublisher).publishFailure(
                eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                eq(SagaStepMessage.ACTION), contains("orderId=1")
        );
        verify(publishOrderCancelFailedEventPort).publish(1L, BUYER_ID);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("UseCase가 cancelled=false를 반환하면 OrderCancelFailedEvent를 발행한다")
    void shouldPublishOrderCancelFailedWhenUseCaseReturnsCancelledFalse() {
        SagaStepMessage stepMessage = new SagaStepMessage(
                SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, preparingPayload
        );
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

        CancelInternalFulfillmentDeliveryCommand expectedCommand = new CancelInternalFulfillmentDeliveryCommand(
                1L, List.of("PK-1", "PK-2")
        );
        when(cancelInternalFulfillmentDeliveryUseCase.cancelDelivery(expectedCommand))
                .thenReturn(new CancelInternalFulfillmentDeliveryResult(1L, false, "already shipping"));

        consumer.handleOrderCancelFulfillmentStep(record, acknowledgment);

        verify(sagaResponsePublisher).publishFailure(
                eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                eq(SagaStepMessage.ACTION), contains("주문 취소 풀필먼트 처리 거부")
        );
        verify(publishOrderCancelFailedEventPort).publish(1L, BUYER_ID);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("productKeys가 빈 리스트이면 UseCase에 빈 리스트로 전달된다")
    void shouldPassEmptyListToUseCaseWhenProductKeysEmpty() throws Exception {
        String emptyKeysPayload = objectMapper.writeValueAsString(new OrderCancelFulfillmentSagaPayload(
                1L, BUYER_ID, "PREPARING", List.of()
        ));
        SagaStepMessage stepMessage = new SagaStepMessage(
                SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, emptyKeysPayload
        );
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

        CancelInternalFulfillmentDeliveryCommand expectedCommand = new CancelInternalFulfillmentDeliveryCommand(
                1L, List.of()
        );
        when(cancelInternalFulfillmentDeliveryUseCase.cancelDelivery(expectedCommand))
                .thenReturn(new CancelInternalFulfillmentDeliveryResult(1L, true, "출고 취소 성공"));

        consumer.handleOrderCancelFulfillmentStep(record, acknowledgment);

        verify(cancelInternalFulfillmentDeliveryUseCase).cancelDelivery(expectedCommand);
        verify(sagaResponsePublisher).publishSuccess(
                SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, "{\"compensated\":true}"
        );
        verifyNoInteractions(publishOrderCancelFailedEventPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("ACTION 이외 타입 메시지 수신 시 실패 응답을 발행한다")
    void shouldPublishFailureOnNonActionMessageType() {
        SagaStepMessage stepMessage = new SagaStepMessage(
                SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, preparingPayload
        );
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

        consumer.handleOrderCancelFulfillmentStep(record, acknowledgment);

        verifyNoInteractions(cancelInternalFulfillmentDeliveryUseCase);
        verifyNoInteractions(publishOrderCancelFailedEventPort);
        verify(sagaResponsePublisher).publishFailure(
                eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                eq(SagaStepMessage.COMPENSATION), contains("지원하지 않는 messageType")
        );
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId 누락 시 OrderCancelFailedEvent를 발행하지 않는다")
    void shouldNotPublishOrderCancelFailedOnMissingOrderId() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", null);
        payload.put("buyerId", BUYER_ID);
        payload.put("originalStatus", "PREPARING");
        payload.put("productKeys", List.of("PK-1"));
        String invalidPayload = objectMapper.writeValueAsString(payload);
        SagaStepMessage stepMessage = new SagaStepMessage(
                SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, invalidPayload
        );
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

        consumer.handleOrderCancelFulfillmentStep(record, acknowledgment);

        verify(sagaResponsePublisher).publishFailure(
                eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                eq(SagaStepMessage.ACTION), contains("orderId")
        );
        verifyNoInteractions(publishOrderCancelFailedEventPort);
        verify(acknowledgment).acknowledge();
        verifyNoInteractions(cancelInternalFulfillmentDeliveryUseCase);
        verifyNoMoreInteractions(sagaResponsePublisher);
    }

    @Test
    @DisplayName("originalStatus 누락 시 OrderCancelFailedEvent를 발행한다")
    void shouldPublishOrderCancelFailedOnMissingOriginalStatus() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", 1L);
        payload.put("buyerId", BUYER_ID);
        payload.put("originalStatus", null);
        payload.put("productKeys", List.of("PK-1"));
        String invalidPayload = objectMapper.writeValueAsString(payload);
        SagaStepMessage stepMessage = new SagaStepMessage(
                SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, invalidPayload
        );
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

        consumer.handleOrderCancelFulfillmentStep(record, acknowledgment);

        verify(sagaResponsePublisher).publishFailure(
                eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                eq(SagaStepMessage.ACTION), contains("originalStatus")
        );
        verify(publishOrderCancelFailedEventPort).publish(1L, BUYER_ID);
        verify(acknowledgment).acknowledge();
        verifyNoInteractions(cancelInternalFulfillmentDeliveryUseCase);
        verifyNoMoreInteractions(sagaResponsePublisher);
    }

    @Test
    @DisplayName("buyerId 누락 시 (구버전 producer 호환) OrderCancelFailedEvent를 발행하지 않는다")
    void shouldNotPublishOrderCancelFailedWhenBuyerIdMissing() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", 1L);
        payload.put("buyerId", null);
        payload.put("originalStatus", "PREPARING");
        payload.put("productKeys", List.of("PK-1"));
        String payloadWithoutBuyerId = objectMapper.writeValueAsString(payload);
        SagaStepMessage stepMessage = new SagaStepMessage(
                SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, payloadWithoutBuyerId
        );
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

        CancelInternalFulfillmentDeliveryCommand expectedCommand = new CancelInternalFulfillmentDeliveryCommand(
                1L, List.of("PK-1")
        );
        doThrow(new RuntimeException("외부 API 실패"))
                .when(cancelInternalFulfillmentDeliveryUseCase).cancelDelivery(expectedCommand);

        consumer.handleOrderCancelFulfillmentStep(record, acknowledgment);

        verify(sagaResponsePublisher).publishFailure(
                eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                eq(SagaStepMessage.ACTION), contains("orderId=1")
        );
        verifyNoInteractions(publishOrderCancelFailedEventPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("OrderCancelFailedEvent 발행 중 예외가 발생해도 Consumer가 ack한다")
    void shouldAcknowledgeEvenIfOrderCancelFailedPublishThrows() {
        SagaStepMessage stepMessage = new SagaStepMessage(
                SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, preparingPayload
        );
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

        CancelInternalFulfillmentDeliveryCommand expectedCommand = new CancelInternalFulfillmentDeliveryCommand(
                1L, List.of("PK-1", "PK-2")
        );
        doThrow(new RuntimeException("외부 API 실패"))
                .when(cancelInternalFulfillmentDeliveryUseCase).cancelDelivery(expectedCommand);
        doThrow(new RuntimeException("outbox 저장 실패"))
                .when(publishOrderCancelFailedEventPort).publish(1L, BUYER_ID);

        consumer.handleOrderCancelFulfillmentStep(record, acknowledgment);

        verify(sagaResponsePublisher).publishFailure(
                eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                eq(SagaStepMessage.ACTION), contains("orderId=1")
        );
        verify(publishOrderCancelFailedEventPort).publish(1L, BUYER_ID);
        verify(acknowledgment).acknowledge();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private ConsumerRecord<String, EventEnvelope<?>> createRecord(SagaStepMessage stepMessage) {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-10-18T01:00:00Z"), ZoneId.of("Asia/Seoul"));
        EventEnvelope<SagaStepMessage> envelope = EventEnvelope.of(
                "saga.ORDER_CANCEL.CANCEL_FULFILLMENT.action",
                "saga-orchestrator", stepMessage, fixedClock
        );
        return new ConsumerRecord<>(
                "saga.order-cancel.fulfillment", 0, 0, SAGA_ID,
                (EventEnvelope<?>) (EventEnvelope) envelope
        );
    }
}
