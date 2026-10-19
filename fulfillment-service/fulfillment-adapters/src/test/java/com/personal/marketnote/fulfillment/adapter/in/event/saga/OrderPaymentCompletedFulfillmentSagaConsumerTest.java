package com.personal.marketnote.fulfillment.adapter.in.event.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.saga.SagaResponsePublisher;
import com.personal.marketnote.common.saga.SagaStepMessage;
import com.personal.marketnote.fulfillment.configuration.FulfillmentAuthProperties;
import com.personal.marketnote.fulfillment.domain.delivery.FulfillmentWorkStatus;
import com.personal.marketnote.fulfillment.exception.FulfillmentDeliveryAlreadyRegisteredException;
import com.personal.marketnote.fulfillment.exception.FulfillmentDeliveryCancellationNotAllowedException;
import com.personal.marketnote.fulfillment.exception.FulfillmentDeliveryRegistrationNotFoundException;
import com.personal.marketnote.fulfillment.port.in.result.CancelInternalFulfillmentDeliveryResult;
import com.personal.marketnote.fulfillment.port.in.usecase.CancelInternalFulfillmentDeliveryUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.CreateShippingTrackerUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RegisterFulfillmentDeliveryUseCase;
import com.personal.marketnote.fulfillment.utility.FulfillmentAuthenticatedCaller;
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
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderPaymentCompletedFulfillmentSagaConsumer 테스트")
class OrderPaymentCompletedFulfillmentSagaConsumerTest {

    private static final String SAGA_ID = "ORDER_PAYMENT_COMPLETED:1";
    private static final String SAGA_TYPE = "ORDER_PAYMENT_COMPLETED";
    private static final String STEP_NAME = "REGISTER_FULFILLMENT_DELIVERY";

    @InjectMocks
    private OrderPaymentCompletedFulfillmentSagaConsumer consumer;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CreateShippingTrackerUseCase createShippingTrackerUseCase;

    @Mock
    private FulfillmentAuthenticatedCaller fulfillmentAuthenticatedCaller;

    @Mock
    private RegisterFulfillmentDeliveryUseCase registerFulfillmentDeliveryUseCase;

    @Mock
    private CancelInternalFulfillmentDeliveryUseCase cancelInternalFulfillmentDeliveryUseCase;

    @Mock
    private SagaResponsePublisher sagaResponsePublisher;

    @Mock
    private FulfillmentAuthProperties fasstoAuthProperties;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-10-17T01:00:00Z"), ZoneId.of("Asia/Seoul"));

    @Mock
    private Acknowledgment acknowledgment;

    private String actionPayload;
    private String compensationPayload;

    @BeforeEach
    void setUp() throws Exception {
        actionPayload = objectMapper.writeValueAsString(Map.of(
                "orderId", 1L,
                "buyerId", 100L,
                "recipientName", "홍길동",
                "recipientPhoneNumber", "010-1234-5678",
                "zipCode", "06236",
                "address", "서울특별시 강남구",
                "addressDetail", "101호",
                "deliveryRequestMessage", "부재시 경비실",
                "orderProducts", List.of(Map.of(
                        "pricePolicyId", 10L,
                        "productKey", java.util.UUID.randomUUID().toString(),
                        "quantity", 2,
                        "unitAmount", 25000L
                ))
        ));
        compensationPayload = objectMapper.writeValueAsString(Map.of("orderId", 1L));
    }

    @SuppressWarnings("unchecked")
    private void stubAuthenticatedCaller() {
        lenient().when(fulfillmentAuthenticatedCaller.call(any())).thenAnswer(invocation -> {
            Function<String, Object> operation = invocation.getArgument(0);
            return operation.apply("test-token");
        });
    }

    @Nested
    @DisplayName("ACTION 처리")
    class ActionHandling {

        @Test
        @DisplayName("출고 등록 성공 시 성공 응답을 발행한다")
        void shouldPublishSuccessOnRegistration() {
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            stubAuthenticatedCaller();
            when(fasstoAuthProperties.getCustomerCode()).thenReturn("CUST01");

            consumer.handleFulfillmentStep(record, acknowledgment);

            verify(registerFulfillmentDeliveryUseCase).registerDeliveryIdempotent(any());
            verify(createShippingTrackerUseCase).createShippingTracker(any());
            verify(sagaResponsePublisher).publishSuccess(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, "{\"success\":true}"
            );
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("이미 등록된 주문이면 멱등 처리로 성공 응답을 발행한다")
        void shouldPublishSuccessOnAlreadyRegistered() {
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            stubAuthenticatedCaller();
            when(fasstoAuthProperties.getCustomerCode()).thenReturn("CUST01");
            doThrow(new FulfillmentDeliveryAlreadyRegisteredException(1L))
                    .when(registerFulfillmentDeliveryUseCase).registerDeliveryIdempotent(any());

            consumer.handleFulfillmentStep(record, acknowledgment);

            verify(sagaResponsePublisher).publishSuccess(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.ACTION), contains("idempotent")
            );
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("출고 등록 처리 실패 시 실패 응답을 발행한다")
        void shouldPublishFailureOnRegistrationError() {
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, actionPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            stubAuthenticatedCaller();
            when(fasstoAuthProperties.getCustomerCode()).thenReturn("CUST01");
            doThrow(new RuntimeException("외부 API 실패"))
                    .when(registerFulfillmentDeliveryUseCase).registerDeliveryIdempotent(any());

            consumer.handleFulfillmentStep(record, acknowledgment);

            verify(sagaResponsePublisher).publishFailure(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, "풀필먼트 출고 등록 처리 실패"
            );
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("orderId가 누락되면 실패 응답을 발행한다")
        void shouldPublishFailureOnMissingOrderId() throws Exception {
            String invalidPayload = objectMapper.writeValueAsString(Map.of(
                    "buyerId", 100L,
                    "orderProducts", List.of(Map.of(
                            "pricePolicyId", 10L,
                            "productKey", java.util.UUID.randomUUID().toString(),
                            "quantity", 2
                    ))
            ));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, invalidPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            consumer.handleFulfillmentStep(record, acknowledgment);

            verifyNoInteractions(registerFulfillmentDeliveryUseCase);
            verify(sagaResponsePublisher).publishFailure(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.ACTION), contains("orderId")
            );
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("orderProducts 내 productKey가 누락되면 실패 응답을 발행한다")
        void shouldPublishFailureOnMissingProductKey() throws Exception {
            String invalidPayload = objectMapper.writeValueAsString(Map.of(
                    "orderId", 1L, "buyerId", 100L,
                    "orderProducts", List.of(Map.of(
                            "pricePolicyId", 10L, "quantity", 2, "unitAmount", 25000L
                    ))
            ));
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, invalidPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            consumer.handleFulfillmentStep(record, acknowledgment);

            verifyNoInteractions(registerFulfillmentDeliveryUseCase);
            verify(sagaResponsePublisher).publishFailure(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.ACTION), contains("productKey")
            );
            verify(acknowledgment).acknowledge();
        }
    }

    @Nested
    @DisplayName("COMPENSATION 처리")
    class CompensationHandling {

        @Test
        @DisplayName("출고 취소 성공 시 보상 성공 응답을 발행한다")
        void shouldPublishSuccessOnCompensation() {
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, compensationPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            when(cancelInternalFulfillmentDeliveryUseCase.cancelDelivery(any()))
                    .thenReturn(new CancelInternalFulfillmentDeliveryResult(1L, true, "cancelled"));

            consumer.handleFulfillmentStep(record, acknowledgment);

            verify(sagaResponsePublisher).publishSuccess(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, "{\"compensated\":true}"
            );
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("출고 취소 거부 시 실패 응답을 발행한다")
        void shouldPublishFailureOnCompensationRejected() {
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, compensationPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            when(cancelInternalFulfillmentDeliveryUseCase.cancelDelivery(any()))
                    .thenReturn(new CancelInternalFulfillmentDeliveryResult(1L, false, "already shipping"));

            consumer.handleFulfillmentStep(record, acknowledgment);

            verify(sagaResponsePublisher).publishFailure(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.COMPENSATION), contains("풀필먼트 출고 취소 거부")
            );
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("보상 처리 중 예외 발생 시 실패 응답을 발행한다")
        void shouldPublishFailureOnCompensationException() {
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, compensationPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            doThrow(new RuntimeException("보상 실패"))
                    .when(cancelInternalFulfillmentDeliveryUseCase).cancelDelivery(any());

            consumer.handleFulfillmentStep(record, acknowledgment);

            verify(sagaResponsePublisher).publishFailure(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION,
                    "풀필먼트 출고 취소 보상 처리 실패"
            );
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("등록되지 않은 출고를 보상하면 멱등 성공 응답을 발행한다")
        void shouldPublishSuccessOnRegistrationNotFound() {
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, compensationPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            doThrow(new FulfillmentDeliveryRegistrationNotFoundException(1L))
                    .when(cancelInternalFulfillmentDeliveryUseCase).cancelDelivery(any());

            consumer.handleFulfillmentStep(record, acknowledgment);

            verify(sagaResponsePublisher).publishSuccess(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.COMPENSATION), contains("idempotent")
            );
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("이미 취소된 출고를 보상하면 멱등 성공 응답을 발행한다")
        void shouldPublishSuccessOnCancellationNotAllowed() {
            SagaStepMessage stepMessage = new SagaStepMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, compensationPayload);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(stepMessage);

            doThrow(new FulfillmentDeliveryCancellationNotAllowedException(FulfillmentWorkStatus.RELEASED))
                    .when(cancelInternalFulfillmentDeliveryUseCase).cancelDelivery(any());

            consumer.handleFulfillmentStep(record, acknowledgment);

            verify(sagaResponsePublisher).publishSuccess(
                    eq(SAGA_ID), eq(SAGA_TYPE), eq(STEP_NAME),
                    eq(SagaStepMessage.COMPENSATION), contains("idempotent")
            );
            verify(acknowledgment).acknowledge();
        }
    }

    @SuppressWarnings("unchecked")
    private ConsumerRecord<String, EventEnvelope<?>> createRecord(SagaStepMessage stepMessage) {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-10-17T01:00:00Z"), ZoneId.of("Asia/Seoul"));
        EventEnvelope<SagaStepMessage> envelope = EventEnvelope.of(
                "saga.ORDER_PAYMENT_COMPLETED.REGISTER_FULFILLMENT_DELIVERY.action",
                "saga-orchestrator", stepMessage, fixedClock
        );
        return new ConsumerRecord<>(
                "saga.order-payment-downstream.fulfillment", 0, 0, SAGA_ID,
                (EventEnvelope<?>) (EventEnvelope) envelope
        );
    }
}
